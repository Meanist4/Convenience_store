/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package view;

import dto.SystemFilter;
import java.math.BigDecimal;
import java.sql.SQLException;

import javax.swing.JOptionPane;

import entity.Product;
import entity.ProductUnit;
import java.util.Optional;
import repository.ProductUnitRepository;
import service.impl.ProductServiceImpl;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author admin
 */
public class AddProductFrame extends javax.swing.JDialog {
    Product curProduct;
    ProductUnit curUnit;

    // Constructor mặc định
    public AddProductFrame(java.awt.Dialog parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

    public AddProductFrame(java.awt.Frame parent, boolean modal, Product product, ProductUnit unit) {
        this.curProduct = product;
        this.curUnit = unit;
        super(parent, modal);
        initComponents();
    }

    ProductServiceImpl productService = new ProductServiceImpl();

    private static final java.util.logging.Logger logger = java.util.logging.Logger
            .getLogger(AddProductFrame.class.getName());

    /**
     * Creates new form AddProductFrame
     */
    public AddProductFrame() {
        initComponents();

        initStatusCombobox();

        javax.swing.event.DocumentListener autoCalculateListener = new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                calculateMarkup();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                calculateMarkup();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                calculateMarkup();
            }

            private void calculateMarkup() {
                try {
                    String impPriceStr = impPriceTxt.getText().trim();
                    String ratioStr = ratioTxt.getText().trim();
                    String sellPriceStr = sellPriceTxt.getText().trim();

                    // Kiểm tra xem người dùng đã điền đủ số liệu ở 3 ô cần thiết chưa
                    if (!impPriceStr.isEmpty() && !ratioStr.isEmpty() && !sellPriceStr.isEmpty()) {
                        java.math.BigDecimal importPrice = new java.math.BigDecimal(impPriceStr);
                        int ratio = Integer.parseInt(ratioStr);
                        java.math.BigDecimal sellingPricePerUnit = new java.math.BigDecimal(sellPriceStr);

                        // Chặn lỗi chia cho số 0
                        if (importPrice.compareTo(java.math.BigDecimal.ZERO) > 0 && ratio > 0) {
                            // Giá bán thùng = Giá bán lẻ * Tỉ lệ quy đổi
                            java.math.BigDecimal calculatedSellPricePerBox = sellingPricePerUnit
                                    .multiply(new java.math.BigDecimal(ratio));

                            // Tỉ Lệ Lãi = (Giá bán thùng - Giá nhập thùng) / Giá nhập thùng
                            java.math.BigDecimal markupRate = calculatedSellPricePerBox.subtract(importPrice)
                                    .divide(importPrice, 4, java.math.RoundingMode.HALF_UP);

                            // Đổi ra phần trăm (Ví dụ: 0.2000 -> 20)
                            java.math.BigDecimal displayMarkup = markupRate.multiply(new java.math.BigDecimal("100"));

                            // Gán text hiển thị lên Label kèm ký tự %
                            markupRateTxt.setText(String.format("%.0f%%", displayMarkup));
                        }
                    } else {
                        markupRateTxt.setText("0%");
                    }
                } catch (NumberFormatException ex) {
                    // Người dùng đang gõ dở hoặc nhập sai ký tự số, đưa nhãn về 0%
                    markupRateTxt.setText("0%");
                }
            }
        };

        impPriceTxt.getDocument().addDocumentListener(autoCalculateListener);
        ratioTxt.getDocument().addDocumentListener(autoCalculateListener);
        sellPriceTxt.getDocument().addDocumentListener(autoCalculateListener);

    }

    private void initStatusCombobox() {
        statusComboBox.removeAllItems();
        statusComboBox.setModel(new javax.swing.DefaultComboBoxModel(new SystemFilter[] {
                SystemFilter.ACTIVE,
                SystemFilter.INACTIVE
        }));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        proTxt = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        selllUnitTxt = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        catTxt = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        impPriceTxt = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        statusComboBox = new javax.swing.JComboBox<>();
        jLabel7 = new javax.swing.JLabel();
        impUnitTxt = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        ratioTxt = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        sellPriceTxt = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        barcodeTxt = new javax.swing.JTextField();
        addProBtn = new javax.swing.JButton();
        lblImagePreview = new javax.swing.JLabel();
        discountTxt1 = new javax.swing.JTextField();
        markupRateTxt = new javax.swing.JLabel();
        scanBtn = new javax.swing.JButton();
        addImgBtn = new javax.swing.JButton();
        jLabel12 = new javax.swing.JLabel();

        jLabel1.setText("Tên Sản Phẩm:");

        proTxt.addActionListener(this::proTxtActionPerformed);

        jLabel2.setText("Đơn Vị Tính:");

        jLabel3.setText("Loại Sản Phẩm:");

        jLabel4.setText("Giá Nhập:");

        jLabel5.setText("Tỉ Lệ Lãi:");

        jLabel6.setText("Trạng Thái:");

        statusComboBox.setModel(
                new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        statusComboBox.addActionListener(this::statusComboBoxActionPerformed);

        jLabel7.setText("Đơn Vị Khi Nhập");

        jLabel8.setText("Tỉ Lệ Tính");

        jLabel9.setText("Giá Bán ");

        jLabel10.setText("SALE *Nếu có");

        addProBtn.setText("Thêm Sản Phẩm");
        addProBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                addProBtnMouseClicked(evt);
            }
        });
        addProBtn.addActionListener(this::addProBtnActionPerformed);

        markupRateTxt.setText("0%");

        scanBtn.setText("Quét mã");
        scanBtn.addActionListener(this::scanBtnActionPerformed);

        addImgBtn.setText("Thêm ảnh");
        addImgBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                addImgBtnMouseClicked(evt);
            }
        });
        addImgBtn.addActionListener(this::addImgBtnActionPerformed);

        jLabel12.setText("Mã barcode:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(42, 42, 42)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jLabel6)
                                        .addComponent(jLabel1)
                                        .addComponent(jLabel3)
                                        .addComponent(jLabel4)
                                        .addComponent(jLabel2)
                                        .addComponent(jLabel5))
                                .addGap(28, 28, 28)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(impPriceTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 97,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(223, 223, 223)
                                                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 88,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(12, 12, 12)
                                                .addComponent(discountTxt1, javax.swing.GroupLayout.PREFERRED_SIZE, 60,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(selllUnitTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 97,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(223, 223, 223)
                                                .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 51,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(49, 49, 49)
                                                .addComponent(sellPriceTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 110,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(catTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 97,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(223, 223, 223)
                                                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 88,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(12, 12, 12)
                                                .addComponent(ratioTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 90,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(proTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 207,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(113, 113, 113)
                                                .addComponent(jLabel7)
                                                .addGap(12, 12, 12)
                                                .addComponent(impUnitTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 300,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(markupRateTxt)
                                                .addGap(192, 192, 192)
                                                .addComponent(barcodeTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 262,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(scanBtn))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout
                                                .createSequentialGroup()
                                                .addComponent(statusComboBox, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        220, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(83, 83, 83)
                                                .addComponent(addImgBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 118,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(lblImagePreview, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        147, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(132, 132, 132)))
                                .addContainerGap(51, Short.MAX_VALUE))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(addProBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 142,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                        .addGap(268, 268, 268)
                                        .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 88,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addContainerGap(565, Short.MAX_VALUE))));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(60, 60, 60)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(proTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 35,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(jLabel1))
                                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 35,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(impUnitTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 35,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(25, 25, 25)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(ratioTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 35,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(10, 10, 10)
                                                .addGroup(layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(layout
                                                                .createParallelGroup(
                                                                        javax.swing.GroupLayout.Alignment.BASELINE)
                                                                .addComponent(catTxt,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 35,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(jLabel3))
                                                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                35, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addGap(25, 25, 25)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(10, 10, 10)
                                                .addGroup(layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(selllUnitTxt,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 35,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jLabel2)))
                                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 35,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(sellPriceTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 35,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(impPriceTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 35,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(jLabel4))
                                        .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 35,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(discountTxt1, javax.swing.GroupLayout.PREFERRED_SIZE, 35,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(10, 10, 10)
                                                .addGroup(layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(jLabel5)
                                                        .addComponent(markupRateTxt)
                                                        .addComponent(barcodeTxt)))
                                        .addComponent(scanBtn, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(25, 25, 25)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(statusComboBox,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 35,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(addImgBtn, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                35, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                        111, Short.MAX_VALUE)
                                                .addComponent(addProBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 42,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(lblImagePreview, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        136, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addGap(17, 17, 17))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                        .addGap(320, 320, 320)
                                        .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGap(190, 190, 190))));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void proTxtActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_proTxtActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_proTxtActionPerformed

    private void addProBtnMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_addProBtnMouseClicked
        // 1. Thu thập dữ liệu chữ từ giao diện
        String proName = proTxt.getText().trim();
        String cat = catTxt.getText().trim();
        String impUnit = impUnitTxt.getText().trim(); // Đơn vị gốc lớn (ví dụ: Thùng)
        String sellUnit = selllUnitTxt.getText().trim(); // Đơn vị bán lẻ nhỏ (ví dụ: Hộp)

        String impPriceStr = impPriceTxt.getText().trim();
        String ratioStr = ratioTxt.getText().trim();
        String sellPriceStr = sellPriceTxt.getText().trim();
        String barcode = barcodeTxt.getText().trim();

        // Nếu không quét mã vạch thì bắt buộc phải chuyển về null để tránh lỗi UNIQUE
        // của DB
        if (barcode.isEmpty()) {
            barcode = null;
        }

        // Kiểm tra tính hợp lệ: Không được để trống các trường bắt buộc
        if (proName.isEmpty() || impPriceStr.isEmpty() || ratioStr.isEmpty() || sellPriceStr.isEmpty()
                || impUnit.isEmpty() || sellUnit.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập đầy đủ toàn bộ thông tin sản phẩm và quy đổi trên Form!");
            return;
        }

        try {
            // 2. Chuyển đổi định dạng số học
            java.math.BigDecimal importPrice = new java.math.BigDecimal(impPriceStr);
            int ratio = Integer.parseInt(ratioStr);
            java.math.BigDecimal sellingPricePerUnit = new java.math.BigDecimal(sellPriceStr);

            // 3. Tính toán ngầm tỉ lệ lãi thực tế để khớp toán học với DB
            java.math.BigDecimal calculatedSellPricePerBox = sellingPricePerUnit
                    .multiply(new java.math.BigDecimal(ratio));
            java.math.BigDecimal markupRate = calculatedSellPricePerBox.subtract(importPrice)
                    .divide(importPrice, 4, java.math.RoundingMode.HALF_UP);
            String finalImageName = "default.png";

            if (selectedImagePath != null) {
                File sourceFile = new File(selectedImagePath);
                if (sourceFile.exists()) {
                    // Tạo tên ảnh duy nhất tránh trùng lặp
                    finalImageName = "prod_" + System.currentTimeMillis() + ".png";

                    // Ghi vào thư mục nguồn cố định để lưu giữ code (src)
                    File srcDestDir = new File("src/main/resources/images");
                    if (!srcDestDir.exists()) {
                        srcDestDir.mkdirs();
                    }
                    File srcDestFile = new File(srcDestDir, finalImageName);
                    java.nio.file.Files.copy(sourceFile.toPath(), srcDestFile.toPath(),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                    // Ghi trực tiếp vào thư mục Build Target (Để phần mềm quét thấy ngay lập tức)
                    try {
                        java.net.URL resource = getClass().getClassLoader().getResource("images");
                        if (resource != null) {
                            File targetDestDir = new File(resource.toURI());
                            File targetDestFile = new File(targetDestDir, finalImageName);
                            java.nio.file.Files.copy(sourceFile.toPath(), targetDestFile.toPath(),
                                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        }
                    } catch (Exception e) {
                        System.out.println("Lưu ý đồng bộ ảnh sang thư mục build: " + e.getMessage());
                    }
                }
            }

            // 4. Khởi tạo đối tượng Product để truyền xuống Service
            Product productToSave = new Product();
            productToSave.setProductName(proName);
            productToSave.setCategory(cat);
            productToSave.setBaseUnit(impUnit);
            productToSave.setImportPrice(importPrice);
            productToSave.setMarkupRate(markupRate);
            productToSave.setImageName(finalImageName);
            productToSave.setStatus("active");

            // Thực hiện lưu thông qua service trả về kết quả boolean

            boolean isSuccess = productService.createProduct(productToSave);

            if (isSuccess) {
                // 5. Lưu đơn vị tính quy đổi phụ vào bảng 'product_units'
                productService.addUnit(
                        productToSave.getId(), // ID tự động sinh đã được nạp ngược vào thực thể này
                        sellUnit,
                        ratio,
                        barcode,
                        sellingPricePerUnit,
                        true);

                JOptionPane.showMessageDialog(this, "Thêm sản phẩm thành công vào cơ sở dữ liệu!");
                this.dispose(); // Đóng form thêm mới sản phẩm
            } else {
                JOptionPane.showMessageDialog(this, "Không thể lưu sản phẩm. Vui lòng kiểm tra lại.");
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Định dạng số không hợp lệ! Vui lòng kiểm tra lại các ô Giá nhập, Giá bán và Tỉ lệ.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi xử lý hệ thống hoặc lỗi DB: " + ex.getMessage());
        }
    }// GEN-LAST:event_addProBtnMouseClicked

    private void statusComboBoxActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_statusComboBoxActionPerformed

    }// GEN-LAST:event_statusComboBoxActionPerformed

    private void addProBtnActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_addProBtnActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_addProBtnActionPerformed

    private void scanBtnActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_scanBtnActionPerformed
        // 1. Khởi tạo Camera mặc định của máy tính
        Webcam webcam = Webcam.getDefault();
        if (webcam == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy Camera/Webcam kết nối với máy tính!");
            return;
        }

        // Đặt độ phân giải cho Camera
        webcam.setViewSize(WebcamResolution.VGA.getSize());

        // Tạo một Panel hiển thị luồng hình ảnh của Camera
        WebcamPanel panel = new WebcamPanel(webcam);
        panel.setFPSDisplayed(true);
        panel.setDisplayDebugInfo(false);
        panel.setImageSizeDisplayed(false);
        panel.setMirrored(false);

        // 2. Tạo một cửa sổ JDialog nhỏ để hiện Camera lên màn hình
        JDialog scanDialog = new JDialog(this, "ĐANG QUÉT MÃ QUA CAMERA", true);
        scanDialog.add(panel);
        scanDialog.pack();
        scanDialog.setLocationRelativeTo(this);

        // 3. Tạo một Thread (luồng chạy ngầm) để chụp ảnh từ camera và phân tích mã
        // liên tục
        Thread scanThread = new Thread(() -> {
            try {
                while (webcam.isOpen()) {
                    BufferedImage image = webcam.getImage();
                    if (image != null) {
                        LuminanceSource source = new BufferedImageLuminanceSource(image);
                        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

                        try {
                            // Thư viện ZXing tiến hành đọc mã vạch từ ảnh
                            Result result = new MultiFormatReader().decode(bitmap);

                            if (result != null) {
                                String barcodeResult = result.getText().trim();

                                // Đọc thành công! Đẩy mã vạch vào ô TextBox trên Form chính
                                barcodeTxt.setText(barcodeResult);

                                // Phát tiếng beep và đóng camera, đóng cửa sổ quét
                                java.awt.Toolkit.getDefaultToolkit().beep();
                                webcam.close();
                                scanDialog.dispose();
                                break;
                            }
                        } catch (Exception e) {
                            // Cứ sau vài mili-giây nếu không tìm thấy mã vạch trong khung hình thì bỏ qua,
                            // quét tiếp khung hình sau
                        }
                    }
                    Thread.sleep(100); // Nghỉ 100ms trước khi quét hình tiếp theo để tránh treo máy
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
    }// GEN-LAST:event_scanBtnActionPerformed

    private void addImgBtnMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_addImgBtnMouseClicked
        // TODO add your handling code here:
    }// GEN-LAST:event_addImgBtnMouseClicked

    private String selectedImagePath = null;

    private void addImgBtnActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_addImgBtnActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        // Chỉ cho phép chọn file ảnh
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Hình ảnh", "jpg", "png", "jpeg");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            selectedImagePath = selectedFile.getAbsolutePath(); // Lưu đường dẫn tạm

            // Hiển thị ảnh lên Label (Cần viết hàm scale ảnh cho vừa vặn Label)
            ImageIcon icon = new ImageIcon(selectedImagePath);
            Image img = icon.getImage().getScaledInstance(lblImagePreview.getWidth(), lblImagePreview.getHeight(),
                    Image.SCALE_SMOOTH);
            lblImagePreview.setIcon(new ImageIcon(img));
        }
    }// GEN-LAST:event_addImgBtnActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        // <editor-fold defaultstate="collapsed" desc=" Look and feel setting code
        // (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the default
         * look and feel.
         * For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        // </editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new AddProductFrame().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addImgBtn;
    private javax.swing.JButton addProBtn;
    private javax.swing.JTextField barcodeTxt;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JTextField catTxt;
    private javax.swing.JTextField discountTxt1;
    private javax.swing.JTextField impPriceTxt;
    private javax.swing.JTextField impUnitTxt;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel lblImagePreview;
    private javax.swing.JLabel markupRateTxt;
    private javax.swing.JTextField proTxt;
    private javax.swing.JTextField ratioTxt;
    private javax.swing.JButton scanBtn;
    private javax.swing.JTextField sellPriceTxt;
    private javax.swing.JTextField selllUnitTxt;
    private javax.swing.JComboBox<String> statusComboBox;
    // End of variables declaration//GEN-END:variables
}
