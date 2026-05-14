package service.impl;

import service.*;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import entity.UserSession;
import repository.UserSessionRepository;

public class UserSessionServiceImpl implements UserSessionService {

    private final UserSessionRepository sessionRepo = new UserSessionRepository();
    private static final long TOKEN_EXPIRY_MILLIS = 7 * 24 * 60 * 60 * 1000;

    @Override
    public UserSession createSession(int userId, String userType, String ipAddress, String userAgent)
            throws SQLException {
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

    @Override
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

    @Override
    public List<UserSession> getActiveSessions(int userId) throws SQLException {
        return sessionRepo.findByUserId(userId);
    }

    @Override
    public boolean revokeSession(int sessionId) throws SQLException {
        return sessionRepo.revokeSession(sessionId);
    }

    @Override
    public boolean revokeAllUserSessions(int userId) throws SQLException {
        return sessionRepo.revokeUserSessions(userId);
    }

    @Override
    public void cleanupExpiredSessions() throws SQLException {
        sessionRepo.cleanupExpiredSessions();
    }

    @Override
    public UserSession extendSessionExpiry(int sessionId) throws SQLException {
        Optional<UserSession> sessionOpt = sessionRepo.findById(sessionId);
        if (sessionOpt.isEmpty())
            throw new IllegalArgumentException("Không tìm thấy session id=" + sessionId);

        UserSession session = sessionOpt.get();
        session.setExpiresAt(new Timestamp(System.currentTimeMillis() + TOKEN_EXPIRY_MILLIS));
        return session;
    }

    @Override
    public boolean isSessionValid(int sessionId) throws SQLException {
        Optional<UserSession> sessionOpt = sessionRepo.findById(sessionId);
        if (sessionOpt.isEmpty())
            return false;

        UserSession session = sessionOpt.get();
        return session.getExpiresAt().after(new Timestamp(System.currentTimeMillis()));
    }
}

