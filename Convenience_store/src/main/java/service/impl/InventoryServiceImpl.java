package service.impl;

import entity.InvoiceDetail;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import entity.Notification;
import entity.ProductUnit;
import entity.StoreInventory;
import exception.NotFoundException;
import exception.ValidationException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import repository.InventoryRepository;
import repository.InventoryRepositoryImpl;
import repository.InvoiceDetailRepository;
import repository.NotificationRepository;
import repository.ProductRepository;
import repository.StoreRepository;
import service.InventoryService;
import util.BarcodeUtil;

public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepo = new InventoryRepositoryImpl();
    private final StoreRepository storeRepo = new StoreRepository();
    private final ProductRepository productRepo = new ProductRepository();
    private final NotificationRepository notificationRepo = new NotificationRepository();
    private final InvoiceDetailRepository detailRepo = new InvoiceDetailRepository();

    @Override
    public List<StoreInventory> getInventoryByStore(int storeId) throws SQLException {
        return inventoryRepo.findByStoreId(storeId);
    }

    @Override
    public Optional<StoreInventory> getInventory(int storeId, int productId) throws SQLException {
        return inventoryRepo.findByStoreAndProduct(storeId, productId);
    }

    @Override
    public void deductStockFEFO(int storeId, int productId, int requiredQty, int invoiceId, int unitId,
            BigDecimal priceAtSale) throws SQLException {
        if (requiredQty <= 0) {
            throw new IllegalArgumentException("Số lượng cần trừ phải lớn hơn 0");
        }

        // Lấy connection hiện tại từ luồng đang chạy Transaction ra để dùng xuyên suốt
        Connection currentConn = util.DatabaseUtil.getConnection();

        // 1. Kiểm tra tổng tồn kho (Dùng repo chung một connection để đảm bảo dữ liệu
        // cô lập tốt)
        int totalAvailable = inventoryRepo.findTotalQuantity(storeId, productId);
        if (totalAvailable < requiredQty) {
            throw new IllegalStateException(
                    "Sản phẩm ID " + productId + " không đủ hàng tồn. Cần hạt nhân: " + requiredQty + ", Có: "
                            + totalAvailable);
        }

        // 2. Lấy hệ số quy đổi (ratio) của đơn vị tính đang dùng để bán hàng
        int unitRatio = 1;
        String sqlGetRatio = "SELECT ratio FROM product_units WHERE id = ? LIMIT 1";
        try (PreparedStatement psRatio = currentConn.prepareStatement(sqlGetRatio)) {
            psRatio.setInt(1, unitId);
            try (ResultSet rs = psRatio.executeQuery()) {
                if (rs.next()) {
                    unitRatio = rs.getInt("ratio");
                }
            }
        }
        if (unitRatio <= 0) {
            unitRatio = 1; // Phòng hờ lỗi dữ liệu gốc
        }
        // 3. Lấy danh sách các lô hàng sắp xếp theo hạn sử dụng tăng dần (Cận đát xuất
        // trước - FEFO)
        List<StoreInventory> activeBatches = inventoryRepo.findAvailableBatches(storeId, productId);
        int remainingToDeduct = requiredQty; // Số lượng hạt nhân (ví dụ: chai) cần trừ giảm dần

        for (StoreInventory batch : activeBatches) {
            if (remainingToDeduct <= 0) {
                break;
            }

            int currentBatchQty = batch.getQuantity(); // Số lượng hạt nhân đang tồn ở lô này
            int deductedFromThisBatch = 0; // Lượng hạt nhân sẽ bẻ từ lô này

            if (currentBatchQty <= remainingToDeduct) {
                deductedFromThisBatch = currentBatchQty;
                remainingToDeduct -= currentBatchQty;

                // Lô này hết sạch, cập nhật tồn kho về 0
                inventoryRepo.updateQuantity(batch.getId(), 0);
            } else {
                deductedFromThisBatch = remainingToDeduct;

                // Lô này vẫn còn dư, trừ bớt đi lượng đã lấy
                inventoryRepo.updateQuantity(batch.getId(), currentBatchQty - remainingToDeduct);
                remainingToDeduct = 0;
            }

            // ═════════════════════════════════════════════════════════════════════
            // 🧮 QUY ĐỔI TOÁN HỌC NGƯỢC: CHUYỂN DỮ LIỆU HẠT NHÂN VỀ ĐƠN VỊ HIỂN THỊ TRÊN
            // HÓA ĐƠN
            // ═════════════════════════════════════════════════════════════════════
            // Ví dụ: Lô này gánh 24 chai lẻ, ratio là 24 (Thùng) -> Số lượng lưu chi tiết
            // hóa đơn = 24 / 24 = 1 (Thùng)
            double displayQty = (double) deductedFromThisBatch / unitRatio;
            BigDecimal displayQtyBigDecimal = BigDecimal.valueOf(displayQty);

            // Tính thành tiền phân đoạn lô: Số lượng đơn vị mua * Giá bán của đơn vị đó
            BigDecimal subtotal = priceAtSale.multiply(displayQtyBigDecimal);

            // 4. Tiến hành ghi nhận dòng phân đoạn lô này vào chi tiết hóa đơn
            InvoiceDetail batchDetail = new InvoiceDetail();
            batchDetail.setInvoiceId(invoiceId);
            batchDetail.setProductId(productId);
            batchDetail.setUnitId(unitId);

            // Lưu số lượng đúng theo đơn vị hiển thị (Cho phép lưu số thực nếu bẻ lẻ thùng,
            // ví dụ: 0.5 thùng)
            // Nếu thuộc tính setQuantity của bạn nhận kiểu int, bạn có thể cân nhắc chuyển
            // đổi hoặc ép cấu trúc
            batchDetail.setQuantity((int) Math.round(displayQty));

            batchDetail.setPriceAtSale(priceAtSale);
            batchDetail.setSubtotal(subtotal);
            batchDetail.setInventoryId(batch.getId()); // Gắn chặt ID lô hàng để sau này truy vết xuất xứ

            if (!detailRepo.insert(batchDetail, currentConn)) {
                throw new RuntimeException("Thêm chi tiết hóa đơn bẻ lô theo FEFO thất bại.");
            }
        }

        // 5. Kiểm tra và bắn thông báo nếu sản phẩm này rơi vào ngưỡng sắp hết hàng
        // công ty
        checkAndNotifyLowStock(storeId, productId);
    }

    @Override
    public List<StoreInventory> getLowStockItems(int storeId, int threshold) throws SQLException {
        if (threshold < 0) {
            throw new IllegalArgumentException("Ngưỡng tồn kho phải >= 0");
        }
        return inventoryRepo.findLowStock(storeId, threshold);
    }

    @Override
    public List<StoreInventory> getLowStockItems(int storeId) throws SQLException {
        return inventoryRepo.findItemsBelowMinLevel(storeId);
    }

    @Override
    public void setStock(int storeId, int productId, int quantity) throws SQLException {
        if (quantity < 0) {
            throw new IllegalArgumentException("Số lượng tồn kho không được âm");
        }

        storeRepo.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy cửa hàng id=" + storeId));
        productRepo.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm id=" + productId));

        StoreInventory inventory = new StoreInventory();
        inventory.setStoreId(storeId);
        inventory.setProductId(productId);
        inventory.setQuantity(quantity);
        inventory.setMinStockLevel(5);

        if (!inventoryRepo.insert(inventory)) {
            throw new RuntimeException("Thiết lập tồn kho thất bại");
        }
    }

    @Override
    public void updateStock(int storeId, int productId, int quantity) throws SQLException {
        if (quantity < 0) {
            throw new IllegalArgumentException("Số lượng tồn kho không được âm");
        }

        inventoryRepo.findByStoreAndProduct(storeId, productId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy tồn kho store=" + storeId + " product=" + productId));

        if (!inventoryRepo.updateQuantity(storeId, productId, quantity)) {
            throw new RuntimeException("Cập nhật tồn kho thất bại");
        }
    }

    @Override
    public void adjustStock(int storeId, int productId, int delta) throws SQLException {
        StoreInventory inv = inventoryRepo.findByStoreAndProduct(storeId, productId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy tồn kho store=" + storeId + " product=" + productId));

        if (inv.getQuantity() + delta < 0) {
            throw new IllegalStateException(
                    "Tồn kho không đủ. Hiện tại: " + inv.getQuantity() + ", điều chỉnh: " + delta);
        }

        if (!inventoryRepo.adjustQuantity(storeId, productId, delta)) {
            throw new RuntimeException("Điều chỉnh tồn kho thất bại");
        }
    }

    @Override
    public void checkAndNotifyLowStock(int storeId, int productId) throws SQLException {
        Optional<StoreInventory> invOpt = inventoryRepo.findByStoreAndProduct(storeId, productId);
        if (invOpt.isPresent()) {
            StoreInventory inv = invOpt.get();
            if (inv.getQuantity() <= inv.getMinStockLevel()) {
                Notification note = new Notification();
                note.setStoreId(storeId);
                note.setTitle("CẢNH BÁO TỒN KHO");
                note.setContent("Sản phẩm ID " + productId + " hiện chỉ còn " + inv.getQuantity()
                        + ". Vui lòng nhập thêm hàng!");
                note.setType("inventory_alert");
                notificationRepo.insert(note);
            }
        }
    }

    @Override
    public void importStock(int storeId, int productId, int quantity) throws SQLException {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng nhập phải > 0");
        }
        adjustStock(storeId, productId, quantity);
    }

    @Override
    public void removeInventory(int storeId, int productId) throws SQLException {
        if (!inventoryRepo.delete(storeId, productId)) {
            throw new IllegalArgumentException("Không tìm thấy bản ghi tồn kho để xóa");
        }
    }

    @Override
    public void processInventoryScan(String barcode, int storeId, int quantity) throws SQLException {
        if (quantity <= 0) {
            throw new ValidationException("Số lượng scan phải > 0");
        }

        Optional<ProductUnit> unitOpt = BarcodeUtil.scanProduct(barcode);
        if (unitOpt.isEmpty()) {
            throw new NotFoundException("Không tìm thấy sản phẩm với barcode: " + barcode);
        }

        adjustStock(storeId, unitOpt.get().getProductId(), quantity);
    }
}
