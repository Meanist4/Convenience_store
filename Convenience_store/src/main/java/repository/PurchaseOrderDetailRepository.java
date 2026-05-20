package repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import convenience_store.DBConnection;
import entity.PurchaseOrderDetail;

public class PurchaseOrderDetailRepository {

    private PurchaseOrderDetail map(ResultSet rs) throws SQLException {
        PurchaseOrderDetail detail = new PurchaseOrderDetail();
        detail.setId(rs.getInt("id"));
        detail.setPurchaseOrderId(rs.getInt("purchase_order_id"));
        detail.setProductId(rs.getInt("product_id"));
        detail.setQuantity(rs.getInt("quantity"));
        detail.setImportPriceAtTime(rs.getBigDecimal("import_price_at_time"));
        detail.setSubtotal(rs.getBigDecimal("subtotal"));
        return detail;
    }

    public List<PurchaseOrderDetail> findByOrderId(int purchaseOrderId) throws SQLException {
        List<PurchaseOrderDetail> list = new ArrayList<>();
        String sql = "SELECT * FROM purchase_order_details WHERE purchase_order_id = ? ORDER BY id";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, purchaseOrderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(map(rs));
            }
        }
        return list;
    }

    public Optional<PurchaseOrderDetail> findById(int id) throws SQLException {
        String sql = "SELECT * FROM purchase_order_details WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    public List<PurchaseOrderDetail> findByProductId(int productId) throws SQLException {
        List<PurchaseOrderDetail> list = new ArrayList<>();
        String sql = "SELECT * FROM purchase_order_details WHERE product_id = ? ORDER BY id DESC";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(map(rs));
            }
        }
        return list;
    }

    public boolean insert(PurchaseOrderDetail detail) throws SQLException {
        String sql = "INSERT INTO purchase_order_details " +
                "(purchase_order_id, product_id, quantity, import_price_at_time, subtotal) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, detail.getPurchaseOrderId());
            ps.setInt(2, detail.getProductId());
            ps.setInt(3, detail.getQuantity());
            ps.setBigDecimal(4, detail.getImportPriceAtTime());
            ps.setBigDecimal(5, detail.getSubtotal());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next())
                        detail.setId(keys.getInt(1));
                }
                return true;
            }
        }
        return false;
    }

    public boolean update(PurchaseOrderDetail detail) throws SQLException {
        String sql = "UPDATE purchase_order_details SET " +
                "quantity = ?, import_price_at_time = ?, subtotal = ? WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, detail.getQuantity());
            ps.setBigDecimal(2, detail.getImportPriceAtTime());
            ps.setBigDecimal(3, detail.getSubtotal());
            ps.setInt(4, detail.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM purchase_order_details WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteByOrderId(int purchaseOrderId) throws SQLException {
        String sql = "DELETE FROM purchase_order_details WHERE purchase_order_id = ?";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, purchaseOrderId);
            ps.executeUpdate();
            return true;
        }
    }

    public BigDecimal sumSubtotalByOrderId(int purchaseOrderId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(subtotal), 0) FROM purchase_order_details WHERE purchase_order_id = ?";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, purchaseOrderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getBigDecimal(1);
            }
        }
        return BigDecimal.ZERO;
    }
}
