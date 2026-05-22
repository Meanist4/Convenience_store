/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package view;

import convenience_store.DBConnection;
import entity.Product;
import entity.ProductUnit;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import service.impl.ProductServiceImpl;

/**
 *
 * @author ADMIN
 */
public class ProductListFrame extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger
            .getLogger(ProductListFrame.class.getName());
    ProductServiceImpl productService = new ProductServiceImpl();

    /**
     * Creates new form ProductListFrame
     */
    public void loadProductData(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            keyword = "";
        }

        String[] columnNames = { "ID", "Mã Barcode", "Tên Sản Phẩm", "Loại Sản Phẩm", "Đơn Vị Tính", "Giá Bán", "Ảnh",
                "productUnit_id", "base_unit", "ratio", "is_default_sale", "import_price", "markup_rate", "status" };
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 6) {
                    return javax.swing.ImageIcon.class;
                }
                return Object.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        productTable.setRowHeight(60);
        // Ẩn cột thứ 7 trở đi
        for (int i = 7; i < columnNames.length; i++) {
            productTable.getColumnModel().getColumn(i).setMinWidth(0);
            productTable.getColumnModel().getColumn(i).setMaxWidth(0);
            productTable.getColumnModel().getColumn(i).setPreferredWidth(0);
            productTable.getColumnModel().getColumn(i).setResizable(false);
        }
        // Giữ nguyên câu lệnh SQL tuyệt vời có LEFT JOIN này của bạn
        String sql = "SELECT p.id, u.barcode, p.product_name, p.category, u.unit_name, u.selling_price, p.image_name, u.id , p.base_unit , u.ratio , u.is_default_sale,p.import_price, p.markup_rate,p.status"
                // SELECT p.id, u.barcode, p.product_name, p.category, u.unit_name,
                // u.selling_price, p.image_name, u.id , p.base_unit , u.ratio ,
                // u.is_default_sale,p.import_price, p.markup_rate,p.status
                + "FROM products p "
                + "LEFT JOIN product_units u ON p.id = u.product_id "
                + "WHERE p.is_deleted = 0 AND (p.product_name LIKE ? OR u.barcode LIKE ?)";

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");

            ResultSet r = ps.executeQuery();
            while (r.next()) {
                String imageName = r.getString("image_name");
                if (imageName == null || imageName.trim().isEmpty()) {
                    imageName = "default.png";
                }

                String imagePath = "src/main/resources/images/" + imageName;
                java.io.File file = new java.io.File(imagePath);

                if (!file.exists()) {
                    imagePath = "src/main/resources/images/default.png";
                    java.io.File defaultFile = new java.io.File(imagePath);
                    if (!defaultFile.exists()) {
                        imagePath = "images/" + imageName;
                    }
                }

                javax.swing.ImageIcon finalIcon = null;
                try {
                    java.io.File finalCheckFile = new java.io.File(imagePath);
                    if (finalCheckFile.exists()) {
                        javax.swing.ImageIcon rawIcon = new javax.swing.ImageIcon(imagePath);
                        java.awt.Image scaledImg = rawIcon.getImage().getScaledInstance(50, 50,
                                java.awt.Image.SCALE_SMOOTH);
                        finalIcon = new javax.swing.ImageIcon(scaledImg);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                String barcode = r.getString("barcode");
                if (barcode == null) {
                    barcode = "Chưa có";
                }

                String unitName = r.getString("unit_name");
                if (unitName == null) {
                    unitName = "Chưa có";
                }

                double price = r.getDouble("selling_price");
                String priceStr = r.wasNull() ? "0 đ" : String.format("%,.0f", price) + " đ";

                model.addRow(new Object[] {
                        r.getInt("id"),
                        barcode,
                        r.getString("product_name"),
                        r.getString("category"),
                        unitName,
                        priceStr,
                        finalIcon,
                        r.getInt("id"),
                        r.getString("base_unit"),
                        r.getInt("ratio"),
                        r.getBoolean("is_default_sale"),
                        r.getDouble("import_price"),
                        r.getDouble("markup_rate"),
                        r.getString("status")

                });
            }

            productTable.setModel(model);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ProductListFrame() {
        initComponents();

        loadProductData("");
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

        jScrollPane2 = new javax.swing.JScrollPane();
        txtTimKiem = new javax.swing.JTextPane();
        jLabel1 = new javax.swing.JLabel();
        searchBtn = new javax.swing.JButton();
        lblImagePreview = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        productTable = new javax.swing.JTable();
        updateProductBtn = new javax.swing.JToggleButton();
        refreshBtn = new javax.swing.JToggleButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jScrollPane2.setViewportView(txtTimKiem);

        jLabel1.setText("Tìm kiếm:");

        searchBtn.setText("Tìm kiếm");
        searchBtn.addActionListener(this::searchBtnActionPerformed);

        productTable.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][] {
                        { null, null, null, null },
                        { null, null, null, null },
                        { null, null, null, null },
                        { null, null, null, null }
                },
                new String[] {
                        "Title 1", "Title 2", "Title 3", "Title 4"
                }));
        jScrollPane1.setViewportView(productTable);

        updateProductBtn.setText("Sửa Thông Tin Sản Phẩm");
        updateProductBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                updateProductBtnMouseClicked(evt);
            }
        });

        refreshBtn.setText("Làm Mới");
        refreshBtn.addActionListener(this::refreshBtnActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 636,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(lblImagePreview, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        142, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(38, 38, 38)
                                                .addComponent(updateProductBtn, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        175, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(38, 38, 38)
                                                .addComponent(refreshBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 112,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 54,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 126,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(searchBtn)
                                                .addGap(25, 25, 25)))
                                .addGap(17, 17, 17)));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(13, 13, 13)
                                                .addGroup(layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addComponent(jScrollPane2,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jLabel1)
                                                        .addComponent(searchBtn, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                22, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGap(6, 6, 6))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout
                                                .createSequentialGroup()
                                                .addContainerGap()
                                                .addGroup(layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(updateProductBtn)
                                                        .addComponent(refreshBtn))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(183, 183, 183)
                                                .addComponent(lblImagePreview, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        130, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 240,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(439, Short.MAX_VALUE)));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void searchBtnActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_searchBtnActionPerformed
        // 1. Lấy từ khóa từ ô textfield
        String keyword = txtTimKiem.getText();

        // Nếu ô tìm kiếm trống, truyền chuỗi rỗng vào để hiển thị toàn bộ
        if (keyword == null) {
            keyword = "";
        }

        // 2. Gọi lại hàm load dữ liệu gốc (Hàm này tự tạo model mới, tự quét SQL có
        // JOIN, tự render ảnh)
        loadProductData(keyword.trim());
    }// GEN-LAST:event_searchBtnActionPerformed

    private void refreshBtnActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_refreshBtnActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_refreshBtnActionPerformed

    private void updateProductBtnMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_updateProductBtnMouseClicked
        // TODO add your handling code here:
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow != -1) {
            // "SELECT p.id, u.barcode, p.product_name, p.category, u.unit_name,
            // u.selling_price, p.image_name, u.id , p.base_unit , u.ratio ,
            // u.is_default_sale,p.import_price, p.markup_rate,p.status"
            int productId = (int) productTable.getValueAt(selectedRow, 1);
            String barcode = (String) productTable.getValueAt(selectedRow, 2);
            String productName = (String) productTable.getValueAt(selectedRow, 3);
            String category = (String) productTable.getValueAt(selectedRow, 4);
            String unitName = (String) productTable.getValueAt(selectedRow, 5);
            BigDecimal sellingPrice = (BigDecimal) productTable.getValueAt(selectedRow, 6);
            String imageName = (String) productTable.getValueAt(selectedRow, 7);
            String baseUnit = (String) productTable.getValueAt(selectedRow, 8);
            int ratio = (int) productTable.getValueAt(selectedRow, 9);
            boolean isDefaultSale = (boolean) productTable.getValueAt(selectedRow, 10);
            BigDecimal importPrice = (BigDecimal) productTable.getValueAt(selectedRow, 11);
            double markupRate = (double) productTable.getValueAt(selectedRow, 12);
            String status = (String) productTable.getValueAt(selectedRow, 13);
            Product product = new Product();
            product.setId(productId);
            product.setProductName(productName);
            product.setCategory(category);
            product.setBaseUnit(baseUnit);
            product.setImportPrice(importPrice);
            product.setMarkupRate(BigDecimal.valueOf(markupRate));
            product.setStatus(status);
            product.setImageName(imageName);
            ProductUnit productUnit = new ProductUnit();
            productUnit.setProductId(productId);
            productUnit.setUnitName(unitName);
            productUnit.setSellingPrice(sellingPrice);
            productUnit.setRatio(ratio);
            productUnit.setDefaultSale(isDefaultSale);

        } else {
            JOptionPane.showMessageDialog(rootPane, "Hãy Chọn 1 Sản Phẩm Để Sửa");
        }
    }// GEN-LAST:event_updateProductBtnMouseClicked

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
        java.awt.EventQueue.invokeLater(() -> new ProductListFrame().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblImagePreview;
    private javax.swing.JTable productTable;
    private javax.swing.JToggleButton refreshBtn;
    private javax.swing.JButton searchBtn;
    private javax.swing.JTextPane txtTimKiem;
    private javax.swing.JToggleButton updateProductBtn;
    // End of variables declaration//GEN-END:variables
}
