package repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import entity.Payroll;

public class PayrollRepository {

    public static class PayrollRecord {
        public int employeeId;
        public String fullName;
        public int year;
        public int month;
        public BigDecimal totalHours;
        public BigDecimal hourlyRate;
        public BigDecimal baseSalary; // totalHours × hourlyRate
        public BigDecimal allowance; // phụ cấp quản lý (0 nếu không phải manager)
        public BigDecimal totalSalary; // baseSalary + allowance

        @Override
        public String toString() {
            return "PayrollRecord{employeeId=" + employeeId +
                    ", fullName='" + fullName + '\'' +
                    ", " + year + "/" + month +
                    ", totalHours=" + totalHours +
                    ", baseSalary=" + baseSalary +
                    ", allowance=" + allowance +
                    ", totalSalary=" + totalSalary + '}';
        }
    }

    public PayrollRecord calculateForEmployee(int employeeId, int year, int month) throws SQLException {
        String sql = "SELECT e.id, e.full_name, e.hourly_rate, " +
                "       COALESCE(SUM(a.total_work_hours), 0) AS total_hours, " +
                "       COALESCE(m.allowance, 0) AS allowance " +
                "FROM employees e " +
                "LEFT JOIN attendance a ON a.employee_id = e.id " +
                "    AND a.status = 'valid' " +
                "    AND YEAR(a.work_date) = ? AND MONTH(a.work_date) = ? " +
                "LEFT JOIN managers m ON m.employee_id = e.id AND m.is_deleted = 0 " +
                "WHERE e.id = ? AND e.is_deleted = 0 " +
                "GROUP BY e.id, e.full_name, e.hourly_rate, m.allowance";

        try (Connection con = util.DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            ps.setInt(3, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return buildRecord(rs, year, month);
            }
        }
        return null;
    }

    public List<PayrollRecord> calculateByStore(int storeId, int year, int month) throws SQLException {
        List<PayrollRecord> list = new ArrayList<>();
        String sql = "SELECT e.id, e.full_name, e.hourly_rate, " +
                "       COALESCE(SUM(a.total_work_hours), 0) AS total_hours, " +
                "       COALESCE(m.allowance, 0) AS allowance " +
                "FROM employees e " +
                "LEFT JOIN attendance a ON a.employee_id = e.id " +
                "    AND a.status = 'valid' " +
                "    AND YEAR(a.work_date) = ? AND MONTH(a.work_date) = ? " +
                "LEFT JOIN managers m ON m.employee_id = e.id AND m.is_deleted = 0 " +
                "WHERE e.store_id = ? AND e.is_deleted = 0 " +
                "GROUP BY e.id, e.full_name, e.hourly_rate, m.allowance " +
                "ORDER BY e.full_name";

        try (Connection con = util.DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            ps.setInt(3, storeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(buildRecord(rs, year, month));
            }
        }
        return list;
    }

    public BigDecimal totalPayrollByMonth(int year, int month) throws SQLException {
        String sql = "SELECT COALESCE(SUM(e.hourly_rate * COALESCE(sub.total_hours, 0) + COALESCE(m.allowance, 0)), 0) "
                +
                "FROM employees e " +
                "LEFT JOIN ( " +
                "    SELECT employee_id, SUM(total_work_hours) AS total_hours " +
                "    FROM attendance " +
                "    WHERE status = 'valid' AND YEAR(work_date) = ? AND MONTH(work_date) = ? " +
                "    GROUP BY employee_id " +
                ") sub ON sub.employee_id = e.id " +
                "LEFT JOIN managers m ON m.employee_id = e.id AND m.is_deleted = 0 " +
                "WHERE e.is_deleted = 0";

        try (Connection con = util.DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getBigDecimal(1);
            }
        }
        return BigDecimal.ZERO;
    }

    public Map<String, BigDecimal> workHourHistory(int employeeId, int months) throws SQLException {
        Map<String, BigDecimal> history = new LinkedHashMap<>();
        String sql = "SELECT DATE_FORMAT(work_date, '%Y-%m') AS ym, COALESCE(SUM(total_work_hours), 0) AS hrs " +
                "FROM attendance " +
                "WHERE employee_id = ? AND status = 'valid' " +
                "GROUP BY ym ORDER BY ym DESC LIMIT ?";
        try (Connection con = util.DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setInt(2, months);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    history.put(rs.getString("ym"), rs.getBigDecimal("hrs"));
            }
        }
        return history;
    }

    private PayrollRecord buildRecord(ResultSet rs, int year, int month) throws SQLException {
        PayrollRecord rec = new PayrollRecord();
        rec.employeeId = rs.getInt("id");
        rec.fullName = rs.getString("full_name");
        rec.year = year;
        rec.month = month;
        rec.totalHours = rs.getBigDecimal("total_hours");
        rec.hourlyRate = rs.getBigDecimal("hourly_rate");
        rec.allowance = rs.getBigDecimal("allowance");
        rec.baseSalary = rec.hourlyRate.multiply(rec.totalHours);
        rec.totalSalary = rec.baseSalary.add(rec.allowance);
        return rec;
    }

    public boolean insert(Payroll p) throws SQLException {
        // Validate month và year
        if (p.getMonth() < 1 || p.getMonth() > 12)
            throw new IllegalArgumentException("Tháng phải từ 1 đến 12, nhận: " + p.getMonth());

        if (p.getYear() < 1900 || p.getYear() > 2999)
            throw new IllegalArgumentException("Năm không hợp lệ: " + p.getYear());

        // Check duplicate
        String checkSql = "SELECT COUNT(*) FROM payrolls WHERE employee_id = ? AND month = ? AND year = ?";
        try (Connection conn = util.DatabaseUtil.getConnection();
                PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
            checkPs.setInt(1, p.getEmployeeId());
            checkPs.setInt(2, p.getMonth());
            checkPs.setInt(3, p.getYear());
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0)
                    throw new IllegalStateException("Lương tháng " + p.getMonth() + "/" + p.getYear() +
                            " cho nhân viên id=" + p.getEmployeeId() + " đã được chốt rồi");
            }
        }

        String sql = "INSERT INTO payrolls (employee_id, month, year, total_hours, hourly_rate_at_time, allowance, bonus, deductions, final_salary, status) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = util.DatabaseUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, p.getEmployeeId());
            ps.setInt(2, p.getMonth());
            ps.setInt(3, p.getYear());
            ps.setBigDecimal(4, p.getTotalHours());
            ps.setBigDecimal(5, p.getHourlyRateAtTime());
            ps.setBigDecimal(6, p.getAllowance());
            ps.setBigDecimal(7, p.getBonus());
            ps.setBigDecimal(8, p.getDeductions());
            ps.setBigDecimal(9, p.getFinalSalary());
            ps.setString(10, p.getStatus());
            return ps.executeUpdate() > 0;
        }
    }

    public List<Payroll> findByMonth(int month, int year) throws SQLException {
        List<Payroll> list = new ArrayList<>();
        String sql = "SELECT * FROM payrolls WHERE month = ? AND year = ?";
        try (Connection conn = util.DatabaseUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, month);
            ps.setInt(2, year);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Payroll p = new Payroll();
                p.setId(rs.getInt("id"));
                p.setEmployeeId(rs.getInt("employee_id"));
                p.setFinalSalary(rs.getBigDecimal("final_salary"));
                p.setStatus(rs.getString("status"));
                list.add(p);
            }
        }
        return list;
    }
}
