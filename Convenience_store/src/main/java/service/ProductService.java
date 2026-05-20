package service;

import entity.Product;
import entity.ProductUnit;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ProductService {

    List<Product> getAllProducts() throws SQLException;

    List<Product> getProductsByStatus(String status) throws SQLException;

    List<Product> getProductsByCategory(String category) throws SQLException;

    Optional<Product> getProductById(int id) throws SQLException;

    Optional<Product> getProductByBarcode(String barcode) throws SQLException;

    List<Product> searchProducts(String keyword) throws SQLException;

    boolean createProduct(Product product) throws SQLException;

    Product updateProduct(int id, String productName, String category, String baseUnit, BigDecimal importPrice, BigDecimal markupRate, String status) throws SQLException;

    void updatePricing(int id, BigDecimal importPrice, BigDecimal markupRate) throws SQLException;

    void deleteProduct(int id) throws SQLException;

    List<ProductUnit> getUnitsByProduct(int productId) throws SQLException;

    Optional<ProductUnit> getUnitById(int id) throws SQLException;

    Optional<ProductUnit> getUnitByBarcode(String barcode) throws SQLException;

    Optional<ProductUnit> getDefaultUnit(int productId) throws SQLException;

    ProductUnit addUnit(int productId, String unitName, int ratio,
            String barcode, BigDecimal sellingPrice, boolean isDefault) throws SQLException, Exception;

    ProductUnit updateUnit(int id, String unitName, int ratio, String barcode, BigDecimal sellingPrice, boolean isDefault) throws SQLException;

    void updateUnitSellingPrice(int unitId, BigDecimal price) throws SQLException;

    void deleteUnit(int id) throws SQLException;
}
