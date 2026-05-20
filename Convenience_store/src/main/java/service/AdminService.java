package service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import entity.Admin;

public interface AdminService {
    List<Admin> getAllAdmins() throws SQLException;

    Optional<Admin> getAdminById(int id) throws SQLException;

    Optional<Admin> getAdminByUsername(String username) throws SQLException;

    Admin createAdmin(String username, String rawPassword, String fullName, String role) throws SQLException;

    Admin updateAdmin(int id, String username, String fullName, String role) throws SQLException;

    void changePassword(int id, String oldRawPassword, String newRawPassword) throws SQLException;

    void deleteAdmin(int id) throws SQLException;
}