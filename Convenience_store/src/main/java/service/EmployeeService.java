package service;

import entity.Employee;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface EmployeeService {
    List<Employee> getAllEmployees() throws SQLException;
    Optional<Employee> getEmployeeById(int id) throws SQLException;
    Optional<Employee> getEmployeeByIdCard(String idCard) throws SQLException;
    Optional<Employee> getEmployeeByBarcode(String barcode) throws SQLException;
    Optional<Employee> getEmployeeByPhone(String phone) throws SQLException;
    List<Employee> getEmployeesByStore(int storeId) throws SQLException;
    List<Employee> getEmployeesByStatus(String status) throws SQLException;
    List<Employee> searchEmployees(String keyword) throws SQLException;
    Employee createEmployee(String fullName, Date birthday, String gender, String idCard, String phone, String email, String address, Integer storeId, BigDecimal hourlyRate) throws SQLException;
    Employee updateEmployee(int id, String fullName, Date birthday, String gender, String idCard, String phone, String email, String address, Integer storeId, BigDecimal hourlyRate, String status) throws SQLException;
    void updateHourlyRate(int id, BigDecimal newRate) throws SQLException;
    void updateStatus(int id, String status) throws SQLException;
    void deleteEmployee(int id) throws SQLException;
}
