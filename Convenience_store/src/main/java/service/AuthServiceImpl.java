package service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

import entity.Admin;
import entity.Manager;
import entity.UserSession;
import repository.AuthRepository;
import util.Argon2Hasher;

public class AuthServiceImpl implements AuthService {

    private final AuthRepository authRepo = new AuthRepository();
    private final UserSessionService sessionService = new UserSessionServiceImpl();

    @Override
    public AuthService.LoginResponse loginAdmin(String username, String rawPassword, String ipAddress, String userAgent)
            throws SQLException {
        if (isInputInvalid(username, rawPassword)) {
            return null;
        }

        Optional<Admin> opt = authRepo.findAdminByUsername(username.trim());

        if (opt.isPresent() && Argon2Hasher.verify(opt.get().getPasswordHash(), rawPassword)) {
            Admin admin = opt.get();
            UserSession session = sessionService.createSession(admin.getId(), "admin", ipAddress, userAgent);
            return new AuthService.LoginResponse(admin, session.getRefreshToken(), session.getExpiresAt().getTime(), "admin");
        }
        return null;
    }

    @Override
    public AuthService.LoginResponse loginManager(String username, String rawPassword, String ipAddress, String userAgent)
            throws SQLException {
        if (isInputInvalid(username, rawPassword)) {
            return null;
        }

        Optional<Manager> opt = authRepo.findManagerByUsername(username.trim());

        if (opt.isPresent() && Argon2Hasher.verify(opt.get().getPasswordHash(), rawPassword)) {
            Manager manager = opt.get();
            UserSession session = sessionService.createSession(manager.getId(), "manager", ipAddress, userAgent);
            return new AuthService.LoginResponse(manager, session.getRefreshToken(), session.getExpiresAt().getTime(), "manager");
        }
        return null;
    }

    @Override
    public Optional<Admin> validateAdminByRefreshToken(String refreshToken) throws SQLException {
        Optional<UserSession> sessionOpt = sessionService.validateRefreshToken(refreshToken);
        if (sessionOpt.isPresent() && "admin".equals(sessionOpt.get().getUserType())) {
            return authRepo.findAdminById(sessionOpt.get().getUserId());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Manager> validateManagerByRefreshToken(String refreshToken) throws SQLException {
        Optional<UserSession> sessionOpt = sessionService.validateRefreshToken(refreshToken);
        if (sessionOpt.isPresent() && "manager".equals(sessionOpt.get().getUserType())) {
            return authRepo.findManagerById(sessionOpt.get().getUserId());
        }
        return Optional.empty();
    }

    @Override
    public void logout(int userId, String userType) throws SQLException {
        sessionService.revokeAllUserSessions(userId);
    }

    @Override
    public void logoutSession(int sessionId) throws SQLException {
        sessionService.revokeSession(sessionId);
    }

    @Override
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
