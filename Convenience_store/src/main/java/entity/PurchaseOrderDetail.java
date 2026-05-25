package entity;

import java.math.BigDecimal;
import java.util.Date; // Thêm import để dùng kiểu dữ liệu Date

public class PurchaseOrderDetail {
    private int id;
    private int purchaseOrderId;
    private int productId;
    private int quantity;
    private Date expiryDate;     // Thêm mới: Hạn sử dụng của sản phẩm
    private String batchCode;    // Thêm mới: Mã lô hàng
    private BigDecimal importPriceAtTime;
    private BigDecimal subtotal;

    // ── Constructors ──────────────────────────────────────────────
    public PurchaseOrderDetail() {
    }

    public PurchaseOrderDetail(int id, int purchaseOrderId, int productId, int quantity, 
                               Date expiryDate, String batchCode, BigDecimal importPriceAtTime, BigDecimal subtotal) {
        this.id = id;
        this.purchaseOrderId = purchaseOrderId;
        this.productId = productId;
        this.quantity = quantity;
        this.expiryDate = expiryDate;   // Khởi tạo thuộc tính mới
        this.batchCode = batchCode;     // Khởi tạo thuộc tính mới
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

    public Date getExpiryDate() { // Getter mới
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) { // Setter mới
        this.expiryDate = expiryDate;
    }

    public String getBatchCode() { // Getter mới
        return batchCode;
    }

    public void setBatchCode(String batchCode) { // Setter mới
        this.batchCode = batchCode;
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

    // ── toString ──────────────────────────────────────────────────
    @Override
    public String toString() {
        return "PurchaseOrderDetail{" +
                "id=" + id +
                ", purchaseOrderId=" + purchaseOrderId +
                ", productId=" + productId +
                ", quantity=" + quantity +
                ", expiryDate=" + expiryDate +   // Cập nhật toString
                ", batchCode='" + batchCode + '\'' + // Cập nhật toString
                ", importPriceAtTime=" + importPriceAtTime +
                ", subtotal=" + subtotal +
                '}';
    }
}