package service;

import java.sql.SQLException;
import java.util.List;

import entity.Notification;

public interface NotificationService {
    void createNotification(Integer storeId, String title, String content, String type) throws SQLException;

    void createInventoryAlert(int storeId, int productId, int currentQuantity, int minLevel) throws SQLException;

    void createSystemAlert(String title, String content) throws SQLException;

    void createPayrollAlert(int storeId, String title, String content) throws SQLException;

    void checkAndAlertLowStockForStore(int storeId) throws SQLException;

    void checkAndAlertLowStockForAllStores() throws SQLException;

    List<Notification> getUnreadNotifications(int storeId) throws SQLException;

    void markAsRead(int notificationId) throws SQLException;

    void markAllAsRead(int storeId) throws SQLException;

    void deleteOldNotifications(int daysOld) throws SQLException;
}
