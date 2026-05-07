package service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import entity.Manager;

public interface ManagerService {
    List<Manager> getAllManagers() throws SQLException;

    Optional<Manager> getManagerById(int id) throws SQLException;

    Optional<Manager> getManagerByEmployeeId(int employeeId) throws SQLException;

    Optional<Manager> getManagerByUsername(String username) throws SQLException;

    Manager createManager(int employeeId, String username, String rawPassword, String managementLevel, BigDecimal allowance)
            throws SQLException;

    Manager updateManager(int id, String username, String managementLevel, BigDecimal allowance) throws SQLException;

    void updateAllowance(int id, BigDecimal allowance) throws SQLException;

    void changePassword(int id, String oldRawPassword, String newRawPassword) throws SQLException;

    void deleteManager(int id) throws SQLException;
}