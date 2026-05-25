package repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import entity.InvoiceDetail;

public class InvoiceDetailRepository {

    private InvoiceDetail map(ResultSet rs) throws SQLException {
        InvoiceDetail d = new InvoiceDetail();
        d.setId(rs.getInt("id"));
        d.setInvoiceId(rs.getInt("invoice_id"));
        d.setProductId(rs.getInt("product_id"));
        d.setUnitId(rs.getInt("unit_id"));
        d.setQuantity(rs.getInt("quantity"));
        d.setPriceAtSale(rs.getBigDecimal("price_at_sale"));
        d.setSubtotal(rs.getBigDecimal("subtotal"));
        return d;
    }

    private boolean shouldClose(Connection conn) throws SQLException {
        return conn != null && !conn.isClosed() && conn.getAutoCommit();
    }

    public List<InvoiceDetail> findByInvoiceId(int invoiceId) throws SQLException {
        List<InvoiceDetail> list = new ArrayList<>();
        String sql = "SELECT * FROM invoice_details WHERE invoice_id = ?";
        try (Connection con = util.DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(map(rs));
            }
        }
        return list;
    }

    public Optional<InvoiceDetail> findById(int id) throws SQLException {
        String sql = "SELECT * FROM invoice_details WHERE id = ?";
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

    public List<InvoiceDetail> findByProductId(int productId) throws SQLException {
        List<InvoiceDetail> list = new ArrayList<>();
        String sql = "SELECT id.* FROM invoice_details id " +
                "JOIN invoices i ON i.id = id.invoice_id " +
                "WHERE id.product_id = ? AND i.status = 'completed'";
        try (Connection con = util.DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(map(rs));
            }
        }
        return list;
    }

    public boolean insert(InvoiceDetail d) throws SQLException {
        Connection con = util.DatabaseUtil.getConnection();
        boolean closeConnection = shouldClose(con);
        try {
            return insert(d, con);
        } finally {
            if (closeConnection) {
                con.close();
            }
        }
    }

    public boolean insert(InvoiceDetail d, Connection con) throws SQLException {
        String sql = "INSERT INTO invoice_details (invoice_id, product_id, unit_id, quantity, price_at_sale, subtotal) "
                +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, d.getInvoiceId());
            ps.setInt(2, d.getProductId());
            ps.setInt(3, d.getUnitId());
            ps.setInt(4, d.getQuantity());
            ps.setBigDecimal(5, d.getPriceAtSale());
            ps.setBigDecimal(6, d.getSubtotal());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next())
                        d.setId(keys.getInt(1));
                }
                return true;
            }
        }
        return false;
    }

    public boolean insertBatch(List<InvoiceDetail> details) throws SQLException {
        String sql = "INSERT INTO invoice_details (invoice_id, product_id, unit_id, quantity, price_at_sale, subtotal) "
                +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = util.DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            con.setAutoCommit(false);
            for (InvoiceDetail d : details) {
                ps.setInt(1, d.getInvoiceId());
                ps.setInt(2, d.getProductId());
                ps.setInt(3, d.getUnitId());
                ps.setInt(4, d.getQuantity());
                ps.setBigDecimal(5, d.getPriceAtSale());
                ps.setBigDecimal(6, d.getSubtotal());
                ps.addBatch();
            }
            int[] result = ps.executeBatch();
            con.commit();
            con.setAutoCommit(true);
            for (int r : result)
                if (r <= 0)
                    return false;
            return true;
        }
    }

    public boolean deleteByInvoiceId(int invoiceId) throws SQLException {
        String sql = "DELETE FROM invoice_details WHERE invoice_id = ?";
        try (Connection con = util.DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            return ps.executeUpdate() > 0;
        }
    }
}
