package entity;

import java.math.BigDecimal;

public class InvoiceDetail {

    private int id;
    private int invoiceId; // Sẽ set sau khi lấy được Generated Key từ cụm 1
    private int productId;
    private int unitId;
    private int quantity;
    private BigDecimal priceAtSale;
    private BigDecimal subtotal;

    public InvoiceDetail() {
    }

    public InvoiceDetail(int id, int invoiceId, int productId, int unitId, int quantity, BigDecimal priceAtSale, BigDecimal subtotal) {
        this.id = id;
        this.invoiceId = invoiceId;
        this.productId = productId;
        this.unitId = unitId;
        this.quantity = quantity;
        this.priceAtSale = priceAtSale;
        this.subtotal = subtotal;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getUnitId() {
        return unitId;
    }

    public void setUnitId(int unitId) {
        this.unitId = unitId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPriceAtSale() {
        return priceAtSale;
    }

    public void setPriceAtSale(BigDecimal priceAtSale) {
        this.priceAtSale = priceAtSale;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    @Override
    public String toString() {
        return "InvoiceDetail{" + "id=" + id + ", invoiceId=" + invoiceId + ", productId=" + productId + ", unitId=" + unitId + ", quantity=" + quantity + ", priceAtSale=" + priceAtSale + ", subtotal=" + subtotal + '}';
    }

}
