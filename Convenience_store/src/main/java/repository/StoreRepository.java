package repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import convenience_store.DBConnection;
import entity.Store;

public class StoreRepository {

    private Store map(ResultSet rs) throws SQLException {
        Store s = new Store();
        s.setId(rs.getInt("id"));
        s.setStoreName(rs.getString("store_name"));
        s.setLocation(rs.getString("location"));
        int managerId = rs.getInt("manager_id");
        s.setManagerId(rs.wasNull() ? null : managerId);
        s.setStatus(rs.getString("status"));
        s.setCreatedAt(rs.getTimestamp("created_at"));
        s.setDeleted(rs.getBoolean("is_deleted"));
        s.setDeletedAt(rs.getTimestamp("deleted_at"));
        return s;
    }

    public List<Store> findAll() throws SQLException {
        List<Store> list = new ArrayList<>();
        String sql = "SELECT * FROM stores WHERE is_deleted = 0 ORDER BY store_name";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(map(rs));
        }
        return list;
    }

    public List<Store> findAllActive() throws SQLException {
        List<Store> list = new ArrayList<>();
        String sql = "SELECT * FROM stores WHERE status = 'active' AND is_deleted = 0";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(map(rs));
        }
        return list;
    }

    public Optional<Store> findById(int id) throws SQLException {
        String sql = "SELECT * FROM stores WHERE id = ? AND is_deleted = 0";
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

    public Optional<Store> findByManagerId(int managerId) throws SQLException {
        String sql = "SELECT * FROM stores WHERE manager_id = ? AND is_deleted = 0";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, managerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    public List<Store> searchByName(String keyword) throws SQLException {
        List<Store> list = new ArrayList<>();
        String sql = "SELECT * FROM stores WHERE store_name LIKE ? AND is_deleted = 0";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(map(rs));
            }
        }
        return list;
    }

    public boolean insert(Store s) throws SQLException {
        String sql = "INSERT INTO stores (store_name, location, manager_id, status) VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getStoreName());
            ps.setString(2, s.getLocation());
            if (s.getManagerId() == null)
                ps.setNull(3, Types.INTEGER);
            else
                ps.setInt(3, s.getManagerId());
            ps.setString(4, s.getStatus() != null ? s.getStatus() : "active");
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next())
                        s.setId(keys.getInt(1));
                }
                return true;
            }
        }
        return false;
    }

    public boolean update(Store s) throws SQLException {
        String sql = "UPDATE stores SET store_name=?, location=?, manager_id=?, status=? WHERE id=? AND is_deleted=0";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, s.getStoreName());
            ps.setString(2, s.getLocation());
            if (s.getManagerId() == null)
                ps.setNull(3, Types.INTEGER);
            else
                ps.setInt(3, s.getManagerId());
            ps.setString(4, s.getStatus());
            ps.setInt(5, s.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean assignManager(int storeId, Integer managerId) throws SQLException {
        String sql = "UPDATE stores SET manager_id = ? WHERE id = ? AND is_deleted = 0";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            if (managerId == null)
                ps.setNull(1, Types.INTEGER);
            else
                ps.setInt(1, managerId);
            ps.setInt(2, storeId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int id, String status) throws SQLException {
        String sql = "UPDATE stores SET status = ? WHERE id = ? AND is_deleted = 0";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "UPDATE stores SET is_deleted = 1, deleted_at = NOW() WHERE id = ? AND is_deleted = 0";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
