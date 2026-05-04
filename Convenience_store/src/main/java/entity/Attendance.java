package entity;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class Attendance {
    private int id;
    private int employeeId;
    private Date workDate;
    private Timestamp checkIn;
    private Timestamp breakStart; // nullable
    private Timestamp breakEnd; // nullable
    private Timestamp checkOut; // nullable
    private BigDecimal totalWorkHours;
    private String status; // 'valid' | 'invalid' | 'rejected'

    public Attendance() {
    }

    public Attendance(int id, int employeeId, Date workDate, Timestamp checkIn,
            Timestamp breakStart, Timestamp breakEnd, Timestamp checkOut,
            BigDecimal totalWorkHours, String status) {
        this.id = id;
        this.employeeId = employeeId;
        this.workDate = workDate;
        this.checkIn = checkIn;
        this.breakStart = breakStart;
        this.breakEnd = breakEnd;
        this.checkOut = checkOut;
        this.totalWorkHours = totalWorkHours;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public Date getWorkDate() {
        return workDate;
    }

    public void setWorkDate(Date workDate) {
        this.workDate = workDate;
    }

    public Timestamp getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(Timestamp checkIn) {
        this.checkIn = checkIn;
    }

    public Timestamp getBreakStart() {
        return breakStart;
    }

    public void setBreakStart(Timestamp breakStart) {
        this.breakStart = breakStart;
    }

    public Timestamp getBreakEnd() {
        return breakEnd;
    }

    public void setBreakEnd(Timestamp breakEnd) {
        this.breakEnd = breakEnd;
    }

    public Timestamp getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(Timestamp checkOut) {
        this.checkOut = checkOut;
    }

    public BigDecimal getTotalWorkHours() {
        return totalWorkHours;
    }

    public void setTotalWorkHours(BigDecimal totalWorkHours) {
        this.totalWorkHours = totalWorkHours;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Attendance{" +
                "id=" + id +
                ", employeeId=" + employeeId +
                ", workDate=" + workDate +
                ", checkIn=" + checkIn +
                ", breakStart=" + breakStart +
                ", breakEnd=" + breakEnd +
                ", checkOut=" + checkOut +
                ", totalWorkHours=" + totalWorkHours +
                ", status='" + status + '\'' +
                '}';
    }
}
