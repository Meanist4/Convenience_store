package service.impl;

import service.*;
import entity.Invoice;
import entity.InvoiceDetail;
import entity.ProductUnit;
import repository.InventoryRepository;
import repository.InventoryRepositoryImpl;
import repository.OrderRepository;
import repository.ProductUnitRepository;
import repository.StoreRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepo = new OrderRepository();
    private final ProductUnitRepository unitRepo = new ProductUnitRepository();
    private final InventoryRepository inventoryRepo = new InventoryRepositoryImpl();
    private final StoreRepository storeRepo = new StoreRepository();

    @Override
    public Invoice placeOrder(int storeId, int employeeId, List<OrderService.CartItem> cart) throws SQLException {
        if (cart == null || cart.isEmpty())
            throw new IllegalArgumentException("Giỏ hàng trống");
        storeRepo.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy cửa hàng id=" + storeId));
        List<InvoiceDetail> details = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (OrderService.CartItem item : cart) {
            if (item.quantity <= 0)
                throw new IllegalArgumentException("Số lượng phải > 0");
            ProductUnit unit = unitRepo.findById(item.unitId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn vị id=" + item.unitId));
            int baseQty = unit.getRatio() * item.quantity;
            inventoryRepo.findByStoreAndProduct(storeId, unit.getProductId())
                    .filter(inv -> inv.getQuantity() >= baseQty)
                    .orElseThrow(() -> new IllegalStateException("Không đủ tồn kho cho sản phẩm id="
                            + unit.getProductId() + " (cần " + baseQty + " base unit)"));
            BigDecimal subtotal = unit.getSellingPrice().multiply(BigDecimal.valueOf(item.quantity));
            total = total.add(subtotal);
            InvoiceDetail d = new InvoiceDetail();
            d.setProductId(unit.getProductId());
            d.setUnitId(item.unitId);
            d.setQuantity(item.quantity);
            d.setPriceAtSale(unit.getSellingPrice());
            d.setSubtotal(subtotal);
            details.add(d);
        }
        Invoice invoice = new Invoice();
        invoice.setStoreId(storeId);
        invoice.setEmployeeId(employeeId);
        invoice.setTotalAmount(total);
        orderRepo.createOrder(invoice, details);
        return invoice;
    }

    @Override
    public void cancelOrder(int invoiceId, int storeId) throws SQLException {
        storeRepo.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy cửa hàng id=" + storeId));
        orderRepo.cancelOrder(invoiceId, storeId);
    }

    @Override
    public BigDecimal getMonthlyRevenue(int storeId, int year, int month) throws SQLException {
        if (month < 1 || month > 12)
            throw new IllegalArgumentException("Tháng không hợp lệ: " + month);
        return orderRepo.getMonthlyRevenue(storeId, year, month);
    }

    @Override
    public OrderService.CartItem buildCartItemFromBarcode(String barcode, int quantity) throws SQLException {
        ProductUnit unit = unitRepo.findByBarcode(barcode)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy barcode: " + barcode));
        return new OrderService.CartItem(unit.getId(), quantity);
    }
}
