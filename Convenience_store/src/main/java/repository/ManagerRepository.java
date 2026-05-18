package repository;

import entity.Manager;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import convenience_store.DBConnection;

public class ManagerRepository {

    private Manager map(ResultSet rs) throws SQLException {
        Manager m = new Manager();
        m.setId(rs.getInt("id"));
        m.setEmployeeId(rs.getInt("employee_id"));
        m.setUsername(rs.getString("username"));
        m.setPasswordHash(rs.getString("password_hash"));
        m.setManagementLevel(rs.getString("management_level"));
        m.setAllowance(rs.getBigDecimal("allowance"));
        m.setDeleted(rs.getBoolean("is_deleted"));
        m.setDeletedAt(rs.getTimestamp("deleted_at"));
        return m;
    }

    public List<Manager> findAll() throws SQLException {
        List<Manager> list = new ArrayList<>();
        String sql = "SELECT * FROM managers WHERE is_deleted = 0";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(map(rs));
        }
        return list;
    }

    public Optional<Manager> findById(int id) throws SQLException {
        String sql = "SELECT * FROM managers WHERE id = ? AND is_deleted = 0";
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

    public Optional<Manager> findByEmployeeId(int employeeId) throws SQLException {
        String sql = "SELECT * FROM managers WHERE employee_id = ? AND is_deleted = 0";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    public Optional<Manager> findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM managers WHERE username = ? AND is_deleted = 0";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    public boolean insert(Manager m) throws SQLException {
        String sql = "INSERT INTO managers (employee_id, username, password_hash, management_level, allowance) VALUES (?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, m.getEmployeeId());
            ps.setString(2, m.getUsername());
            ps.setString(3, m.getPasswordHash());
            ps.setString(4, m.getManagementLevel());
            ps.setBigDecimal(5, m.getAllowance());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next())
                        m.setId(keys.getInt(1));
                }
                return true;
            }
        }
        return false;
    }

    public boolean update(Manager m) throws SQLException {
        String sql = "UPDATE managers SET username=?, password_hash=?, management_level=?, allowance=? WHERE id=? AND is_deleted=0";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, m.getUsername());
            ps.setString(2, m.getPasswordHash());
            ps.setString(3, m.getManagementLevel());
            ps.setBigDecimal(4, m.getAllowance());
            ps.setInt(5, m.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateAllowance(int id, BigDecimal allowance) throws SQLException {
        String sql = "UPDATE managers SET allowance = ? WHERE id = ? AND is_deleted = 0";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBigDecimal(1, allowance);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updatePassword(int id, String newPasswordHash) throws SQLException {
        String sql = "UPDATE managers SET password_hash = ? WHERE id = ? AND is_deleted = 0";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newPasswordHash);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "UPDATE managers SET is_deleted = 1, deleted_at = NOW() WHERE id = ? AND is_deleted = 0";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
    
}
