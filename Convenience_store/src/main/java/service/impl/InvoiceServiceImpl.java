package service.impl;

import service.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import entity.Invoice;
import entity.InvoiceDetail;
import entity.ProductUnit;
import entity.StoreInventory;
import exception.NotFoundException;
import exception.ValidationException;
import repository.InventoryRepository;
import repository.InventoryRepositoryImpl;
import repository.InvoiceDetailRepository;
import repository.InvoiceRepository;
import util.BarcodeUtil;

public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepo = new InvoiceRepository();
    private final InvoiceDetailRepository detailRepo = new InvoiceDetailRepository();
    private final InventoryRepository inventoryRepo = new InventoryRepositoryImpl();
    private final InventoryService inventoryService = new InventoryServiceImpl();

    @Override
    public List<Invoice> getAllInvoices() throws SQLException {
        return invoiceRepo.findAll();
    }

    @Override
    public Optional<Invoice> getInvoiceById(int id) throws SQLException {
        return invoiceRepo.findById(id);
    }

    @Override
    public List<Invoice> getInvoicesByStore(int storeId) throws SQLException {
        return invoiceRepo.findByStoreId(storeId);
    }

    @Override
    public List<Invoice> getInvoicesByEmployee(int employeeId) throws SQLException {
        return invoiceRepo.findByEmployeeId(employeeId);
    }

    @Override
    public List<Invoice> getInvoicesByDateRange(Timestamp from, Timestamp to) throws SQLException {
        if (from.after(to)) {
            throw new IllegalArgumentException("Ngày bắt đầu phải trước ngày kết thúc");
        }
        return invoiceRepo.findByDateRange(from, to);
    }

    @Override
    public List<InvoiceDetail> getDetailsByInvoice(int invoiceId) throws SQLException {
        return detailRepo.findByInvoiceId(invoiceId);
    }

    @Override
    public List<InvoiceDetail> getDetailsByProduct(int productId) throws SQLException {
        return detailRepo.findByProductId(productId);
    }

    @Override
    public BigDecimal getRevenue(int storeId, Timestamp from, Timestamp to) throws SQLException {
        return invoiceRepo.sumRevenueByStore(storeId, from, to);
    }

    @Override
    public Invoice createInvoice(int storeId, int employeeId, List<InvoiceDetail> details) throws SQLException {
        if (details == null || details.isEmpty()) {
            throw new IllegalArgumentException("Hóa đơn phải có ít nhất một sản phẩm");
        }

        BigDecimal total = details.stream()
                .map(InvoiceDetail::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Invoice inv = new Invoice();
        inv.setStoreId(storeId);
        inv.setEmployeeId(employeeId);
        inv.setTotalAmount(total);
        inv.setStatus("completed");

        Connection conn = util.DatabaseUtil.getConnection();
        boolean previousAutoCommit = conn.getAutoCommit();
        PreparedStatement psGetRatio = null;

        try {
            conn.setAutoCommit(false);

            if (!invoiceRepo.insert(inv, conn)) {
                throw new RuntimeException("Tạo hóa đơn thất bại");
            }

            // Chuẩn bị SQL tìm tỷ lệ quy đổi của đơn vị để trừ kho chính xác
            psGetRatio = conn.prepareStatement("SELECT ratio FROM product_units WHERE id = ? LIMIT 1");

            for (InvoiceDetail detail : details) {
                detail.setInvoiceId(inv.getId());

                // Tìm hệ số quy đổi của đơn vị tính được chọn trên giao diện
                int unitRatio = 1;
                psGetRatio.setInt(1, detail.getUnitId());
                try (ResultSet rs = psGetRatio.executeQuery()) {
                    if (rs.next()) {
                        unitRatio = rs.getInt("ratio");
                    }
                }

                // Chuyển đổi số lượng từ hóa đơn (Ví dụ: 2 Thùng) thành số lượng hạt nhân (48
                // Chai)
                int requiredQuantityInBaseUnit = detail.getQuantity() * unitRatio;

                // Gọi hàm bẻ lô FEFO trừ theo đơn vị hạt nhân lẻ
                inventoryService.deductStockFEFO(
                        storeId,
                        detail.getProductId(),
                        requiredQuantityInBaseUnit,
                        inv.getId(),
                        detail.getUnitId(),
                        detail.getPriceAtSale());
            }

            conn.commit();
            return inv;
        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                e.addSuppressed(rollbackEx);
            }
            throw e;
        } finally {
            try {
                if (psGetRatio != null) {
                    psGetRatio.close();
                }
            } catch (SQLException ignored) {
            }
            try {
                conn.setAutoCommit(previousAutoCommit);
            } catch (SQLException ignored) {
            }
            util.DatabaseUtil.closeConnection();
        }
    }

    @Override
    public void cancelInvoice(int id) throws SQLException {
        invoiceRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hóa đơn id=" + id));
        if (!invoiceRepo.cancelInvoice(id)) {
            throw new IllegalStateException("Hóa đơn đã hủy hoặc không thể hủy");
        }
    }

    @Override
    public void updateTotalAmount(int id, BigDecimal totalAmount) throws SQLException {
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Tổng tiền không hợp lệ");
        }
        if (!invoiceRepo.updateTotalAmount(id, totalAmount)) {
            throw new RuntimeException("Cập nhật tổng tiền thất bại");
        }
    }

    @Override
    public Invoice processSale(int storeId, int employeeId, String barcode, int quantity) throws SQLException {
        if (quantity <= 0) {
            throw new ValidationException("Số lượng phải > 0");
        }

        // 1. Quét mã vạch để tìm thông tin đơn vị tính (Unit) đang bán
        Optional<ProductUnit> unitOpt = BarcodeUtil.scanProduct(barcode);
        if (unitOpt.isEmpty()) {
            throw new NotFoundException("Không tìm thấy sản phẩm với barcode: " + barcode);
        }
        ProductUnit unit = unitOpt.get();
        int productId = unit.getProductId();
        int unitRatio = unit.getRatio(); // Lấy hệ số quy đổi động (Ví dụ: Thùng = 24, Chai = 1)

        // 2. Quy đổi số lượng khách mua ra số lượng đơn vị nhỏ nhất (Hạt nhân)
        int totalRequiredQuantityInBaseUnit = quantity * unitRatio;

        // 3. ĐÃ SỬA: Tính TỔNG số lượng tồn kho thực tế của TẤT CẢ CÁC LÔ cộng lại
        int totalAvailableStock = 0;
        Connection conn = util.DatabaseUtil.getConnection();
        boolean previousAutoCommit = conn.getAutoCommit();

        String sqlSumStock = "SELECT SUM(quantity) FROM store_inventory WHERE store_id = ? AND product_id = ?";
        try (PreparedStatement psSum = conn.prepareStatement(sqlSumStock)) {
            psSum.setInt(1, storeId);
            psSum.setInt(2, productId);
            try (ResultSet rsSum = psSum.executeQuery()) {
                if (rsSum.next()) {
                    totalAvailableStock = rsSum.getInt(1); // Lấy tổng lượng SUM của các lô
                }
            }
        }

        // Kiểm tra xem tổng kho hạt nhân có đủ đáp ứng đơn hàng không
        if (totalAvailableStock < totalRequiredQuantityInBaseUnit) {
            throw new IllegalStateException("Không đủ tồn kho thực tế cho sản phẩm. Yêu cầu quy đổi hạt nhân: "
                    + totalRequiredQuantityInBaseUnit + " lẻ, Hiện có tổng cộng tất cả các lô: " + totalAvailableStock
                    + " lẻ");
        }

        // 4. Tính toán tổng tiền hóa đơn
        BigDecimal subtotal = unit.getSellingPrice().multiply(BigDecimal.valueOf(quantity));

        Invoice invoice = new Invoice();
        invoice.setStoreId(storeId);
        invoice.setEmployeeId(employeeId);
        invoice.setTotalAmount(subtotal);
        invoice.setStatus("completed");

        // 5. Thực hiện Transaction lưu hóa đơn và bẻ lô kho bằng FEFO
        try {
            conn.setAutoCommit(false);

            // Chèn hóa đơn vào DB
            if (!invoiceRepo.insert(invoice, conn)) {
                throw new RuntimeException("Tạo hóa đơn thất bại");
            }

            // Ghi nhận chi tiết hóa đơn và bẻ nhỏ lô hàng theo thuật toán FEFO cận đát xuất
            // trước
            inventoryService.deductStockFEFO(
                    storeId,
                    productId,
                    totalRequiredQuantityInBaseUnit, // Đẩy số lượng đã quy đổi hạt nhân nguyên vẹn vào đây
                    invoice.getId(),
                    unit.getId(),
                    unit.getSellingPrice());

            conn.commit();
            return invoice;
        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                e.addSuppressed(rollbackEx);
            }
            throw new SQLException(
                    "Quá trình xử lý bán hàng thất bại, hệ thống đã khôi phục trạng thái kho: " + e.getMessage(), e);
        } finally {
            try {
                conn.setAutoCommit(previousAutoCommit);
            } catch (SQLException ignored) {
            }
            util.DatabaseUtil.closeConnection();
        }
    }
}
