package repository;

import entity.Notification;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationRepository {
    private boolean shouldClose(Connection conn) throws SQLException {
        return conn != null && !conn.isClosed() && conn.getAutoCommit();
    }

    public void insert(Notification n) throws SQLException {
        Connection conn = util.DatabaseUtil.getConnection();
        boolean closeConnection = shouldClose(conn);
        try {
            insert(n, conn);
        } finally {
            if (closeConnection) {
                conn.close();
            }
        }
    }

    public void insert(Notification n, Connection conn) throws SQLException {
        String sql = "INSERT INTO notifications (store_id, title, content, type) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (n.getStoreId() != null)
                ps.setInt(1, n.getStoreId());
            else
                ps.setNull(1, Types.INTEGER);
            ps.setString(2, n.getTitle());
            ps.setString(3, n.getContent());
            ps.setString(4, n.getType());
            ps.executeUpdate();
        }
    }

    public List<Notification> findUnreadByStore(int storeId) throws SQLException {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE (store_id = ? OR store_id IS NULL) AND is_read = 0 ORDER BY created_at DESC";
        try (Connection conn = util.DatabaseUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, storeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Notification n = new Notification();
                n.setId(rs.getInt("id"));
                n.setTitle(rs.getString("title"));
                n.setContent(rs.getString("content"));
                n.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(n);
            }
        }
        return list;
    }
}