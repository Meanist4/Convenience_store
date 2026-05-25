package entity;

import java.math.BigDecimal;

public class Manager {

    private int id;
    private int employeeId;
    private String username;
    private String passwordHash;
    private String managementLevel;
    private BigDecimal allowance;

    public Manager() {
    }

    public Manager(int id, int employeeId, String username, String passwordHash,
            String managementLevel, BigDecimal allowance) {
        this.id = id;
        this.employeeId = employeeId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.managementLevel = managementLevel;
        this.allowance = allowance;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getManagementLevel() {
        return managementLevel;
    }

    public void setManagementLevel(String managementLevel) {
        this.managementLevel = managementLevel;
    }

    public BigDecimal getAllowance() {
        return allowance;
    }

    public void setAllowance(BigDecimal allowance) {
        this.allowance = allowance;
    }

    @Override
    public String toString() {
        return "Manager{id=" + id + ", employeeId=" + employeeId + ", username='" + username + "', managementLevel='" + managementLevel + "'}";
    }
}
