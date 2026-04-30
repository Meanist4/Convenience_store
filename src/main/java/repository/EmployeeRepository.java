package repository;

import convenience_store.DatabaseConnection;
import entity.Employee;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmployeeRepository {

    private static final Logger LOGGER = Logger.getLogger(EmployeeRepository.class.getName());

    private static final String FIND_BY_BARCODE_SQL = "SELECT * FROM employees WHERE employee_barcode = ?";

    public Employee findByBarcode(String barcode) {
        if (barcode == null || barcode.trim().isEmpty()) {
            return null;
        }

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(FIND_BY_BARCODE_SQL)) {

            pstmt.setString(1, barcode.trim());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEmployee(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn Employee với barcode: " + barcode, e);
        }
        return null;
    }

    private Employee mapResultSetToEmployee(ResultSet rs) throws SQLException {
        Employee emp = new Employee();

        emp.setId(rs.getInt("id"));
        emp.setFullName(rs.getString("full_name"));
        emp.setIdCard(rs.getString("id_card"));
        emp.setEmployeeBarcode(rs.getString("employee_barcode"));
        emp.setPhone(rs.getString("phone"));
        emp.setEmail(rs.getString("email"));
        emp.setAddress(rs.getString("address"));
        emp.setStoreId(rs.getInt("store_id"));
        emp.setHourlyRate(rs.getBigDecimal("hourly_rate"));

        Date bDate = rs.getDate("birthday");
        if (bDate != null) {
            emp.setBirthday(bDate.toLocalDate());
        }

        String genderDB = rs.getString("gender");
        emp.setGender(switch (genderDB != null ? genderDB : "") {
            case "Nam" ->
                Employee.Gender.MALE;
            case "Nu" ->
                Employee.Gender.FEMALE;
            case "Khac" ->
                Employee.Gender.OTHER;
            default ->
                Employee.Gender.OTHER;
        });

        String statusDB = rs.getString("status");
        emp.setStatus(switch (statusDB != null ? statusDB : "") {
            case "active" ->
                Employee.Status.ACTIVE;
            case "inactive" ->
                Employee.Status.INACTIVE;
            default ->
                Employee.Status.ON_LEAVE;
        });

        return emp;
    }
}
