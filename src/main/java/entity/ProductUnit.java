package entity;

import java.math.BigDecimal;

public class ProductUnit {

    private int id;
    private int productId;
    private String unitName;
    private Integer ratio;
    private String barcode;
    private BigDecimal sellingPrice;
    private Boolean isDefaultSale;

    public ProductUnit() {
    }

    public ProductUnit(int id, int productId, String unitName, Integer ratio,
            String barcode, BigDecimal sellingPrice, Boolean isDefaultSale) {
        this.id = id;
        this.productId = productId;
        this.unitName = unitName;
        this.ratio = ratio;
        this.barcode = barcode;
        this.sellingPrice = sellingPrice;
        this.isDefaultSale = isDefaultSale;
    }

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

    public Integer getRatio() {
        return ratio;
    }

    public void setRatio(Integer ratio) {
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

    public Boolean getIsDefaultSale() {
        return isDefaultSale;
    }

    public void setIsDefaultSale(Boolean isDefaultSale) {
        this.isDefaultSale = isDefaultSale;
    }

    @Override
    public String toString() {
        return "ProductUnit{"
                + "id=" + id
                + ", unitName='" + unitName + '\''
                + ", ratio=" + ratio
                + ", barcode='" + barcode + '\''
                + ", sellingPrice=" + sellingPrice
                + '}';
    }
}
