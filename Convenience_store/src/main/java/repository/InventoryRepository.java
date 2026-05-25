package repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import entity.StoreInventory;

public interface InventoryRepository {
    boolean insert(StoreInventory inventory) throws SQLException;

    List<StoreInventory> findAvailableBatches(int storeId, int productId) throws SQLException;

    boolean updateQuantity(int inventoryId, int newQuantity) throws SQLException;

    int findTotalQuantity(int storeId, int productId) throws SQLException;

    List<StoreInventory> findByStoreId(int storeId) throws SQLException;

    Optional<StoreInventory> findByStoreAndProduct(int storeId, int productId) throws SQLException;

    List<StoreInventory> findLowStock(int storeId, int threshold) throws SQLException;

    boolean updateQuantity(int storeId, int productId, int quantity) throws SQLException;

    boolean adjustQuantity(int storeId, int productId, int delta) throws SQLException;

    boolean delete(int storeId, int productId) throws SQLException;

    List<StoreInventory> findItemsBelowMinLevel(int storeId) throws SQLException;
}
