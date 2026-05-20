package entity;

import java.sql.Timestamp;

public class Notification {
    private int id;
    private Integer storeId; // Có thể null nếu là thông báo hệ thống chung
    private String title;
    private String content;
    private String type; // inventory_alert, system_alert, payroll_alert
    private boolean isRead;
    private Timestamp createdAt;

    public Notification() {
    }

    public Notification(int id, Integer storeId, String title, String content, String type, boolean isRead,
            Timestamp createdAt) {
        this.id = id;
        this.storeId = storeId;
        this.title = title;
        this.content = content;
        this.type = type;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public void setStoreId(Integer storeId) {
        this.storeId = storeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Notification [id=" + id + ", storeId=" + storeId + ", title=" + title + ", content=" + content
                + ", type=" + type + ", isRead=" + isRead + ", createdAt=" + createdAt + "]";
    }

}
