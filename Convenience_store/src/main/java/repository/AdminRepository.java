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
import entity.Admin;

public class AdminRepository {

    private Admin map(ResultSet rs) throws SQLException {
        Admin a = new Admin();
        a.setId(rs.getInt("id"));
        a.setUsername(rs.getString("username"));
        a.setPasswordHash(rs.getString("password_hash"));
        a.setFullName(rs.getString("full_name"));
        a.setRole(rs.getString("role"));
        a.setCreatedAt(rs.getTimestamp("created_at"));
        a.setDeleted(rs.getBoolean("is_deleted"));
        a.setDeletedAt(rs.getTimestamp("deleted_at"));
        return a;
    }

    public List<Admin> findAll() throws SQLException {
        List<Admin> list = new ArrayList<>();
        String sql = "SELECT * FROM admins WHERE is_deleted = 0";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(map(rs));
        }
        return list;
    }

    public Optional<Admin> findById(int id) throws SQLException {
        String sql = "SELECT * FROM admins WHERE id = ? AND is_deleted = 0";
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

    public Optional<Admin> findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM admins WHERE username = ? AND is_deleted = 0";
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

    public boolean insert(Admin a) throws SQLException {
        String sql = "INSERT INTO admins (username, password_hash, full_name, role) VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, a.getUsername());
            ps.setString(2, a.getPasswordHash());
            ps.setString(3, a.getFullName());
            ps.setString(4, a.getRole());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next())
                        a.setId(keys.getInt(1));
                }
                return true;
            }
        }
        return false;
    }

    public boolean update(Admin a) throws SQLException {
        String sql = "UPDATE admins SET username = ?, password_hash = ?, full_name = ?, role = ? WHERE id = ? AND is_deleted = 0";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, a.getUsername());
            ps.setString(2, a.getPasswordHash());
            ps.setString(3, a.getFullName());
            ps.setString(4, a.getRole());
            ps.setInt(5, a.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "UPDATE admins SET is_deleted = 1, deleted_at = NOW() WHERE id = ? AND is_deleted = 0";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
