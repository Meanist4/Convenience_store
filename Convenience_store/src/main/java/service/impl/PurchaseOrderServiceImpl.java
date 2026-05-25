package service.impl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import entity.PurchaseOrder;
import entity.PurchaseOrderDetail;
import entity.StoreInventory;
import repository.InventoryRepository;
import repository.InventoryRepositoryImpl;
import repository.PurchaseOrderDetailRepository;
import repository.PurchaseOrderRepository;
import repository.StoreRepository;
import repository.SupplierRepository;
import service.PurchaseOrderService;
import util.DatabaseUtil;

public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final PurchaseOrderRepository orderRepo = new PurchaseOrderRepository();
    private final PurchaseOrderDetailRepository detailRepo = new PurchaseOrderDetailRepository();
    private final SupplierRepository supplierRepo = new SupplierRepository();
    private final StoreRepository storeRepo = new StoreRepository();
    private final InventoryRepository inventoryRepo = new InventoryRepositoryImpl();

    @Override
    public PurchaseOrder createOrder(int supplierId, int storeId, Integer adminId,
            List<PurchaseOrderDetail> details) throws SQLException {
        validateDetails(details);

        supplierRepo.findById(supplierId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhà cung cấp id=" + supplierId));

        storeRepo.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy cửa hàng id=" + storeId));

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PurchaseOrderDetail detail : details) {
            normalizeDetail(detail);
            totalAmount = totalAmount.add(detail.getSubtotal());
        }

        PurchaseOrder order = new PurchaseOrder();
        order.setSupplierId(supplierId);
        order.setStoreId(storeId);
        order.setAdminId(adminId);
        order.setTotalAmount(totalAmount);
        order.setStatus("pending");

        Connection conn = DatabaseUtil.getConnection();
        boolean previousAutoCommit = conn.getAutoCommit();

        try {
            conn.setAutoCommit(false);

            int orderId = orderRepo.insert(order, conn);
            if (orderId <= 0) {
                throw new RuntimeException("Tạo đơn nhập thất bại");
            }

            order.setId(orderId);

            for (PurchaseOrderDetail detail : details) {
                detail.setPurchaseOrderId(orderId);
                if (!detailRepo.insert(detail, conn)) {
                    throw new RuntimeException("Tạo chi tiết đơn nhập thất bại");
                }
            }

            conn.commit();
            return order;
        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                e.addSuppressed(rollbackEx);
            }
            throw e;
        } finally {
            try {
                conn.setAutoCommit(previousAutoCommit);
            } catch (SQLException ignored) {
            }
            DatabaseUtil.closeConnection();
        }
    }

    @Override
    public PurchaseOrder getOrderById(int id) throws SQLException {
        return orderRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn nhập id=" + id));
    }

    @Override
    public List<PurchaseOrderDetail> getOrderDetails(int orderId) throws SQLException {
        return detailRepo.findByOrderId(orderId);
    }

    @Override
    public void updateOrderStatus(int orderId, String status) throws SQLException {
        if (!"pending".equals(status) && !"received".equals(status) && !"cancelled".equals(status)) {
            throw new IllegalArgumentException("Status không hợp lệ: " + status);
        }

        orderRepo.updateStatus(orderId, status);
    }

    @Override
    public void receiveOrder(int orderId) throws SQLException {
        PurchaseOrder order = getOrderById(orderId);

        if (!"pending".equals(order.getStatus())) {
            throw new IllegalStateException("Chỉ có thể nhận đơn ở trạng thái pending");
        }

        List<PurchaseOrderDetail> details = getOrderDetails(orderId);
        Connection conn = DatabaseUtil.getConnection();
        boolean previousAutoCommit = conn.getAutoCommit();

        try {
            conn.setAutoCommit(false);

            for (PurchaseOrderDetail detail : details) {
                StoreInventory newBatch = new StoreInventory();
                newBatch.setStoreId(order.getStoreId());
                newBatch.setProductId(detail.getProductId());
                newBatch.setQuantity(detail.getQuantity());
                newBatch.setMinStockLevel(5);
                newBatch.setBatchCode(detail.getBatchCode());
                newBatch.setImportPrice(detail.getImportPriceAtTime());

                if (detail.getExpiryDate() != null) {
                    newBatch.setExpiryDate(new java.sql.Date(detail.getExpiryDate().getTime()));
                }
                newBatch.setReceivedAt(new java.sql.Date(System.currentTimeMillis()));

                if (!inventoryRepo.insert(newBatch)) {
                    throw new RuntimeException(
                            "Tạo lô tồn kho mới cho sản phẩm ID " + detail.getProductId() + " thất bại");
                }
            }

            orderRepo.updateStatus(orderId, "received", conn);
            conn.commit();
        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                e.addSuppressed(rollbackEx);
            }
            throw e;
        } finally {
            try {
                conn.setAutoCommit(previousAutoCommit);
            } catch (SQLException ignored) {
            }
            DatabaseUtil.closeConnection();
        }
    }

    @Override
    public void cancelOrder(int orderId) throws SQLException {
        PurchaseOrder order = getOrderById(orderId);

        if ("received".equals(order.getStatus())) {
            throw new IllegalStateException("Không thể hủy đơn đã nhận");
        }

        orderRepo.updateStatus(orderId, "cancelled");
    }

    @Override
    public void addDetailToOrder(int orderId, PurchaseOrderDetail detail) throws SQLException {
        PurchaseOrder order = getOrderById(orderId);

        if (!"pending".equals(order.getStatus())) {
            throw new IllegalStateException("Chỉ có thể thêm chi tiết khi đơn ở trạng thái pending");
        }

        normalizeDetail(detail);
        detail.setPurchaseOrderId(orderId);

        if (!detailRepo.insert(detail)) {
            throw new RuntimeException("Thêm chi tiết đơn thất bại");
        }

        BigDecimal newTotal = detailRepo.sumSubtotalByOrderId(orderId);
        orderRepo.updateTotal(orderId, newTotal);
    }

    @Override
    public void removeDetailFromOrder(int detailId) throws SQLException {
        PurchaseOrderDetail detail = detailRepo.findById(detailId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chi tiết đơn id=" + detailId));

        PurchaseOrder order = getOrderById(detail.getPurchaseOrderId());
        if (!"pending".equals(order.getStatus())) {
            throw new IllegalStateException("Chỉ có thể xóa chi tiết khi đơn ở trạng thái pending");
        }

        if (!detailRepo.delete(detailId)) {
            throw new RuntimeException("Xóa chi tiết đơn thất bại");
        }

        BigDecimal newTotal = detailRepo.sumSubtotalByOrderId(order.getId());
        orderRepo.updateTotal(order.getId(), newTotal);
    }

    private void validateDetails(List<PurchaseOrderDetail> details) {
        if (details == null || details.isEmpty()) {
            throw new IllegalArgumentException("Đơn nhập phải có ít nhất một sản phẩm");
        }

        for (PurchaseOrderDetail detail : details) {
            if (detail == null) {
                throw new IllegalArgumentException("Chi tiết đơn nhập không được null");
            }
            if (detail.getProductId() <= 0) {
                throw new IllegalArgumentException("Sản phẩm không hợp lệ");
            }
            if (detail.getQuantity() <= 0) {
                throw new IllegalArgumentException("Số lượng phải > 0");
            }
        }
    }

    private void normalizeDetail(PurchaseOrderDetail detail) {
        if (detail.getImportPriceAtTime() == null || detail.getImportPriceAtTime().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Giá nhập không hợp lệ");
        }

        BigDecimal subtotal = detail.getImportPriceAtTime().multiply(BigDecimal.valueOf(detail.getQuantity()));
        detail.setSubtotal(subtotal);
    }
}
