package entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Manager {
    private int id;
    private int employeeId;
    private String username;
    private String passwordHash;
    private String managementLevel;
    private BigDecimal allowance;
    private boolean isDeleted;
    private Timestamp deletedAt; // nullable

    public Manager() {
    }

    public Manager(int id, int employeeId, String username, String passwordHash,
            String managementLevel, BigDecimal allowance,
            boolean isDeleted, Timestamp deletedAt) {
        this.id = id;
        this.employeeId = employeeId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.managementLevel = managementLevel;
        this.allowance = allowance;
        this.isDeleted = isDeleted;
        this.deletedAt = deletedAt;
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

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public Timestamp getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Timestamp deletedAt) {
        this.deletedAt = deletedAt;
    }

    @Override
    public String toString() {
        return "Manager{" +
                "id=" + id +
                ", employeeId=" + employeeId +
                ", username='" + username + '\'' +
                ", managementLevel='" + managementLevel + '\'' +
                ", allowance=" + allowance +
                ", isDeleted=" + isDeleted +
                ", deletedAt=" + deletedAt +
                '}';
    }
}
