package entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class ProductUnit {

    
    private int id;
    private int productId;
    private String unitName;
    private int ratio;
    private String barcode;
    private BigDecimal sellingPrice;
    private boolean isDefaultSale;
    private boolean isDeleted;
    private Timestamp deletedAt; // nullable

    // ── Constructors ──────────────────────────────────────────────
    public ProductUnit() {
    }

    public ProductUnit(int id, int productId, String unitName, int ratio, String barcode,
            BigDecimal sellingPrice, boolean isDefaultSale,
            boolean isDeleted, Timestamp deletedAt) {
        this.id = id;
        this.productId = productId;
        this.unitName = unitName;
        this.ratio = ratio;
        this.barcode = barcode;
        this.sellingPrice = sellingPrice;
        this.isDefaultSale = isDefaultSale;
        this.isDeleted = isDeleted;
        this.deletedAt = deletedAt;
    }

    // ── Getters & Setters ─────────────────────────────────────────
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public int getRatio() {
        return ratio;
    }

    public void setRatio(int ratio) {
        this.ratio = ratio;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public boolean isDefaultSale() {
        return isDefaultSale;
    }

    public void setDefaultSale(boolean defaultSale) {
        isDefaultSale = defaultSale;
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

    // ── toString ──────────────────────────────────────────────────
    @Override
    public String toString() {
        return "ProductUnit{"
                + "id=" + id
                + ", productId=" + productId
                + ", unitName='" + unitName + '\''
                + ", ratio=" + ratio
                + ", barcode='" + barcode + '\''
                + ", sellingPrice=" + sellingPrice
                + ", isDefaultSale=" + isDefaultSale
                + ", isDeleted=" + isDeleted
                + ", deletedAt=" + deletedAt
                + '}';
    }
}
