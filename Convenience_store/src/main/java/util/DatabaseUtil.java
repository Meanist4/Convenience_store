package util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseUtil {
    private static HikariDataSource dataSource;
    private static final ThreadLocal<Connection> threadLocalConnection = new ThreadLocal<>();

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(
                "jdbc:mysql://localhost:3306/store_management?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
        config.setUsername("root");
        config.setPassword("");

        // Khống chế luồng tối ưu
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(2000);

        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        // Nếu trong luồng này đã có kết nối đang mở Transaction -> Trả về kết nối đó
        // luôn để xài chung
        Connection conn = threadLocalConnection.get();
        if (conn == null || conn.isClosed()) {
            conn = dataSource.getConnection(); // Nếu chưa có thì mượn từ Pool ra
            threadLocalConnection.set(conn);
        }
        return conn;
    }

    // Hàm quản lý Transaction dành riêng cho tầng Service gọi
    public static void startTransaction() throws SQLException {
        Connection conn = getConnection();
        conn.setAutoCommit(false);
    }

    public static void commitTransaction() throws SQLException {
        Connection conn = threadLocalConnection.get();
        if (conn != null && !conn.isClosed()) {
            conn.commit(); // Chốt hạ lưu dữ liệu thành công
        }
    }

    public static void rollbackTransaction() {
        try {
            Connection conn = threadLocalConnection.get();
            if (conn != null && !conn.isClosed()) {
                conn.rollback(); 
            }
        } catch (SQLException ignored) {
        }
    }

    public static void closeConnection() {
        try {
            Connection conn = threadLocalConnection.get();
            if (conn != null) {
                conn.close(); 
            }
        } catch (SQLException ignored) {
        } finally {
            threadLocalConnection.remove(); // Xóa vết luồng để tránh rò rỉ RAM
        }
    }
}
