package repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import convenience_store.DBConnection;
import entity.StoreInventory;

public class InventoryRepository {

    private StoreInventory map(ResultSet rs) throws SQLException {
        StoreInventory inv = new StoreInventory();
        inv.setStoreId(rs.getInt("store_id"));
        inv.setProductId(rs.getInt("product_id"));
        inv.setQuantity(rs.getInt("quantity"));
        inv.setMinStockLevel(rs.getInt("min_stock_level"));
        return inv;
    }

    private StoreInventory mapResultSetToEntity(ResultSet rs) throws SQLException {
        return map(rs);
    }

    public List<StoreInventory> findByStoreId(int storeId) throws SQLException {
        List<StoreInventory> list = new ArrayList<>();
        String sql = "SELECT si.* FROM store_inventory si " +
                "JOIN products p ON p.id = si.product_id " +
                "WHERE si.store_id = ? AND p.is_deleted = 0";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, storeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(map(rs));
            }
        }
        return list;
    }

    public Optional<StoreInventory> findByStoreAndProduct(int storeId, int productId) throws SQLException {
        String sql = "SELECT * FROM store_inventory WHERE store_id = ? AND product_id = ?";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, storeId);
            ps.setInt(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    public List<StoreInventory> findLowStock(int storeId, int threshold) throws SQLException {
        List<StoreInventory> list = new ArrayList<>();
        String sql = "SELECT * FROM store_inventory WHERE store_id = ? AND quantity <= ?";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, storeId);
            ps.setInt(2, threshold);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(map(rs));
            }
        }
        return list;
    }

    public boolean insert(StoreInventory inv) throws SQLException {
        String sql = "INSERT INTO store_inventory (store_id, product_id, quantity, min_stock_level) VALUES (?, ?, ?, ?) "
                +
                "ON DUPLICATE KEY UPDATE quantity = VALUES(quantity), min_stock_level = VALUES(min_stock_level)";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, inv.getStoreId());
            ps.setInt(2, inv.getProductId());
            ps.setInt(3, inv.getQuantity());
            ps.setInt(4, inv.getMinStockLevel());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateQuantity(int storeId, int productId, int quantity) throws SQLException {
        String sql = "UPDATE store_inventory SET quantity = ? WHERE store_id = ? AND product_id = ?";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, storeId);
            ps.setInt(3, productId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean adjustQuantity(int storeId, int productId, int delta) throws SQLException {
        String sql = "UPDATE store_inventory SET quantity = quantity + ? WHERE store_id = ? AND product_id = ?";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setInt(2, storeId);
            ps.setInt(3, productId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int storeId, int productId) throws SQLException {
        String sql = "DELETE FROM store_inventory WHERE store_id = ? AND product_id = ?";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, storeId);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<StoreInventory> findItemsBelowMinLevel(int storeId) throws SQLException {
        List<StoreInventory> list = new ArrayList<>();
        // SQL tìm những sản phẩm có số lượng <= ngưỡng tối thiểu của riêng sản phẩm đó
        String sql = "SELECT * FROM store_inventory WHERE store_id = ? AND quantity <= min_stock_level";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, storeId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToEntity(rs));
                }
            }
        }
        return list;
    }
}
