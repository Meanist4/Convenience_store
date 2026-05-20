package entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Product {

    private int id;
    private String productName;
    private String category;
    private String baseUnit;
    private BigDecimal importPrice;
    private BigDecimal markupRate;
    private String status;      // 'active' | 'inactive'
    private Timestamp createdAt;
    private boolean isDeleted;
    private Timestamp deletedAt; // nullable
    private String imageName;

    // ── Constructors ──────────────────────────────────────────────
    public Product() {
    }

    public Product(int id, String productName, String category, String baseUnit,
            BigDecimal importPrice, BigDecimal markupRate, String status,
            Timestamp createdAt, boolean isDeleted, Timestamp deletedAt, String imageName) {
        this.id = id;
        this.productName = productName;
        this.category = category;
        this.baseUnit = baseUnit;
        this.importPrice = importPrice;
        this.markupRate = markupRate;
        this.status = status;
        this.createdAt = createdAt;
        this.isDeleted = isDeleted;
        this.deletedAt = deletedAt;
        this.imageName = imageName;
    }

    // ── Getters & Setters ─────────────────────────────────────────
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBaseUnit() {
        return baseUnit;
    }

    public void setBaseUnit(String baseUnit) {
        this.baseUnit = baseUnit;
    }

    public BigDecimal getImportPrice() {
        return importPrice;
    }

    public void setImportPrice(BigDecimal importPrice) {
        this.importPrice = importPrice;
    }

    public BigDecimal getMarkupRate() {
        return markupRate;
    }

    public void setMarkupRate(BigDecimal markupRate) {
        this.markupRate = markupRate;
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

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Timestamp getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Timestamp deletedAt) {
        this.deletedAt = deletedAt;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    // ── toString ──────────────────────────────────────────────────
    @Override
    public String toString() {
        return "Product{"
                + "id=" + id
                + ", productName='" + productName + '\''
                + ", category='" + category + '\''
                + ", baseUnit='" + baseUnit + '\''
                + ", importPrice=" + importPrice
                + ", markupRate=" + markupRate
                + ", status='" + status + '\''
                + ", createdAt=" + createdAt
                + ", isDeleted=" + isDeleted
                + ", deletedAt=" + deletedAt
                + ", imageName='" + imageName + '\''
                + '}';
    }
}
