package entity;

import java.math.BigDecimal;
import java.sql.Date;

public class StoreInventory {
    // Khóa chính đơn độc lập mới (thay thế cho khóa chính phức hợp cũ)
    private int id;
    
    // Các thuộc tính cũ được giữ lại
    private int storeId;
    private int productId;
    private int quantity;
    private int minStockLevel = 5; // Giá trị mặc định là 5
    
    // Các thuộc tính mới phục vụ cho việc quản lý theo từng lô cụ thể
    private String batchCode;        // Mã lô hàng
    private BigDecimal importPrice;  // Giá nhập riêng của lô này
    private Date expiryDate;         // Hạn sử dụng
    private Date receivedAt;         // Ngày nhập lô hàng về kho

    // Constructor không tham số (No-args Constructor)
    public StoreInventory() {
    }

    // Constructor đầy đủ tham số (All-args Constructor)
    public StoreInventory(int id, int storeId, int productId, int quantity, int minStockLevel, 
                          String batchCode, BigDecimal importPrice, Date expiryDate, Date receivedAt) {
        this.id = id;
        this.storeId = storeId;
        this.productId = productId;
        this.quantity = quantity;
        this.minStockLevel = minStockLevel;
        this.batchCode = batchCode;
        this.importPrice = importPrice;
        this.expiryDate = expiryDate;
        this.receivedAt = receivedAt;
    }

    // ================= Getter và Setter =================

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

    public int getMinStockLevel() {
        return minStockLevel;
    }

    public void setMinStockLevel(int minStockLevel) {
        this.minStockLevel = minStockLevel;
    }

    public String getBatchCode() {
        return batchCode;
    }

    public void setBatchCode(String batchCode) {
        this.batchCode = batchCode;
    }

    public BigDecimal getImportPrice() {
        return importPrice;
    }

    public void setImportPrice(BigDecimal importPrice) {
        this.importPrice = importPrice;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Date getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Date receivedAt) {
        this.receivedAt = receivedAt;
    }

    // Tùy chọn: Ghi đè phương thức toString để tiện log/debug dữ liệu
    @Override
    public String toString() {
        return "StoreInventory{" +
                "id=" + id +
                ", storeId=" + storeId +
                ", productId=" + productId +
                ", quantity=" + quantity +
                ", minStockLevel=" + minStockLevel +
                ", batchCode='" + batchCode + '\'' +
                ", importPrice=" + importPrice +
                ", expiryDate=" + expiryDate +
                ", receivedAt=" + receivedAt +
                '}';
    }
}
