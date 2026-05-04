package service;

import java.sql.SQLException;
import java.util.List;

import entity.Notification;
import entity.StoreInventory;
import repository.InventoryRepository;
import repository.NotificationRepository;

public class NotificationService {

    private final NotificationRepository notificationRepo = new NotificationRepository();
    private final InventoryRepository inventoryRepo = new InventoryRepository();

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

    public void createInventoryAlert(int storeId, int productId, int currentQuantity, int minLevel)
            throws SQLException {
        String title = "CẢNH BÁO TỒN KHO - SẢN PHẨM SẮP HẾT";
        String content = String.format(
                "Sản phẩm ID: %d | Tồn kho hiện tại: %d cái | Ngưỡng tối thiểu: %d cái. Vui lòng nhập thêm hàng ngay!",
                productId, currentQuantity, minLevel);

        createNotification(storeId, title, content, "inventory_alert");
    }

    public void createSystemAlert(String title, String content) throws SQLException {
        createNotification(null, title, content, "system_alert");
    }

    public void createPayrollAlert(int storeId, String title, String content) throws SQLException {
        createNotification(storeId, title, content, "payroll_alert");
    }

    public void checkAndAlertLowStockForStore(int storeId) throws SQLException {
        List<StoreInventory> lowStockItems = inventoryRepo.findItemsBelowMinLevel(storeId);

        for (StoreInventory item : lowStockItems) {
            createInventoryAlert(storeId, item.getProductId(), item.getQuantity(), item.getMinStockLevel());
        }
    }

    public void checkAndAlertLowStockForAllStores() throws SQLException {
        // Lấy tất cả items có tồn kho dưới ngưỡng tối thiểu
        // Giả sử có method getAll() trong InventoryRepository
        // Với giới hạn hiện tại, chúng ta cần iterate qua từng store
        // Đây là placeholder - trong thực tế cần implement
        // getItemsBelowMinLevelAllStores()
    }

    public List<Notification> getUnreadNotifications(int storeId) throws SQLException {
        return notificationRepo.findUnreadByStore(storeId);
    }

    public void markAsRead(int notificationId) throws SQLException {
        // Cần implement trong NotificationRepository
        // UPDATE notifications SET is_read = 1 WHERE id = ?
    }

    public void markAllAsRead(int storeId) throws SQLException {
        // Cần implement trong NotificationRepository
        // UPDATE notifications SET is_read = 1 WHERE store_id = ?
    }

    public void deleteOldNotifications(int daysOld) throws SQLException {
        // Cần implement trong NotificationRepository
        // DELETE FROM notifications WHERE created_at < DATE_SUB(NOW(), INTERVAL ? DAY)
    }
}
