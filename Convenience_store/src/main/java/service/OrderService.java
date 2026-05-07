package service;

import entity.Invoice;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public interface OrderService {
    public static class CartItem {
        public int unitId; // ProductUnit.id (xác định đơn vị bán và barcode)
        public int quantity; // số lượng theo đơn vị bán

        public CartItem(int unitId, int quantity) {
            this.unitId = unitId;
            this.quantity = quantity;
        }
    }

    Invoice placeOrder(int storeId, int employeeId, List<CartItem> cart) throws SQLException;
    void cancelOrder(int invoiceId, int storeId) throws SQLException;
    BigDecimal getMonthlyRevenue(int storeId, int year, int month) throws SQLException;
    CartItem buildCartItemFromBarcode(String barcode, int quantity) throws SQLException;
}
