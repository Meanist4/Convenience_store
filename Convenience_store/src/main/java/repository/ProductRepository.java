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
import entity.Product;
import java.util.HashSet;
import java.util.Set;

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
        try (Connection con = util.DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public List<Product> findByStatus(String status) throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE status = ? AND is_deleted = 0";
        try (Connection con = util.DatabaseUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
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
        try (Connection con = util.DatabaseUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
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
        try (Connection con = util.DatabaseUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
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
        String sql = "SELECT p.* FROM products p "
                + "LEFT JOIN product_units pu ON p.id = pu.product_id "
                + "WHERE (p.product_name LIKE ? "
                + "   OR p.category LIKE ? "
                + "   OR p.id LIKE ? "
                + "   OR pu.barcode LIKE ?) "
                + "AND p.is_deleted = 0 AND (pu.is_deleted = 0 OR pu.is_deleted IS NULL)";
        try (Connection con = util.DatabaseUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            String formattedKeyword = "%" + keyword + "%";
            ps.setString(1, formattedKeyword); // Tìm theo Tên
            ps.setString(2, formattedKeyword); // Tìm theo Danh mục
            ps.setString(3, formattedKeyword); // Tìm theo ID (ép kiểu chuỗi ngầm định trong SQL)
            ps.setString(4, formattedKeyword); // Tìm theo Mã vạch (Barcode)
            try (ResultSet rs = ps.executeQuery()) {
                Set<Integer> addedIds = new HashSet<>();
                while (rs.next()) {
                    Product p = map(rs);
                    if (!addedIds.contains(p.getId())) {
                        list.add(p);
                        addedIds.add(p.getId());
                    }
                }
            }
        }
        return list;
    }

    public Optional<Product> findByBarcode(String barcode) throws SQLException {
        String sql = "SELECT p.* FROM products p "
                + "JOIN product_units pu ON pu.product_id = p.id "
                + "WHERE pu.barcode = ? AND p.is_deleted = 0 AND pu.is_deleted = 0";
        try (Connection con = util.DatabaseUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
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
        try (Connection con = util.DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
        try (Connection con = util.DatabaseUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
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
        try (Connection con = util.DatabaseUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBigDecimal(1, importPrice);
            ps.setBigDecimal(2, markupRate);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "UPDATE products SET is_deleted = 1, deleted_at = NOW() WHERE id = ? AND is_deleted = 0";
        try (Connection con = util.DatabaseUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean restore(int id) throws SQLException {
        String sql = "UPDATE products SET is_deleted = 0, delete_at = NULL WHERE id = ? AND is_deleted = 1";
        try (Connection con = util.DatabaseUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getOrCreateProductIdByName(String productName, String category, String baseUnit, BigDecimal importPrice)
            throws SQLException {
        String checkSql = "SELECT id FROM products WHERE product_name = ? AND is_deleted = 0";
        try (Connection con = util.DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(checkSql)) {
            ps.setString(1, productName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id"); // Có rồi thì trả về ID cũ để sài luôn
                }
            }
        }
        String insertSql = "INSERT INTO products (product_name, category, base_unit, import_price, markup_rate, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = util.DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, productName);
            // Nếu giao diện truyền category/baseUnit trống thì gán giá trị mặc định để
            // tránh lỗi Null dưới DB
            ps.setString(2, (category != null && !category.isEmpty()) ? category : "Mặt hàng mới");
            ps.setString(3, (baseUnit != null && !baseUnit.isEmpty()) ? baseUnit : "Cái");
            ps.setBigDecimal(4, importPrice != null ? importPrice : BigDecimal.ZERO);
            ps.setBigDecimal(5, new BigDecimal("0.2")); // Mặc định biên lợi nhuận 20% hoặc tùy bạn thay đổi
            ps.setString(6, "active");

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1); // Trả về ID mới tinh vừa sinh ra dưới DB
                }
            }
        }
        throw new SQLException("Thất bại khi tự động tạo danh mục sản phẩm mới.");
    }

    /**
     * Insert a new product within an existing transaction.
     * Returns the auto-generated product_id.
     */
    public int insertWithConnection(Product p, Connection conn) throws SQLException {
        String sql = "INSERT INTO products (product_name, category, base_unit, import_price, markup_rate, image_name, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getProductName());
            ps.setString(2, p.getCategory());
            ps.setString(3, p.getBaseUnit());
            ps.setBigDecimal(4, p.getImportPrice());
            ps.setBigDecimal(5, p.getMarkupRate());
            ps.setString(6, p.getImageName() != null ? p.getImageName() : "default.png");
            ps.setString(7, p.getStatus() != null ? p.getStatus() : "active");
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
        }
        throw new SQLException("Failed to insert product");
    }
}
