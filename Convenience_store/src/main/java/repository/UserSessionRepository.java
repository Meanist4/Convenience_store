package repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import convenience_store.DBConnection;
import entity.UserSession;

public class UserSessionRepository {

    private UserSession map(ResultSet rs) throws SQLException {
        UserSession session = new UserSession();
        session.setId(rs.getInt("id"));
        session.setUserId(rs.getInt("user_id"));
        session.setUserType(rs.getString("user_type"));
        session.setRefreshToken(rs.getString("refresh_token"));
        session.setIpAddress(rs.getString("ip_address"));
        session.setUserAgent(rs.getString("user_agent"));
        session.setExpiresAt(rs.getTimestamp("expires_at"));
        session.setCreatedAt(rs.getTimestamp("created_at"));
        return session;
    }

    public List<UserSession> findByUserId(int userId) throws SQLException {
        List<UserSession> list = new ArrayList<>();
        String sql = "SELECT * FROM user_sessions WHERE user_id = ? AND expires_at > NOW() ORDER BY created_at DESC";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(map(rs));
            }
        }
        return list;
    }

    public Optional<UserSession> findByRefreshToken(String refreshToken) throws SQLException {
        String sql = "SELECT * FROM user_sessions WHERE refresh_token = ? AND expires_at > NOW()";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, refreshToken);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    public Optional<UserSession> findById(int id) throws SQLException {
        String sql = "SELECT * FROM user_sessions WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    public boolean insert(UserSession session) throws SQLException {
        String sql = "INSERT INTO user_sessions " +
                "(user_id, user_type, refresh_token, ip_address, user_agent, expires_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, session.getUserId());
            ps.setString(2, session.getUserType());
            ps.setString(3, session.getRefreshToken());
            ps.setString(4, session.getIpAddress());
            ps.setString(5, session.getUserAgent());
            ps.setTimestamp(6, session.getExpiresAt());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next())
                        session.setId(keys.getInt(1));
                }
                return true;
            }
        }
        return false;
    }

    public boolean revokeSession(int id) throws SQLException {
        String sql = "UPDATE user_sessions SET expires_at = NOW() WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean revokeUserSessions(int userId) throws SQLException {
        String sql = "UPDATE user_sessions SET expires_at = NOW() WHERE user_id = ?";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
            return true;
        }
    }

    public int cleanupExpiredSessions() throws SQLException {
        String sql = "DELETE FROM user_sessions WHERE expires_at < NOW()";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            return ps.executeUpdate();
        }
    }

    public boolean validateTokenStillValid(String refreshToken) throws SQLException {
        return findByRefreshToken(refreshToken).isPresent();
    }
}
