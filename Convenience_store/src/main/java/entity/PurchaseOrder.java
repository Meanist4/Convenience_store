package entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class PurchaseOrder {
    private int id;
    private int supplierId;
    private int storeId;
    private Integer adminId; // Người tạo đơn
    private BigDecimal totalAmount;
    private String status; // pending, received, cancelled
    private Timestamp createdAt;
    private Timestamp receivedAt;

    public PurchaseOrder() {
    }

    public PurchaseOrder(int id, int supplierId, int storeId, Integer adminId, BigDecimal totalAmount, String status,
            Timestamp createdAt, Timestamp receivedAt) {
        this.id = id;
        this.supplierId = supplierId;
        this.storeId = storeId;
        this.adminId = adminId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
        this.receivedAt = receivedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }

    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

    public Integer getAdminId() {
        return adminId;
    }

    public void setAdminId(Integer adminId) {
        this.adminId = adminId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
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

    public Timestamp getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Timestamp receivedAt) {
        this.receivedAt = receivedAt;
    }

    @Override
    public String toString() {
        return "PurchaseOrder [id=" + id + ", supplierId=" + supplierId + ", storeId=" + storeId + ", adminId="
                + adminId + ", totalAmount=" + totalAmount + ", status=" + status + ", createdAt=" + createdAt
                + ", receivedAt=" + receivedAt + "]";
    }

}
