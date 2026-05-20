package service.impl;
import service.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import entity.Notification;
import entity.ProductUnit;
import entity.StoreInventory;
import exception.NotFoundException;
import exception.ValidationException;
import repository.InventoryRepository;
import repository.NotificationRepository;
import repository.ProductRepository;
import repository.StoreRepository;
import util.BarcodeUtil;

public class InventoryServiceImpl implements InventoryService {
    private final InventoryRepository inventoryRepo = new InventoryRepository();
    private final StoreRepository storeRepo = new StoreRepository();
    private final ProductRepository productRepo = new ProductRepository();
    private final NotificationRepository notificationRepo = new NotificationRepository();

    @Override public List<StoreInventory> getInventoryByStore(int storeId) throws SQLException { return inventoryRepo.findByStoreId(storeId); }
    @Override public Optional<StoreInventory> getInventory(int storeId, int productId) throws SQLException { return inventoryRepo.findByStoreAndProduct(storeId, productId); }
    @Override public List<StoreInventory> getLowStockItems(int storeId, int threshold) throws SQLException { if (threshold < 0) throw new IllegalArgumentException("Ngưỡng tồn kho phải >= 0"); return inventoryRepo.findLowStock(storeId, threshold); }
    @Override public List<StoreInventory> getLowStockItems(int storeId) throws SQLException { return inventoryRepo.findItemsBelowMinLevel(storeId); }

    @Override
    public void setStock(int storeId, int productId, int quantity) throws SQLException {
        if (quantity < 0) throw new IllegalArgumentException("Số lượng tồn kho không được âm");
        storeRepo.findById(storeId).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy cửa hàng id=" + storeId));
        productRepo.findById(productId).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm id=" + productId));
        if (!inventoryRepo.insert(new StoreInventory(storeId, productId, quantity))) throw new RuntimeException("Thiết lập tồn kho thất bại");
    }

    @Override
    public void updateStock(int storeId, int productId, int quantity) throws SQLException {
        if (quantity < 0) throw new IllegalArgumentException("Số lượng tồn kho không được âm");
        inventoryRepo.findByStoreAndProduct(storeId, productId).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tồn kho store=" + storeId + " product=" + productId));
        if (!inventoryRepo.updateQuantity(storeId, productId, quantity)) throw new RuntimeException("Cập nhật tồn kho thất bại");
    }

    @Override
    public void adjustStock(int storeId, int productId, int delta) throws SQLException {
        StoreInventory inv = inventoryRepo.findByStoreAndProduct(storeId, productId).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tồn kho store=" + storeId + " product=" + productId));
        if (inv.getQuantity() + delta < 0) throw new IllegalStateException("Tồn kho không đủ. Hiện tại: " + inv.getQuantity() + ", điều chỉnh: " + delta);
        if (!inventoryRepo.adjustQuantity(storeId, productId, delta)) throw new RuntimeException("Điều chỉnh tồn kho thất bại");
    }

    @Override
    public void checkAndNotifyLowStock(int storeId, int productId) throws SQLException {
        Optional<StoreInventory> invOpt = inventoryRepo.findByStoreAndProduct(storeId, productId);
        if (invOpt.isPresent()) {
            StoreInventory inv = invOpt.get();
            if (inv.getQuantity() <= inv.getMinStockLevel()) {
                Notification note = new Notification();
                note.setStoreId(storeId); note.setTitle("CẢNH BÁO TỒN KHO");
                note.setContent("Sản phẩm ID " + productId + " hiện chỉ còn " + inv.getQuantity() + ". Vui lòng nhập thêm hàng!");
                note.setType("inventory_alert");
                notificationRepo.insert(note);
            }
        }
    }

    @Override public void importStock(int storeId, int productId, int quantity) throws SQLException { if (quantity <= 0) throw new IllegalArgumentException("Số lượng nhập phải > 0"); adjustStock(storeId, productId, quantity); }
    @Override public void removeInventory(int storeId, int productId) throws SQLException { if (!inventoryRepo.delete(storeId, productId)) throw new IllegalArgumentException("Không tìm thấy bản ghi tồn kho để xóa"); }

    @Override
    public void processInventoryScan(String barcode, int storeId, int quantity) throws SQLException {
        if (quantity <= 0) throw new ValidationException("Số lượng scan phải > 0");
        Optional<ProductUnit> unitOpt = BarcodeUtil.scanProduct(barcode);
        if (unitOpt.isEmpty()) throw new NotFoundException("Không tìm thấy sản phẩm với barcode: " + barcode);
        adjustStock(storeId, unitOpt.get().getProductId(), quantity);
    }
}

