package repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import entity.Admin;
import entity.Manager;

public class AuthRepository {

    public Optional<Admin> findAdminById(int id) throws SQLException {
        String sql = "SELECT * FROM admins WHERE id = ? AND is_deleted = 0";
        try (Connection con = util.DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(mapAdmin(rs));
            }
        }
        return Optional.empty();
    }

    public Optional<Manager> findManagerById(int id) throws SQLException {
        String sql = "SELECT * FROM managers WHERE id = ? AND is_deleted = 0";
        try (Connection con = util.DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(mapManager(rs));
            }
        }
        return Optional.empty();
    }

    public Optional<Admin> findAdminByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM admins WHERE username = ? AND is_deleted = 0";
        try (Connection con = util.DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(mapAdmin(rs));
            }
        }
        return Optional.empty();
    }

    public Optional<Manager> findManagerByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM managers WHERE username = ? AND is_deleted = 0";
        try (Connection con = util.DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(mapManager(rs));
            }
        }
        return Optional.empty();
    }

    public boolean assignManagerToStore(int employeeId, String username, String passwordHash,
            String managementLevel, java.math.BigDecimal allowance,
            int storeId) throws SQLException {

        Connection con = util.DatabaseUtil.getConnection();
        try {
            con.setAutoCommit(false); // Bắt đầu giao dịch

            int managerId;
            String sqlManager = "INSERT INTO managers (employee_id, username, password_hash, management_level, allowance) VALUES (?,?,?,?,?)";
            try (PreparedStatement ps = con.prepareStatement(sqlManager, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, employeeId);
                ps.setString(2, username);
                ps.setString(3, passwordHash);
                ps.setString(4, managementLevel);
                ps.setBigDecimal(5, allowance);
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next())
                        throw new SQLException("Không lấy được manager ID");
                    managerId = keys.getInt(1);
                }
            }

            String sqlStore = "UPDATE stores SET manager_id = ? WHERE id = ? AND is_deleted = 0";
            try (PreparedStatement ps = con.prepareStatement(sqlStore)) {
                ps.setInt(1, managerId);
                ps.setInt(2, storeId);
                if (ps.executeUpdate() == 0)
                    throw new SQLException("Store không tồn tại hoặc đã bị xóa: id=" + storeId);
            }

            con.commit();
            return true;

        } catch (SQLException e) {
            if (con != null)
                con.rollback();
            throw e;
        } finally {
            if (con != null)
                con.setAutoCommit(true);
        }
    }

    private Admin mapAdmin(ResultSet rs) throws SQLException {
        Admin a = new Admin();
        a.setId(rs.getInt("id"));
        a.setUsername(rs.getString("username"));
        a.setPasswordHash(rs.getString("password_hash")); // Đây là chuỗi Argon2 hash
        a.setFullName(rs.getString("full_name"));
        a.setRole(rs.getString("role"));
        a.setCreatedAt(rs.getTimestamp("created_at"));
        a.setDeleted(rs.getBoolean("is_deleted"));
        a.setDeletedAt(rs.getTimestamp("deleted_at"));
        return a;
    }

    private Manager mapManager(ResultSet rs) throws SQLException {
        Manager m = new Manager();
        m.setId(rs.getInt("id"));
        m.setEmployeeId(rs.getInt("employee_id"));
        m.setUsername(rs.getString("username"));
        m.setPasswordHash(rs.getString("password_hash")); // Đây là chuỗi Argon2 hash
        m.setManagementLevel(rs.getString("management_level"));
        m.setAllowance(rs.getBigDecimal("allowance"));
        m.setDeleted(rs.getBoolean("is_deleted"));
        m.setDeletedAt(rs.getTimestamp("deleted_at"));
        return m;
    }
}