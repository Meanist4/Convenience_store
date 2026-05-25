package repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import util.ShortHash;
import entity.Employee;

public class EmployeeRepository {

    private Employee map(ResultSet rs) throws SQLException {
        Employee e = new Employee();
        e.setId(rs.getInt("id"));
        e.setFullName(rs.getString("full_name"));
        e.setBirthday(rs.getDate("birthday"));
        e.setGender(rs.getString("gender"));
        e.setIdCard(rs.getString("id_card"));
        e.setEmployeeBarcode(rs.getString("employee_barcode"));
        e.setPhone(rs.getString("phone"));
        e.setEmail(rs.getString("email"));
        e.setAddress(rs.getString("address"));
        int storeId = rs.getInt("store_id");
        e.setStoreId(rs.wasNull() ? null : storeId);
        e.setHourlyRate(rs.getBigDecimal("hourly_rate"));
        e.setStatus(rs.getString("status"));
        e.setCreatedAt(rs.getTimestamp("created_at"));
        e.setUpdatedAt(rs.getTimestamp("updated_at"));
        e.setDeleted(rs.getBoolean("is_deleted"));
        e.setDeletedAt(rs.getTimestamp("deleted_at"));
        return e;
    }

    public List<Employee> findAll() throws SQLException {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT * FROM employees WHERE is_deleted = 0";
        try (Connection con = util.DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public Optional<Employee> findById(int id) throws SQLException {
        String sql = "SELECT * FROM employees WHERE id = ? AND is_deleted = 0";
        try (Connection con = util.DatabaseUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Employee> findByIdCard(String idCard) throws SQLException {
        String sql = "SELECT * FROM employees WHERE id_card = ? AND is_deleted = 0";
        try (Connection con = util.DatabaseUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, idCard);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Employee> findByBarcode(String barcode) throws SQLException {
        String sql = "SELECT * FROM employees WHERE employee_barcode = ? AND is_deleted = 0";
        try (Connection con = util.DatabaseUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, barcode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Employee> findByPhone(String phone) throws SQLException {
        String sql = "SELECT * FROM employees WHERE phone = ? AND is_deleted = 0";
        try (Connection con = util.DatabaseUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Integer> findIdByBarcode(String barcode) throws SQLException {
        String sql = "SELECT id FROM employees WHERE employee_barcode = ? AND is_deleted = 0";
        try (Connection conn = util.DatabaseUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, barcode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getInt("id"));
                }
            }
        }
        return Optional.empty();
    }

    public List<Employee> findByStoreId(int storeId) throws SQLException {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT * FROM employees WHERE store_id = ? AND is_deleted = 0 ORDER BY full_name";
        try (Connection con = util.DatabaseUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, storeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public List<Employee> findByStatus(String status) throws SQLException {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT * FROM employees WHERE status = ? AND is_deleted = 0";
        try (Connection con = util.DatabaseUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public List<Employee> searchByName(String keyword) throws SQLException {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT * FROM employees WHERE full_name LIKE ? AND is_deleted = 0";
        try (Connection con = util.DatabaseUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public boolean insert(Employee e) throws SQLException, Exception {
        String sql = "INSERT INTO employees (full_name, birthday, gender, id_card, employee_barcode, "
                + "phone, email, address, store_id, hourly_rate, status) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection con = util.DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getFullName());
            ps.setDate(2, e.getBirthday());
            ps.setString(3, e.getGender());
            ps.setString(4, e.getIdCard());
            ps.setString(5, ShortHash.EmployeeBarcodeHash(e.getIdCard()));
            ps.setString(6, e.getPhone());
            ps.setString(7, e.getEmail());
            ps.setString(8, e.getAddress());
            if (e.getStoreId() == null) {
                ps.setNull(9, Types.INTEGER);
            } else {
                ps.setInt(9, e.getStoreId());
            }
            ps.setBigDecimal(10, e.getHourlyRate());
            ps.setString(11, e.getStatus());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        e.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean update(Employee e) throws SQLException, Exception {
        String sql = "UPDATE employees SET full_name=?, birthday=?, gender=?, id_card=?, employee_barcode=?, "
                + "phone=?, email=?, address=?, store_id=?, hourly_rate=?, status=? WHERE id=? AND is_deleted=0";
        try (Connection con = util.DatabaseUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, e.getFullName());
            ps.setDate(2, e.getBirthday());
            ps.setString(3, e.getGender());
            ps.setString(4, e.getIdCard());
            // ps.setString(5, e.getEmployeeBarcode());
            ps.setString(5, ShortHash.EmployeeBarcodeHash(e.getIdCard()));
            ps.setString(6, e.getPhone());
            ps.setString(7, e.getEmail());
            ps.setString(8, e.getAddress());
            if (e.getStoreId() == null) {
                ps.setNull(9, Types.INTEGER);
            } else {
                ps.setInt(9, e.getStoreId());
            }
            ps.setBigDecimal(10, e.getHourlyRate());
            ps.setString(11, e.getStatus());
            ps.setInt(12, e.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateHourlyRate(int id, BigDecimal newRate) throws SQLException {
        String sql = "UPDATE employees SET hourly_rate = ? WHERE id = ? AND is_deleted = 0";
        try (Connection con = util.DatabaseUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBigDecimal(1, newRate);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int id, String status) throws SQLException {
        String sql = "UPDATE employees SET status = ? WHERE id = ? AND is_deleted = 0";
        try (Connection con = util.DatabaseUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "UPDATE employees SET is_deleted = 1, deleted_at = NOW() WHERE id = ? AND is_deleted = 0";
        try (Connection con = util.DatabaseUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
