package service;

import entity.Store;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface StoreService {
    List<Store> getAllStores() throws SQLException;
    List<Store> getActiveStores() throws SQLException;
    Optional<Store> getStoreById(int id) throws SQLException;
    Optional<Store> getStoreByManagerId(int managerId) throws SQLException;
    List<Store> searchStores(String keyword) throws SQLException;
    Store createStore(String storeName, String location) throws SQLException;
    Store updateStore(int id, String storeName, String location, String status) throws SQLException;
    void assignManager(int storeId, Integer managerId) throws SQLException;
    void updateStatus(int id, String status) throws SQLException;
    void deleteStore(int id) throws SQLException;
}
