/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package util;

import com.google.zxing.client.j2se.MatrixToImageWriter;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class GenBarcode {

    public static void main(String[] args) {
        try {
            String text = "EMP-AAE4B1210393"; // Nội dung mã
            int width = 300;
            int height = 100;
            String filePath = "zxing_barcode.png";

            // 1. Khởi tạo Writer cho loại mã cụ thể (Code 128)
            Code128Writer barcodeWriter = new Code128Writer();

            // 2. Tạo ma trận bit (BitMatrix)
            BitMatrix bitMatrix = barcodeWriter.encode(text, BarcodeFormat.CODE_128, width, height);

            // 3. Lưu thành file ảnh
            Path path = FileSystems.getDefault().getPath(filePath);
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);

            System.out.println("Tạo barcode thành công tại: " + filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void genBarcode(String code) {
        try {
            int width = 300;
            int height = 100;
            String folderPath = "Barcodes";

            // 0. Tạo đối tượng File đại diện cho thư mục
            java.io.File directory = new java.io.File(folderPath);
            if (!directory.exists()) {
                directory.mkdirs(); // Tự động tạo thư mục "barcodes" nếu chưa có
            }
            String filePath = code + ".png";
            // 1. Khởi tạo Writer cho loại mã cụ thể (Code 128)
            Code128Writer barcodeWriter = new Code128Writer();

            // 2. Tạo ma trận bit (BitMatrix)
            BitMatrix bitMatrix = barcodeWriter.encode(code, BarcodeFormat.CODE_128, width, height);

            // 3. Lưu thành file ảnh
            Path path = FileSystems.getDefault().getPath(filePath);
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);

            System.out.println("Tạo barcode thành công tại: " + filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
