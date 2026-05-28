package repository;

import entity.ProductUnit;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import util.ShortHash;

public class ProductUnitRepository {

    private ProductUnit map(ResultSet rs) throws SQLException {
        ProductUnit u = new ProductUnit();
        u.setId(rs.getInt("id"));
        u.setProductId(rs.getInt("product_id"));
        u.setUnitName(rs.getString("unit_name"));
        u.setRatio(rs.getInt("ratio"));
        u.setBarcode(rs.getString("barcode"));
        u.setSellingPrice(rs.getBigDecimal("selling_price"));
        u.setDefaultSale(rs.getBoolean("is_default_sale"));
        u.setDeleted(rs.getBoolean("is_deleted"));
        u.setDeletedAt(rs.getTimestamp("deleted_at"));
        return u;
    }

    public List<ProductUnit> findByProductId(int productId) throws SQLException {
        List<ProductUnit> list = new ArrayList<>();
        String sql = "SELECT * FROM product_units WHERE product_id = ? AND is_deleted = 0 ORDER BY ratio ASC";
        try (Connection con = util.DatabaseUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public Optional<ProductUnit> findById(int id) throws SQLException {
        String sql = "SELECT * FROM product_units WHERE id = ? AND is_deleted = 0";
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

    public Optional<ProductUnit> findByBarcode(String barcode) throws SQLException {
        String sql = "SELECT id, product_id, unit_name, ratio, barcode, selling_price, is_default_sale, is_deleted, deleted_at "
                +
                "FROM product_units WHERE barcode = ? AND is_deleted = 0";

        try (Connection conn = util.DatabaseUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, barcode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ProductUnit unit = new ProductUnit();
                    unit.setId(rs.getInt("id"));
                    unit.setProductId(rs.getInt("product_id"));
                    unit.setUnitName(rs.getString("unit_name"));
                    unit.setRatio(rs.getInt("ratio"));
                    unit.setBarcode(rs.getString("barcode"));
                    unit.setSellingPrice(rs.getBigDecimal("selling_price"));
                    unit.setDefaultSale(rs.getBoolean("is_default_sale"));
                    unit.setDeleted(rs.getBoolean("is_deleted"));
                    unit.setDeletedAt(rs.getTimestamp("deleted_at"));
                    return Optional.of(unit);
                }
            }
        }
        return Optional.empty();
    }

    public Optional<ProductUnit> findDefaultByProductId(int productId) throws SQLException {
        String sql = "SELECT * FROM product_units WHERE product_id = ? AND is_default_sale = TRUE AND is_deleted = 0 LIMIT 1";
        try (Connection con = util.DatabaseUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        }
        return Optional.empty();
    }

    public boolean insert(ProductUnit u) throws SQLException, Exception {
        // Nếu unit mới là default, bỏ default của các unit cũ cùng sản phẩm
        if (u.isDefaultSale()) {
            clearDefaultSale(u.getProductId(), -1);
        }

        String sql = "INSERT INTO product_units (product_id, unit_name, ratio, barcode, selling_price, is_default_sale) VALUES (?,?,?,?,?,?)";
        try (Connection con = util.DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, u.getProductId());
            ps.setString(2, u.getUnitName());
            ps.setInt(3, u.getRatio());
            // ps.setString(4, u.getBarcode());
            ps.setString(4, ShortHash.ProductBarcodeHash(u.getBarcode()));
            ps.setBigDecimal(5, u.getSellingPrice());
            ps.setBoolean(6, u.isDefaultSale());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        u.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean update(ProductUnit u) throws SQLException, Exception {
        if (u.isDefaultSale()) {
            clearDefaultSale(u.getProductId(), u.getId());
        }

        String sql = "UPDATE product_units SET unit_name=?, ratio=?, barcode=?, selling_price=?, is_default_sale=? WHERE id=? AND is_deleted=0";
        try (Connection con = util.DatabaseUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, u.getUnitName());
            ps.setInt(2, u.getRatio());
            // ps.setString(3, u.getBarcode());
            ps.setString(3, ShortHash.ProductBarcodeHash(u.getBarcode()));

            ps.setBigDecimal(4, u.getSellingPrice());
            ps.setBoolean(5, u.isDefaultSale());
            ps.setInt(6, u.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateSellingPrice(int id, BigDecimal price) throws SQLException {
        String sql = "UPDATE product_units SET selling_price = ? WHERE id = ? AND is_deleted = 0";
        try (Connection con = util.DatabaseUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBigDecimal(1, price);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "UPDATE product_units SET is_deleted = 1, deleted_at = NOW() WHERE id = ? AND is_deleted = 0";
        try (Connection con = util.DatabaseUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private void clearDefaultSale(int productId, int excludeId) throws SQLException {
        String sql = "UPDATE product_units SET is_default_sale = FALSE WHERE product_id = ? AND id != ? AND is_deleted = 0";
        try (Connection con = util.DatabaseUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.setInt(2, excludeId);
            ps.executeUpdate();
        }
    }

    public void createProductUnitIfNotExist(int productId, String unitName, String barcode, BigDecimal sellingPrice)
            throws Exception {
        String checkSql = "SELECT id FROM product_units WHERE barcode = ? AND is_deleted = 0";
        try (Connection con = util.DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(checkSql)) {
            ps.setString(1, util.ShortHash.ProductBarcodeHash(barcode));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return; // Nếu mã vạch này đã được đăng ký rồi thì thôi, thoát ra
                }
            }
        }
        String insertSql = "INSERT INTO product_units (product_id, unit_name, ratio, barcode, selling_price, is_default_sale) VALUES (?, ?, 1, ?, ?, TRUE)";
        try (Connection con = util.DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(insertSql)) {
            ps.setInt(1, productId);
            ps.setString(2, (unitName != null && !unitName.isEmpty()) ? unitName : "Cái");
            ps.setString(3, util.ShortHash.ProductBarcodeHash(barcode));
            ps.setBigDecimal(4, sellingPrice);
            ps.executeUpdate();
        }
    }

    /**
     * Insert a new product unit within an existing transaction.
     * Uses the provided connection to stay within the transaction boundary.
     */
    public void insertWithConnection(ProductUnit u, Connection conn) throws SQLException {
        String sql = "INSERT INTO product_units (product_id, unit_name, ratio, barcode, selling_price, is_default_sale) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, u.getProductId());
            ps.setString(2, u.getUnitName());
            ps.setInt(3, u.getRatio());
            try {
                ps.setString(4, ShortHash.ProductBarcodeHash(u.getBarcode()));
            } catch (Exception e) {
                throw new SQLException("Lỗi khi băm mã vạch sản phẩm: " + e.getMessage(), e);
            }

            ps.setBigDecimal(5, u.getSellingPrice());
            ps.setBoolean(6, u.isDefaultSale());
            ps.executeUpdate();
        }
    }
}
