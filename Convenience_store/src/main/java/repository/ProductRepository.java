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
import entity.Product;

public class ProductRepository {

    private Product map(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getInt("id"));
        p.setProductName(rs.getString("product_name"));
        p.setCategory(rs.getString("category"));
        p.setBaseUnit(rs.getString("base_unit"));
        p.setImportPrice(rs.getBigDecimal("import_price"));
        p.setMarkupRate(rs.getBigDecimal("markup_rate"));
        p.setStatus(rs.getString("status"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        p.setDeleted(rs.getBoolean("is_deleted"));
        p.setDeletedAt(rs.getTimestamp("deleted_at"));
        return p;
    }

    public List<Product> findAll() throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE is_deleted = 0 ORDER BY product_name";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public List<Product> findByStatus(String status) throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE status = ? AND is_deleted = 0";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public Optional<Product> findById(int id) throws SQLException {
        String sql = "SELECT * FROM products WHERE id = ? AND is_deleted = 0";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Product> findByCategory(String category) throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE category = ? AND is_deleted = 0";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, category);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public List<Product> searchByName(String keyword) throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE product_name LIKE ? AND is_deleted = 0";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public Optional<Product> findByBarcode(String barcode) throws SQLException {
        String sql = "SELECT p.* FROM products p "
                + "JOIN product_units pu ON pu.product_id = p.id "
                + "WHERE pu.barcode = ? AND p.is_deleted = 0 AND pu.is_deleted = 0";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, barcode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        }
        return Optional.empty();
    }

    public boolean insert(Product p) throws SQLException {
        String sql = "INSERT INTO products (product_name, category, base_unit, import_price, markup_rate, image_name, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getProductName());
            ps.setString(2, p.getCategory());
            ps.setString(3, p.getBaseUnit());
            ps.setBigDecimal(4, p.getImportPrice());
            ps.setBigDecimal(5, p.getMarkupRate());
            ps.setString(6, p.getImageName());
            ps.setString(7, p.getStatus() != null ? p.getStatus() : "active");
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        p.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean update(Product p) throws SQLException {
        String sql = "UPDATE products SET product_name=?, category=?, base_unit=?, import_price=?, markup_rate=?, status=? WHERE id=? AND is_deleted=0";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getProductName());
            ps.setString(2, p.getCategory());
            ps.setString(3, p.getBaseUnit());
            ps.setBigDecimal(4, p.getImportPrice());
            ps.setBigDecimal(5, p.getMarkupRate());
            ps.setString(6, p.getStatus());
            ps.setInt(7, p.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updatePricing(int id, BigDecimal importPrice, BigDecimal markupRate) throws SQLException {
        String sql = "UPDATE products SET import_price = ?, markup_rate = ? WHERE id = ? AND is_deleted = 0";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBigDecimal(1, importPrice);
            ps.setBigDecimal(2, markupRate);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "UPDATE products SET is_deleted = 1, deleted_at = NOW() WHERE id = ? AND is_deleted = 0";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean restore(int id) throws SQLException {
        String sql = "UPDATE products SET is_deleted = 0, delete_at = NULL WHERE id = ? AND is_deleted = 1";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
