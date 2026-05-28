package service;

import java.sql.SQLException;
import java.util.List;

import entity.PurchaseOrder;
import entity.PurchaseOrderDetail;

public interface PurchaseOrderService {

    PurchaseOrder createOrder(int supplierId, int storeId, Integer adminId, List<PurchaseOrderDetail> details)
            throws SQLException;

    int insert(entity.PurchaseOrder po, java.sql.Connection conn) throws java.sql.SQLException;

    PurchaseOrder getOrderById(int id) throws SQLException;

    List<PurchaseOrderDetail> getOrderDetails(int orderId) throws SQLException;

    void updateOrderStatus(int orderId, String status) throws SQLException;

    void receiveOrder(int orderId) throws SQLException, Exception;

    void cancelOrder(int orderId) throws SQLException;

    void addDetailToOrder(int orderId, PurchaseOrderDetail detail) throws SQLException;

    void removeDetailFromOrder(int detailId) throws SQLException;

    void approveAndImportInventory(int orderId, int storeId, java.util.List<dto.InventoryImportDTO> importItems)
            throws java.sql.SQLException;

    List<PurchaseOrder> getAllOrders() throws SQLException;

    List<PurchaseOrder> getOrdersByStatus(String status) throws SQLException;

    List<String[]> getAvailableStatuses() throws SQLException;

    String[] getSavedBatchAndExpiry(int orderId, int productId) throws Exception;

    void insertOrderDetailSmart(int orderId, int productId, String productName, String barcode,
            int quantity, java.math.BigDecimal importPrice, java.math.BigDecimal sellingPrice,
            java.math.BigDecimal markupRate, String category, String baseUnit, java.sql.Connection conn)
            throws Exception;

}
