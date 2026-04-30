package repository;

import convenience_store.DatabaseConnection;
import java.sql.*;

public class InventoryRepository {

    public int getStock(int storeId, int productId) {
        String sql = "SELECT quantity FROM store_inventory WHERE store_id = ? AND product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, storeId);
            pstmt.setInt(2, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("quantity");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean updateStock(int storeId, int productId, int changeAmount) {
        String sql = "UPDATE store_inventory "
                + "SET quantity = quantity + ? "
                + "WHERE store_id = ? AND product_id = ? AND (quantity + ?) >= 0";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, changeAmount);
            pstmt.setInt(2, storeId);
            pstmt.setInt(3, productId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
