package service.impl;

import service.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import entity.Attendance;
import entity.Employee;
import exception.NotFoundException;
import repository.AttendanceRepository;
import repository.EmployeeRepository;
import util.BarcodeUtil;

public class AttendanceServiceImpl implements AttendanceService {
    private final AttendanceRepository attendanceRepo = new AttendanceRepository();
    private final EmployeeRepository employeeRepo = new EmployeeRepository();

    @Override
    public Optional<Attendance> processSwipe(int employeeId) throws SQLException {
        employeeRepo.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên ID: " + employeeId));
        return attendanceRepo.findTodayByEmployee(employeeId);
    }

    @Override
    public String getAttendanceStatus(int employeeId) throws SQLException {
        Optional<Attendance> attendanceOpt = attendanceRepo.findTodayByEmployee(employeeId);
        if (attendanceOpt.isEmpty()) {
            return "NOT_CHECKED_IN";
        }
        Attendance a = attendanceOpt.get();
        if (a.getCheckOut() != null) {
            return "ALREADY_CHECKED_OUT";
        }
        if (a.getBreakStart() != null && a.getBreakEnd() == null) {
            return "ON_BREAK";
        }
        if (a.getBreakStart() == null) {
            return "WORKING_BEFORE_BREAK";
        }
        return "WORKING_AFTER_BREAK";
    }

    @Override
    public Attendance checkIn(int employeeId) throws SQLException {
        if (attendanceRepo.findTodayByEmployee(employeeId).isPresent()) {
            throw new IllegalStateException("Hệ thống nhận thấy bạn đã Check-in rồi.");
        }
        Attendance a = new Attendance();
        a.setEmployeeId(employeeId);
        a.setWorkDate(new Date(System.currentTimeMillis()));
        a.setCheckIn(new Timestamp(System.currentTimeMillis()));
        a.setTotalWorkHours(BigDecimal.ZERO);
        a.setStatus("valid");
        if (!attendanceRepo.insert(a))
            throw new RuntimeException("Lỗi database khi Check-in");
        return a;
    }

    @Override
    public Attendance startBreak(int employeeId) throws SQLException {
        Attendance a = getActiveAttendance(employeeId);
        if (a.getCheckOut() != null)
            throw new IllegalStateException("Ca làm đã kết thúc.");
        if (a.getBreakStart() != null)
            throw new IllegalStateException("Bạn đã sử dụng lượt nghỉ rồi.");
        a.setBreakStart(new Timestamp(System.currentTimeMillis()));
        if (!attendanceRepo.update(a))
            throw new RuntimeException("Lỗi database khi bắt đầu nghỉ");
        return a;
    }

    @Override
    public Attendance endBreak(int employeeId) throws SQLException {
        Attendance a = getActiveAttendance(employeeId);
        if (a.getBreakStart() == null)
            throw new IllegalStateException("Bạn chưa bắt đầu nghỉ.");
        if (a.getBreakEnd() != null)
            throw new IllegalStateException("Giờ nghỉ đã kết thúc trước đó.");
        a.setBreakEnd(new Timestamp(System.currentTimeMillis()));
        if (!attendanceRepo.update(a))
            throw new RuntimeException("Lỗi database khi kết thúc nghỉ");
        return a;
    }

    @Override
    public Attendance checkOut(int employeeId) throws SQLException {
        Attendance a = getActiveAttendance(employeeId);
        if (a.getCheckOut() != null)
            throw new IllegalStateException("Ca làm này đã đóng.");
        if (a.getBreakStart() != null && a.getBreakEnd() == null) {
            a.setBreakEnd(new Timestamp(System.currentTimeMillis()));
        }
        Timestamp now = new Timestamp(System.currentTimeMillis());
        a.setCheckOut(now);
        long workMillis = now.getTime() - a.getCheckIn().getTime();
        if (a.getBreakStart() != null && a.getBreakEnd() != null) {
            long breakMillis = a.getBreakEnd().getTime() - a.getBreakStart().getTime();
            workMillis -= breakMillis;
        }
        BigDecimal hours = BigDecimal.valueOf(workMillis)
                .divide(BigDecimal.valueOf(3_600_000), 2, RoundingMode.HALF_UP);
        a.setTotalWorkHours(hours.max(BigDecimal.ZERO));
        if (!attendanceRepo.update(a))
            throw new RuntimeException("Lỗi database khi Check-out");
        return a;
    }

    @Override
    public List<Attendance> getAllAttendance() throws SQLException {
        return attendanceRepo.findAll();
    }

    @Override
    public void updateStatus(int id, String status) throws SQLException {
        if (!List.of("valid", "invalid", "rejected").contains(status))
            throw new IllegalArgumentException("Trạng thái không hợp lệ");
        attendanceRepo.updateStatus(id, status);
    }

    @Override
    public Attendance processAttendance(String barcode) throws SQLException {
        Optional<Employee> empOpt = BarcodeUtil.scanEmployee(barcode);
        if (empOpt.isEmpty())
            throw new NotFoundException("Không tìm thấy nhân viên với barcode: " + barcode);
        int employeeId = empOpt.get().getId();
        String status = getAttendanceStatus(employeeId);
        return switch (status) {
            case "NOT_CHECKED_IN" -> checkIn(employeeId);
            case "WORKING_BEFORE_BREAK" -> startBreak(employeeId);
            case "ON_BREAK" -> endBreak(employeeId);
            case "WORKING_AFTER_BREAK" -> checkOut(employeeId);
            case "ALREADY_CHECKED_OUT" -> throw new IllegalStateException("Ca làm đã kết thúc. Không thể scan thêm.");
            default -> throw new IllegalStateException("Trạng thái không hợp lệ: " + status);
        };
    }

    private Attendance getActiveAttendance(int employeeId) throws SQLException {
        return attendanceRepo.findTodayByEmployee(employeeId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy dữ liệu bắt đầu ca của bạn."));
    }
}

