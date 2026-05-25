package repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import entity.Attendance;

public class AttendanceRepository {

    private Attendance map(ResultSet rs) throws SQLException {
        Attendance a = new Attendance();
        a.setId(rs.getInt("id"));
        a.setEmployeeId(rs.getInt("employee_id"));
        a.setWorkDate(rs.getDate("work_date"));
        a.setCheckIn(rs.getTimestamp("check_in"));
        a.setBreakStart(rs.getTimestamp("break_start"));
        a.setBreakEnd(rs.getTimestamp("break_end"));
        a.setCheckOut(rs.getTimestamp("check_out"));
        a.setTotalWorkHours(rs.getBigDecimal("total_work_hours"));
        a.setStatus(rs.getString("status"));
        return a;
    }

    public List<Attendance> findAll() throws SQLException {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT * FROM attendance ORDER BY work_date DESC";
        try (Connection con = util.DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(map(rs));
        }
        return list;
    }

    public Optional<Attendance> findById(int id) throws SQLException {
        String sql = "SELECT * FROM attendance WHERE id = ?";
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

    public List<Attendance> findByEmployeeId(int employeeId) throws SQLException {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT * FROM attendance WHERE employee_id = ? ORDER BY work_date DESC";
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

    public List<Attendance> findByEmployeeAndDateRange(int employeeId, Date from, Date to) throws SQLException {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT * FROM attendance WHERE employee_id = ? AND work_date BETWEEN ? AND ? ORDER BY work_date ASC";
        try (Connection con = util.DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setDate(2, from);
            ps.setDate(3, to);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(map(rs));
            }
        }
        return list;
    }

    public Optional<Attendance> findTodayByEmployee(int employeeId) throws SQLException {
        String sql = "SELECT * FROM attendance WHERE employee_id = ? AND work_date = CURDATE() LIMIT 1";
        try (Connection con = util.DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    public BigDecimal sumWorkHoursByEmployeeAndMonth(int employeeId, int year, int month) throws SQLException {
        String sql = "SELECT COALESCE(SUM(total_work_hours), 0) FROM attendance " +
                "WHERE employee_id = ? AND YEAR(work_date) = ? AND MONTH(work_date) = ? AND status = 'valid'";
        try (Connection con = util.DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setInt(2, year);
            ps.setInt(3, month);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getBigDecimal(1);
            }
        }
        return BigDecimal.ZERO;
    }

    public boolean insert(Attendance a) throws SQLException {
        // Validate required fields
        if (a.getCheckIn() == null)
            throw new IllegalArgumentException("Check-in không được để trống");

        if (a.getWorkDate() == null)
            throw new IllegalArgumentException("Ngày làm việc không được để trống");

        // Validate work_date not in future
        java.util.Date today = new java.util.Date();
        if (a.getWorkDate().getTime() > today.getTime())
            throw new IllegalArgumentException("Ngày làm việc không được trong tương lai");

        // Validate checkout > checkin if both exist
        if (a.getCheckOut() != null && a.getCheckOut().before(a.getCheckIn()))
            throw new IllegalArgumentException("Thời gian checkout phải sau checkin");

        // Validate status
        if (a.getStatus() == null || (!a.getStatus().equals("valid") &&
                !a.getStatus().equals("invalid") && !a.getStatus().equals("rejected")))
            throw new IllegalArgumentException("Status không hợp lệ: " + a.getStatus());

        String sql = "INSERT INTO attendance (employee_id, work_date, check_in, break_start, break_end, check_out, total_work_hours, status) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = util.DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.getEmployeeId());
            ps.setDate(2, a.getWorkDate());
            ps.setTimestamp(3, a.getCheckIn());
            ps.setTimestamp(4, a.getBreakStart());
            ps.setTimestamp(5, a.getBreakEnd());
            ps.setTimestamp(6, a.getCheckOut());
            ps.setBigDecimal(7, a.getTotalWorkHours());
            ps.setString(8, a.getStatus());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next())
                        a.setId(keys.getInt(1));
                }
                return true;
            }
        }
        return false;
    }

    public boolean update(Attendance a) throws SQLException {
        String sql = "UPDATE attendance SET break_start = ?, break_end = ?, check_out = ?, total_work_hours = ?, status = ? WHERE id = ?";
        try (Connection con = util.DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, a.getBreakStart());
            ps.setTimestamp(2, a.getBreakEnd());
            ps.setTimestamp(3, a.getCheckOut());
            ps.setBigDecimal(4, a.getTotalWorkHours());
            ps.setString(5, a.getStatus());
            ps.setInt(6, a.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int id, String status) throws SQLException {
        String sql = "UPDATE attendance SET status = ? WHERE id = ?";
        try (Connection con = util.DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }
}
