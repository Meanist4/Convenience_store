package repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import entity.StoreInventory;
import util.DatabaseUtil;

public class InventoryRepositoryImpl implements InventoryRepository {

    private StoreInventory map(ResultSet rs) throws SQLException {
        StoreInventory inventory = new StoreInventory();
        inventory.setId(rs.getInt("id"));
        inventory.setStoreId(rs.getInt("store_id"));
        inventory.setProductId(rs.getInt("product_id"));
        inventory.setQuantity(rs.getInt("quantity"));
        inventory.setMinStockLevel(rs.getInt("min_stock_level"));
        inventory.setBatchCode(rs.getString("batch_code"));
        inventory.setImportPrice(rs.getBigDecimal("import_price"));
        inventory.setExpiryDate(rs.getDate("expiry_date"));
        inventory.setReceivedAt(rs.getDate("received_at"));
        return inventory;
    }

    private boolean shouldClose(Connection conn) throws SQLException {
        return conn != null && !conn.isClosed() && conn.getAutoCommit();
    }

    @Override
    public boolean insert(StoreInventory inventory) throws SQLException {
        String sql = "INSERT INTO store_inventory (store_id, product_id, batch_code, quantity, import_price, expiry_date, received_at, min_stock_level) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = DatabaseUtil.getConnection();
        boolean closeConnection = shouldClose(conn);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, inventory.getStoreId());
            ps.setInt(2, inventory.getProductId());
            ps.setString(3, inventory.getBatchCode());
            ps.setInt(4, inventory.getQuantity());
            if (inventory.getImportPrice() != null) {
                ps.setBigDecimal(5, inventory.getImportPrice());
            } else {
                ps.setNull(5, java.sql.Types.DECIMAL);
            }
            if (inventory.getExpiryDate() != null) {
                ps.setDate(6, new Date(inventory.getExpiryDate().getTime()));
            } else {
                ps.setNull(6, java.sql.Types.DATE);
            }
            if (inventory.getReceivedAt() != null) {
                ps.setDate(7, new Date(inventory.getReceivedAt().getTime()));
            } else {
                ps.setDate(7, new Date(System.currentTimeMillis()));
            }
            ps.setInt(8, inventory.getMinStockLevel());
            return ps.executeUpdate() > 0;
        } finally {
            if (closeConnection) {
                conn.close();
            }
        }
    }

    @Override
    public List<StoreInventory> findAvailableBatches(int storeId, int productId) throws SQLException {
        String sql = "SELECT * FROM store_inventory WHERE store_id = ? AND product_id = ? AND quantity > 0 "
                + "ORDER BY (expiry_date IS NULL), expiry_date ASC, received_at ASC, id ASC";
        List<StoreInventory> result = new ArrayList<>();

        Connection conn = DatabaseUtil.getConnection();
        boolean closeConnection = shouldClose(conn);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, storeId);
            ps.setInt(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
        } finally {
            if (closeConnection) {
                conn.close();
            }
        }

        return result;
    }

    @Override
    public boolean updateQuantity(int inventoryId, int newQuantity) throws SQLException {
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Số lượng không được âm");
        }

        String sql = "UPDATE store_inventory SET quantity = ? WHERE id = ?";
        Connection conn = DatabaseUtil.getConnection();
        boolean closeConnection = shouldClose(conn);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newQuantity);
            ps.setInt(2, inventoryId);
            return ps.executeUpdate() > 0;
        } finally {
            if (closeConnection) {
                conn.close();
            }
        }
    }

    @Override
    public int findTotalQuantity(int storeId, int productId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(quantity), 0) FROM store_inventory "
                + "WHERE store_id = ? AND product_id = ? AND quantity > 0";

        Connection conn = DatabaseUtil.getConnection();
        boolean closeConnection = shouldClose(conn);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, storeId);
            ps.setInt(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } finally {
            if (closeConnection) {
                conn.close();
            }
        }

        return 0;
    }

    @Override
    public List<StoreInventory> findByStoreId(int storeId) throws SQLException {
        String sql = "SELECT si.* FROM store_inventory si JOIN products p ON p.id = si.product_id "
                + "WHERE si.store_id = ? AND p.is_deleted = 0";
        List<StoreInventory> result = new ArrayList<>();

        Connection conn = DatabaseUtil.getConnection();
        boolean closeConnection = shouldClose(conn);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, storeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
        } finally {
            if (closeConnection) {
                conn.close();
            }
        }

        return result;
    }

    @Override
    public Optional<StoreInventory> findByStoreAndProduct(int storeId, int productId) throws SQLException {
        String sql = "SELECT * FROM store_inventory WHERE store_id = ? AND product_id = ?";

        Connection conn = DatabaseUtil.getConnection();
        boolean closeConnection = shouldClose(conn);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, storeId);
            ps.setInt(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        } finally {
            if (closeConnection) {
                conn.close();
            }
        }

        return Optional.empty();
    }

    @Override
    public List<StoreInventory> findLowStock(int storeId, int threshold) throws SQLException {
        String sql = "SELECT * FROM store_inventory WHERE store_id = ? AND quantity <= ?";
        List<StoreInventory> result = new ArrayList<>();

        Connection conn = DatabaseUtil.getConnection();
        boolean closeConnection = shouldClose(conn);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, storeId);
            ps.setInt(2, threshold);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
        } finally {
            if (closeConnection) {
                conn.close();
            }
        }

        return result;
    }

    @Override
    public boolean updateQuantity(int storeId, int productId, int quantity) throws SQLException {
        if (quantity < 0) {
            throw new IllegalArgumentException("Số lượng không được âm");
        }

        String sql = "UPDATE store_inventory SET quantity = ? WHERE store_id = ? AND product_id = ?";
        Connection conn = DatabaseUtil.getConnection();
        boolean closeConnection = shouldClose(conn);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, storeId);
            ps.setInt(3, productId);
            return ps.executeUpdate() > 0;
        } finally {
            if (closeConnection) {
                conn.close();
            }
        }
    }

    @Override
    public boolean adjustQuantity(int storeId, int productId, int delta) throws SQLException {
        String sql = "UPDATE store_inventory SET quantity = quantity + ? WHERE store_id = ? AND product_id = ?";

        Connection conn = DatabaseUtil.getConnection();
        boolean closeConnection = shouldClose(conn);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setInt(2, storeId);
            ps.setInt(3, productId);
            return ps.executeUpdate() > 0;
        } finally {
            if (closeConnection) {
                conn.close();
            }
        }
    }

    @Override
    public boolean delete(int storeId, int productId) throws SQLException {
        String sql = "DELETE FROM store_inventory WHERE store_id = ? AND product_id = ?";

        Connection conn = DatabaseUtil.getConnection();
        boolean closeConnection = shouldClose(conn);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, storeId);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        } finally {
            if (closeConnection) {
                conn.close();
            }
        }
    }

    @Override
    public List<StoreInventory> findItemsBelowMinLevel(int storeId) throws SQLException {
        String sql = "SELECT * FROM store_inventory WHERE store_id = ? AND quantity <= min_stock_level";
        List<StoreInventory> result = new ArrayList<>();

        Connection conn = DatabaseUtil.getConnection();
        boolean closeConnection = shouldClose(conn);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, storeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
        } finally {
            if (closeConnection) {
                conn.close();
            }
        }

        return result;
    }

    @Override
    public boolean insertWithConnection(StoreInventory inventory, Connection conn) throws SQLException {
        String sql = "INSERT INTO store_inventory (store_id, product_id, batch_code, quantity, import_price, expiry_date, received_at, min_stock_level) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, inventory.getStoreId());
            ps.setInt(2, inventory.getProductId());
            ps.setString(3, inventory.getBatchCode());
            ps.setInt(4, inventory.getQuantity());
            if (inventory.getImportPrice() != null) {
                ps.setBigDecimal(5, inventory.getImportPrice());
            } else {
                ps.setNull(5, java.sql.Types.DECIMAL);
            }
            if (inventory.getExpiryDate() != null) {
                ps.setDate(6, new Date(inventory.getExpiryDate().getTime()));
            } else {
                ps.setNull(6, java.sql.Types.DATE);
            }
            if (inventory.getReceivedAt() != null) {
                ps.setDate(7, new Date(inventory.getReceivedAt().getTime()));
            } else {
                ps.setDate(7, new Date(System.currentTimeMillis()));
            }
            ps.setInt(8, inventory.getMinStockLevel());
            return ps.executeUpdate() > 0;
        }
    }
}
