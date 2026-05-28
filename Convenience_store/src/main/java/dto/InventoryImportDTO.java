package dto;

import java.math.BigDecimal;
import java.sql.Date;

public class InventoryImportDTO {

    private int productId;
    private int quantity;
    private BigDecimal importPrice;
    private String batchCode;
    private java.sql.Date expiryDate;
    private String unitName;  // Khách nhập tay: "Thùng", "Két", "Lốc"...
    private int unitRatio;    // Khách nhập tay: 24, 12, 6...
    private String baseUnitName; // <-- THÊM BIẾN NÀY

    public InventoryImportDTO() {
    }

    public InventoryImportDTO(int productId, int quantity, BigDecimal importPrice, String batchCode, Date expiryDate, String unitName, int unitRatio, String baseUnitName) {
        this.productId = productId;
        this.quantity = quantity;
        this.importPrice = importPrice;
        this.batchCode = batchCode;
        this.expiryDate = expiryDate;
        this.unitName = unitName;
        this.unitRatio = unitRatio;
        this.baseUnitName = baseUnitName;
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

    public BigDecimal getImportPrice() {
        return importPrice;
    }

    public void setImportPrice(BigDecimal importPrice) {
        this.importPrice = importPrice;
    }

    public String getBatchCode() {
        return batchCode;
    }

    public void setBatchCode(String batchCode) {
        this.batchCode = batchCode;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public int getUnitRatio() {
        return unitRatio;
    }

    public void setUnitRatio(int unitRatio) {
        this.unitRatio = unitRatio;
    }

    public String getBaseUnitName() {
        return baseUnitName;
    }

    public void setBaseUnitName(String baseUnitName) {
        this.baseUnitName = baseUnitName;
    }

    @Override
    public String toString() {
        return "InventoryImportDTO{" + "productId=" + productId + ", quantity=" + quantity + ", importPrice=" + importPrice + ", batchCode=" + batchCode + ", expiryDate=" + expiryDate + ", unitName=" + unitName + ", unitRatio=" + unitRatio + ", baseUnitName=" + baseUnitName + '}';
    }

}
