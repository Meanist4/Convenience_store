package service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import entity.Invoice;
import entity.InvoiceDetail;
import entity.ProductUnit;
import entity.StoreInventory;
import repository.InventoryRepository;
import repository.InvoiceDetailRepository;
import repository.InvoiceRepository;
import repository.ProductUnitRepository;

public class InvoiceService {

    private final InvoiceRepository invoiceRepo = new InvoiceRepository();
    private final InvoiceDetailRepository detailRepo = new InvoiceDetailRepository();
    private final ProductUnitRepository unitRepo = new ProductUnitRepository();
    private final InventoryRepository inventoryRepo = new InventoryRepository();

    public List<Invoice> getAllInvoices() throws SQLException {
        return invoiceRepo.findAll();
    }

    public Optional<Invoice> getInvoiceById(int id) throws SQLException {
        return invoiceRepo.findById(id);
    }

    public List<Invoice> getInvoicesByStore(int storeId) throws SQLException {
        return invoiceRepo.findByStoreId(storeId);
    }

    public List<Invoice> getInvoicesByEmployee(int employeeId) throws SQLException {
        return invoiceRepo.findByEmployeeId(employeeId);
    }

    public List<Invoice> getInvoicesByDateRange(Timestamp from, Timestamp to) throws SQLException {
        if (from.after(to))
            throw new IllegalArgumentException("Ngày bắt đầu phải trước ngày kết thúc");
        return invoiceRepo.findByDateRange(from, to);
    }

    public List<InvoiceDetail> getDetailsByInvoice(int invoiceId) throws SQLException {
        return detailRepo.findByInvoiceId(invoiceId);
    }

    public List<InvoiceDetail> getDetailsByProduct(int productId) throws SQLException {
        return detailRepo.findByProductId(productId);
    }

    public BigDecimal getRevenue(int storeId, Timestamp from, Timestamp to) throws SQLException {
        return invoiceRepo.sumRevenueByStore(storeId, from, to);
    }

    public Invoice createInvoice(int storeId, int employeeId, List<InvoiceDetail> details) throws SQLException {
        if (details == null || details.isEmpty())
            throw new IllegalArgumentException("Hóa đơn phải có ít nhất một sản phẩm");

        BigDecimal total = details.stream()
                .map(InvoiceDetail::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Invoice inv = new Invoice();
        inv.setStoreId(storeId);
        inv.setEmployeeId(employeeId);
        inv.setTotalAmount(total);
        inv.setStatus("completed");

        if (!invoiceRepo.insert(inv))
            throw new RuntimeException("Tạo hóa đơn thất bại");

        for (InvoiceDetail d : details) {
            d.setInvoiceId(inv.getId());
            detailRepo.insert(d);
        }
        return inv;
    }

    public void cancelInvoice(int id) throws SQLException {
        invoiceRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hóa đơn id=" + id));
        if (!invoiceRepo.cancelInvoice(id))
            throw new IllegalStateException("Hóa đơn đã hủy hoặc không thể hủy");
    }

    public void updateTotalAmount(int id, BigDecimal totalAmount) throws SQLException {
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Tổng tiền không hợp lệ");
        if (!invoiceRepo.updateTotalAmount(id, totalAmount))
            throw new RuntimeException("Cập nhật tổng tiền thất bại");
    }

    public Invoice processSale(int storeId, int employeeId, String barcode, int quantity) throws SQLException {
        if (quantity <= 0)
            throw new IllegalArgumentException("Số lượng phải > 0");

        Optional<ProductUnit> unitOpt = unitRepo.findByBarcode(barcode);
        if (unitOpt.isEmpty())
            throw new IllegalArgumentException("Không tìm thấy sản phẩm với barcode: " + barcode);

        ProductUnit unit = unitOpt.get();

        // Kiểm tra tồn kho
        Optional<StoreInventory> invOpt = inventoryRepo.findByStoreAndProduct(storeId, unit.getProductId());
        if (invOpt.isEmpty() || invOpt.get().getQuantity() < quantity)
            throw new IllegalStateException("Không đủ tồn kho cho sản phẩm ID " + unit.getProductId());

        BigDecimal subtotal = unit.getSellingPrice().multiply(BigDecimal.valueOf(quantity));

        Invoice invoice = new Invoice();
        invoice.setStoreId(storeId);
        invoice.setEmployeeId(employeeId);
        invoice.setTotalAmount(subtotal);
        invoice.setStatus("completed");

        if (!invoiceRepo.insert(invoice))
            throw new RuntimeException("Tạo hóa đơn thất bại");

        InvoiceDetail detail = new InvoiceDetail();
        detail.setInvoiceId(invoice.getId());
        detail.setProductId(unit.getProductId());
        detail.setUnitId(unit.getId());
        detail.setQuantity(quantity);
        detail.setPriceAtSale(unit.getSellingPrice());
        detail.setSubtotal(subtotal);

        if (!detailRepo.insert(detail))
            throw new RuntimeException("Thêm chi tiết hóa đơn thất bại");

        // Cập nhật tồn kho
        inventoryRepo.adjustQuantity(storeId, unit.getProductId(), -quantity);

        return invoice;
    }
}
