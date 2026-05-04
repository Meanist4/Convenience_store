package service;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import entity.UserSession;
import repository.UserSessionRepository;

public class UserSessionService {

    private final UserSessionRepository sessionRepo = new UserSessionRepository();
    private static final long TOKEN_EXPIRY_MILLIS = 7 * 24 * 60 * 60 * 1000; // 7 days

    public UserSession createSession(int userId, String userType, String ipAddress, String userAgent)
            throws SQLException {
        // Generate refresh token
        String refreshToken = UUID.randomUUID().toString() + "-" + System.currentTimeMillis();

        Timestamp expiresAt = new Timestamp(System.currentTimeMillis() + TOKEN_EXPIRY_MILLIS);
        Timestamp createdAt = new Timestamp(System.currentTimeMillis());

        UserSession session = new UserSession();
        session.setUserId(userId);
        session.setUserType(userType);
        session.setRefreshToken(refreshToken);
        session.setIpAddress(ipAddress);
        session.setUserAgent(userAgent);
        session.setExpiresAt(expiresAt);
        session.setCreatedAt(createdAt);

        if (!sessionRepo.insert(session))
            throw new RuntimeException("Tạo session thất bại");

        return session;
    }

    public Optional<UserSession> validateRefreshToken(String refreshToken) throws SQLException {
        Optional<UserSession> sessionOpt = sessionRepo.findByRefreshToken(refreshToken);

        if (sessionOpt.isPresent()) {
            UserSession session = sessionOpt.get();
            if (session.getExpiresAt().after(new Timestamp(System.currentTimeMillis()))) {
                return Optional.of(session);
            }
        }

        return Optional.empty();
    }

    public List<UserSession> getActiveSessions(int userId) throws SQLException {
        return sessionRepo.findByUserId(userId);
    }

    public boolean revokeSession(int sessionId) throws SQLException {
        return sessionRepo.revokeSession(sessionId);
    }

    public boolean revokeAllUserSessions(int userId) throws SQLException {
        return sessionRepo.revokeUserSessions(userId);
    }

    public void cleanupExpiredSessions() throws SQLException {
        sessionRepo.cleanupExpiredSessions();
    }

    public UserSession extendSessionExpiry(int sessionId) throws SQLException {
        Optional<UserSession> sessionOpt = sessionRepo.findById(sessionId);

        if (!sessionOpt.isPresent())
            throw new IllegalArgumentException("Không tìm thấy session id=" + sessionId);

        UserSession session = sessionOpt.get();

        // Extend expiry by TOKEN_EXPIRY_MILLIS
        session.setExpiresAt(new Timestamp(System.currentTimeMillis() + TOKEN_EXPIRY_MILLIS));

        // Update in repository (need to implement update method or just validate)
        // For now, we just return the updated session object
        return session;
    }

    public boolean isSessionValid(int sessionId) throws SQLException {
        Optional<UserSession> sessionOpt = sessionRepo.findById(sessionId);

        if (!sessionOpt.isPresent())
            return false;

        UserSession session = sessionOpt.get();
        return session.getExpiresAt().after(new Timestamp(System.currentTimeMillis()));
    }
}
