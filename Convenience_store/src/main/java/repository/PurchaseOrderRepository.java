package repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import entity.PurchaseOrder;
import util.DatabaseUtil;
import util.ShortHash;

public class PurchaseOrderRepository {

    private PurchaseOrder map(ResultSet rs) throws SQLException {
        PurchaseOrder po = new PurchaseOrder();
        po.setId(rs.getInt("id"));
        po.setSupplierId(rs.getInt("supplier_id"));
        po.setStoreId(rs.getInt("store_id"));
        int adminId = rs.getInt("admin_id");
        po.setAdminId(rs.wasNull() ? null : adminId);
        po.setTotalAmount(rs.getBigDecimal("total_amount"));
        po.setStatus(rs.getString("status"));
        po.setCreatedAt(rs.getTimestamp("created_at"));
        po.setReceivedAt(rs.getTimestamp("received_at"));
        return po;
    }

    public List<PurchaseOrder> findAll() throws SQLException {
        List<PurchaseOrder> list = new ArrayList<>();
        String sql = "SELECT * FROM purchase_orders ORDER BY created_at DESC";
        try (Connection conn = util.DatabaseUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public String[] getSavedBatchAndExpiry(int orderId, int productId) throws Exception {
        String expectedBatchCode = ShortHash.BatchBarcodeHash(orderId, productId);
        String sql = "SELECT batch_code, expiry_date FROM store_inventory WHERE product_id = ? AND batch_code = ?";

        try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);
            stmt.setString(2, expectedBatchCode);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String batchCode = rs.getString("batch_code");
                    String expiryDate = rs.getString("expiry_date");
                    return new String[] { batchCode, expiryDate };
                }
            }
        }
        return null;
    }

    public List<String[]> getAvailableStatuses() throws SQLException {
        List<String[]> list = new ArrayList<>();
        list.add(new String[] { "all", "Tất cả" });

        String sql = "SELECT status_code, status_name FROM order_statuses";
        try (Connection conn = util.DatabaseUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new String[] { rs.getString("status_code"), rs.getString("status_name") });
            }
        }
        return list;
    }

    public Optional<PurchaseOrder> findById(int id) throws SQLException {
        String sql = "SELECT * FROM purchase_orders WHERE id = ?";
        try (Connection conn = util.DatabaseUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<PurchaseOrder> findByStoreId(int storeId) throws SQLException {
        List<PurchaseOrder> list = new ArrayList<>();
        String sql = "SELECT * FROM purchase_orders WHERE store_id = ? ORDER BY created_at DESC";
        try (Connection conn = util.DatabaseUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, storeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public List<PurchaseOrder> findBySupplierId(int supplierId) throws SQLException {
        List<PurchaseOrder> list = new ArrayList<>();
        String sql = "SELECT * FROM purchase_orders WHERE supplier_id = ? ORDER BY created_at DESC";
        try (Connection conn = util.DatabaseUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, supplierId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public List<PurchaseOrder> findByStatus(String status) throws SQLException {
        List<PurchaseOrder> list = new ArrayList<>();
        String sql = "SELECT * FROM purchase_orders WHERE status = ? ORDER BY created_at DESC";
        try (Connection conn = util.DatabaseUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public int insert(PurchaseOrder po, Connection conn) throws SQLException {
        if (po.getSupplierId() <= 0) {
            throw new IllegalArgumentException("Supplier ID không hợp lệ");
        }

        if (po.getStoreId() <= 0) {
            throw new IllegalArgumentException("Store ID không hợp lệ");
        }

        if (po.getStatus() == null || po.getStatus().isBlank()) {
            throw new IllegalArgumentException("Status không được để trống");
        }

        if (!po.getStatus().equals("pending") && !po.getStatus().equals("received")
                && !po.getStatus().equals("cancelled")) {
            throw new IllegalArgumentException("Status không hợp lệ: " + po.getStatus());
        }

        String checkSupplier = "SELECT 1 FROM suppliers WHERE id = ? AND is_deleted = 0";
        try (PreparedStatement ps = conn.prepareStatement(checkSupplier)) {
            ps.setInt(1, po.getSupplierId());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Nhà cung cấp id=" + po.getSupplierId() + " không tồn tại");
                }
            }
        }

        String checkStore = "SELECT 1 FROM stores WHERE id = ? AND is_deleted = 0";
        try (PreparedStatement ps = conn.prepareStatement(checkStore)) {
            ps.setInt(1, po.getStoreId());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Cửa hàng id=" + po.getStoreId() + " không tồn tại");
                }
            }
        }

        if (po.getAdminId() != null) {
            String checkCreator = "SELECT 1 FROM admins WHERE id = ? AND is_deleted = 0 "
                    + "UNION ALL "
                    + "SELECT 1 FROM managers WHERE id = ? AND is_deleted = 0";
            try (PreparedStatement ps = conn.prepareStatement(checkCreator)) {
                ps.setInt(1, po.getAdminId());
                ps.setInt(2, po.getAdminId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalArgumentException(
                                "Admin id=" + po.getAdminId() + " không tồn tại trong bảng admins hoặc managers");
                    }
                }
            }
        }

        String sql = "INSERT INTO purchase_orders (supplier_id, store_id, admin_id, total_amount, status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, po.getSupplierId());
            ps.setInt(2, po.getStoreId());
            if (po.getAdminId() != null) {
                ps.setInt(3, po.getAdminId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setBigDecimal(4, po.getTotalAmount());
            ps.setString(5, po.getStatus());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    public int insert(PurchaseOrder po) throws SQLException {
        Connection conn = util.DatabaseUtil.getConnection();
        boolean closeConnection = conn.getAutoCommit();
        try {
            return insert(po, conn);
        } finally {
            if (closeConnection) {
                conn.close();
            }
        }
    }

    public void updateStatus(int id, String status, Connection conn) throws SQLException {
        if (status == null
                || (!status.equals("pending") && !status.equals("received") && !status.equals("cancelled"))) {
            throw new IllegalArgumentException("Status không hợp lệ: " + status);
        }

        String sql = "UPDATE purchase_orders SET status = ?, received_at = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setTimestamp(2, status.equals("received") ? new Timestamp(System.currentTimeMillis()) : null);
            ps.setInt(3, id);
            ps.executeUpdate();
        }
    }

    public void updateStatus(int id, String status) throws SQLException {
        if (status == null
                || (!status.equals("pending") && !status.equals("received") && !status.equals("cancelled"))) {
            throw new IllegalArgumentException("Status không hợp lệ: " + status);
        }

        String sql = "UPDATE purchase_orders SET status = ?, received_at = ? WHERE id = ?";
        try (Connection conn = util.DatabaseUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setTimestamp(2, status.equals("received") ? new Timestamp(System.currentTimeMillis()) : null);
            ps.setInt(3, id);
            ps.executeUpdate();
        }
    }

    public void updateTotal(int id, BigDecimal total, Connection conn) throws SQLException {
        if (total == null || total.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Tổng tiền không hợp lệ");
        }

        String sql = "UPDATE purchase_orders SET total_amount = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, total);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void updateTotal(int id, BigDecimal total) throws SQLException {
        if (total == null || total.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Tổng tiền không hợp lệ");
        }

        String sql = "UPDATE purchase_orders SET total_amount = ? WHERE id = ?";
        try (Connection conn = util.DatabaseUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, total);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void insertOrderDetailSmart(int orderId, int productId, String productName, String barcode,
            int quantity, BigDecimal importPrice, BigDecimal sellingPrice,
            BigDecimal markupRate, String category, String baseUnit, Connection conn) throws Exception {

        BigDecimal subtotal = importPrice.multiply(BigDecimal.valueOf(quantity));

        String sqlDetail = "INSERT INTO purchase_order_details "
                + "(purchase_order_id, product_id, quantity, import_price_at_time, subtotal, "
                + "raw_barcode, temp_product_name, temp_category, temp_base_unit, temp_markup_rate) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement psDetail = conn.prepareStatement(sqlDetail)) {
            psDetail.setInt(1, orderId);
            if (productId <= 0) {
                psDetail.setNull(2, java.sql.Types.INTEGER); // Nếu là hàng mới tinh, ghi nhận giá trị NULL thực sự vào
                                                             // DB
            } else {
                psDetail.setInt(2, productId); // Nếu là hàng có sẵn, lưu ID như bình thường
            }
            psDetail.setInt(3, quantity);
            psDetail.setBigDecimal(4, importPrice);
            psDetail.setBigDecimal(5, subtotal);

            psDetail.setString(6, barcode); // raw_barcode
            psDetail.setString(7, productName); // temp_product_name
            psDetail.setString(8, category); // temp_category
            psDetail.setString(9, baseUnit); // temp_base_unit
            psDetail.setBigDecimal(10, markupRate != null ? markupRate : new BigDecimal("0.20")); // temp_markup_rate

            psDetail.executeUpdate();
        }
    }
}
