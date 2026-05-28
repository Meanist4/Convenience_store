package entity;

import java.math.BigDecimal;

public class PendingProductEntry {

    public int productId;
    public String barcode;
    public String productName;
    public int quantity;
    public int ratio = 1;
    public BigDecimal importPrice;
    public BigDecimal sellingPrice;
    public BigDecimal markupRate;
    public String category;
    public String baseUnit;
    public String imageName;
}
