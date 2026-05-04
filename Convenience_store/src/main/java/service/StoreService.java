package service;

import entity.Store;
import repository.StoreRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class StoreService {

    private final StoreRepository storeRepo = new StoreRepository();

    public List<Store> getAllStores() throws SQLException {
        return storeRepo.findAll();
    }

    public List<Store> getActiveStores() throws SQLException {
        return storeRepo.findAllActive();
    }

    public Optional<Store> getStoreById(int id) throws SQLException {
        return storeRepo.findById(id);
    }

    public Optional<Store> getStoreByManagerId(int managerId) throws SQLException {
        return storeRepo.findByManagerId(managerId);
    }

    public List<Store> searchStores(String keyword) throws SQLException {
        if (keyword == null || keyword.isBlank())
            return storeRepo.findAll();
        return storeRepo.searchByName(keyword.trim());
    }

    public Store createStore(String storeName, String location) throws SQLException {
        if (storeName == null || storeName.isBlank())
            throw new IllegalArgumentException("Tên cửa hàng không được để trống");

        Store s = new Store();
        s.setStoreName(storeName.trim());
        s.setLocation(location);
        s.setStatus("active");

        if (!storeRepo.insert(s))
            throw new RuntimeException("Tạo cửa hàng thất bại");
        return s;
    }

    public Store updateStore(int id, String storeName, String location, String status) throws SQLException {
        Store s = storeRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy cửa hàng id=" + id));

        if (storeName == null || storeName.isBlank())
            throw new IllegalArgumentException("Tên cửa hàng không được để trống");

        s.setStoreName(storeName.trim());
        s.setLocation(location);
        s.setStatus(status);

        if (!storeRepo.update(s))
            throw new RuntimeException("Cập nhật cửa hàng thất bại");
        return s;
    }

    public void assignManager(int storeId, Integer managerId) throws SQLException {
        storeRepo.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy cửa hàng id=" + storeId));

        if (!storeRepo.assignManager(storeId, managerId))
            throw new RuntimeException("Gán quản lý thất bại");
    }

    public void updateStatus(int id, String status) throws SQLException {
        if (!"active".equals(status) && !"inactive".equals(status))
            throw new IllegalArgumentException("Status phải là 'active' hoặc 'inactive'");

        storeRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy cửa hàng id=" + id));

        if (!storeRepo.updateStatus(id, status))
            throw new RuntimeException("Cập nhật trạng thái thất bại");
    }

    public void deleteStore(int id) throws SQLException {
        if (!storeRepo.delete(id))
            throw new IllegalArgumentException("Không tìm thấy cửa hàng hoặc đã bị xóa: id=" + id);
    }
}
