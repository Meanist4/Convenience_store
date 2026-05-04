package service;

import entity.Admin;
import entity.Manager;
import entity.UserSession;
import repository.AuthRepository;
import repository.UserSessionRepository;
import convenience_store.Argon2Hasher; // Import lớp tiện ích của bạn

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

public class AuthService {

    private final AuthRepository authRepo = new AuthRepository();
    private final UserSessionRepository sessionRepo = new UserSessionRepository();
    private final UserSessionService sessionService = new UserSessionService();

    public class LoginResponse {
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

    public LoginResponse loginAdmin(String username, String rawPassword, String ipAddress, String userAgent)
            throws SQLException {
        if (isInputInvalid(username, rawPassword)) {
            return null;
        }

        Optional<Admin> opt = authRepo.findAdminByUsername(username.trim());

        if (opt.isPresent() && Argon2Hasher.verify(opt.get().getPasswordHash(), rawPassword)) {
            Admin admin = opt.get();

            // Tạo session mới
            UserSession session = sessionService.createSession(admin.getId(), "admin", ipAddress, userAgent);

            return new LoginResponse(admin, session.getRefreshToken(), session.getExpiresAt().getTime(), "admin");
        }
        return null;
    }

    public LoginResponse loginManager(String username, String rawPassword, String ipAddress, String userAgent)
            throws SQLException {
        if (isInputInvalid(username, rawPassword)) {
            return null;
        }

        Optional<Manager> opt = authRepo.findManagerByUsername(username.trim());

        if (opt.isPresent() && Argon2Hasher.verify(opt.get().getPasswordHash(), rawPassword)) {
            Manager manager = opt.get();

            // Tạo session mới
            UserSession session = sessionService.createSession(manager.getId(), "manager", ipAddress, userAgent);

            return new LoginResponse(manager, session.getRefreshToken(), session.getExpiresAt().getTime(), "manager");
        }
        return null;
    }

    public Optional<Admin> validateAdminByRefreshToken(String refreshToken) throws SQLException {
        Optional<UserSession> sessionOpt = sessionService.validateRefreshToken(refreshToken);

        if (sessionOpt.isPresent() && "admin".equals(sessionOpt.get().getUserType())) {
            return authRepo.findAdminById(sessionOpt.get().getUserId());
        }

        return Optional.empty();
    }

    public Optional<Manager> validateManagerByRefreshToken(String refreshToken) throws SQLException {
        Optional<UserSession> sessionOpt = sessionService.validateRefreshToken(refreshToken);

        if (sessionOpt.isPresent() && "manager".equals(sessionOpt.get().getUserType())) {
            return authRepo.findManagerById(sessionOpt.get().getUserId());
        }

        return Optional.empty();
    }

    public void logout(int userId, String userType) throws SQLException {
        sessionService.revokeAllUserSessions(userId);
    }

    public void logoutSession(int sessionId) throws SQLException {
        sessionService.revokeSession(sessionId);
    }

    public void assignManagerToStore(int employeeId, String username, String rawPassword,
            String managementLevel, BigDecimal allowance, int storeId) throws SQLException {

        if (username == null || username.isBlank())
            throw new IllegalArgumentException("Username không được để trống");

        if (rawPassword == null || rawPassword.length() < 6)
            throw new IllegalArgumentException("Password phải có ít nhất 6 ký tự");

        if (allowance == null || allowance.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Allowance không hợp lệ");

        String hash = Argon2Hasher.hash(rawPassword);

        authRepo.assignManagerToStore(employeeId, username.trim(), hash, managementLevel, allowance, storeId);
    }

    private boolean isInputInvalid(String user, String pass) {
        return user == null || user.isBlank() || pass == null || pass.isBlank();
    }
}