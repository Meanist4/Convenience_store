package service.impl;

import service.*;
import entity.Product;
import entity.ProductUnit;
import repository.ProductRepository;
import repository.ProductUnitRepository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepo = new ProductRepository();
    private final ProductUnitRepository unitRepo = new ProductUnitRepository();
    

    @Override
    public List<Product> getAllProducts() throws SQLException {
        return productRepo.findAll();
    }

    @Override
    public List<Product> getProductsByStatus(String status) throws SQLException {
        return productRepo.findByStatus(status);
    }

    @Override
    public List<Product> getProductsByCategory(String category) throws SQLException {
        return productRepo.findByCategory(category);
    }

    @Override
    public Optional<Product> getProductById(int id) throws SQLException {
        return productRepo.findById(id);
    }

    @Override
    public Optional<Product> getProductByBarcode(String barcode) throws SQLException {
        return productRepo.findByBarcode(barcode);
    }

    @Override
    public List<Product> searchProducts(String keyword) throws SQLException {
        if (keyword == null || keyword.isBlank()) {
            return productRepo.findAll();
        }
        return productRepo.searchByName(keyword.trim());
    }

    @Override
    public boolean createProduct(Product product) throws SQLException {

        String sql = "INSERT INTO products (product_name, category, base_unit, import_price, markup_rate, image_name, status, is_deleted) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, 0)";

        // Thêm tham số Statement.RETURN_GENERATED_KEYS để lấy ID tự tăng từ DB
        try (Connection conn = util.DatabaseUtil.getConnection(); // Thay bằng cách lấy Connection của bạn
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, product.getProductName());
            ps.setString(2, product.getCategory());
            ps.setString(3, product.getBaseUnit());
            ps.setBigDecimal(4, product.getImportPrice());
            ps.setBigDecimal(5, product.getMarkupRate());
            ps.setString(6, product.getImageName()); // <-- Lưu chuẩn image_name vào DB
            ps.setString(7, product.getStatus());

            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                // 2. LẤY ID TỰ ĐỘNG TĂNG TỪ DATABASE VÀ GÁN VÀO ENTITY
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        product.setId(generatedKeys.getInt(1)); // Gán ID để tầng Service dùng tiếp
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public Product updateProduct(int id, String productName, String category, String baseUnit,
            BigDecimal importPrice, BigDecimal markupRate, String status) throws SQLException {

        Product p = productRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm id=" + id));

        p.setProductName(productName.trim());
        p.setCategory(category);
        p.setBaseUnit(baseUnit);
        p.setImportPrice(importPrice);
        p.setMarkupRate(markupRate);
        p.setStatus(status);

        if (!productRepo.update(p)) {
            throw new RuntimeException("Cập nhật sản phẩm thất bại");
        }
        return p;
    }

    @Override
    public void updatePricing(int id, BigDecimal importPrice, BigDecimal markupRate) throws SQLException {
        productRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm id=" + id));
        if (!productRepo.updatePricing(id, importPrice, markupRate)) {
            throw new RuntimeException("Cập nhật giá thất bại");
        }
    }

    @Override
    public void deleteProduct(int id) throws SQLException {
        if (!productRepo.delete(id)) {
            throw new IllegalArgumentException("Không tìm thấy sản phẩm hoặc đã bị xóa: id=" + id);
        }
    }

    @Override
    public List<ProductUnit> getUnitsByProduct(int productId) throws SQLException {
        return unitRepo.findByProductId(productId);
    }

    @Override
    public Optional<ProductUnit> getUnitById(int id) throws SQLException {
        return unitRepo.findById(id);
    }

    @Override
    public Optional<ProductUnit> getUnitByBarcode(String barcode) throws SQLException {
        return unitRepo.findByBarcode(barcode);
    }

    @Override
    public Optional<ProductUnit> getDefaultUnit(int productId) throws SQLException {
        return unitRepo.findDefaultByProductId(productId);
    }
    
    

    @Override
    public ProductUnit addUnit(int productId, String unitName, int ratio,
            String barcode, BigDecimal sellingPrice, boolean isDefault) throws SQLException, Exception { // <-- Thêm Exception ở đây

        // BỎ đoạn check productRepo.findById cũ gây chặn luồng dữ liệu mới
        if (barcode != null && unitRepo.findByBarcode(barcode).isPresent()) {
            throw new IllegalStateException("Barcode đã tồn tại: " + barcode);
        }

        ProductUnit u = new ProductUnit();
        u.setProductId(productId);
        u.setUnitName(unitName);
        u.setRatio(ratio);
        u.setBarcode(barcode);
        u.setSellingPrice(sellingPrice);
        u.setDefaultSale(isDefault);

        // Không nuốt lỗi nữa, nếu lỗi insert phải ném ra để giao diện hiển thị cho lập trình viên biết lỗi gì
        if (!unitRepo.insert(u)) {
            throw new RuntimeException("Thêm đơn vị phụ vào cơ sở dữ liệu thất bại!");
        }

        return u;
    }

    @Override
    public ProductUnit updateUnit(int id, String unitName, int ratio,
            String barcode, BigDecimal sellingPrice, boolean isDefault) throws SQLException {

        ProductUnit u = unitRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn vị id=" + id));

        if (barcode != null) {
            Optional<ProductUnit> sameBarcode = unitRepo.findByBarcode(barcode);
            if (sameBarcode.isPresent() && sameBarcode.get().getId() != id) {
                throw new IllegalStateException("Barcode đã tồn tại: " + barcode);
            }
        }

        u.setUnitName(unitName);
        u.setRatio(ratio);
        u.setBarcode(barcode);
        u.setSellingPrice(sellingPrice);
        u.setDefaultSale(isDefault);

        try {
            if (!unitRepo.update(u)) {
                throw new RuntimeException("Cập nhật đơn vị thất bại");
            }
        } catch (Exception ex) {
            System.getLogger(ProductServiceImpl.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        return u;
    }

    @Override
    public void updateUnitSellingPrice(int unitId, BigDecimal price) throws SQLException {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Giá bán không hợp lệ");
        }
        if (!unitRepo.updateSellingPrice(unitId, price)) {
            throw new RuntimeException("Cập nhật giá bán thất bại");
        }
    }

    @Override
    public void deleteUnit(int id) throws SQLException {
        if (!unitRepo.delete(id)) {
            throw new IllegalArgumentException("Không tìm thấy đơn vị hoặc đã bị xóa: id=" + id);
        }
    }
}
