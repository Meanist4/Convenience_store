package repository;

import convenience_store.DBConnection;

import entity.ProductUnit;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public Optional<ProductUnit> findById(int id) throws SQLException {
        String sql = "SELECT * FROM product_units WHERE id = ? AND is_deleted = 0";
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

    public Optional<ProductUnit> findByBarcode(String barcode) throws SQLException {
        String sql = "SELECT * FROM product_units WHERE barcode = ? AND is_deleted = 0";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, barcode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    public Optional<ProductUnit> findDefaultByProductId(int productId) throws SQLException {
        String sql = "SELECT * FROM product_units WHERE product_id = ? AND is_default_sale = TRUE AND is_deleted = 0 LIMIT 1";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    public boolean insert(ProductUnit u) throws SQLException {
        // Nếu unit mới là default, bỏ default của các unit cũ cùng sản phẩm
        if (u.isDefaultSale())
            clearDefaultSale(u.getProductId(), -1);

        String sql = "INSERT INTO product_units (product_id, unit_name, ratio, barcode, selling_price, is_default_sale) VALUES (?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, u.getProductId());
            ps.setString(2, u.getUnitName());
            ps.setInt(3, u.getRatio());
            ps.setString(4, u.getBarcode());
            ps.setBigDecimal(5, u.getSellingPrice());
            ps.setBoolean(6, u.isDefaultSale());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next())
                        u.setId(keys.getInt(1));
                }
                return true;
            }
        }
        return false;
    }

    public boolean update(ProductUnit u) throws SQLException {
        if (u.isDefaultSale())
            clearDefaultSale(u.getProductId(), u.getId());

        String sql = "UPDATE product_units SET unit_name=?, ratio=?, barcode=?, selling_price=?, is_default_sale=? WHERE id=? AND is_deleted=0";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, u.getUnitName());
            ps.setInt(2, u.getRatio());
            ps.setString(3, u.getBarcode());
            ps.setBigDecimal(4, u.getSellingPrice());
            ps.setBoolean(5, u.isDefaultSale());
            ps.setInt(6, u.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateSellingPrice(int id, BigDecimal price) throws SQLException {
        String sql = "UPDATE product_units SET selling_price = ? WHERE id = ? AND is_deleted = 0";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBigDecimal(1, price);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "UPDATE product_units SET is_deleted = 1, deleted_at = NOW() WHERE id = ? AND is_deleted = 0";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private void clearDefaultSale(int productId, int excludeId) throws SQLException {
        String sql = "UPDATE product_units SET is_default_sale = FALSE WHERE product_id = ? AND id != ? AND is_deleted = 0";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.setInt(2, excludeId);
            ps.executeUpdate();
        }
    }
}
