package repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import entity.Invoice;
import entity.InvoiceDetail;

public class OrderRepository {

    public boolean createOrder(Invoice invoice, List<InvoiceDetail> details) throws SQLException {
        String sqlInvoice = "INSERT INTO invoices (store_id, employee_id, total_amount, status) VALUES (?, ?, ?, 'completed')";
        String sqlDetail = "INSERT INTO invoice_details (invoice_id, product_id, unit_id, quantity, price_at_sale, subtotal) VALUES (?,?,?,?,?,?)";
        String sqlStock = "UPDATE store_inventory SET quantity = quantity - ? WHERE store_id = ? AND product_id = ? AND quantity >= ?";

        Connection con = util.DatabaseUtil.getConnection();
        try {
            con.setAutoCommit(false);

            int invoiceId;
            try (PreparedStatement ps = con.prepareStatement(sqlInvoice, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, invoice.getStoreId());
                ps.setInt(2, invoice.getEmployeeId());
                ps.setBigDecimal(3, invoice.getTotalAmount());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next())
                        throw new SQLException("Không lấy được invoice ID");
                    invoiceId = keys.getInt(1);
                    invoice.setId(invoiceId);
                }
            }

            try (PreparedStatement psDetail = con.prepareStatement(sqlDetail, Statement.RETURN_GENERATED_KEYS);
                    PreparedStatement psStock = con.prepareStatement(sqlStock)) {

                for (InvoiceDetail d : details) {
                    d.setInvoiceId(invoiceId);

                    psDetail.setInt(1, d.getInvoiceId());
                    psDetail.setInt(2, d.getProductId());
                    psDetail.setInt(3, d.getUnitId());
                    psDetail.setInt(4, d.getQuantity());
                    psDetail.setBigDecimal(5, d.getPriceAtSale());
                    psDetail.setBigDecimal(6, d.getSubtotal());
                    psDetail.addBatch();

                    int baseQty = resolveBaseQuantity(con, d.getUnitId(), d.getQuantity());
                    psStock.setInt(1, baseQty);
                    psStock.setInt(2, invoice.getStoreId());
                    psStock.setInt(3, d.getProductId());
                    psStock.setInt(4, baseQty); // đảm bảo không âm
                    int updated = psStock.executeUpdate();
                    if (updated == 0) {
                        throw new SQLException("Không đủ tồn kho cho product_id=" + d.getProductId());
                    }
                }

                psDetail.executeBatch();
            }

            con.commit();
            return true;

        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(true);
        }
    }

    public boolean cancelOrder(int invoiceId, int storeId) throws SQLException {
        String sqlCancel = "UPDATE invoices SET status = 'cancelled' WHERE id = ? AND status = 'completed'";
        String sqlDetails = "SELECT product_id, unit_id, quantity FROM invoice_details WHERE invoice_id = ?";
        String sqlRestock = "UPDATE store_inventory SET quantity = quantity + ? WHERE store_id = ? AND product_id = ?";

        Connection con = util.DatabaseUtil.getConnection();
        try {
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(sqlCancel)) {
                ps.setInt(1, invoiceId);
                if (ps.executeUpdate() == 0)
                    throw new SQLException("Hóa đơn không tồn tại hoặc đã hủy");
            }

            try (PreparedStatement psDetail = con.prepareStatement(sqlDetails);
                    PreparedStatement psRestock = con.prepareStatement(sqlRestock)) {

                psDetail.setInt(1, invoiceId);
                try (ResultSet rs = psDetail.executeQuery()) {
                    while (rs.next()) {
                        int productId = rs.getInt("product_id");
                        int unitId = rs.getInt("unit_id");
                        int qty = rs.getInt("quantity");
                        int baseQty = resolveBaseQuantity(con, unitId, qty);

                        psRestock.setInt(1, baseQty);
                        psRestock.setInt(2, storeId);
                        psRestock.setInt(3, productId);
                        psRestock.addBatch();
                    }
                }
                psRestock.executeBatch();
            }

            con.commit();
            return true;

        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(true);
        }
    }

    public BigDecimal getMonthlyRevenue(int storeId, int year, int month) throws SQLException {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM invoices " +
                "WHERE store_id = ? AND status = 'completed' AND YEAR(created_at) = ? AND MONTH(created_at) = ?";
        try (Connection con = util.DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, storeId);
            ps.setInt(2, year);
            ps.setInt(3, month);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getBigDecimal(1);
            }
        }
        return BigDecimal.ZERO;
    }

    private int resolveBaseQuantity(Connection con, int unitId, int saleQty) throws SQLException {
        String sql = "SELECT ratio FROM product_units WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, unitId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getInt("ratio") * saleQty;
            }
        }
        return saleQty;
    }
}
