package service.impl;

import service.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import entity.Manager;
import repository.EmployeeRepository;
import repository.ManagerRepository;
import util.Argon2Hasher;

public class ManagerServiceImpl implements ManagerService {

    private final ManagerRepository managerRepo = new ManagerRepository();
    private final EmployeeRepository employeeRepo = new EmployeeRepository();

    @Override
    public List<Manager> getAllManagers() throws SQLException {
        return managerRepo.findAll();
    }

    @Override
    public Optional<Manager> getManagerById(int id) throws SQLException {
        return managerRepo.findById(id);
    }

    @Override
    public Optional<Manager> getManagerByEmployeeId(int employeeId) throws SQLException {
        return managerRepo.findByEmployeeId(employeeId);
    }

    @Override
    public Optional<Manager> getManagerByUsername(String username) throws SQLException {
        return managerRepo.findByUsername(username);
    }

    @Override
    public Manager createManager(int employeeId, String username, String rawPassword,
            String managementLevel, BigDecimal allowance) throws SQLException {

        employeeRepo.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên id=" + employeeId));

        if (managerRepo.findByEmployeeId(employeeId).isPresent())
            throw new IllegalStateException("Nhân viên này đã là quản lý");

        if (managerRepo.findByUsername(username).isPresent())
            throw new IllegalStateException("Username đã tồn tại: " + username);

        if (rawPassword == null || rawPassword.length() < 6)
            throw new IllegalArgumentException("Password phải có ít nhất 6 ký tự");

        Manager m = new Manager();
        m.setEmployeeId(employeeId);
        m.setUsername(username.trim());
        m.setPasswordHash(Argon2Hasher.hash(rawPassword));
        m.setManagementLevel(managementLevel);
        m.setAllowance(allowance != null ? allowance : BigDecimal.ZERO);

        if (!managerRepo.insert(m))
            throw new RuntimeException("Tạo manager thất bại");
        return m;
    }

    @Override
    public Manager updateManager(int id, String username, String managementLevel, BigDecimal allowance)
            throws SQLException {

        Manager m = managerRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy manager id=" + id));

        Optional<Manager> sameUsername = managerRepo.findByUsername(username.trim());
        if (sameUsername.isPresent() && sameUsername.get().getId() != id)
            throw new IllegalStateException("Username đã tồn tại: " + username);

        m.setUsername(username.trim());
        m.setManagementLevel(managementLevel);
        m.setAllowance(allowance);

        if (!managerRepo.update(m))
            throw new RuntimeException("Cập nhật manager thất bại");
        return m;
    }

    @Override
    public void updateAllowance(int id, BigDecimal allowance) throws SQLException {
        if (allowance == null || allowance.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Allowance không hợp lệ");
        managerRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy manager id=" + id));
        if (!managerRepo.updateAllowance(id, allowance))
            throw new RuntimeException("Cập nhật allowance thất bại");
    }

    @Override
    public void changePassword(int id, String oldRawPassword, String newRawPassword) throws SQLException {
        if (newRawPassword == null || newRawPassword.length() < 6)
            throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 6 ký tự");

        Manager m = managerRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy manager id=" + id));

        if (!Argon2Hasher.verify(m.getPasswordHash(), oldRawPassword))
            throw new IllegalArgumentException("Mật khẩu cũ không đúng");

        String newHash = Argon2Hasher.hash(newRawPassword);
        if (!managerRepo.updatePassword(id, newHash))
            throw new RuntimeException("Đổi mật khẩu thất bại");
    }

    @Override
    public void deleteManager(int id) throws SQLException {
        if (!managerRepo.delete(id))
            throw new IllegalArgumentException("Không tìm thấy manager hoặc đã bị xóa: id=" + id);
    }
}

