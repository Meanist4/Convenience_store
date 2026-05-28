package dto;

import java.math.BigDecimal;
import java.sql.Date;

public class InventoryImportDTO {

    private final int productId;
    private final int quantity;
    private final BigDecimal importPrice;
    private final String batchCode;
    private final Date expiryDate;
    private final int conversionRatio; // <-- THÊM BIẾN NÀY

    // Cập nhật lại Constructor để nhận thêm tham số conversionRatio
    public InventoryImportDTO(int productId, int quantity, BigDecimal importPrice, String batchCode, Date expiryDate, int conversionRatio) {
        this.productId = productId;
        this.quantity = quantity;
        this.importPrice = importPrice;
        this.batchCode = batchCode;
        this.expiryDate = expiryDate;
        this.conversionRatio = conversionRatio; // <-- THÊM DÒNG NÀY
    }

    public int getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getImportPrice() {
        return importPrice;
    }

    public String getBatchCode() {
        return batchCode;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public int getConversionRatio() {
        return conversionRatio;
    } // <-- THÊM HÀM GET NÀY
}
