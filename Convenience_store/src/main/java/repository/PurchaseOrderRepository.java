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

    public Optional<PurchaseOrder> findById(int id) throws SQLException {
        String sql = "SELECT * FROM purchase_orders WHERE id = ?";
        try (Connection conn = util.DatabaseUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    public List<PurchaseOrder> findByStoreId(int storeId) throws SQLException {
        List<PurchaseOrder> list = new ArrayList<>();
        String sql = "SELECT * FROM purchase_orders WHERE store_id = ? ORDER BY created_at DESC";
        try (Connection conn = util.DatabaseUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, storeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(map(rs));
            }
        }
        return list;
    }

    public List<PurchaseOrder> findBySupplierId(int supplierId) throws SQLException {
        List<PurchaseOrder> list = new ArrayList<>();
        String sql = "SELECT * FROM purchase_orders WHERE supplier_id = ? ORDER BY created_at DESC";
        try (Connection conn = util.DatabaseUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, supplierId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(map(rs));
            }
        }
        return list;
    }

    public List<PurchaseOrder> findByStatus(String status) throws SQLException {
        List<PurchaseOrder> list = new ArrayList<>();
        String sql = "SELECT * FROM purchase_orders WHERE status = ? ORDER BY created_at DESC";
        try (Connection conn = util.DatabaseUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(map(rs));
            }
        }
        return list;
    }

    public int insert(PurchaseOrder po, Connection conn) throws SQLException {
        if (po.getSupplierId() <= 0)
            throw new IllegalArgumentException("Supplier ID không hợp lệ");

        if (po.getStoreId() <= 0)
            throw new IllegalArgumentException("Store ID không hợp lệ");

        if (po.getStatus() == null || po.getStatus().isBlank())
            throw new IllegalArgumentException("Status không được để trống");

        if (!po.getStatus().equals("pending") && !po.getStatus().equals("received")
                && !po.getStatus().equals("cancelled"))
            throw new IllegalArgumentException("Status không hợp lệ: " + po.getStatus());

        String checkSupplier = "SELECT 1 FROM suppliers WHERE id = ? AND is_deleted = 0";
        try (PreparedStatement ps = conn.prepareStatement(checkSupplier)) {
            ps.setInt(1, po.getSupplierId());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next())
                    throw new IllegalArgumentException("Nhà cung cấp id=" + po.getSupplierId() + " không tồn tại");
            }
        }

        String checkStore = "SELECT 1 FROM stores WHERE id = ? AND is_deleted = 0";
        try (PreparedStatement ps = conn.prepareStatement(checkStore)) {
            ps.setInt(1, po.getStoreId());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next())
                    throw new IllegalArgumentException("Cửa hàng id=" + po.getStoreId() + " không tồn tại");
            }
        }

        if (po.getAdminId() != null) {
            String checkAdmin = "SELECT 1 FROM admins WHERE id = ? AND is_deleted = 0";
            try (PreparedStatement ps = conn.prepareStatement(checkAdmin)) {
                ps.setInt(1, po.getAdminId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next())
                        throw new IllegalArgumentException("Admin id=" + po.getAdminId() + " không tồn tại");
                }
            }
        }

        String sql = "INSERT INTO purchase_orders (supplier_id, store_id, admin_id, total_amount, status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, po.getSupplierId());
            ps.setInt(2, po.getStoreId());
            if (po.getAdminId() != null)
                ps.setInt(3, po.getAdminId());
            else
                ps.setNull(3, Types.INTEGER);
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
        if (status == null || (!status.equals("pending") && !status.equals("received") && !status.equals("cancelled")))
            throw new IllegalArgumentException("Status không hợp lệ: " + status);

        String sql = "UPDATE purchase_orders SET status = ?, received_at = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setTimestamp(2, status.equals("received") ? new Timestamp(System.currentTimeMillis()) : null);
            ps.setInt(3, id);
            ps.executeUpdate();
        }
    }

    public void updateStatus(int id, String status) throws SQLException {
        if (status == null || (!status.equals("pending") && !status.equals("received") && !status.equals("cancelled")))
            throw new IllegalArgumentException("Status không hợp lệ: " + status);

        String sql = "UPDATE purchase_orders SET status = ?, received_at = ? WHERE id = ?";
        try (Connection conn = util.DatabaseUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setTimestamp(2, status.equals("received") ? new Timestamp(System.currentTimeMillis()) : null);
            ps.setInt(3, id);
            ps.executeUpdate();
        }
    }

    public void updateTotal(int id, BigDecimal total, Connection conn) throws SQLException {
        if (total == null || total.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Tổng tiền không hợp lệ");

        String sql = "UPDATE purchase_orders SET total_amount = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, total);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void updateTotal(int id, BigDecimal total) throws SQLException {
        if (total == null || total.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Tổng tiền không hợp lệ");

        String sql = "UPDATE purchase_orders SET total_amount = ? WHERE id = ?";
        try (Connection conn = util.DatabaseUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, total);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }
}