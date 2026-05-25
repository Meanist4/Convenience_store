package entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Invoice {

    private int id;
    private int storeId;
    private int employeeId;
    private BigDecimal totalAmount;
    private Timestamp createdAt;

    public Invoice() {
    }

    public Invoice(int id, int storeId, int employeeId, BigDecimal totalAmount, Timestamp createdAt) {
        this.id = id;
        this.storeId = storeId;
        this.employeeId = employeeId;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Invoice{" + "id=" + id + ", storeId=" + storeId + ", employeeId=" + employeeId + ", totalAmount=" + totalAmount + ", createdAt=" + createdAt + '}';
    }

}
