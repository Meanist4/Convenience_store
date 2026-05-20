package service;

import java.sql.SQLException;
import java.util.List;

import entity.PurchaseOrder;
import entity.PurchaseOrderDetail;

public interface PurchaseOrderService {
    PurchaseOrder createOrder(int supplierId, int storeId, Integer adminId, List<PurchaseOrderDetail> details)
            throws SQLException;

    PurchaseOrder getOrderById(int id) throws SQLException;

    List<PurchaseOrderDetail> getOrderDetails(int orderId) throws SQLException;

    void updateOrderStatus(int orderId, String status) throws SQLException;

    void receiveOrder(int orderId) throws SQLException;

    void cancelOrder(int orderId) throws SQLException;

    void addDetailToOrder(int orderId, PurchaseOrderDetail detail) throws SQLException;

    void removeDetailFromOrder(int detailId) throws SQLException;
}
