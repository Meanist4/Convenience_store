package service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

import entity.Admin;
import entity.Manager;

public interface AuthService {
    public static class LoginResponse {
        public Object user; // Admin hoặc Manager
        public String refreshToken;
        public long expiresAt;
        public String userType;

        public LoginResponse(Object user, String refreshToken, long expiresAt, String userType) {
            this.user = user;
            this.refreshToken = refreshToken;
            this.expiresAt = expiresAt;
            this.userType = userType;
        }
    }

    LoginResponse loginAdmin(String username, String rawPassword, String ipAddress, String userAgent)
            throws SQLException;

    LoginResponse loginManager(String username, String rawPassword, String ipAddress, String userAgent)
            throws SQLException;

    Optional<Admin> validateAdminByRefreshToken(String refreshToken) throws SQLException;

    Optional<Manager> validateManagerByRefreshToken(String refreshToken) throws SQLException;

    void logout(int userId, String userType) throws SQLException;

    void logoutSession(int sessionId) throws SQLException;

    void assignManagerToStore(int employeeId, String username, String rawPassword,
            String managementLevel, BigDecimal allowance, int storeId) throws SQLException;
}