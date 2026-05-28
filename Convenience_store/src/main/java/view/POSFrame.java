package view;

import entity.Invoice;
import entity.InvoiceDetail;
import entity.ProductUnit;
import service.InvoiceService;
import service.impl.InvoiceServiceImpl;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import repository.EmployeeRepository;
import repository.ProductUnitRepository;
import repository.StoreRepository;
import util.ShortHash;

public class POSFrame extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(POSFrame.class.getName());

    private final InvoiceService invoiceService = new InvoiceServiceImpl();
    private final List<InvoiceDetail> cartList = new ArrayList<>();
    private final ProductUnitRepository productUnitRepo = new ProductUnitRepository();
    private final StoreRepository storeRepo = new StoreRepository();
    private final EmployeeRepository employeeRepo = new EmployeeRepository();
    private BigDecimal totalInvoiceAmount = BigDecimal.ZERO;

    private boolean isUpdatingCbb = false;

    public POSFrame() {
        setTitle("HỆ THỐNG BÁN HÀNG TẠI QUẦY - POS");
        initComponents();
        initTableCartHeaders();
        initCustomEvent();
        this.pack();
        this.setLocationRelativeTo(null);

    }

    private void initCustomEvent() {
        unitCbb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ProductUnit) {
                    setText(((ProductUnit) value).getUnitName()); // Lấy tên đơn vị (Thùng/Chai)
                }
                return this;
            }
        });
        txtBarcode.addActionListener(e -> handleBarcodeScanned());
    }

    private void initTableCartHeaders() {
        String[] headers = { "Mã Vạch", "Tên Sản Phẩm", "Đơn Vị", "Số Lượng", "Đơn Giá", "Thành Tiền" };
        DefaultTableModel model = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho phép gõ sửa trực tiếp trên ô của bảng
            }
        };
        tableCart.setModel(model);
    }

    private void handleBarcodeScanned() {
        String rawBarcode = txtBarcode.getText().replaceAll("\\s+", "");
        if (rawBarcode.isEmpty()) {
            return;
        }

        try {
            String processedBarcode = rawBarcode;

            if (!rawBarcode.startsWith("PRO-")) {
                try {
                    processedBarcode = ShortHash.ProductBarcodeHash(rawBarcode);
                    txtBarcode.setText(processedBarcode);
                } catch (Exception ex) {
                    logger.log(java.util.logging.Level.SEVERE, "Lỗi băm mã vạch sản phẩm", ex);
                }
            }

            // 1. Tìm thông tin đơn vị tính bằng mã vạch đã được chuẩn hóa (PRO-XXXX)
            Optional<ProductUnit> currentUnitOpt = productUnitRepo.findByBarcode(processedBarcode);
            if (!currentUnitOpt.isPresent()) {
                JOptionPane.showMessageDialog(this,
                        "Không tìm thấy sản phẩm nào khớp với mã vạch gốc '" + rawBarcode
                                + "'\n(Mã định dạng hệ thống: " + processedBarcode + ")!",
                        "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
                txtBarcode.selectAll();
                return;
            }

            ProductUnit currentUnit = currentUnitOpt.get();
            int productId = currentUnit.getProductId();

            // 2. Truy vấn tất cả các đơn vị đóng gói khác của sản phẩm này
            List<ProductUnit> availableUnits = productUnitRepo.findByProductId(productId);

            // 3. Đổ dữ liệu động vào Combo Box
            isUpdatingCbb = true;
            unitCbb.removeAllItems();

            ProductUnit itemToSelect = null;
            for (ProductUnit unit : availableUnits) {
                ((JComboBox) unitCbb).addItem(unit);
                if (unit.getId() == currentUnit.getId()) {
                    itemToSelect = unit;
                }
            }
            isUpdatingCbb = false;

            // 4. Chọn đúng đơn vị tính ứng với mã vạch vừa quét trên màn hình
            if (itemToSelect != null) {
                unitCbb.setSelectedItem(itemToSelect);
            }

            // Tự động chuyển tiêu điểm sang ô số lượng mua
            txtQuantity.requestFocus();
            txtQuantity.selectAll();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi đồng bộ đơn vị tính: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panel1 = new java.awt.Panel();
        jLabel1 = new javax.swing.JLabel();
        txtStoreBarcode = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtBarcode = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtEmployeeBarcode = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtQuantity = new javax.swing.JTextField();
        btnAdd = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableCart = new javax.swing.JTable();
        panelBottom = new java.awt.Panel();
        lblTotalAmount = new javax.swing.JLabel();
        btnCheckout = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        unitCbb = new javax.swing.JComboBox<>();
        btnScan = new javax.swing.JButton();
        btnRemove = new javax.swing.JButton();

        javax.swing.GroupLayout panel1Layout = new javax.swing.GroupLayout(panel1);
        panel1.setLayout(panel1Layout);
        panel1Layout.setHorizontalGroup(
                panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 100, Short.MAX_VALUE));
        panel1Layout.setVerticalGroup(
                panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 100, Short.MAX_VALUE));

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Mã Cửa Hàng:");

        jLabel2.setText("Mã Nhân Viên");

        jLabel3.setText("Mã Vạch Sản Phẩm:");

        jLabel4.setText("Số Lượng Mua:");

        btnAdd.setText("Thêm hàng");
        btnAdd.addActionListener(this::btnAddActionPerformed);

        tableCart.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][] {
                        { null, null, null, null },
                        { null, null, null, null },
                        { null, null, null, null },
                        { null, null, null, null }
                },
                new String[] {
                        "Title 1", "Title 2", "Title 3", "Title 4"
                }));
        jScrollPane1.setViewportView(tableCart);

        lblTotalAmount.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        lblTotalAmount.setText("Tổng tiền:");

        javax.swing.GroupLayout panelBottomLayout = new javax.swing.GroupLayout(panelBottom);
        panelBottom.setLayout(panelBottomLayout);
        panelBottomLayout.setHorizontalGroup(
                panelBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelBottomLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(lblTotalAmount, javax.swing.GroupLayout.DEFAULT_SIZE, 316,
                                        Short.MAX_VALUE)
                                .addContainerGap()));
        panelBottomLayout.setVerticalGroup(
                panelBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelBottomLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(lblTotalAmount)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        btnCheckout.setText("Thanh toán");
        btnCheckout.addActionListener(this::btnCheckoutActionPerformed);

        jLabel5.setText("Đơn vị tính:");

        unitCbb.setModel(
                new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        unitCbb.addActionListener(this::unitCbbActionPerformed);

        btnScan.setText("Quét mã");
        btnScan.addActionListener(this::btnScanActionPerformed);

        btnRemove.setText("Xóa");
        btnRemove.addActionListener(this::btnRemoveActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(panelBottom, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(326, 326, 326)
                                .addComponent(btnCheckout, javax.swing.GroupLayout.PREFERRED_SIZE, 112,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout
                                                .createSequentialGroup()
                                                .addGap(14, 14, 14)
                                                .addGroup(layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addComponent(jLabel2)
                                                        .addComponent(jLabel1))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addGroup(layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING,
                                                                false)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(txtStoreBarcode,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 93,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        Short.MAX_VALUE)
                                                                .addComponent(jLabel3))
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(txtEmployeeBarcode,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 153,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(36, 36, 36)
                                                                .addComponent(jLabel4)))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING,
                                                                false)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(txtQuantity,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 48,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        Short.MAX_VALUE)
                                                                .addComponent(jLabel5))
                                                        .addComponent(txtBarcode,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 171,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addGroup(layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(btnScan)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(btnRemove)
                                                                .addGap(0, 0, Short.MAX_VALUE))
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(unitCbb,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 91,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        Short.MAX_VALUE)
                                                                .addComponent(btnAdd))))
                                        .addGroup(layout.createSequentialGroup()
                                                .addContainerGap()
                                                .addComponent(jScrollPane1)))
                                .addContainerGap()));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(txtStoreBarcode, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel1)
                                        .addComponent(jLabel3)
                                        .addComponent(txtBarcode, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnScan)
                                        .addComponent(btnRemove))
                                .addGap(33, 33, 33)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel2)
                                        .addComponent(txtEmployeeBarcode, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel4)
                                        .addComponent(txtQuantity, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel5)
                                        .addComponent(unitCbb, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 22,
                                                Short.MAX_VALUE))
                                .addGap(18, 18, 18)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 251,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(panelBottom, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(btnCheckout, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(131, 131, 131)));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCheckoutActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnCheckoutActionPerformed
        if (cartList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Giỏ hàng hiện tại đang trống!", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String storeCode = txtStoreBarcode.getText().trim();
        String empBarcode = txtEmployeeBarcode.getText().trim();

        if (storeCode.isEmpty() || empBarcode.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ Mã Cửa Hàng và Mã Nhân Viên!", "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!empBarcode.startsWith("EMP-")) {
            try {
                empBarcode = util.ShortHash.EmployeeBarcodeHash(empBarcode);
                txtEmployeeBarcode.setText(empBarcode);
            } catch (Exception e) {
                logger.log(java.util.logging.Level.SEVERE, "Lỗi băm mã vạch nhân viên", e);
            }
        }
        String finalEmpBarcode = empBarcode;
        btnCheckout.setEnabled(false);

        try {
            int realStoreId = storeRepo.findIdByCode(storeCode)
                    .orElseThrow(() -> new RuntimeException("Mã Cửa Hàng '" + storeCode + "' không tồn tại!"));

            int realEmployeeId = employeeRepo.findIdByBarcode(finalEmpBarcode)
                    .orElseThrow(() -> new RuntimeException("Mã Nhân Viên '" + finalEmpBarcode + "' không hợp lệ!"));

            // 4. CHẠY TRANSACTION
            Invoice completedInvoice = invoiceService.createInvoice(realStoreId, realEmployeeId, cartList);
            JOptionPane.showMessageDialog(this,
                    "Thanh toán thành công!\nHóa đơn số: " + completedInvoice.getId()
                            + "\nSố kho đã được khấu trừ tự động theo niên hạn lô hàng (FEFO)!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            cartList.clear();
            ((DefaultTableModel) tableCart.getModel()).setRowCount(0);
            totalInvoiceAmount = BigDecimal.ZERO;
            lblTotalAmount.setText("Tổng tiền: 0 VND");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "GIAO DỊCH THẤT BẠI - HỆ THỐNG ĐÃ HOÀN TÁC (ROLLBACK) KHO BÃI!\nChi tiết: " + ex.getMessage(),
                    "Lỗi Xuất Kho", JOptionPane.ERROR_MESSAGE);
        } finally {
            btnCheckout.setEnabled(true);
        }
    }// GEN-LAST:event_btnCheckoutActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnAddActionPerformed
        Object selectedItem = unitCbb.getSelectedItem();
        String qtyStr = txtQuantity.getText().trim();

        if (selectedItem == null || !(selectedItem instanceof ProductUnit)) {
            JOptionPane.showMessageDialog(this, "Vui lòng quét mã vạch hợp lệ trước khi thêm hàng!", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            txtBarcode.requestFocus();
            return;
        }

        try {
            int quantity = Integer.parseInt(qtyStr);
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Số lượng mua phải lớn hơn 0!", "Lỗi nhập liệu",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 🌟 LẤY THỰC THỂ UNIT ĐANG ĐƯỢC CHỌN TRÊN COMBOBOX (Bất kể thu ngân vừa đổi
            // đơn vị gì)
            ProductUnit unit = (ProductUnit) selectedItem;
            BigDecimal price = unit.getSellingPrice();
            String activeBarcode = unit.getBarcode(); // Đồng bộ mã vạch của đơn vị được chốt mua

            DefaultTableModel model = (DefaultTableModel) tableCart.getModel();
            int existingRowIndex = -1;

            // Kiểm tra trùng lặp dựa trên mã vạch đơn vị tính cụ thể trong giỏ
            for (int i = 0; i < model.getRowCount(); i++) {
                if (model.getValueAt(i, 0).toString().equals(activeBarcode)) {
                    existingRowIndex = i;
                    break;
                }
            }

            if (existingRowIndex != -1) {
                // TRƯỜNG HỢP TRÙNG ĐƠN VỊ TÍNH: Cộng dồn số lượng
                int oldQty = (int) model.getValueAt(existingRowIndex, 3);
                int newQty = oldQty + quantity;
                BigDecimal newSubtotal = price.multiply(BigDecimal.valueOf(newQty));

                model.setValueAt(newQty, existingRowIndex, 3);
                model.setValueAt(newSubtotal, existingRowIndex, 5);

                for (InvoiceDetail detail : cartList) {
                    if (detail.getUnitId() == unit.getId()) {
                        detail.setQuantity(newQty);
                        detail.setSubtotal(newSubtotal);
                        break;
                    }
                }
            } else {
                // TRƯỜNG HỢP MỚI: Thêm bản ghi mới hoàn toàn vào giỏ
                BigDecimal subtotal = price.multiply(BigDecimal.valueOf(quantity));

                InvoiceDetail detail = new InvoiceDetail();
                detail.setProductId(unit.getProductId());
                detail.setUnitId(unit.getId());
                detail.setQuantity(quantity);
                detail.setPriceAtSale(price);
                detail.setSubtotal(subtotal);
                cartList.add(detail);

                model.addRow(new Object[] {
                        unit.getBarcode(),
                        "Sản phẩm ID: " + unit.getProductId(),
                        unit.getUnitName(),
                        quantity,
                        price,
                        subtotal
                });
            }

            // Cập nhật tổng tiền hóa đơn lớn hiển thị góc dưới
            BigDecimal currentItemSubtotal = price.multiply(BigDecimal.valueOf(quantity));
            totalInvoiceAmount = totalInvoiceAmount.add(currentItemSubtotal);
            lblTotalAmount.setText("Tổng tiền: " + totalInvoiceAmount + " VND");

            // Reset trạng thái ô nhập dữ liệu, giữ nguyên Combo box để thu ngân xem dữ liệu
            txtBarcode.setText("");
            txtQuantity.setText("1");
            txtBarcode.requestFocus();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số lượng mua bắt buộc phải là số nguyên!", "Lỗi định dạng",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi đưa hàng vào giỏ: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }// GEN-LAST:event_btnAddActionPerformed

    private void unitCbbActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_unitCbbActionPerformed
        if (isUpdatingCbb) {
            return;
        }
        Object selected = unitCbb.getSelectedItem();
        if (selected instanceof ProductUnit) {
            ProductUnit selectedUnit = (ProductUnit) selected;
            txtBarcode.setText(selectedUnit.getBarcode());
        }
    }// GEN-LAST:event_unitCbbActionPerformed

    private void btnScanActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnScanActionPerformed
        // 1. Khởi tạo Camera mặc định của máy tính
        com.github.sarxos.webcam.Webcam webcam = com.github.sarxos.webcam.Webcam.getDefault();
        if (webcam == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy Camera/Webcam kết nối với máy tính!", "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Đặt độ phân giải cho Camera
        webcam.setViewSize(com.github.sarxos.webcam.WebcamResolution.VGA.getSize());

        // Tạo một Panel hiển thị luồng hình ảnh của Camera
        com.github.sarxos.webcam.WebcamPanel panel = new com.github.sarxos.webcam.WebcamPanel(webcam);
        panel.setFPSDisplayed(true);
        panel.setDisplayDebugInfo(false);
        panel.setImageSizeDisplayed(false);
        panel.setMirrored(false);

        // 2. Tạo một cửa sổ JDialog nhỏ để hiện Camera lên màn hình
        JDialog scanDialog = new JDialog(this, "ĐANG QUÉT MÃ SẢN PHẨM QUA CAMERA", true);
        scanDialog.add(panel);
        scanDialog.pack();
        scanDialog.setLocationRelativeTo(this);

        // 3. Tạo một Thread để chụp ảnh từ camera và phân tích mã liên tục
        Thread scanThread = new Thread(() -> {
            try {
                while (webcam.isOpen()) {
                    java.awt.image.BufferedImage image = webcam.getImage();
                    if (image != null) {
                        com.google.zxing.LuminanceSource source = new com.google.zxing.client.j2se.BufferedImageLuminanceSource(
                                image);
                        com.google.zxing.BinaryBitmap bitmap = new com.google.zxing.BinaryBitmap(
                                new com.google.zxing.common.HybridBinarizer(source));

                        try {
                            // Thư viện ZXing tiến hành đọc mã vạch từ ảnh
                            com.google.zxing.Result result = new com.google.zxing.MultiFormatReader().decode(bitmap);

                            if (result != null) {
                                String barcodeResult = result.getText().trim();

                                // Đọc thành công! Đẩy mã vạch vào ô TextBox trên Form POS
                                txtBarcode.setText(barcodeResult);

                                // Phát tiếng beep và đóng camera, đóng cửa sổ quét
                                java.awt.Toolkit.getDefaultToolkit().beep();
                                webcam.close();
                                scanDialog.dispose();

                                // 🌟 Kích hoạt luồng tìm sản phẩm và đổ đơn vị tính tương thích lên Combo Box
                                SwingUtilities.invokeLater(() -> handleBarcodeScanned());
                                break;
                            }
                        } catch (Exception e) {
                            // Bỏ qua khung hình lỗi, quét tiếp khung hình sau
                        }
                    }
                    Thread.sleep(100); // Tránh treo máy
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        });

        // Sự kiện nếu người dùng chủ động tắt cửa sổ Dialog quét đi thì phải tắt Camera
        // ngầm
        scanDialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (webcam.isOpen()) {
                    webcam.close();
                }
            }
        });

        // Kích hoạt luồng quét ngầm và mở cửa sổ Camera lên
        scanThread.start();
        scanDialog.setVisible(true);
    }// GEN-LAST:event_btnScanActionPerformed

    private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnRemoveActionPerformed
        int selectedRow = tableCart.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một dòng sản phẩm trong giỏ hàng để xóa!", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        DefaultTableModel model = (DefaultTableModel) tableCart.getModel();
        BigDecimal itemSubtotal = (BigDecimal) model.getValueAt(selectedRow, 5);

        // Trừ bớt tiền tổng
        totalInvoiceAmount = totalInvoiceAmount.subtract(itemSubtotal);
        lblTotalAmount.setText("Tổng tiền: " + totalInvoiceAmount + " VND");

        // Xóa khỏi danh sách lưu trữ hóa đơn ngầm và UI
        cartList.remove(selectedRow);
        model.removeRow(selectedRow);
    }// GEN-LAST:event_btnRemoveActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        SwingUtilities.invokeLater(() -> new POSFrame().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnCheckout;
    private javax.swing.JButton btnRemove;
    private javax.swing.JButton btnScan;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblTotalAmount;
    private java.awt.Panel panel1;
    private java.awt.Panel panelBottom;
    private javax.swing.JTable tableCart;
    private javax.swing.JTextField txtBarcode;
    private javax.swing.JTextField txtEmployeeBarcode;
    private javax.swing.JTextField txtQuantity;
    private javax.swing.JTextField txtStoreBarcode;
    private javax.swing.JComboBox<String> unitCbb;
    // End of variables declaration//GEN-END:variables
}
