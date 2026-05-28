package view;

import dto.InventoryImportDTO;
import java.sql.SQLException;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import service.impl.PurchaseOrderServiceImpl;
import entity.PurchaseOrder;
import entity.PurchaseOrderDetail;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.regex.Pattern;
import javax.swing.DefaultComboBoxModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;

public class ApproveProductFrame extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger
            .getLogger(ApproveProductFrame.class.getName());
    private final PurchaseOrderServiceImpl service = new PurchaseOrderServiceImpl();

    private DefaultTableModel modelOrders;
    private DefaultTableModel modelOrderDetails;
    private TableRowSorter<DefaultTableModel> rowSorter;

    private PurchaseOrder currentSelectedOrder = null;

    public ApproveProductFrame() {
        initComponents();
        initCustomTableModels(); // Cấu hình chuẩn hóa bảng
        initStatusComboBox(); // Nạp dữ liệu ComboBox trạng thái
        initSearchEvent();
        loadOrderData(); // Tải danh sách đơn hàng lên bảng
        setupTableSelection();
        tblOrders.clearSelection();
    }

    private void initCustomTableModels() {
        // 1. Cấu hình bảng danh sách đơn hàng (Tuyệt đối không cho sửa trực tiếp trên
        // ô)
        modelOrders = new DefaultTableModel(
                new String[] { "Mã Đơn", "Mã Cửa Hàng", "Nhà Cung Cấp (ID)", "Tổng Tiền", "Trạng Thái", "Ngày Tạo" },
                0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblOrders.setModel(modelOrders);

        rowSorter = new TableRowSorter<>(modelOrders);
        tblOrders.setRowSorter(rowSorter);
        modelOrderDetails = new DefaultTableModel(
                new String[] { "Mã SP", "Số Lượng", "Giá Nhập", "Thành Tiền", "Mã Lô (Edit)",
                        "Hạn Dùng yyyy-MM-dd (Edit)" },
                0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Cột 4 (Mã lô) và Cột 5 (Hạn dùng) được phép chỉnh sửa dữ liệu thực tế
                return column == 4 || column == 5;
            }
        };
        tblOrderDetails.setModel(modelOrderDetails);
    }

    private void initSearchEvent() {
        txtSearchOrder.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                fireSearch();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                fireSearch();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                fireSearch();
            }

            private void fireSearch() {
                String text = txtSearchOrder.getText().trim();
                if (text.isEmpty()) {
                    rowSorter.setRowFilter(null);
                } else {
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
                }
            }
        });
    }

    private void initStatusComboBox() {
        try {
            List<String[]> statusDB = service.getAvailableStatuses();

            List<String> displayNames = new ArrayList<>();
            for (String[] status : statusDB) {
                displayNames.add(status[1]);
            }
            cbStatusFilter.setModel(new DefaultComboBoxModel<>(displayNames.toArray(String[]::new)));
            cbStatusFilter.setSelectedIndex(0);
            cbStatusFilter.addActionListener(e -> loadOrderData());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Lỗi nạp trạng thái", e);
        }
    }

    private void loadOrderData() {
        modelOrders.setRowCount(0);
        modelOrderDetails.setRowCount(0);
        lblOrderInfo.setText("---");
        currentSelectedOrder = null;
        btnApprove.setEnabled(false);
        btnCancelOrder.setEnabled(false);

        try {
            int selectedIndex = cbStatusFilter.getSelectedIndex();
            if (selectedIndex == -1) {
                return;
            }

            List<String[]> statusDB = service.getAvailableStatuses();
            String statusCode = statusDB.get(selectedIndex)[0];

            List<PurchaseOrder> orders;
            if ("all".equals(statusCode)) {
                orders = service.getAllOrders();
            } else {
                orders = service.getOrdersByStatus(statusCode);
            }

            for (PurchaseOrder po : orders) {
                modelOrders.addRow(new Object[] {
                        po.getId(),
                        po.getStoreId(),
                        po.getSupplierId(),
                        po.getTotalAmount(),
                        po.getStatus(),
                        po.getCreatedAt()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải danh sách đơn hàng: " + e.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setupTableSelection() {
        tblOrders.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tblOrders.getSelectedRow();
                if (selectedRow != -1) {
                    int modelRow = tblOrders.convertRowIndexToModel(selectedRow);

                    int orderId = (int) modelOrders.getValueAt(modelRow, 0);
                    // Chỉ truyền 1 tham số orderId duy nhất như code gốc của bạn
                    loadOrderDetails(orderId);
                }
            }
        });
    }

    private void loadOrderDetails(int orderId) {
        modelOrderDetails.setRowCount(0);
        try {
            currentSelectedOrder = service.getOrderById(orderId);
            lblOrderInfo.setText("PO-" + orderId);

            String orderStatus = "";
            int selectedRow = tblOrders.getSelectedRow();
            if (selectedRow != -1) {
                int modelRow = tblOrders.convertRowIndexToModel(selectedRow);
                orderStatus = (String) modelOrders.getValueAt(modelRow, 4);
            }

            boolean isPending = "pending".equalsIgnoreCase(orderStatus);
            btnApprove.setEnabled(isPending);
            btnCancelOrder.setEnabled(isPending);

            List<PurchaseOrderDetail> details = service.getOrderDetails(orderId);

            for (PurchaseOrderDetail detail : details) {
                String batchCode = "";
                String expiryDate = "";

                if ("received".equalsIgnoreCase(orderStatus)) {
                    String[] savedData = service.getSavedBatchAndExpiry(orderId, detail.getProductId());
                    if (savedData != null && savedData.length >= 2) {
                        batchCode = savedData[0]; // Lấy ra chính xác mã lô tĩnh (VD: LOT-AUTO-... hoặc LOT-2D...)
                        expiryDate = savedData[1]; // Lấy ra chuỗi ngày hạn dùng tương ứng
                    } else {
                        batchCode = "N/A";
                        expiryDate = "N/A";
                    }
                } else {
                    // Trường hợp đơn đang chờ duyệt (pending) -> Giữ nguyên logic băm xem trước
                    batchCode = util.ShortHash.BatchBarcodeHash(orderId, detail.getProductId());
                    expiryDate = "";
                }

                BigDecimal importPrice = detail.getImportPriceAtTime();
                if (importPrice == null) {
                    importPrice = java.math.BigDecimal.ZERO;
                }
                java.math.BigDecimal subTotal = importPrice
                        .multiply(java.math.BigDecimal.valueOf(detail.getQuantity()));

                modelOrderDetails.addRow(new Object[] {
                        detail.getProductId(),
                        detail.getQuantity(),
                        importPrice,
                        subTotal,
                        batchCode,
                        expiryDate
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải chi tiết đơn hàng: " + e.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void executeApproval() {
        if (currentSelectedOrder == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một đơn hàng trước khi duyệt.", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (tblOrderDetails.isEditing()) {
            tblOrderDetails.getCellEditor().stopCellEditing();
        }

        int rowCount = modelOrderDetails.getRowCount();
        if (rowCount == 0) {
            JOptionPane.showMessageDialog(this, "Đơn hàng không có chi tiết. Vui lòng kiểm tra lại.", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<InventoryImportDTO> importList = new ArrayList<>();

        for (int i = 0; i < rowCount; i++) {
            // Column 0: Product ID
            int productId = 0;
            Object prodObj = modelOrderDetails.getValueAt(i, 0);
            if (prodObj != null && !prodObj.toString().trim().isEmpty()) {
                try {
                    productId = Integer.parseInt(prodObj.toString().trim());
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(this, "Mã sản phẩm tại dòng " + (i + 1) + " không hợp lệ.",
                            "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            // Column 1: Quantity (raw value, e.g., 2)
            int quantity = 0;
            Object qtyObj = modelOrderDetails.getValueAt(i, 1);
            if (qtyObj != null && !qtyObj.toString().trim().isEmpty()) {
                try {
                    quantity = Integer.parseInt(qtyObj.toString().trim());
                    if (quantity <= 0) {
                        JOptionPane.showMessageDialog(this, "Số lượng tại dòng " + (i + 1) + " phải lớn hơn 0.",
                                "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(this, "Số lượng tại dòng " + (i + 1) + " không hợp lệ.",
                            "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            // Column 2: Import Price (raw value, e.g., 650000.00)
            java.math.BigDecimal importPrice = java.math.BigDecimal.ZERO;
            Object priceObj = modelOrderDetails.getValueAt(i, 2);
            if (priceObj != null && !priceObj.toString().trim().isEmpty()) {
                try {
                    importPrice = new java.math.BigDecimal(priceObj.toString().trim());
                    if (importPrice.compareTo(java.math.BigDecimal.ZERO) < 0) {
                        JOptionPane.showMessageDialog(this, "Giá nhập tại dòng " + (i + 1) + " không được âm.",
                                "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(this, "Giá nhập tại dòng " + (i + 1) + " không hợp lệ.",
                            "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            // Column 3: Skip (Total Price)
            // Column 4: Batch Code
            String batchCode = (modelOrderDetails.getValueAt(i, 4) != null)
                    ? modelOrderDetails.getValueAt(i, 4).toString().trim()
                    : "";
            if (batchCode.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập Mã Lô tại dòng " + (i + 1) + ".", "Lỗi nhập liệu",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Column 5: Expiry Date (yyyy-MM-dd format)
            String expiryDateStr = (modelOrderDetails.getValueAt(i, 5) != null)
                    ? modelOrderDetails.getValueAt(i, 5).toString().trim()
                    : "";
            if (expiryDateStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập Hạn Sử Dụng (yyyy-MM-dd) tại dòng " + (i + 1) + ".",
                        "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                return;
            }

            java.sql.Date sqlExpiryDate = null;
            try {
                sqlExpiryDate = java.sql.Date.valueOf(expiryDateStr);
            } catch (IllegalArgumentException iae) {
                JOptionPane.showMessageDialog(this,
                        "Định dạng Hạn Sử Dụng tại dòng " + (i + 1)
                                + " không hợp lệ. Vui lòng dùng định dạng yyyy-MM-dd.",
                        "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                return;
            }

            dto.InventoryImportDTO itemDto = new dto.InventoryImportDTO();
            itemDto.setProductId(productId);
            itemDto.setQuantity(quantity);
            itemDto.setImportPrice(importPrice);
            itemDto.setBatchCode(batchCode);
            itemDto.setExpiryDate(sqlExpiryDate);

            // Lấy dữ liệu tên đơn vị lớn, hệ số và đơn vị gốc lẻ từ Table/UI nếu giao diện
            // của bạn có cột đó.
            // Nếu giao diện của bạn hiện tại chưa bổ sung cột nhập tay, ta tạm thời lấy mặc
            // định như sau:
            itemDto.setUnitName("Thùng"); // Tên đơn vị nhập (Ví dụ dòng này nhập Thùng)
            itemDto.setUnitRatio(24); // Hệ số quy đổi nhập tay (Ví dụ: 24)
            itemDto.setBaseUnitName("Chai"); // Tên đơn vị hạt nhân lẻ làm gốc

            importList.add(itemDto);
        }

        // Call service with transaction handling
        try {
            service.approveAndImportInventory(currentSelectedOrder.getId(), currentSelectedOrder.getStoreId(),
                    importList);
            JOptionPane.showMessageDialog(this, "Duyệt đơn nhập kho thành công!", "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
            loadOrderData();
        } catch (SQLException sqlEx) {
            logger.log(Level.SEVERE, "SQL Exception during approval", sqlEx);
            JOptionPane.showMessageDialog(this, "Lỗi cơ sở dữ liệu: " + sqlEx.getMessage(), "Lỗi hệ thống",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Exception during approval", ex);
            JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void executeCancelOrder() {
        if (currentSelectedOrder == null) {
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn HỦY đơn nhập hàng này không?",
                "Xác nhận hủy", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            try {
                service.cancelOrder(currentSelectedOrder.getId());
                JOptionPane.showMessageDialog(this, "Đã hủy đơn nhập hàng thành công.");
                loadOrderData();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi hủy đơn hàng: " + e.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        cbStatusFilter = new javax.swing.JComboBox<>();
        jLabel1 = new javax.swing.JLabel();
        txtSearchOrder = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblOrderDetails = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();
        lblOrderInfo = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblOrders = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        btnApprove = new javax.swing.JButton();
        btnCancelOrder = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        cbStatusFilter.setModel(
                new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbStatusFilter.addActionListener(this::cbStatusFilterActionPerformed);

        jLabel1.setText("Lọc trạng thái:");

        jLabel2.setText("Tìm kiếm:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtSearchOrder, javax.swing.GroupLayout.PREFERRED_SIZE, 102,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(62, 62, 62)
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(cbStatusFilter, javax.swing.GroupLayout.PREFERRED_SIZE, 114,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap()));
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(7, 7, 7)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel1)
                                        .addComponent(cbStatusFilter, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(txtSearchOrder, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel2))
                                .addContainerGap(7, Short.MAX_VALUE)));

        tblOrderDetails.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][] {
                        { null, null, null, null },
                        { null, null, null, null },
                        { null, null, null, null },
                        { null, null, null, null }
                },
                new String[] {
                        "Title 1", "Title 2", "Title 3", "Title 4"
                }));
        jScrollPane1.setViewportView(tblOrderDetails);

        jLabel3.setText("Đang xử lý đơn:");

        tblOrders.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][] {
                        { null, null, null, null },
                        { null, null, null, null },
                        { null, null, null, null },
                        { null, null, null, null }
                },
                new String[] {
                        "Title 1", "Title 2", "Title 3", "Title 4"
                }));
        jScrollPane2.setViewportView(tblOrders);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Xử lý đơn đặt"));

        btnApprove.setText("Duyệt");
        btnApprove.addActionListener(this::btnApproveActionPerformed);

        btnCancelOrder.setText("Hủy");
        btnCancelOrder.addActionListener(this::btnCancelOrderActionPerformed);

        btnRefresh.setText("Làm mới");
        btnRefresh.addActionListener(this::btnRefreshActionPerformed);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(42, 42, 42)
                                .addComponent(btnApprove)
                                .addGap(18, 18, 18)
                                .addComponent(btnRefresh)
                                .addGap(18, 18, 18)
                                .addComponent(btnCancelOrder)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        jPanel2Layout.setVerticalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(btnApprove)
                                        .addComponent(btnRefresh)
                                        .addComponent(btnCancelOrder))
                                .addGap(0, 6, Short.MAX_VALUE)));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(jLabel3)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(lblOrderInfo,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 132,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addComponent(jScrollPane2,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 721,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jScrollPane1,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 721,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGap(0, 9, Short.MAX_VALUE))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                                layout.createSequentialGroup()
                                                        .addGap(0, 0, Short.MAX_VALUE)
                                                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap()));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(14, 14, 14)
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 170,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel3)
                                        .addComponent(lblOrderInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 16,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(16, 16, 16)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 58,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(19, Short.MAX_VALUE)));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnRefreshActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_btnRefreshActionPerformed

    private void btnApproveActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnApproveActionPerformed
        executeApproval();
    }// GEN-LAST:event_btnApproveActionPerformed

    private void btnCancelOrderActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnCancelOrderActionPerformed
        executeCancelOrder();
    }// GEN-LAST:event_btnCancelOrderActionPerformed

    private void cbStatusFilterActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cbStatusFilterActionPerformed

    }// GEN-LAST:event_cbStatusFilterActionPerformed

    public static void main(String args[]) {

        java.awt.EventQueue.invokeLater(() -> new ApproveProductFrame().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnApprove;
    private javax.swing.JButton btnCancelOrder;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JComboBox<String> cbStatusFilter;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblOrderInfo;
    private javax.swing.JTable tblOrderDetails;
    private javax.swing.JTable tblOrders;
    private javax.swing.JTextField txtSearchOrder;
    // End of variables declaration//GEN-END:variables
}
