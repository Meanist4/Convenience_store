package service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import entity.Invoice;
import entity.InvoiceDetail;
import exception.NotFoundException;
import exception.ValidationException;

public interface InvoiceService {
    List<Invoice> getAllInvoices() throws SQLException;
    Optional<Invoice> getInvoiceById(int id) throws SQLException;
    List<Invoice> getInvoicesByStore(int storeId) throws SQLException;
    List<Invoice> getInvoicesByEmployee(int employeeId) throws SQLException;
    List<Invoice> getInvoicesByDateRange(Timestamp from, Timestamp to) throws SQLException;
    List<InvoiceDetail> getDetailsByInvoice(int invoiceId) throws SQLException;
    List<InvoiceDetail> getDetailsByProduct(int productId) throws SQLException;
    BigDecimal getRevenue(int storeId, Timestamp from, Timestamp to) throws SQLException;
    Invoice createInvoice(int storeId, int employeeId, List<InvoiceDetail> details) throws SQLException;
    void cancelInvoice(int id) throws SQLException;
    void updateTotalAmount(int id, BigDecimal totalAmount) throws SQLException;
    Invoice processSale(int storeId, int employeeId, String barcode, int quantity) throws SQLException, NotFoundException, ValidationException;
}
