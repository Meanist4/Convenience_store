package repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import convenience_store.DBConnection;
import entity.Supplier;

public class SupplierRepository {
    public List<Supplier> findAll() throws SQLException {
        List<Supplier> list = new ArrayList<>();
        String sql = "SELECT * FROM suppliers WHERE is_deleted = 0";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(mapResultSetToEntity(rs));
        }
        return list;
    }

    public boolean insert(Supplier s) throws SQLException {
        String sql = "INSERT INTO suppliers (supplier_name, contact_person, phone, email, address) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getSupplierName());
            ps.setString(2, s.getContactPerson());
            ps.setString(3, s.getPhone());
            ps.setString(4, s.getEmail());
            ps.setString(5, s.getAddress());
            return ps.executeUpdate() > 0;
        }
    }

    private Supplier mapResultSetToEntity(ResultSet rs) throws SQLException {
        Supplier s = new Supplier();
        s.setId(rs.getInt("id"));
        s.setSupplierName(rs.getString("supplier_name"));
        s.setContactPerson(rs.getString("contact_person"));
        s.setPhone(rs.getString("phone"));
        s.setEmail(rs.getString("email"));
        s.setAddress(rs.getString("address"));
        s.setDeleted(rs.getBoolean("is_deleted"));
        s.setDeletedAt(rs.getTimestamp("deleted_at"));
        return s;
    }

    public Optional<Supplier> findById(int id) throws SQLException {
        String sql = "SELECT * FROM suppliers WHERE id = ? AND is_deleted = 0";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(mapResultSetToEntity(rs));
            }
        }
        return Optional.empty();
    }

    public Optional<Supplier> findByPhone(String phone) throws SQLException {
        String sql = "SELECT * FROM suppliers WHERE phone = ? AND is_deleted = 0";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(mapResultSetToEntity(rs));
            }
        }
        return Optional.empty();
    }

    public Optional<Supplier> findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM suppliers WHERE email = ? AND is_deleted = 0";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(mapResultSetToEntity(rs));
            }
        }
        return Optional.empty();
    }

    public boolean update(Supplier s) throws SQLException {
        String sql = "UPDATE suppliers SET supplier_name = ?, contact_person = ?, phone = ?, email = ?, address = ? " +
                "WHERE id = ? AND is_deleted = 0";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getSupplierName());
            ps.setString(2, s.getContactPerson());
            ps.setString(3, s.getPhone());
            ps.setString(4, s.getEmail());
            ps.setString(5, s.getAddress());
            ps.setInt(6, s.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "UPDATE suppliers SET is_deleted = 1, deleted_at = NOW() WHERE id = ? AND is_deleted = 0";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}