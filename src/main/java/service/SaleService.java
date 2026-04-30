package service;

import repository.ProductRepository;
import repository.InventoryRepository;
import repository.OrderRepository;
import java.math.BigDecimal;
import java.util.List;

public class SaleService {

    private final ProductRepository productRepo = new ProductRepository();
    private final InventoryRepository inventoryRepo = new InventoryRepository();
    private final OrderRepository orderRepository = new OrderRepository();

    public void processSale(int storeId, String barcode, int quantityToSell) {
        Object[] productData = productRepo.getProductByBarCode(barcode);

        if (productData == null) {
            System.out.println("Sản phẩm không tồn tại!");
            return;
        }

        int productId = (int) productData[0];
        int ratio = (int) productData[4];
        int totalBaseQuantity = quantityToSell * ratio; 

        int currentStock = inventoryRepo.getStock(storeId, productId);
        if (currentStock < totalBaseQuantity) {
            System.out.println("Không đủ hàng! Tồn kho hiện tại: " + currentStock);
            return;
        }

        if (inventoryRepo.updateStock(storeId, productId, -totalBaseQuantity)) {
            System.out.println("Bán hàng thành công. Đã trừ kho!");
        }
    }
    
    public void checkout(int storeId, int employeeId, BigDecimal totalAmount, List<Object[]> cartItems) {
        if (cartItems.isEmpty()) {
            System.out.println("Giỏ hàng trống!");
            return;
        }

        boolean success = orderRepository.createOrder(storeId, employeeId, totalAmount, cartItems);
        
        if (success) {
            System.out.println("Thanh toán thành công! Hóa đơn đã được lưu và kho đã trừ.");
        } else {
            System.out.println("Thanh toán thất bại! Vui lòng kiểm tra lại hệ thống hoặc tồn kho.");
        }
    }
}
