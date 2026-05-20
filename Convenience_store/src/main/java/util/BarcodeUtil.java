package util;

import java.sql.SQLException;
import java.util.Optional;

import entity.Employee;
import entity.ProductUnit;
import repository.EmployeeRepository;
import repository.ProductUnitRepository;

public class BarcodeUtil {

    private static final EmployeeRepository employeeRepo = new EmployeeRepository();
    private static final ProductUnitRepository unitRepo = new ProductUnitRepository();

    public static Optional<Employee> scanEmployee(String barcode) throws SQLException {
        return employeeRepo.findByBarcode(barcode);
    }

    public static Optional<ProductUnit> scanProduct(String barcode) throws SQLException {
        return unitRepo.findByBarcode(barcode);
    }
}