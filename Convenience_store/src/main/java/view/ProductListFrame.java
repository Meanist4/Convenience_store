/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package view;

import convenience_store.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.table.DefaultTableModel;
import service.impl.ProductServiceImpl;

/**
 *
 * @author ADMIN
 */
public class ProductListFrame extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ProductListFrame.class.getName());
    ProductServiceImpl productService = new ProductServiceImpl();

    /**
     * Creates new form ProductListFrame
     */
    public void loadProductData(String keyword) {
        String[] columnNames = {"ID", "Mã Barcode", "Tên Sản Phẩm", "Loại Sản Phẩm", "Đơn Vị Tính", "Giá Bán", "Tên Ảnh"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                // Nếu là cột số 6 (cột Hình Ảnh), bắt Java phải hiểu đây là loại ImageIcon
                if (columnIndex == 6) {
                    return javax.swing.ImageIcon.class;
                }
                return Object.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Khóa không cho người dùng sửa trực tiếp trên ô bảng
            }
        };

        productTable.setRowHeight(60);

        String sql = "SELECT p.id, u.barcode, p.product_name, p.category, u.unit_name, u.selling_price, p.image_name "
                + "FROM products p "
                + "LEFT JOIN product_units u ON p.id = u.product_id "
                + "WHERE p.is_deleted = 0 AND (p.product_name LIKE ? OR u.barcode LIKE ?)";

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");

            ResultSet r = ps.executeQuery();
            while (r.next()) {
                // Lấy tên ảnh từ cơ sở dữ liệu
                String imageName = r.getString("image_name");
                if (imageName == null || imageName.trim().isEmpty()) {
                    imageName = "default.png";
                }

                // Xử lý đường dẫn file ảnh (Kiểm tra cả thư mục src và build target của NetBeans)
                String imagePath = "src/main/resources/images/" + imageName;
                java.io.File file = new java.io.File(imagePath);

                // Nếu không tìm thấy file theo đường dẫn trên, đổi sang dùng file default phòng hờ
                if (!file.exists()) {
                    imagePath = "src/main/resources/images/default.png";
                    java.io.File defaultFile = new java.io.File(imagePath);
                    // Nếu thư mục src chưa tạo hoặc sai cấu trúc folder, thử kiểm tra thư mục gốc dự án
                    if (!defaultFile.exists()) {
                        imagePath = "images/" + imageName;
                    }
                }

                // Tiến hành đọc ảnh và co dãn (Scale) vừa khít với ô lưới 50x50 pixel
                javax.swing.ImageIcon finalIcon = null;
                try {
                    java.io.File finalCheckFile = new java.io.File(imagePath);
                    if (finalCheckFile.exists()) {
                        javax.swing.ImageIcon rawIcon = new javax.swing.ImageIcon(imagePath);
                        java.awt.Image scaledImg = rawIcon.getImage().getScaledInstance(50, 50, java.awt.Image.SCALE_SMOOTH);
                        finalIcon = new javax.swing.ImageIcon(scaledImg);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                // Xử lý các giá trị null tránh lỗi phát sinh do LEFT JOIN
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

                // Đẩy dòng dữ liệu vào model (Ném đối tượng finalIcon vào cột cuối cùng)
                model.addRow(new Object[]{
                    r.getInt("id"),
                    barcode,
                    r.getString("product_name"),
                    r.getString("category"),
                    unitName,
                    priceStr,
                    finalIcon // Truyền đối tượng Icon đã xử lý vẽ đồ họa vào đây
                });
            }

            // Cập nhật lại model mới tinh cho productTable
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        searchTxt = new javax.swing.JTextPane();
        jLabel1 = new javax.swing.JLabel();
        searchBtn = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        productTable = new javax.swing.JTable();
        lblImagePreview = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jScrollPane2.setViewportView(searchTxt);

        jLabel1.setText("Tìm kiếm:");

        searchBtn.addActionListener(this::searchBtnActionPerformed);

        productTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(productTable);

        jScrollPane3.setViewportView(jScrollPane1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 648, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lblImagePreview, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(searchBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addGap(17, 17, 17))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(searchBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1))))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblImagePreview, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(138, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void searchBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_searchBtnActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
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
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new ProductListFrame().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lblImagePreview;
    private javax.swing.JTable productTable;
    private javax.swing.JButton searchBtn;
    private javax.swing.JTextPane searchTxt;
    // End of variables declaration//GEN-END:variables
}
