package service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import entity.PurchaseOrder;
import entity.PurchaseOrderDetail;
import repository.InventoryRepository;
import repository.PurchaseOrderDetailRepository;
import repository.PurchaseOrderRepository;
import repository.StoreRepository;
import repository.SupplierRepository;

public class PurchaseOrderService {

    private final PurchaseOrderRepository orderRepo = new PurchaseOrderRepository();
    private final PurchaseOrderDetailRepository detailRepo = new PurchaseOrderDetailRepository();
    private final SupplierRepository supplierRepo = new SupplierRepository();
    private final StoreRepository storeRepo = new StoreRepository();
    private final InventoryRepository inventoryRepo = new InventoryRepository();

    public PurchaseOrder createOrder(int supplierId, int storeId, Integer adminId,
            List<PurchaseOrderDetail> details) throws SQLException {

        if (details == null || details.isEmpty())
            throw new IllegalArgumentException("Đơn nhập phải có ít nhất một sản phẩm");

        supplierRepo.findById(supplierId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhà cung cấp id=" + supplierId));

        storeRepo.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy cửa hàng id=" + storeId));

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PurchaseOrderDetail d : details) {
            if (d.getQuantity() <= 0)
                throw new IllegalArgumentException("Số lượng phải > 0");
            if (d.getImportPriceAtTime() == null || d.getImportPriceAtTime().compareTo(BigDecimal.ZERO) < 0)
                throw new IllegalArgumentException("Giá nhập không hợp lệ");
            totalAmount = totalAmount.add(d.getSubtotal());
        }

        PurchaseOrder order = new PurchaseOrder();
        order.setSupplierId(supplierId);
        order.setStoreId(storeId);
        order.setAdminId(adminId);
        order.setTotalAmount(totalAmount);
        order.setStatus("pending");

        int orderId = orderRepo.insert(order);
        if (orderId <= 0)
            throw new RuntimeException("Tạo đơn nhập thất bại");

        order.setId(orderId);

        for (PurchaseOrderDetail d : details) {
            d.setPurchaseOrderId(orderId);
            detailRepo.insert(d);
        }

        return order;
    }

    public PurchaseOrder getOrderById(int id) throws SQLException {
        return orderRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn nhập id=" + id));
    }

    public List<PurchaseOrderDetail> getOrderDetails(int orderId) throws SQLException {
        return detailRepo.findByOrderId(orderId);
    }

    public void updateOrderStatus(int orderId, String status) throws SQLException {
        if (!status.equals("pending") && !status.equals("received") && !status.equals("cancelled"))
            throw new IllegalArgumentException("Status không hợp lệ: " + status);

        orderRepo.updateStatus(orderId, status);
    }

    public void receiveOrder(int orderId) throws SQLException {
        PurchaseOrder order = getOrderById(orderId);

        if (!order.getStatus().equals("pending"))
            throw new IllegalStateException("Chỉ có thể nhận đơn ở trạng thái pending");

        List<PurchaseOrderDetail> details = getOrderDetails(orderId);

        for (PurchaseOrderDetail d : details) {
            Optional<entity.StoreInventory> invOpt = inventoryRepo.findByStoreAndProduct(order.getStoreId(),
                    d.getProductId());

            if (invOpt.isPresent()) {
                entity.StoreInventory inv = invOpt.get();
                inventoryRepo.adjustQuantity(order.getStoreId(), d.getProductId(), d.getQuantity());
            } else {
                entity.StoreInventory newInv = new entity.StoreInventory(order.getStoreId(), d.getProductId(),
                        d.getQuantity());
                inventoryRepo.insert(newInv);
            }
        }

        orderRepo.updateStatus(orderId, "received");
    }

    public void cancelOrder(int orderId) throws SQLException {
        PurchaseOrder order = getOrderById(orderId);

        if (order.getStatus().equals("received"))
            throw new IllegalStateException("Không thể hủy đơn đã nhận");

        orderRepo.updateStatus(orderId, "cancelled");
    }

    public void addDetailToOrder(int orderId, PurchaseOrderDetail detail) throws SQLException {
        PurchaseOrder order = getOrderById(orderId);

        if (!order.getStatus().equals("pending"))
            throw new IllegalStateException("Chỉ có thể thêm chi tiết khi đơn ở trạng thái pending");

        if (detail.getQuantity() <= 0)
            throw new IllegalArgumentException("Số lượng phải > 0");

        detail.setPurchaseOrderId(orderId);
        if (!detailRepo.insert(detail))
            throw new RuntimeException("Thêm chi tiết đơn thất bại");

        // Cập nhật total amount
        BigDecimal newTotal = detailRepo.sumSubtotalByOrderId(orderId);
        orderRepo.updateStatus(orderId, order.getStatus()); // Update vẫn gọi hàm này để keep logic
    }

    public void removeDetailFromOrder(int detailId) throws SQLException {
        detailRepo.delete(detailId);
    }
}
