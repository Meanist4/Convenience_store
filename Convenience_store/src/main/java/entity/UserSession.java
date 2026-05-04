package entity;

import java.sql.Timestamp;

public class UserSession {
    private int id;
    private int userId;
    private String userType; // 'admin' | 'manager'
    private String refreshToken;
    private String ipAddress;
    private String userAgent;
    private Timestamp expiresAt;
    private Timestamp createdAt;

    // ── Constructors ──────────────────────────────────────────────
    public UserSession() {
    }

    public UserSession(int id, int userId, String userType, String refreshToken,
            String ipAddress, String userAgent, Timestamp expiresAt, Timestamp createdAt) {
        this.id = id;
        this.userId = userId;
        this.userType = userType;
        this.refreshToken = refreshToken;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    // ── Getters & Setters ─────────────────────────────────────────
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Timestamp getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Timestamp expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    // ── toString ──────────────────────────────────────────────────
    @Override
    public String toString() {
        return "UserSession{" +
                "id=" + id +
                ", userId=" + userId +
                ", userType='" + userType + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", userAgent='" + userAgent + '\'' +
                ", expiresAt=" + expiresAt +
                ", createdAt=" + createdAt +
                '}';
    }
}
