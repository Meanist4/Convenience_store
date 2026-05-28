package entity;

import java.math.BigDecimal;
import java.util.Date; // Thêm import để dùng kiểu dữ liệu Date

public class PurchaseOrderDetail {
    private int id;
    private int purchaseOrderId;
    private int productId;
    private int quantity;
    private BigDecimal importPriceAtTime;
    private BigDecimal subtotal;

    // ── Deferred Product Creation Fields ──────────────────────────
    private String rawBarcode; // Raw barcode for new products (hand-typed)
    private String tempProductName; // Temporary product name for new items
    private String tempCategory; // Temporary category for new items
    private String tempBaseUnit; // Temporary base unit for new items
    private BigDecimal tempMarkupRate; // Temporary markup rate for new items

    // ── Constructors ──────────────────────────────────────────────
    public PurchaseOrderDetail() {
    }

    public PurchaseOrderDetail(int id, int purchaseOrderId, int productId, int quantity,
            BigDecimal importPriceAtTime, BigDecimal subtotal) {
        this.id = id;
        this.purchaseOrderId = purchaseOrderId;
        this.productId = productId;
        this.quantity = quantity;
        this.importPriceAtTime = importPriceAtTime;
        this.subtotal = subtotal;
    }

    // ── Getters & Setters ─────────────────────────────────────────
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public void setPurchaseOrderId(int purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getImportPriceAtTime() {
        return importPriceAtTime;
    }

    public void setImportPriceAtTime(BigDecimal importPriceAtTime) {
        this.importPriceAtTime = importPriceAtTime;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    // ── Deferred Creation Getters & Setters ───────────────────────
    public String getRawBarcode() {
        return rawBarcode;
    }

    public void setRawBarcode(String rawBarcode) {
        this.rawBarcode = rawBarcode;
    }

    public String getTempProductName() {
        return tempProductName;
    }

    public void setTempProductName(String tempProductName) {
        this.tempProductName = tempProductName;
    }

    public String getTempCategory() {
        return tempCategory;
    }

    public void setTempCategory(String tempCategory) {
        this.tempCategory = tempCategory;
    }

    public String getTempBaseUnit() {
        return tempBaseUnit;
    }

    public void setTempBaseUnit(String tempBaseUnit) {
        this.tempBaseUnit = tempBaseUnit;
    }

    public BigDecimal getTempMarkupRate() {
        return tempMarkupRate;
    }

    public void setTempMarkupRate(BigDecimal tempMarkupRate) {
        this.tempMarkupRate = tempMarkupRate;
    }

    // ── toString ──────────────────────────────────────────────────
    @Override
    public String toString() {
        return "PurchaseOrderDetail{" +
                "id=" + id +
                ", purchaseOrderId=" + purchaseOrderId +
                ", productId=" + productId +
                ", quantity=" + quantity +
                ", importPriceAtTime=" + importPriceAtTime +
                ", subtotal=" + subtotal +
                ", rawBarcode='" + rawBarcode + '\'' +
                ", tempProductName='" + tempProductName + '\'' +
                ", tempCategory='" + tempCategory + '\'' +
                ", tempBaseUnit='" + tempBaseUnit + '\'' +
                ", tempMarkupRate=" + tempMarkupRate +
                '}';
    }
}