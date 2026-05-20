package entity;

public class StoreInventory {
    private int storeId;
    private int productId;
    private int quantity;
    private int minStockLevel = 5;

    public StoreInventory() {
    }

    public StoreInventory(int storeId, int productId, int quantity) {
        this.storeId = storeId;
        this.productId = productId;
        this.quantity = quantity;
    }

    public StoreInventory(int storeId, int productId, int quantity, int minStockLevel) {
        this.storeId = storeId;
        this.productId = productId;
        this.quantity = quantity;
        this.minStockLevel = minStockLevel;
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof StoreInventory))
            return false;
        StoreInventory that = (StoreInventory) o;
        return storeId == that.storeId && productId == that.productId;
    }

    @Override
    public int hashCode() {
        int result = storeId;
        result = 31 * result + productId;
        return result;
    }

    @Override
    public String toString() {
        return "StoreInventory{" +
                "storeId=" + storeId +
                ", productId=" + productId +
                ", quantity=" + quantity +
                ", minStockLevel=" + minStockLevel +
                '}';
    }
}
