package service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import entity.UserSession;

public interface UserSessionService {
    UserSession createSession(int userId, String userType, String ipAddress, String userAgent) throws SQLException;

    Optional<UserSession> validateRefreshToken(String refreshToken) throws SQLException;

    List<UserSession> getActiveSessions(int userId) throws SQLException;

    boolean revokeSession(int sessionId) throws SQLException;

    boolean revokeAllUserSessions(int userId) throws SQLException;

    void cleanupExpiredSessions() throws SQLException;

    UserSession extendSessionExpiry(int sessionId) throws SQLException;

    boolean isSessionValid(int sessionId) throws SQLException;
}
