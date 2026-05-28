package util;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import java.sql.SQLException;
import java.util.Optional;

import entity.Employee;
import entity.ProductUnit;
import java.awt.image.BufferedImage;
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

    public static String decodeBarcodeFromImage(BufferedImage image) {
        if (image == null)
            return null;
        try {
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            // Dùng MultiFormatReader của ZXing để quét nhận diện mọi loại mã (EAN-13, Code
            // 128, QR...)
            Result result = new MultiFormatReader().decode(bitmap);
            if (result != null) {
                return result.getText(); // Trả về chuỗi mã vạch tìm thấy
            }
        } catch (Exception e) {
            // Khử log lỗi liên tục khi webcam đang bắt khung hình trống không có mã vạch
        }
        return null;
    }
}