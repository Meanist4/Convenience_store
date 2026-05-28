package service.impl;

import entity.Supplier;
import repository.SupplierRepository;
import service.SupplierService;

import java.sql.SQLException;
import java.util.List;

public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepo = new SupplierRepository();

    @Override
    public List<Supplier> getAllActiveSuppliers() throws SQLException {
        return supplierRepo.findAll();
    }
}
