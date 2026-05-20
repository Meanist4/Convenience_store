/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package util;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

public class ShortHash {

    public static String getBarcodeData(String uniqueNumber) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(uniqueNumber.getBytes(StandardCharsets.UTF_8));

        // Lấy 6 byte đầu tiên (tương đương 12 ký tự Hex) để barcode ngắn gọn
        String fullHex = HexFormat.of().formatHex(hash);
        return fullHex.substring(0, 12).toUpperCase();
    }

    public static String ProductBarcodeHash(String uniqueNumber) throws Exception {
        return "PRO-" + getBarcodeData(uniqueNumber);
    }

    
    public static String EmployeeBarcodeHash(String uniqueNumber) throws Exception {
        return "EMP-" + getBarcodeData(uniqueNumber);
    }
}
