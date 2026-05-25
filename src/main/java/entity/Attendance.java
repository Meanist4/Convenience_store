package entity;

import java.sql.Date;
import java.sql.Timestamp;

public class Attendance {

    private int id;
    private int employeeId;
    private Timestamp checkIn;
    private Timestamp breakStart;
    private Timestamp breakEnd;
    private Timestamp checkOut;
    private double totalWorkHours;
    private java.sql.Date workDate;

    public Attendance() {
    }

    public Attendance(int id, int employeeId, Timestamp checkIn, Timestamp breakStart, Timestamp breakEnd, Timestamp checkOut, double totalWorkHours, Date workDate) {
        this.id = id;
        this.employeeId = employeeId;
        this.checkIn = checkIn;
        this.breakStart = breakStart;
        this.breakEnd = breakEnd;
        this.checkOut = checkOut;
        this.totalWorkHours = totalWorkHours;
        this.workDate = workDate;
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

    public double getTotalWorkHours() {
        return totalWorkHours;
    }

    public void setTotalWorkHours(double totalWorkHours) {
        this.totalWorkHours = totalWorkHours;
    }

    public Date getWorkDate() {
        return workDate;
    }

    public void setWorkDate(Date workDate) {
        this.workDate = workDate;
    }

    @Override
    public String toString() {
        return "Attendance{" + "id=" + id + ", employeeId=" + employeeId + ", checkIn=" + checkIn + ", breakStart=" + breakStart + ", breakEnd=" + breakEnd + ", checkOut=" + checkOut + ", totalWorkHours=" + totalWorkHours + ", workDate=" + workDate + '}';
    }

}
