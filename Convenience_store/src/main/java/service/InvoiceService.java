package service;

import entity.Invoice;
import entity.InvoiceDetail;
import repository.InvoiceDetailRepository;
import repository.InvoiceRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public class InvoiceService {

    private final InvoiceRepository invoiceRepo = new InvoiceRepository();
    private final InvoiceDetailRepository detailRepo = new InvoiceDetailRepository();

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
}
