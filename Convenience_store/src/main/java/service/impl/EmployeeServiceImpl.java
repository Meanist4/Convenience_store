package service.impl;

import service.*;
import entity.Employee;
import repository.EmployeeRepository;
import repository.StoreRepository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepo = new EmployeeRepository();
    private final StoreRepository storeRepo = new StoreRepository();

    @Override
    public List<Employee> getAllEmployees() throws SQLException {
        return employeeRepo.findAll();
    }

    @Override
    public Optional<Employee> getEmployeeById(int id) throws SQLException {
        return employeeRepo.findById(id);
    }

    @Override
    public Optional<Employee> getEmployeeByIdCard(String idCard) throws SQLException {
        return employeeRepo.findByIdCard(idCard);
    }

    @Override
    public Optional<Employee> getEmployeeByBarcode(String barcode) throws SQLException {
        return employeeRepo.findByBarcode(barcode);
    }

    @Override
    public Optional<Employee> getEmployeeByPhone(String phone) throws SQLException {
        return employeeRepo.findByPhone(phone);
    }

    @Override
    public List<Employee> getEmployeesByStore(int storeId) throws SQLException {
        return employeeRepo.findByStoreId(storeId);
    }

    @Override
    public List<Employee> getEmployeesByStatus(String status) throws SQLException {
        return employeeRepo.findByStatus(status);
    }

    @Override
    public List<Employee> searchEmployees(String keyword) throws SQLException {
        if (keyword == null || keyword.isBlank()) {
            return employeeRepo.findAll();
        }
        return employeeRepo.searchByName(keyword.trim());
    }

    @Override
    public Employee createEmployee(String fullName, Date birthday, String gender,
            String idCard, String phone, String email, String address,
            Integer storeId, BigDecimal hourlyRate) throws SQLException {

        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Họ tên không được để trống");
        }
        if (idCard == null || idCard.isBlank()) {
            throw new IllegalArgumentException("CMND/CCCD không được để trống");
        }
        if (hourlyRate == null || hourlyRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Lương giờ không hợp lệ");
        }

        if (employeeRepo.findByIdCard(idCard).isPresent()) {
            throw new IllegalStateException("CMND/CCCD đã tồn tại: " + idCard);
        }
        if (phone != null && !phone.isBlank() && employeeRepo.findByPhone(phone).isPresent()) {
            throw new IllegalStateException("Số điện thoại đã tồn tại: " + phone);
        }
        if (storeId != null) {
            storeRepo.findById(storeId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy cửa hàng id=" + storeId));
        }

        Employee e = new Employee();
        e.setFullName(fullName.trim());
        e.setBirthday(birthday);
        e.setGender(gender);
        e.setIdCard(idCard.trim());

        e.setPhone(phone);
        e.setEmail(email);
        e.setAddress(address);
        e.setStoreId(storeId);
        e.setHourlyRate(hourlyRate);
        e.setStatus("active");
        try {
            e.setEmployeeBarcode(genBarcode(idCard));
        } catch (Exception ex) {
            System.getLogger(EmployeeServiceImpl.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        try {
            if (!employeeRepo.insert(e)) {
                throw new RuntimeException("Tạo nhân viên thất bại");
            }
        } catch (Exception ex) {
            System.getLogger(EmployeeServiceImpl.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        return e;
    }

    @Override
    public Employee updateEmployee(int id, String fullName, Date birthday, String gender,
            String idCard, String phone, String email, String address,
            Integer storeId, BigDecimal hourlyRate, String status) throws SQLException {

        Employee e = employeeRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên id=" + id));

        Optional<Employee> sameCard = employeeRepo.findByIdCard(idCard);
        if (sameCard.isPresent() && sameCard.get().getId() != id) {
            throw new IllegalStateException("CMND/CCCD đã tồn tại: " + idCard);
        }

        if (phone != null && !phone.isBlank()) {
            Optional<Employee> samePhone = employeeRepo.findByPhone(phone);
            if (samePhone.isPresent() && samePhone.get().getId() != id) {
                throw new IllegalStateException("Số điện thoại đã tồn tại: " + phone);
            }
        }

        e.setFullName(fullName.trim());
        e.setBirthday(birthday);
        e.setGender(gender);
        e.setIdCard(idCard.trim());
        e.setPhone(phone);
        e.setEmail(email);
        e.setAddress(address);
        e.setStoreId(storeId);
        e.setHourlyRate(hourlyRate);
        e.setStatus(status);

        try {
            if (!employeeRepo.update(e)) {
                throw new RuntimeException("Cập nhật nhân viên thất bại");
            }
        } catch (Exception ex) {
            System.getLogger(EmployeeServiceImpl.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        return e;
    }

    @Override
    public void updateHourlyRate(int id, BigDecimal newRate) throws SQLException {
        if (newRate == null || newRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Lương giờ không hợp lệ");
        }
        employeeRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên id=" + id));
        if (!employeeRepo.updateHourlyRate(id, newRate)) {
            throw new RuntimeException("Cập nhật lương giờ thất bại");
        }
    }

    @Override
    public void updateStatus(int id, String status) throws SQLException {
        List<String> validStatuses = List.of("active", "inactive", "on_leave");
        if (!validStatuses.contains(status)) {
            throw new IllegalArgumentException("Status không hợp lệ: " + status);
        }
        employeeRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên id=" + id));
        if (!employeeRepo.updateStatus(id, status)) {
            throw new RuntimeException("Cập nhật trạng thái thất bại");
        }
    }

    @Override
    public void deleteEmployee(int id) throws SQLException {
        if (!employeeRepo.delete(id)) {
            throw new IllegalArgumentException("Không tìm thấy nhân viên hoặc đã bị xóa: id=" + id);
        }
    }

    private String generateBarcode() {
        return "EMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String genBarcode(String idCard) throws Exception {
        return util.ShortHash.getBarcodeData(idCard);
    }
}
