package service.impl;

import entity.Product;
import entity.StoreInventory;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import dto.InventoryImportDTO;
import entity.PurchaseOrder;
import entity.PurchaseOrderDetail;
import repository.InventoryRepository;
import repository.InventoryRepositoryImpl;
import repository.ProductRepository;
import repository.ProductUnitRepository;
import repository.PurchaseOrderDetailRepository;
import repository.PurchaseOrderRepository;
import repository.StoreRepository;
import repository.SupplierRepository;
import service.PurchaseOrderService;
import util.DatabaseUtil;

public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final PurchaseOrderRepository orderRepo = new PurchaseOrderRepository();
    private final PurchaseOrderDetailRepository detailRepo = new PurchaseOrderDetailRepository();
    private final SupplierRepository supplierRepo = new SupplierRepository();
    private final StoreRepository storeRepo = new StoreRepository();
    private final ProductRepository productRepo = new ProductRepository();
    private final InventoryRepository inventoryRepo = new InventoryRepositoryImpl();

    @Override
    public PurchaseOrder createOrder(int supplierId, int storeId, Integer adminId,
            List<PurchaseOrderDetail> details) throws SQLException {
        validateDetails(details);

        supplierRepo.findById(supplierId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhà cung cấp id=" + supplierId));

        storeRepo.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy cửa hàng id=" + storeId));

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PurchaseOrderDetail detail : details) {
            normalizeDetail(detail);
            totalAmount = totalAmount.add(detail.getSubtotal());
        }

        PurchaseOrder order = new PurchaseOrder();
        order.setSupplierId(supplierId);
        order.setStoreId(storeId);
        order.setAdminId(adminId);
        order.setTotalAmount(totalAmount);
        order.setStatus("pending");

        Connection conn = DatabaseUtil.getConnection();
        boolean previousAutoCommit = conn.getAutoCommit();

        try {
            conn.setAutoCommit(false);

            int orderId = orderRepo.insert(order, conn);
            if (orderId <= 0) {
                throw new RuntimeException("Tạo đơn nhập thất bại");
            }

            order.setId(orderId);

            for (PurchaseOrderDetail detail : details) {
                detail.setPurchaseOrderId(orderId);
                if (!detailRepo.insert(detail, conn)) {
                    throw new RuntimeException("Tạo chi tiết đơn nhập thất bại");
                }
            }

            conn.commit();
            return order;
        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                e.addSuppressed(rollbackEx);
            }
            throw e;
        } finally {
            try {
                conn.setAutoCommit(previousAutoCommit);
            } catch (SQLException ignored) {
            }
            DatabaseUtil.closeConnection();
        }
    }

    @Override
    public PurchaseOrder getOrderById(int id) throws SQLException {
        return orderRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn nhập id=" + id));
    }

    @Override
    public List<PurchaseOrderDetail> getOrderDetails(int orderId) throws SQLException {
        return detailRepo.findByOrderId(orderId);
    }

    @Override
    public void updateOrderStatus(int orderId, String status) throws SQLException {
        if (!"pending".equals(status) && !"received".equals(status) && !"cancelled".equals(status)) {
            throw new IllegalArgumentException("Status không hợp lệ: " + status);
        }

        orderRepo.updateStatus(orderId, status);
    }

    @Override
    public void receiveOrder(int orderId) throws Exception {
        PurchaseOrder order = getOrderById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Không tìm thấy đơn nhập hàng có ID: " + orderId);
        }
        if (!"pending".equals(order.getStatus())) {
            throw new IllegalStateException("Chỉ có thể nhận đơn ở trạng thái 'pending'!");
        }

        List<PurchaseOrderDetail> details = getOrderDetails(orderId);
        if (details == null || details.isEmpty()) {
            throw new IllegalArgumentException("Đơn nhập hàng trống!");
        }

        Connection conn = util.DatabaseUtil.getConnection();
        boolean previousAutoCommit = conn.getAutoCommit();

        try {
            conn.setAutoCommit(false);

            for (PurchaseOrderDetail detail : details) {
                // Lấy productId hiện tại từ chi tiết đơn hàng nháp (Thường là NULL hoặc <= 0
                // đối với hàng mới)
                int productId = detail.getProductId();

                if (productId <= 0) {
                    String rawBarcode = detail.getRawBarcode(); // Lấy trường raw_barcode thô đã lưu lúc tạo đơn nháp
                    if (rawBarcode == null || rawBarcode.isBlank()) {
                        throw new IllegalStateException(
                                "Chi tiết đơn nhập ID " + detail.getId() + " thiếu Barcode thô để xử lý!");
                    }

                    // 1. Kiểm tra trùng lặp bằng mã băm trước khi tạo mới
                    String hashedBarcodeLookup;
                    try {
                        hashedBarcodeLookup = util.ShortHash.ProductBarcodeHash(rawBarcode);
                    } catch (Exception ex) {
                        throw new SQLException("Lỗi khi băm mã vạch để đối chiếu: " + ex.getMessage(), ex);
                    }

                    String barcodeLookupSql = "SELECT product_id FROM product_units WHERE barcode = ? AND is_deleted = 0 LIMIT 1";
                    try (PreparedStatement lookupStmt = conn.prepareStatement(barcodeLookupSql)) {
                        lookupStmt.setString(1, hashedBarcodeLookup);
                        try (ResultSet rs = lookupStmt.executeQuery()) {
                            if (rs.next()) {
                                productId = rs.getInt("product_id"); // Nếu trùng mã, lấy lại ID cũ đã tồn tại
                            }
                        }
                    }

                    // 2. Nếu hoàn toàn mới tinh -> Tiến hành "Lễ khai sinh" sản phẩm
                    if (productId <= 0) {
                        String insertProductSql = "INSERT INTO products (product_name, category, base_unit, import_price, markup_rate, status) VALUES (?, ?, ?, ?, ?, 'active')";
                        try (PreparedStatement insertProductStmt = conn.prepareStatement(insertProductSql,
                                Statement.RETURN_GENERATED_KEYS)) {
                            insertProductStmt.setString(1, detail.getTempProductName());
                            insertProductStmt.setString(2,
                                    detail.getTempCategory() != null ? detail.getTempCategory() : "Mặt hàng mới");
                            insertProductStmt.setString(3,
                                    detail.getTempBaseUnit() != null ? detail.getTempBaseUnit() : "Cái");
                            insertProductStmt.setBigDecimal(4, detail.getImportPriceAtTime());
                            insertProductStmt.setBigDecimal(5,
                                    detail.getTempMarkupRate() != null ? detail.getTempMarkupRate()
                                            : new BigDecimal("0.20"));
                            insertProductStmt.executeUpdate();

                            // --- ĐOẠN SỬA LỖI CHÍ MẠNG: LẤY ID TỰ TĂNG VỪA KHỞI TẠO ---
                            try (ResultSet generatedKeys = insertProductStmt.getGeneratedKeys()) {
                                if (generatedKeys.next()) {
                                    productId = generatedKeys.getInt(1); // CẬP NHẬT BIẾN productId THÀNH ID THẬT TRONG
                                    // DB
                                }
                            }
                        }

                        if (productId <= 0) {
                            throw new SQLException("Tạo sản phẩm mới thất bại, không lấy được ID từ cơ sở dữ liệu.");
                        }

                        // 3. Tạo đơn vị tính gắn liền với productId thật vừa lấy
                        String insertUnitSql = "INSERT INTO product_units (product_id, unit_name, ratio, barcode, selling_price, is_default_sale) VALUES (?, ?, 1, ?, ?, TRUE)";
                        try (PreparedStatement psUnit = conn.prepareStatement(insertUnitSql)) {
                            psUnit.setInt(1, productId); // Truyền chuẩn productId thật
                            psUnit.setString(2, detail.getTempBaseUnit() != null ? detail.getTempBaseUnit() : "Cái");
                            psUnit.setString(3, hashedBarcodeLookup); // Lưu mã đã băm hệ thống

                            // Tính giá bán lẻ mặc định = Giá nhập * (1 + tỉ lệ lãi)
                            BigDecimal markup = detail.getTempMarkupRate() != null ? detail.getTempMarkupRate()
                                    : new BigDecimal("0.20");
                            BigDecimal sellingPrice = detail.getImportPriceAtTime()
                                    .multiply(BigDecimal.ONE.add(markup));
                            psUnit.setBigDecimal(4, sellingPrice);

                            psUnit.executeUpdate();
                        }

                        // 4. Cập nhật ngược lại cột product_id trong bảng purchase_order_details từ
                        // NULL thành ID thật để lưu vết lịch sử đơn hàng
                        String updateDetailSql = "UPDATE purchase_order_details SET product_id = ? WHERE id = ?";
                        try (PreparedStatement updateDetailStmt = conn.prepareStatement(updateDetailSql)) {
                            updateDetailStmt.setInt(1, productId);
                            updateDetailStmt.setInt(2, detail.getId());
                            updateDetailStmt.executeUpdate();
                        }
                    }
                }

                // 5. Thêm hàng vào kho theo lô (Bảng store_inventory) - Ăn theo biến productId
                // chuẩn đã được giải cứu!
                String insertInvSql = "INSERT INTO store_inventory (store_id, product_id, batch_code, quantity, import_price, received_at, min_stock_level) VALUES (?, ?, ?, ?, ?, NOW(), 5)";
                try (PreparedStatement invStmt = conn.prepareStatement(insertInvSql)) {
                    invStmt.setInt(1, order.getStoreId());
                    invStmt.setInt(2, productId); // Ghi ID thật vào đây -> Hết sạch lỗi khóa ngoại fk_inv_product!
                    invStmt.setString(3, util.ShortHash.BatchBarcodeHash(orderId, productId));
                    invStmt.setInt(4, detail.getQuantity());
                    invStmt.setBigDecimal(5, detail.getImportPriceAtTime());
                    invStmt.executeUpdate();
                }
            }

            // Đổi trạng thái đơn hàng sang đã nhận
            orderRepo.updateStatus(orderId, "received", conn);
            conn.commit(); // Hoàn thành trọn vẹn Transaction một cách an toàn!

        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (SQLException re) {
                e.addSuppressed(re);
            }
            throw new SQLException("Duyệt đơn thất bại, hệ thống đã rollback toàn bộ! Chi tiết: " + e.getMessage(), e);
        } finally {
            try {
                conn.setAutoCommit(previousAutoCommit);
            } catch (SQLException ignored) {
            }
            try {
                conn.close();
            } catch (SQLException ignored) {
            }
        }
    }

    /**
     * Helper method to stock inventory with the given product_id. Uses the
     * provided connection to stay within the transaction boundary.
     */
    private void stockInventory(PurchaseOrder order, PurchaseOrderDetail detail, int productId, Connection conn)
            throws SQLException, Exception {
        StoreInventory inventory = new StoreInventory();
        inventory.setStoreId(order.getStoreId());
        inventory.setProductId(productId);
        inventory.setQuantity(detail.getQuantity());
        inventory.setImportPrice(detail.getImportPriceAtTime() != null
                ? detail.getImportPriceAtTime()
                : BigDecimal.ZERO);
        inventory.setBatchCode(util.ShortHash.BatchBarcodeHash(order.getId(), productId));
        inventory.setReceivedAt(new java.sql.Date(System.currentTimeMillis()));
        inventory.setMinStockLevel(5); // Default minimum stock level
        // expiry_date is NULL (no expiration)

        inventoryRepo.insertWithConnection(inventory, conn);
    }

    @Override
    public void cancelOrder(int orderId) throws SQLException {
        PurchaseOrder order = getOrderById(orderId);

        if ("received".equals(order.getStatus())) {
            throw new IllegalStateException("Không thể hủy đơn đã nhận");
        }

        orderRepo.updateStatus(orderId, "cancelled");
    }

    @Override
    public void addDetailToOrder(int orderId, PurchaseOrderDetail detail) throws SQLException {
        PurchaseOrder order = getOrderById(orderId);

        if (!"pending".equals(order.getStatus())) {
            throw new IllegalStateException("Chỉ có thể thêm chi tiết khi đơn ở trạng thái pending");
        }

        normalizeDetail(detail);
        detail.setPurchaseOrderId(orderId);

        if (!detailRepo.insert(detail)) {
            throw new RuntimeException("Thêm chi tiết đơn thất bại");
        }

        BigDecimal newTotal = detailRepo.sumSubtotalByOrderId(orderId);
        orderRepo.updateTotal(orderId, newTotal);
    }

    @Override
    public void removeDetailFromOrder(int detailId) throws SQLException {
        PurchaseOrderDetail detail = detailRepo.findById(detailId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chi tiết đơn id=" + detailId));

        PurchaseOrder order = getOrderById(detail.getPurchaseOrderId());
        if (!"pending".equals(order.getStatus())) {
            throw new IllegalStateException("Chỉ có thể xóa chi tiết khi đơn ở trạng thái pending");
        }

        if (!detailRepo.delete(detailId)) {
            throw new RuntimeException("Xóa chi tiết đơn thất bại");
        }

        BigDecimal newTotal = detailRepo.sumSubtotalByOrderId(order.getId());
        orderRepo.updateTotal(order.getId(), newTotal);
    }

    @Override
    public List<PurchaseOrder> getAllOrders() throws SQLException {
        return orderRepo.findAll(); // Hàm findAll() bạn vừa thêm ở Repository
    }

    @Override
    public List<String[]> getAvailableStatuses() throws SQLException {
        return orderRepo.getAvailableStatuses();
    }

    @Override
    public String[] getSavedBatchAndExpiry(int orderId, int productId) throws Exception {
        // 1. Lấy thông tin số lượng và giá nhập của sản phẩm thuộc đơn hàng hiện tại
        String sqlOrderDetail = "SELECT quantity, import_price_at_time FROM purchase_order_details "
                + "WHERE purchase_order_id = ? AND product_id = ?";

        int orderQuantity = 0;
        double orderPrice = 0.0;

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmtDetail = conn.prepareStatement(sqlOrderDetail)) {
            stmtDetail.setInt(1, orderId);
            stmtDetail.setInt(2, productId);
            try (ResultSet rsDetail = stmtDetail.executeQuery()) {
                if (rsDetail.next()) {
                    orderQuantity = rsDetail.getInt("quantity");
                    orderPrice = rsDetail.getDouble("import_price_at_time");
                }
            }
        }
        if (orderQuantity > 0) {
            String sqlCountPrevious = "SELECT COUNT(*) FROM purchase_orders po "
                    + "JOIN purchase_order_details pod ON po.id = pod.purchase_order_id "
                    + "WHERE pod.product_id = ? AND po.status = 'received' "
                    + "AND po.received_at < (SELECT received_at FROM purchase_orders WHERE id = ?)";

            int previousOrdersCount = 0;
            try (Connection conn = DatabaseUtil.getConnection();
                    PreparedStatement stmtCount = conn.prepareStatement(sqlCountPrevious)) {
                stmtCount.setInt(1, productId);
                stmtCount.setInt(2, orderId);
                try (ResultSet rsCount = stmtCount.executeQuery()) {
                    if (rsCount.next()) {
                        previousOrdersCount = rsCount.getInt(1);
                    }
                }
            }
            String sqlInventory = "SELECT batch_code, expiry_date FROM store_inventory "
                    + "WHERE product_id = ? AND quantity = ? AND import_price = ? "
                    + "ORDER BY id ASC LIMIT 1 OFFSET ?";

            try (Connection conn = DatabaseUtil.getConnection();
                    PreparedStatement stmtInv = conn.prepareStatement(sqlInventory)) {

                stmtInv.setInt(1, productId);
                stmtInv.setInt(2, orderQuantity);
                stmtInv.setDouble(3, orderPrice);
                stmtInv.setInt(4, previousOrdersCount); // Bỏ qua các dòng của đơn cũ, lấy đúng dòng của mình

                try (ResultSet rsInv = stmtInv.executeQuery()) {
                    if (rsInv.next()) {
                        String batchCode = rsInv.getString("batch_code");
                        java.sql.Date expiryDateSql = rsInv.getDate("expiry_date");

                        String expiryDateStr = (expiryDateSql != null) ? expiryDateSql.toString() : "Không có hạn";
                        return new String[] { batchCode, expiryDateStr };
                    }
                }
            }
        }

        return null;
    }

    @Override
    public List<PurchaseOrder> getOrdersByStatus(String status) throws SQLException {
        return orderRepo.findByStatus(status); // Hàm findByStatus() có sẵn ở Repository
    }

    private void validateDetails(List<PurchaseOrderDetail> details) {
        if (details == null || details.isEmpty()) {
            throw new IllegalArgumentException("Đơn nhập phải có ít nhất một sản phẩm");
        }
        for (PurchaseOrderDetail detail : details) {
            if (detail.getProductId() > 0) {
                java.util.Optional<Product> productOpt;
                try {
                    productOpt = productRepo.findById(detail.getProductId());
                } catch (java.sql.SQLException e) {
                    throw new RuntimeException("Lỗi kết nối cơ sở dữ liệu khi kiểm tra sản phẩm: " + e.getMessage(), e);
                }

                if (productOpt.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Sản phẩm có ID " + detail.getProductId() + " không tồn tại trong hệ thống!");
                }
            }
            if (detail.getQuantity() <= 0) {
                throw new IllegalArgumentException("Số lượng nhập phải lớn hơn 0");
            }
            if (detail.getImportPriceAtTime() == null || detail.getImportPriceAtTime().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Giá nhập không được nhỏ hơn 0");
            }
        }
    }

    private void normalizeDetail(PurchaseOrderDetail detail) {
        if (detail.getImportPriceAtTime() == null || detail.getImportPriceAtTime().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Giá nhập không hợp lệ");
        }

        BigDecimal subtotal = detail.getImportPriceAtTime().multiply(BigDecimal.valueOf(detail.getQuantity()));
        detail.setSubtotal(subtotal);
    }

    @Override
    public void approveAndImportInventory(int orderId, int storeId, List<InventoryImportDTO> importItems)
            throws SQLException {
        if (importItems == null || importItems.isEmpty()) {
            throw new IllegalArgumentException("Danh sách hàng nhập không được rỗng.");
        }

        // Guard: chỉ cho phép duyệt đơn ở trạng thái 'pending'
        PurchaseOrder existingOrder = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng có ID: " + orderId));
        if (!"pending".equals(existingOrder.getStatus())) {
            throw new IllegalStateException(
                    "Chỉ có thể duyệt đơn ở trạng thái 'pending'. Trạng thái hiện tại: " + existingOrder.getStatus());
        }

        Connection conn = null;
        PreparedStatement psLookupDetail = null;
        PreparedStatement psCheckBarcodeExists = null;
        PreparedStatement psVerifyProductName = null;
        PreparedStatement psInsertProduct = null;
        PreparedStatement psUpdateDetailId = null;
        PreparedStatement psInsertUnit = null; // Đã sửa để truyền động ratio & default flag
        PreparedStatement psGetRatioByBarcode = null;
        PreparedStatement psGetDefaultRatio = null;
        PreparedStatement psInsertInventory = null;
        PreparedStatement psUpdateOrder = null;
        boolean previousAutoCommit = true;

        try {
            conn = DatabaseUtil.getConnection();
            previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            // ── Pre-fetch metadata từ purchase_order_details ──────────────────────────
            String sqlLookupDetail = "SELECT id, raw_barcode, temp_product_name, temp_category, temp_base_unit, temp_markup_rate "
                    + "FROM purchase_order_details WHERE purchase_order_id = ? ORDER BY id ASC";
            psLookupDetail = conn.prepareStatement(sqlLookupDetail);
            psLookupDetail.setInt(1, orderId);

            java.util.List<java.util.Map<String, Object>> detailsMetadata = new java.util.ArrayList<>();
            try (ResultSet rsDetail = psLookupDetail.executeQuery()) {
                while (rsDetail.next()) {
                    java.util.Map<String, Object> row = new java.util.HashMap<>();
                    row.put("id", rsDetail.getInt("id"));
                    row.put("raw_barcode", rsDetail.getString("raw_barcode"));
                    row.put("temp_product_name", rsDetail.getString("temp_product_name"));
                    row.put("temp_category", rsDetail.getString("temp_category"));
                    row.put("temp_base_unit", rsDetail.getString("temp_base_unit"));
                    row.put("temp_markup_rate", rsDetail.getBigDecimal("temp_markup_rate"));
                    detailsMetadata.add(row);
                }
            }

            // ── Chuẩn bị tất cả PreparedStatement dùng chung ─────────────────────────
            psCheckBarcodeExists = conn.prepareStatement(
                    "SELECT product_id FROM product_units WHERE barcode = ? AND is_deleted = 0 LIMIT 1");

            psVerifyProductName = conn.prepareStatement(
                    "SELECT product_name FROM products WHERE id = ? AND status != 'deleted' LIMIT 1");

            psInsertProduct = conn.prepareStatement(
                    "INSERT INTO products (product_name, status) VALUES (?, 'active')",
                    Statement.RETURN_GENERATED_KEYS);

            psUpdateDetailId = conn.prepareStatement(
                    "UPDATE purchase_order_details SET product_id = ?, quantity = ?, import_price_at_time = ? WHERE id = ?");

            // ĐÃ CẬP NHẬT: Trở thành câu lệnh động hoàn toàn nhận cả ratio và cờ default từ
            // Java
            psInsertUnit = conn.prepareStatement(
                    "INSERT INTO product_units (product_id, unit_name, ratio, barcode, selling_price, is_default_sale) "
                            + "VALUES (?, ?, ?, ?, ?, ?)");

            // Lấy ratio của đơn vị tương ứng với mã vạch quét được lúc nhập
            psGetRatioByBarcode = conn.prepareStatement(
                    "SELECT ratio FROM product_units WHERE barcode = ? AND is_deleted = 0 LIMIT 1");

            // Lấy ratio mặc định nếu chọn sản phẩm trực tiếp từ dropdown (đơn vị bán lẻ hạt
            // nhân có ratio = 1)
            psGetDefaultRatio = conn.prepareStatement(
                    "SELECT ratio FROM product_units WHERE product_id = ? AND is_deleted = 0 AND is_default_sale = TRUE LIMIT 1");

            psInsertInventory = conn.prepareStatement(
                    "INSERT INTO store_inventory "
                            + "(store_id, product_id, batch_code, quantity, import_price, expiry_date, received_at) "
                            + "VALUES (?, ?, ?, ?, ?, ?, NOW())");

            // ── Vòng lặp xử lý từng dòng hàng ────────────────────────────────────────
            int index = 0;
            for (InventoryImportDTO item : importItems) {

                int productId = item.getProductId();
                int quantity = item.getQuantity();
                BigDecimal importPrice = item.getImportPrice();
                String batchCode = item.getBatchCode();
                java.sql.Date expiryDate = item.getExpiryDate();

                // ĐỌC DỮ LIỆU ĐỘNG DO NGƯỜI DÙNG NHẬP TAY TỪ UI QUA DTO
                int ratioNhap = item.getUnitRatio() <= 0 ? 1 : item.getUnitRatio(); // Tránh lỗi chia cho 0
                String tenDonViNhap = item.getUnitName(); // Ví dụ: "Thùng"
                String tenDonViLeGoc = item.getBaseUnitName(); // Ví dụ: "Chai"

                // Validate dữ liệu đầu vào
                if (quantity <= 0) {
                    throw new IllegalArgumentException("Số lượng không hợp lệ: " + quantity);
                }
                if (importPrice == null || importPrice.compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("Giá nhập không hợp lệ: " + importPrice);
                }
                if (batchCode == null || batchCode.trim().isEmpty()) {
                    throw new IllegalArgumentException("Mã lô không được để trống.");
                }
                if (expiryDate == null) {
                    throw new IllegalArgumentException("Hạn sử dụng không được để trống.");
                }

                // Lấy metadata tương ứng từ danh sách đã pre-fetch làm backup dữ liệu
                String tempProductName = "Sản phẩm mới";
                BigDecimal tempMarkupRate = new BigDecimal("0.20");
                int detailRecordId = 0;
                String rawBarcode = "";

                if (index < detailsMetadata.size()) {
                    java.util.Map<String, Object> meta = detailsMetadata.get(index);
                    detailRecordId = (int) meta.get("id");
                    if (meta.get("raw_barcode") != null) {
                        // ⚠️ CRITICAL: Làm sạch rawBarcode xóa ALL hidden chars trước khi dùng
                        rawBarcode = ((String) meta.get("raw_barcode")).replaceAll("\\s+", "");
                    }
                    if (meta.get("temp_product_name") != null) {
                        tempProductName = (String) meta.get("temp_product_name");
                    }
                    if (meta.get("temp_markup_rate") != null) {
                        tempMarkupRate = (BigDecimal) meta.get("temp_markup_rate");
                    }
                    // Nếu UI truyền lên rỗng, lấy từ thông tin tạm lưu trong chi tiết đơn làm dự
                    // phòng
                    if ((tenDonViNhap == null || tenDonViNhap.isEmpty()) && meta.get("temp_base_unit") != null) {
                        tenDonViNhap = (String) meta.get("temp_base_unit");
                    }
                }

                // Đảm bảo tên đơn vị không bị null
                if (tenDonViNhap == null || tenDonViNhap.trim().isEmpty()) {
                    tenDonViNhap = "Thùng";
                }
                if (tenDonViLeGoc == null || tenDonViLeGoc.trim().isEmpty()) {
                    tenDonViLeGoc = "Chai";
                }

                // ── BƯỚC 0: Tạo SHA-256 hash động cho Barcode theo chuẩn ShortHash
                // ───────────────────────────────
                String hashedBarcode;
                try {
                    hashedBarcode = util.ShortHash.ProductBarcodeHash(rawBarcode);
                } catch (Exception e) {
                    throw new SQLException("Lỗi khi tạo SHA-256 hash cho barcode: " + e.getMessage(), e);
                }

                // ── BƯỚC 1: Barcode lookup bằng hash mới vừa tính ───────────────────
                int existingProductId = 0;
                psCheckBarcodeExists.setString(1, hashedBarcode);
                try (ResultSet rsBarcodeCheck = psCheckBarcodeExists.executeQuery()) {
                    if (rsBarcodeCheck.next()) {
                        existingProductId = rsBarcodeCheck.getInt("product_id");
                    }
                }

                // ── BƯỚC 2: DUAL-VALIDATION ──────────────────────────────────────────────
                if (existingProductId > 0) {
                    psVerifyProductName.setInt(1, existingProductId);
                    try (ResultSet rsVerify = psVerifyProductName.executeQuery()) {
                        if (rsVerify.next()) {
                            String actualName = rsVerify.getString("product_name");
                            boolean nameMatches = tempProductName.trim()
                                    .equalsIgnoreCase(actualName.trim());
                            if (!nameMatches) {
                                existingProductId = 0;
                            }
                        } else {
                            existingProductId = 0;
                        }
                    }
                }

                // Khai báo các biến lưu trữ dữ liệu sau quy đổi hạt nhân
                int finalInventoryQuantity;
                BigDecimal finalInventoryImportPrice;

                // ── BƯỚC 3: Xử lý quy đổi động theo từng Case ──────────────────────────
                if (existingProductId > 0) {
                    // ═════════════════════════════════════════════════════════════════════
                    // CASE 1: ĐÃ TỒN TẠI SẢN PHẨM KHỚP MÃ VẠCH & TÊN THẬT
                    // ═════════════════════════════════════════════════════════════════════
                    productId = existingProductId;

                    if (detailRecordId > 0) {
                        psUpdateDetailId.setInt(1, productId);
                        psUpdateDetailId.setInt(2, quantity);
                        psUpdateDetailId.setBigDecimal(3, importPrice);
                        psUpdateDetailId.setInt(4, detailRecordId);
                        psUpdateDetailId.executeUpdate();
                    }

                    // Lấy hệ số ratio quy đổi động từ cấu trúc DB đã lưu
                    int unitRatio = 1;
                    psGetRatioByBarcode.setString(1, hashedBarcode);
                    try (ResultSet rsRatio = psGetRatioByBarcode.executeQuery()) {
                        if (rsRatio.next()) {
                            unitRatio = rsRatio.getInt("ratio");
                        }
                    }

                    // Thực hiện quy đổi động về đơn vị hạt nhân để cộng dồn vào kho
                    finalInventoryQuantity = quantity * unitRatio;
                    finalInventoryImportPrice = importPrice.divide(BigDecimal.valueOf(unitRatio), 4,
                            java.math.RoundingMode.HALF_UP);

                    // Tiến hành nạp kho lô hàng quy đổi
                    psInsertInventory.setInt(1, storeId);
                    psInsertInventory.setInt(2, productId);
                    psInsertInventory.setString(3, batchCode);
                    psInsertInventory.setInt(4, finalInventoryQuantity);
                    psInsertInventory.setBigDecimal(5, finalInventoryImportPrice);
                    psInsertInventory.setDate(6, expiryDate);
                    psInsertInventory.executeUpdate();

                } else if (productId == 0) {
                    // ═════════════════════════════════════════════════════════════════════
                    // CASE 2: SẢN PHẨM MỚI TINH HOÀN TOÀN (KHAI SINH SẢN PHẨM ĐỘNG TỪ UI)
                    // ═════════════════════════════════════════════════════════════════════
                    psInsertProduct.setString(1, tempProductName);
                    psInsertProduct.executeUpdate();

                    try (ResultSet generatedKeys = psInsertProduct.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            productId = generatedKeys.getInt(1);
                        } else {
                            throw new SQLException("Không thể lấy ID sản phẩm tự động tăng.");
                        }
                    }

                    if (detailRecordId > 0) {
                        psUpdateDetailId.setInt(1, productId);
                        psUpdateDetailId.setInt(2, quantity);
                        psUpdateDetailId.setBigDecimal(3, importPrice);
                        psUpdateDetailId.setInt(4, detailRecordId);
                        psUpdateDetailId.executeUpdate();
                    }

                    // A. Tính toán giá bán lẻ dự kiến cho đơn vị hạt nhân (Chai/Lon) làm gốc
                    BigDecimal giaNhapTungDonViLe = importPrice.divide(BigDecimal.valueOf(ratioNhap), 4,
                            java.math.RoundingMode.HALF_UP);
                    BigDecimal giaBanLeTungDonViLe = giaNhapTungDonViLe.multiply(BigDecimal.ONE.add(tempMarkupRate));

                    // B. Tự động chèn ĐƠN VỊ LẺ LÀM GỐC (Chai/Lon) trước với ratio = 1 và
                    // is_default_sale = TRUE
                    psInsertUnit.setInt(1, productId);
                    psInsertUnit.setString(2, tenDonViLeGoc); // Động từ UI: "Chai"
                    psInsertUnit.setInt(3, 1); // Hạt nhân luôn có ratio = 1
                    psInsertUnit.setString(4, hashedBarcode + "-LE"); // Tạo barcode phụ cho chai lẻ
                    psInsertUnit.setBigDecimal(5, giaBanLeTungDonViLe);
                    psInsertUnit.setBoolean(6, true); // Đặt làm mặc định bán lẻ tại quầy POS
                    psInsertUnit.executeUpdate();

                    // C. Chèn ĐƠN VỊ LỚN DÙNG ĐỂ NHẬP (Thùng/Lốc) với ratio động nhập tay từ UI và
                    // is_default_sale = FALSE
                    BigDecimal giaBanThungDuKien = importPrice.multiply(BigDecimal.ONE.add(tempMarkupRate));
                    psInsertUnit.setInt(1, productId);
                    psInsertUnit.setString(2, tenDonViNhap); // Động từ UI: "Thùng"
                    psInsertUnit.setInt(3, ratioNhap); // Động từ UI: 24
                    psInsertUnit.setString(4, hashedBarcode); // Gắn chính xác mã vạch thùng quét được vào đây
                    psInsertUnit.setBigDecimal(5, giaBanThungDuKien);
                    psInsertUnit.setBoolean(6, false); // Đơn vị sỉ không dùng làm mặc định quét lẻ tại quầy
                    psInsertUnit.executeUpdate();

                    // D. Quy đổi số lượng thực tế lưu trữ trong kho sang dạng hạt nhân lẻ (ví dụ: 5
                    // thùng * 24 = 120 chai)
                    finalInventoryQuantity = quantity * ratioNhap;
                    finalInventoryImportPrice = giaNhapTungDonViLe;

                    // E. Nạp vào bảng store_inventory theo dữ liệu đã được quy đổi sạch sẽ số
                    // nguyên
                    psInsertInventory.setInt(1, storeId);
                    psInsertInventory.setInt(2, productId);
                    psInsertInventory.setString(3, batchCode);
                    psInsertInventory.setInt(4, finalInventoryQuantity);
                    psInsertInventory.setBigDecimal(5, finalInventoryImportPrice);
                    psInsertInventory.setDate(6, expiryDate);
                    psInsertInventory.executeUpdate();

                } else {
                    // ═════════════════════════════════════════════════════════════════════
                    // CASE 3: CHỌN SẢN PHẨM CÓ SẴN BẰNG DROPDOWN THỦ CÔNG
                    // ═════════════════════════════════════════════════════════════════════
                    if (detailRecordId > 0) {
                        psUpdateDetailId.setInt(1, productId);
                        psUpdateDetailId.setInt(2, quantity);
                        psUpdateDetailId.setBigDecimal(3, importPrice);
                        psUpdateDetailId.setInt(4, detailRecordId);
                        psUpdateDetailId.executeUpdate();
                    }

                    // Lấy ratio đơn vị mặc định bán lẻ của sản phẩm có sẵn
                    int unitRatio = 1;
                    psGetDefaultRatio.setInt(1, productId);
                    try (ResultSet rsRatio = psGetDefaultRatio.executeQuery()) {
                        if (rsRatio.next()) {
                            unitRatio = rsRatio.getInt("ratio");
                        }
                    }

                    finalInventoryQuantity = quantity * unitRatio;
                    finalInventoryImportPrice = importPrice.divide(BigDecimal.valueOf(unitRatio), 4,
                            java.math.RoundingMode.HALF_UP);

                    psInsertInventory.setInt(1, storeId);
                    psInsertInventory.setInt(2, productId);
                    psInsertInventory.setString(3, batchCode);
                    psInsertInventory.setInt(4, finalInventoryQuantity);
                    psInsertInventory.setBigDecimal(5, finalInventoryImportPrice);
                    psInsertInventory.setDate(6, expiryDate);
                    psInsertInventory.executeUpdate();
                }

                index++;
            }

            // ── Cập nhật trạng thái đơn hàng — Thao tác cuối cùng trước commit ──
            String updateOrderSql = "UPDATE purchase_orders SET status = ?, received_at = NOW() WHERE id = ?";
            psUpdateOrder = conn.prepareStatement(updateOrderSql);
            psUpdateOrder.setString(1, "received");
            psUpdateOrder.setInt(2, orderId);
            int orderUpdateCount = psUpdateOrder.executeUpdate();
            if (orderUpdateCount == 0) {
                throw new SQLException("Không tìm thấy đơn hàng có ID: " + orderId);
            }

            conn.commit();

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    e.addSuppressed(rollbackEx);
                }
            }
            throw new SQLException(
                    "Duyệt đơn hàng thất bại. Hệ thống đã rollback toàn bộ thay đổi. Chi tiết: " + e.getMessage(), e);

        } finally {
            // ── Giải phóng toàn bộ tài nguyên prepared statements và connection ──
            try {
                if (psLookupDetail != null) {
                    psLookupDetail.close();
                }
                if (psCheckBarcodeExists != null) {
                    psCheckBarcodeExists.close();
                }
                if (psVerifyProductName != null) {
                    psVerifyProductName.close();
                }
                if (psInsertProduct != null) {
                    psInsertProduct.close();
                }
                if (psUpdateDetailId != null) {
                    psUpdateDetailId.close();
                }
                if (psInsertUnit != null) {
                    psInsertUnit.close();
                }
                if (psGetRatioByBarcode != null) {
                    psGetRatioByBarcode.close();
                }
                if (psGetDefaultRatio != null) {
                    psGetDefaultRatio.close();
                }
                if (psInsertInventory != null) {
                    psInsertInventory.close();
                }
                if (psUpdateOrder != null) {
                    psUpdateOrder.close();
                }
            } catch (SQLException ignored) {
            }

            if (conn != null) {
                try {
                    conn.setAutoCommit(previousAutoCommit);
                } catch (SQLException ignored) {
                }
                try {
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    @Override
    public int insert(PurchaseOrder po, Connection conn) throws SQLException {
        return orderRepo.insert(po, conn);
    }

    @Override
    public void insertOrderDetailSmart(int orderId, int productId, String productName, String barcode,
            int quantity, BigDecimal importPrice, BigDecimal sellingPrice,
            BigDecimal markupRate, String category, String baseUnit, Connection conn) throws Exception {
        orderRepo.insertOrderDetailSmart(orderId, productId, productName, barcode,
                quantity, importPrice, sellingPrice, markupRate, category, baseUnit, conn);
    }
}
