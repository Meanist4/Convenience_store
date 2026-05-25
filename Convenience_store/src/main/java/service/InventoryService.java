package service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import entity.StoreInventory;

public interface InventoryService {
    List<StoreInventory> getInventoryByStore(int storeId) throws SQLException;

    Optional<StoreInventory> getInventory(int storeId, int productId) throws SQLException;

    List<StoreInventory> getLowStockItems(int storeId, int threshold) throws SQLException;

    List<StoreInventory> getLowStockItems(int storeId) throws SQLException;

    void setStock(int storeId, int productId, int quantity) throws SQLException;

    void updateStock(int storeId, int productId, int quantity) throws SQLException;

    void adjustStock(int storeId, int productId, int delta) throws SQLException;

    void checkAndNotifyLowStock(int storeId, int productId) throws SQLException;

    void importStock(int storeId, int productId, int quantity) throws SQLException;

    void removeInventory(int storeId, int productId) throws SQLException;

    void processInventoryScan(String barcode, int storeId, int quantity) throws SQLException;

    void deductStockFEFO(int storeId, int productId, int requiredQty, int invoiceId) throws SQLException;
}
