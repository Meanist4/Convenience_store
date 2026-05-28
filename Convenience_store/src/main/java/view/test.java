package view;

import dto.InventoryImportDTO;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import service.PurchaseOrderService;
import service.impl.PurchaseOrderServiceImpl;

public class test {

    public static void main(String[] args) {
        PurchaseOrderService poService = new PurchaseOrderServiceImpl();

        // 1. Giả lập danh sách hàng nhập từ giao diện truyền xuống (DTO)
        List<InventoryImportDTO> importItems = new ArrayList<>();

        // Trong file test.java
        InventoryImportDTO item = new InventoryImportDTO();
        item.setProductId(0);
        item.setQuantity(5);  // Nhập 5
        item.setImportPrice(new BigDecimal("200000.00")); // Giá 200k
        item.setBatchCode("LOT-COCA-2026");
        item.setExpiryDate(java.sql.Date.valueOf("2027-05-28"));

// Giả lập người dùng nhập tay vào các ô Input trên giao diện:
        item.setUnitName("Thùng");
        item.setUnitRatio(24);         // Nhập tay ratio = 24
        item.setBaseUnitName("Chai");  // Nhập tay đơn vị gốc là Chai

        importItems.add(item);

        try {
            // 2. Thực hiện gọi hàm duyệt đơn hàng PO-10 cho Cửa hàng ID = 1
            poService.approveAndImportInventory(10, 1, importItems);
            System.out.println("🎉 Duyệt đơn hàng và nhập kho thành công!");

        } catch (Exception e) {
            System.err.println("❌ Lỗi duyệt đơn: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
