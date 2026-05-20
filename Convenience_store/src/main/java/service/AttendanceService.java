package service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import entity.Attendance;
import exception.NotFoundException;

public interface AttendanceService {
    Optional<Attendance> processSwipe(int employeeId) throws SQLException;

    String getAttendanceStatus(int employeeId) throws SQLException;

    Attendance checkIn(int employeeId) throws SQLException;

    Attendance startBreak(int employeeId) throws SQLException;

    Attendance endBreak(int employeeId) throws SQLException;

    Attendance checkOut(int employeeId) throws SQLException;

    List<Attendance> getAllAttendance() throws SQLException;

    void updateStatus(int id, String status) throws SQLException;

    Attendance processAttendance(String barcode) throws SQLException, NotFoundException;
}