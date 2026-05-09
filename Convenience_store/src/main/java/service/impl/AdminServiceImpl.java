package service.impl;

import service.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import entity.Admin;
import repository.AdminRepository;
import util.Argon2Hasher;

public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepo = new AdminRepository();

    @Override
    public List<Admin> getAllAdmins() throws SQLException {
        return adminRepo.findAll();
    }

    @Override
    public Optional<Admin> getAdminById(int id) throws SQLException {
        return adminRepo.findById(id);
    }

    @Override
    public Optional<Admin> getAdminByUsername(String username) throws SQLException {
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("Username không được để trống");
        return adminRepo.findByUsername(username.trim());
    }

    @Override
    public Admin createAdmin(String username, String rawPassword, String fullName, String role) throws SQLException {
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("Username không được để trống");
        if (rawPassword == null || rawPassword.length() < 6)
            throw new IllegalArgumentException("Password phải có ít nhất 6 ký tự");

        if (adminRepo.findByUsername(username.trim()).isPresent())
            throw new IllegalStateException("Username đã tồn tại: " + username);

        Admin a = new Admin();
        a.setUsername(username.trim());
        a.setPasswordHash(Argon2Hasher.hash(rawPassword));
        a.setFullName(fullName);
        a.setRole(role != null ? role : "system_admin");

        if (!adminRepo.insert(a))
            throw new RuntimeException("Tạo admin thất bại");
        return a;
    }

    @Override
    public Admin updateAdmin(int id, String username, String fullName, String role) throws SQLException {
        Admin a = adminRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy admin id=" + id));

        Optional<Admin> existing = adminRepo.findByUsername(username.trim());
        if (existing.isPresent() && existing.get().getId() != id)
            throw new IllegalStateException("Username đã tồn tại: " + username);

        a.setUsername(username.trim());
        a.setFullName(fullName);
        a.setRole(role);

        if (!adminRepo.update(a))
            throw new RuntimeException("Cập nhật admin thất bại");
        return a;
    }

    @Override
    public void changePassword(int id, String oldRawPassword, String newRawPassword) throws SQLException {
        if (newRawPassword == null || newRawPassword.length() < 6)
            throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 6 ký tự");

        Admin a = adminRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy admin id=" + id));

        if (!Argon2Hasher.verify(a.getPasswordHash(), oldRawPassword))
            throw new IllegalArgumentException("Mật khẩu cũ không đúng");

        a.setPasswordHash(Argon2Hasher.hash(newRawPassword));

        if (!adminRepo.update(a))
            throw new RuntimeException("Đổi mật khẩu thất bại");
    }

    @Override
    public void deleteAdmin(int id) throws SQLException {
        if (!adminRepo.delete(id))
            throw new IllegalArgumentException("Không tìm thấy admin hoặc đã bị xóa: id=" + id);
    }
}

