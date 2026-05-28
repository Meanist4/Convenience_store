package service;

import entity.Supplier;
import java.sql.SQLException;
import java.util.List;

public interface SupplierService {
    List<Supplier> getAllActiveSuppliers() throws SQLException;
}
