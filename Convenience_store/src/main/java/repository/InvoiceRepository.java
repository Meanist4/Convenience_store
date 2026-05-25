package repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import entity.Invoice;

public class InvoiceRepository {

    private Invoice map(ResultSet rs) throws SQLException {
        Invoice inv = new Invoice();
        inv.setId(rs.getInt("id"));
        inv.setStoreId(rs.getInt("store_id"));
        inv.setEmployeeId(rs.getInt("employee_id"));
        inv.setTotalAmount(rs.getBigDecimal("total_amount"));
        inv.setStatus(rs.getString("status"));
        inv.setCreatedAt(rs.getTimestamp("created_at"));
        return inv;
    }

    private boolean shouldClose(Connection conn) throws SQLException {
        return conn != null && !conn.isClosed() && conn.getAutoCommit();
    }

    public List<Invoice> findAll() throws SQLException {
        List<Invoice> list = new ArrayList<>();
        String sql = "SELECT * FROM invoices WHERE status = 'completed' ORDER BY created_at DESC";
        try (Connection con = util.DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(map(rs));
        }
        return list;
    }

    public List<Invoice> findAllCancelled() throws SQLException {
        List<Invoice> list = new ArrayList<>();
        String sql = "SELECT * FROM invoices WHERE status = 'cancelled' ORDER BY created_at DESC";
        try (Connection con = util.DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(map(rs));
        }
        return list;
    }

    public Optional<Invoice> findById(int id) throws SQLException {
        String sql = "SELECT * FROM invoices WHERE id = ?";
        try (Connection con = util.DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    public List<Invoice> findByStoreId(int storeId) throws SQLException {
        List<Invoice> list = new ArrayList<>();
        String sql = "SELECT * FROM invoices WHERE store_id = ? AND status = 'completed' ORDER BY created_at DESC";
        try (Connection con = util.DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, storeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(map(rs));
            }
        }
        return list;
    }

    public List<Invoice> findByEmployeeId(int employeeId) throws SQLException {
        List<Invoice> list = new ArrayList<>();
        String sql = "SELECT * FROM invoices WHERE employee_id = ? AND status = 'completed' ORDER BY created_at DESC";
        try (Connection con = util.DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(map(rs));
            }
        }
        return list;
    }

    public List<Invoice> findByDateRange(Timestamp from, Timestamp to) throws SQLException {
        List<Invoice> list = new ArrayList<>();
        String sql = "SELECT * FROM invoices WHERE created_at BETWEEN ? AND ? ORDER BY created_at DESC";
        try (Connection con = util.DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, from);
            ps.setTimestamp(2, to);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(map(rs));
            }
        }
        return list;
    }

    public BigDecimal sumRevenueByStore(int storeId, Timestamp from, Timestamp to) throws SQLException {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM invoices " +
                "WHERE store_id = ? AND status = 'completed' AND created_at BETWEEN ? AND ?";
        try (Connection con = util.DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, storeId);
            ps.setTimestamp(2, from);
            ps.setTimestamp(3, to);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getBigDecimal(1);
            }
        }
        return BigDecimal.ZERO;
    }

    public boolean insert(Invoice inv) throws SQLException {
        Connection con = util.DatabaseUtil.getConnection();
        boolean closeConnection = shouldClose(con);
        try {
            return insert(inv, con);
        } finally {
            if (closeConnection) {
                con.close();
            }
        }
    }

    public boolean insert(Invoice inv, Connection con) throws SQLException {
        String sql = "INSERT INTO invoices (store_id, employee_id, total_amount, status) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, inv.getStoreId());
            ps.setInt(2, inv.getEmployeeId());
            ps.setBigDecimal(3, inv.getTotalAmount());
            ps.setString(4, inv.getStatus() != null ? inv.getStatus() : "completed");
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next())
                        inv.setId(keys.getInt(1));
                }
                return true;
            }
        }
        return false;
    }

    public boolean updateTotalAmount(int id, BigDecimal totalAmount) throws SQLException {
        String sql = "UPDATE invoices SET total_amount = ? WHERE id = ?";
        try (Connection con = util.DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBigDecimal(1, totalAmount);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean cancelInvoice(int id) throws SQLException {
        String sql = "UPDATE invoices SET status = 'cancelled' WHERE id = ? AND status = 'completed'";
        try (Connection con = util.DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
