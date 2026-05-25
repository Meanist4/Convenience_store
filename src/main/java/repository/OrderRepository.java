package repository;

import convenience_store.DatabaseConnection;
import java.sql.*;
import java.math.BigDecimal;
import java.util.List;

public class OrderRepository {

    public boolean createOrder(int storeId, int employeeId, BigDecimal totalAmount, List<Object[]> cartItems) {
        String sqlInvoice = "INSERT INTO invoices (store_id, employee_id, total_amount) VALUES (?, ?, ?)";
        String sqlDetail = "INSERT INTO invoice_details (invoice_id, product_id, unit_id, quantity, price_at_sale, subtotal) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlUpdateStock = "UPDATE store_inventory SET quantity = quantity - ? WHERE store_id = ? AND product_id = ? AND quantity >= ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            int invoiceId = -1;
            try (PreparedStatement pstmt = conn.prepareStatement(sqlInvoice, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, storeId);
                pstmt.setInt(2, employeeId);
                pstmt.setBigDecimal(3, totalAmount);
                pstmt.executeUpdate();

                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        invoiceId = rs.getInt(1);
                    }
                }
            }

            if (invoiceId == -1) {
                throw new SQLException("Không lấy được ID hóa đơn.");
            }

            try (PreparedStatement pstmtDetail = conn.prepareStatement(sqlDetail); PreparedStatement pstmtStock = conn.prepareStatement(sqlUpdateStock)) {

                for (Object[] item : cartItems) {
                    int productId = (int) item[0];
                    BigDecimal price = (BigDecimal) item[3];
                    int ratio = (int) item[4];
                    int quantity = (int) item[5];
                    int unitId = (int) item[6];
                    BigDecimal subtotal = price.multiply(new BigDecimal(quantity));

                    pstmtDetail.setInt(1, invoiceId);
                    pstmtDetail.setInt(2, productId);
                    pstmtDetail.setInt(3, unitId);
                    pstmtDetail.setInt(4, quantity);
                    pstmtDetail.setBigDecimal(5, price);
                    pstmtDetail.setBigDecimal(6, subtotal);
                    pstmtDetail.addBatch();

                    pstmtStock.setInt(1, quantity * ratio);
                    pstmtStock.setInt(2, storeId);
                    pstmtStock.setInt(3, productId);
                    pstmtStock.addBatch();
                }

                pstmtDetail.executeBatch();
                pstmtStock.executeBatch();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
