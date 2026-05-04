package entity;

import java.sql.Timestamp;

public class Store {
    private int id;
    private String storeName;
    private String location;
    private Integer managerId; // nullable
    private String status;     // 'active' | 'inactive'
    private Timestamp createdAt;
    private boolean isDeleted;
    private Timestamp deletedAt; // nullable

    // ── Constructors ──────────────────────────────────────────────
    public Store() {}

    public Store(int id, String storeName, String location, Integer managerId,
                 String status, Timestamp createdAt, boolean isDeleted, Timestamp deletedAt) {
        this.id         = id;
        this.storeName  = storeName;
        this.location   = location;
        this.managerId  = managerId;
        this.status     = status;
        this.createdAt  = createdAt;
        this.isDeleted  = isDeleted;
        this.deletedAt  = deletedAt;
    }

    // ── Getters & Setters ─────────────────────────────────────────
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Integer getManagerId() { return managerId; }
    public void setManagerId(Integer managerId) { this.managerId = managerId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    public Timestamp getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Timestamp deletedAt) { this.deletedAt = deletedAt; }

    // ── toString ──────────────────────────────────────────────────
    @Override
    public String toString() {
        return "Store{" +
                "id=" + id +
                ", storeName='" + storeName + '\'' +
                ", location='" + location + '\'' +
                ", managerId=" + managerId +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", isDeleted=" + isDeleted +
                ", deletedAt=" + deletedAt +
                '}';
    }
}
