package entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Product {

    private int id;
    private String productName;
    private String category;
    private String baseUnit;
    private BigDecimal importPrice;
    private BigDecimal markupRate;
    private List<ProductUnit> productUnits = new ArrayList<>();

    public Product() {
    }

    public Product(int id, String productName, String category, String baseUnit,
            BigDecimal importPrice, BigDecimal markupRate) {
        this.id = id;
        this.productName = productName;
        this.category = category;
        this.baseUnit = baseUnit;
        this.importPrice = importPrice;
        this.markupRate = markupRate;
    }

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

    public List<ProductUnit> getProductUnits() {
        return productUnits;
    }

    public void setProductUnits(List<ProductUnit> productUnits) {
        this.productUnits = productUnits;
    }

    @Override
    public String toString() {
        return "Product{id=" + id + ", productName='" + productName + "', category='" + category + "', baseUnit='" + baseUnit + "'}";
    }
}
