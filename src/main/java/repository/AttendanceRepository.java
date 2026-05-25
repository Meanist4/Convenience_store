package repository;

import convenience_store.DatabaseConnection;
import java.sql.*;
import javax.swing.JOptionPane;

public class AttendanceRepository {

    public boolean isCurrentlyWorking(int employeeId) {
        String sql = "SELECT id FROM attendance WHERE employee_id = ? AND check_out IS NULL";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, employeeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean checkIn(int employeeId) {
        String sql = "INSERT INTO attendance (employee_id, work_date, check_in) VALUES (?, CURDATE(), NOW())";
        try (Connection conn = DatabaseConnection.getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, employeeId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            showError("Lỗi Check-in: " + e.getMessage());
            return false;
        }
    }

    public boolean checkOut(int employeeId) {
        String sql = "UPDATE attendance SET check_out = NOW(), "
                + "total_work_hours = TIMESTAMPDIFF(SECOND, check_in, NOW()) / 3600.0 "
                + "WHERE employee_id = ? AND check_out IS NULL "
                + "ORDER BY check_in DESC LIMIT 1"; 

        try (Connection conn = DatabaseConnection.getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, employeeId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            showError("Lỗi Check-out: " + e.getMessage());
            return false;
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "Lỗi Hệ Thống", JOptionPane.ERROR_MESSAGE);
    }
}
