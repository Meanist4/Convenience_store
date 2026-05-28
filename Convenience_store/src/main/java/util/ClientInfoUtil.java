package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

public class ClientInfoUtil {

    // 1. Hàm lấy IP của Client
    public static String getClientIP() {
        try {
            // Thử lấy Public IP (IP mạng Internet bên ngoài) bằng API miễn phí, nhanh và ổn
            // định
            URL url = new URL("https://api.ipify.org");
            try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
                return br.readLine().trim();
            }
        } catch (IOException e) {
            try {
                // Nếu máy không có mạng Internet (chỉ chạy mạng LAN nội bộ), lấy IP của máy
                // trong mạng LAN
                return InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException ex) {
                return "127.0.0.1"; // Phương án cuối cùng nếu lỗi hoàn toàn
            }
        }
    }

    // 2. Hàm tự định nghĩa User-Agent cho ứng dụng Swing
    public static String getClientUserAgent() {
        String osName = System.getProperty("os.name"); // Ví dụ: Windows 11, Mac OS X
        String osArch = System.getProperty("os.arch"); // Ví dụ: amd64, aarch64
        String javaVersion = System.getProperty("java.version"); // Ví dụ: 17.0.2

        // Trả về chuỗi thông tin định danh thiết bị của bạn
        return String.format("SwingClient/1.0 (%s; %s) Java/%s", osName, osArch, javaVersion);
    }
}
