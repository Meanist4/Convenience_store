package service.impl;

import service.*;
import java.sql.SQLException;
import java.util.List;

import entity.Notification;
import entity.StoreInventory;
import repository.InventoryRepository;
import repository.InventoryRepositoryImpl;
import repository.NotificationRepository;

public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepo = new NotificationRepository();
    private final InventoryRepository inventoryRepo = new InventoryRepositoryImpl();

    @Override
    public void createNotification(Integer storeId, String title, String content, String type) throws SQLException {
        if (title == null || title.isBlank())
            throw new IllegalArgumentException("Tiêu đề không được để trống");

        if (content == null || content.isBlank())
            throw new IllegalArgumentException("Nội dung không được để trống");

        if (type == null
                || (!type.equals("inventory_alert") && !type.equals("system_alert") && !type.equals("payroll_alert")))
            throw new IllegalArgumentException("Loại thông báo không hợp lệ");

        Notification notif = new Notification();
        notif.setStoreId(storeId);
        notif.setTitle(title);
        notif.setContent(content);
        notif.setType(type);
        notif.setRead(false);

        notificationRepo.insert(notif);
    }

    @Override
    public void createInventoryAlert(int storeId, int productId, int currentQuantity, int minLevel)
            throws SQLException {
        String title = "CẢNH BÁO TỒN KHO - SẢN PHẨM SẮP HẾT";
        String content = String.format(
                "Sản phẩm ID: %d | Tồn kho hiện tại: %d cái | Ngưỡng tối thiểu: %d cái. Vui lòng nhập thêm hàng ngay!",
                productId, currentQuantity, minLevel);

        createNotification(storeId, title, content, "inventory_alert");
    }

    @Override
    public void createSystemAlert(String title, String content) throws SQLException {
        createNotification(null, title, content, "system_alert");
    }

    @Override
    public void createPayrollAlert(int storeId, String title, String content) throws SQLException {
        createNotification(storeId, title, content, "payroll_alert");
    }

    @Override
    public void checkAndAlertLowStockForStore(int storeId) throws SQLException {
        List<StoreInventory> lowStockItems = inventoryRepo.findItemsBelowMinLevel(storeId);

        for (StoreInventory item : lowStockItems) {
            createInventoryAlert(storeId, item.getProductId(), item.getQuantity(), item.getMinStockLevel());
        }
    }

    @Override
    public void checkAndAlertLowStockForAllStores() throws SQLException {
        // Placeholder for all-store low-stock scan implementation.
    }

    @Override
    public List<Notification> getUnreadNotifications(int storeId) throws SQLException {
        return notificationRepo.findUnreadByStore(storeId);
    }

    @Override
    public void markAsRead(int notificationId) throws SQLException {
        // TODO: implement in NotificationRepository.
    }

    @Override
    public void markAllAsRead(int storeId) throws SQLException {
        // TODO: implement in NotificationRepository.
    }

    @Override
    public void deleteOldNotifications(int daysOld) throws SQLException {
        // TODO: implement in NotificationRepository.
    }
}
