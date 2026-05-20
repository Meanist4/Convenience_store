package entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Payroll {
    private int id;
    private int employeeId;
    private int month;
    private int year;
    private BigDecimal totalHours;
    private BigDecimal hourlyRateAtTime;
    private BigDecimal allowance;
    private BigDecimal bonus;
    private BigDecimal deductions;
    private BigDecimal finalSalary;
    private String status; // draft, finalized, paid
    private Timestamp createdAt;

    public Payroll() {
    }

    public Payroll(int id, int employeeId, int month, int year, BigDecimal totalHours, BigDecimal hourlyRateAtTime,
            BigDecimal allowance, BigDecimal bonus, BigDecimal deductions, BigDecimal finalSalary, String status,
            Timestamp createdAt) {
        this.id = id;
        this.employeeId = employeeId;
        this.month = month;
        this.year = year;
        this.totalHours = totalHours;
        this.hourlyRateAtTime = hourlyRateAtTime;
        this.allowance = allowance;
        this.bonus = bonus;
        this.deductions = deductions;
        this.finalSalary = finalSalary;
        this.status = status;
        this.createdAt = createdAt;
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

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public BigDecimal getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(BigDecimal totalHours) {
        this.totalHours = totalHours;
    }

    public BigDecimal getHourlyRateAtTime() {
        return hourlyRateAtTime;
    }

    public void setHourlyRateAtTime(BigDecimal hourlyRateAtTime) {
        this.hourlyRateAtTime = hourlyRateAtTime;
    }

    public BigDecimal getAllowance() {
        return allowance;
    }

    public void setAllowance(BigDecimal allowance) {
        this.allowance = allowance;
    }

    public BigDecimal getBonus() {
        return bonus;
    }

    public void setBonus(BigDecimal bonus) {
        this.bonus = bonus;
    }

    public BigDecimal getDeductions() {
        return deductions;
    }

    public void setDeductions(BigDecimal deductions) {
        this.deductions = deductions;
    }

    public BigDecimal getFinalSalary() {
        return finalSalary;
    }

    public void setFinalSalary(BigDecimal finalSalary) {
        this.finalSalary = finalSalary;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "PayRoll [id=" + id + ", employeeId=" + employeeId + ", month=" + month + ", year=" + year
                + ", totalHours=" + totalHours + ", hourlyRateAtTime=" + hourlyRateAtTime + ", allowance=" + allowance
                + ", bonus=" + bonus + ", deductions=" + deductions + ", finalSalary=" + finalSalary + ", status="
                + status + ", createdAt=" + createdAt + "]";
    }

}
