package repository;

import convenience_store.DatabaseConnection;
import java.sql.*;
import javax.swing.JOptionPane;

public class ProductRepository {

    public Object[] getProductByBarCode(String barcode) {
        String sql = "SELECT p.id, p.product_name, u.unit_name, u.selling_price, u.ratio "
                + "FROM product_units u "
                + "JOIN products p ON u.product_id = p.id "
                + "WHERE u.barcode = ?";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, barcode);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{
                        rs.getInt("id"),
                        rs.getString("product_name"),
                        rs.getString("unit_name"),
                        rs.getBigDecimal("selling_price"),
                        rs.getInt("ratio")
                    };
                } else {
                    System.out.println("Không tìm thấy sản phẩm với mã: " + barcode);
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Lỗi hệ thống: " + e.getMessage());
        }
        return null;
    }
}
