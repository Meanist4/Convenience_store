/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package view;

import entity.Employee;
import entity.Store;
import java.awt.Component;
import java.math.BigDecimal;
import java.sql.SQLException;
//import java.util.Date;
import java.util.List;
import java.sql.Date;
import util.*;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JOptionPane;
import repository.StoreRepository;
import service.impl.EmployeeServiceImpl;

/**
 *
 * @author admin
 */
// Default constructor
public class AddEmpDialog extends javax.swing.JDialog {
        public Employee curEmployee;

        public AddEmpDialog(java.awt.Dialog parent, boolean modal) {
                super(parent, modal);
                initComponents();
        }

        // Constructor to edit emp and load data to form
        public AddEmpDialog(java.awt.Frame parent, boolean modal, Employee emp) {
                super(parent, modal);
                curEmployee = emp;
                initComponents();
                fullNameTxt.setText(emp.getFullName());
                jCalendar1.setDate(emp.getBirthday());
                // Sửa lại radio btn sau khi truyênf
                if (emp.getGender().equals("Nam")) {
                        genderNamRadioBTN.setSelected(true);
                } else if (emp.getGender().equals("Nu")) {
                        genderNuRadioBTN.setSelected(true);
                } else if (emp.getGender().equals("Khac")) {
                        genderKhacRadioBTN.setSelected(true);
                }
                idCardTxt.setText(emp.getIdCard());
                phoneNumberTxt.setText(emp.getPhone());
                emailAddressTxt.setText(emp.getEmail());
                addressTxt.setText(emp.getAddress());
                storeIdComboBox.setSelectedItem(emp.getStoreId());
                hourlyRateTxt.setText(emp.getHourlyRate().toString());
                setupStoreComboBoxRenderer();
                try {
                        loadCbbStore();
                } catch (SQLException e) {
                        logger.log(java.util.logging.Level.SEVERE, "Không thể tải danh sách cửa hàng", e);
                        JOptionPane.showMessageDialog(this,
                                        "Không thể tải danh sách cửa hàng: " + e.getMessage(),
                                        "Lỗi",
                                        JOptionPane.ERROR_MESSAGE);
                }
        }

        EmployeeServiceImpl empSrv = new EmployeeServiceImpl();

        private static final java.util.logging.Logger logger = java.util.logging.Logger
                        .getLogger(AddEmpDialog.class.getName());

        /**
         * storeId của cửa hàng đang chọn trên combobox
         */
        private Integer selectedStoreId;

        /**
         * Creates new form AddEmpDialog
         */
        public void loadCbbStore() throws SQLException {
                DefaultComboBoxModel<Store> model = new DefaultComboBoxModel<>();
                List<Store> stores = new StoreRepository().findAll();
                for (Store store : stores) {
                        model.addElement(store);
                }
                storeIdComboBox.setModel((javax.swing.ComboBoxModel) model);
                if (model.getSize() > 0) {
                        storeIdComboBox.setSelectedIndex(0);
                        updateSelectedStoreId();
                }
        }

        private void setupStoreComboBoxRenderer() {
                storeIdComboBox.setRenderer(new DefaultListCellRenderer() {
                        @Override
                        public Component getListCellRendererComponent(JList<?> list, Object value,
                                        int index, boolean isSelected, boolean cellHasFocus) {
                                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                                if (value instanceof Store store) {
                                        setText(store.getStoreName() + " - " + store.getLocation());
                                }
                                return this;
                        }
                });
        }

        private void updateSelectedStoreId() {
                Store selected = (Store) storeIdComboBox.getSelectedItem();
                selectedStoreId = selected != null ? selected.getId() : null;
        }

        public Integer getSelectedStoreId() {
                return selectedStoreId;
        }

        public AddEmpDialog() {
                initComponents();
                setupStoreComboBoxRenderer();
                try {
                        loadCbbStore();
                } catch (SQLException e) {
                        logger.log(java.util.logging.Level.SEVERE, "Không thể tải danh sách cửa hàng", e);
                        JOptionPane.showMessageDialog(this,
                                        "Không thể tải danh sách cửa hàng: " + e.getMessage(),
                                        "Lỗi",
                                        JOptionPane.ERROR_MESSAGE);
                }
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

                jLabel1 = new javax.swing.JLabel();
                fullNameTxt = new javax.swing.JTextField();
                jLabel2 = new javax.swing.JLabel();
                jLabel3 = new javax.swing.JLabel();
                jLabel4 = new javax.swing.JLabel();
                genderNamRadioBTN = new javax.swing.JRadioButton();
                genderNuRadioBTN = new javax.swing.JRadioButton();
                genderKhacRadioBTN = new javax.swing.JRadioButton();
                jLabel5 = new javax.swing.JLabel();
                idCardTxt = new javax.swing.JTextField();
                jLabel6 = new javax.swing.JLabel();
                phoneNumberTxt = new javax.swing.JTextField();
                jLabel7 = new javax.swing.JLabel();
                emailAddressTxt = new javax.swing.JTextField();
                jLabel8 = new javax.swing.JLabel();
                addressTxt = new javax.swing.JTextField();
                jLabel9 = new javax.swing.JLabel();
                storeIdComboBox = new javax.swing.JComboBox<>();
                jLabel11 = new javax.swing.JLabel();
                hourlyRateTxt = new javax.swing.JTextField();
                addEMPBtn = new javax.swing.JButton();
                jCalendar1 = new com.toedter.calendar.JCalendar();

                setResizable(false);

                jLabel1.setText("Họ và Tên");

                fullNameTxt.addActionListener(this::fullNameTxtActionPerformed);

                jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
                jLabel2.setText("Thêm Nhân Viên");

                jLabel3.setText("Ngày Tháng Năm Sinh");

                jLabel4.setText("Giới Tính");

                ButtonGroup genderGroup = new ButtonGroup();
                genderGroup.add(genderNamRadioBTN);
                genderGroup.add(genderNuRadioBTN);
                genderGroup.add(genderKhacRadioBTN);
                genderNamRadioBTN.setSelected(true);
                genderNamRadioBTN.setText("Nam");
                genderNamRadioBTN.addActionListener(this::genderNamRadioBTNActionPerformed);

                genderNuRadioBTN.setText("Nữ");
                genderNuRadioBTN.addActionListener(this::genderNuRadioBTNActionPerformed);

                genderKhacRadioBTN.setText("Khác");
                genderKhacRadioBTN.addActionListener(this::genderKhacRadioBTNActionPerformed);

                jLabel5.setText("Số CMND");

                jLabel6.setText("Số Điện Thoại");

                jLabel7.setText("Địa chỉ Email");

                jLabel8.setText("Địa chỉ thường trú");

                jLabel9.setText("Làm Việc Tại Cửa Hàng");

                storeIdComboBox.setModel(
                                new javax.swing.DefaultComboBoxModel<>(
                                                new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
                storeIdComboBox.addActionListener(this::storeIdComboBoxActionPerformed);

                jLabel11.setText("Lương Theo Giờ");

                addEMPBtn.setText("Thêm");
                addEMPBtn.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                                addEMPBtnMouseClicked(evt);
                        }
                });

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
                getContentPane().setLayout(layout);
                layout.setHorizontalGroup(
                                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(layout.createSequentialGroup()
                                                                .addGap(110, 110, 110)
                                                                .addGroup(layout.createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                .addGroup(layout.createSequentialGroup()
                                                                                                .addGroup(layout
                                                                                                                .createParallelGroup(
                                                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                                                                                                                layout.createSequentialGroup()
                                                                                                                                                .addComponent(jLabel1)
                                                                                                                                                .addGap(96, 96, 96))
                                                                                                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                                                                                                                layout.createSequentialGroup()
                                                                                                                                                .addComponent(jLabel3)
                                                                                                                                                .addGap(30, 30, 30)))
                                                                                                .addGroup(layout
                                                                                                                .createParallelGroup(
                                                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                                                .addComponent(jCalendar1,
                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                189,
                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                .addComponent(fullNameTxt,
                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                300,
                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                                                .addComponent(jLabel5)
                                                                                .addGroup(layout.createParallelGroup(
                                                                                                javax.swing.GroupLayout.Alignment.TRAILING)
                                                                                                .addGroup(layout.createSequentialGroup()
                                                                                                                .addComponent(jLabel4)
                                                                                                                .addGap(103, 103,
                                                                                                                                103)
                                                                                                                .addGroup(layout
                                                                                                                                .createParallelGroup(
                                                                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                                                                .addComponent(idCardTxt,
                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                300,
                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                                .addComponent(genderNamRadioBTN)
                                                                                                                                .addComponent(genderNuRadioBTN)
                                                                                                                                .addComponent(genderKhacRadioBTN)))
                                                                                                .addGroup(layout
                                                                                                                .createParallelGroup(
                                                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                                                .addComponent(jLabel6)
                                                                                                                .addGroup(layout.createSequentialGroup()
                                                                                                                                .addGap(150, 150,
                                                                                                                                                150)
                                                                                                                                .addComponent(phoneNumberTxt,
                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                300,
                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                                                                .addGroup(layout.createSequentialGroup()
                                                                                                                .addComponent(jLabel7)
                                                                                                                .addGap(81, 81, 81)
                                                                                                                .addComponent(emailAddressTxt,
                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                300,
                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))))
                                                                .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                                                185,
                                                                                Short.MAX_VALUE)
                                                                .addGroup(layout.createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                .addComponent(jLabel8)
                                                                                .addGroup(layout.createSequentialGroup()
                                                                                                .addGroup(layout
                                                                                                                .createParallelGroup(
                                                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                                                .addGroup(layout.createSequentialGroup()
                                                                                                                                .addComponent(jLabel11)
                                                                                                                                .addGap(44, 44, 44))
                                                                                                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                                                                                                                layout
                                                                                                                                                .createSequentialGroup()
                                                                                                                                                .addComponent(jLabel9)
                                                                                                                                                .addPreferredGap(
                                                                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                                                                                                .addGroup(layout
                                                                                                                .createParallelGroup(
                                                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                                                .addComponent(addEMPBtn,
                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                188,
                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                .addGroup(layout
                                                                                                                                .createParallelGroup(
                                                                                                                                                javax.swing.GroupLayout.Alignment.LEADING,
                                                                                                                                                false)
                                                                                                                                .addComponent(addressTxt,
                                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                                300,
                                                                                                                                                Short.MAX_VALUE)
                                                                                                                                .addComponent(storeIdComboBox,
                                                                                                                                                0,
                                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                                Short.MAX_VALUE)
                                                                                                                                .addComponent(hourlyRateTxt)))))
                                                                .addGap(106, 106, 106))
                                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout
                                                                .createSequentialGroup()
                                                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                Short.MAX_VALUE)
                                                                .addComponent(jLabel2,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                190,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(504, 504, 504)));
                layout.setVerticalGroup(
                                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(layout.createSequentialGroup()
                                                                .addComponent(jLabel2,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                60,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addGroup(layout.createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                .addComponent(jLabel1)
                                                                                .addComponent(fullNameTxt,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                35,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addComponent(jLabel8)
                                                                                .addComponent(addressTxt,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                35,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                .addGap(18, 18, 18)
                                                                .addGroup(layout.createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                .addComponent(jLabel3)
                                                                                .addGroup(layout.createParallelGroup(
                                                                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                                .addComponent(storeIdComboBox,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                35,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                .addComponent(jLabel9))
                                                                                .addComponent(jCalendar1,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                .addGap(18, 18, 18)
                                                                .addGroup(layout.createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                .addGroup(layout.createSequentialGroup()
                                                                                                .addGroup(layout
                                                                                                                .createParallelGroup(
                                                                                                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                                                .addComponent(hourlyRateTxt,
                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                35,
                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                .addComponent(jLabel11))
                                                                                                .addGap(102, 102, 102)
                                                                                                .addComponent(addEMPBtn,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                37,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                                .addGroup(layout.createSequentialGroup()
                                                                                                .addGroup(layout
                                                                                                                .createParallelGroup(
                                                                                                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                                                .addComponent(jLabel4)
                                                                                                                .addComponent(genderNamRadioBTN))
                                                                                                .addGap(4, 4, 4)
                                                                                                .addComponent(genderNuRadioBTN)
                                                                                                .addGap(18, 18, 18)
                                                                                                .addComponent(genderKhacRadioBTN)
                                                                                                .addGap(19, 19, 19)
                                                                                                .addGroup(layout
                                                                                                                .createParallelGroup(
                                                                                                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                                                .addComponent(jLabel5)
                                                                                                                .addComponent(idCardTxt,
                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                35,
                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                                                .addGap(18, 18, 18)
                                                                                                .addGroup(layout
                                                                                                                .createParallelGroup(
                                                                                                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                                                .addComponent(jLabel6)
                                                                                                                .addComponent(phoneNumberTxt,
                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                35,
                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                                                .addGap(18, 18, 18)
                                                                                                .addGroup(layout
                                                                                                                .createParallelGroup(
                                                                                                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                                                .addComponent(jLabel7)
                                                                                                                .addComponent(emailAddressTxt,
                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                35,
                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))))
                                                                .addContainerGap(189, Short.MAX_VALUE)));

                pack();
        }// </editor-fold>//GEN-END:initComponents

        public String getGender() {
                if (genderNamRadioBTN.isSelected()) {
                        return "Nam";
                } else if (genderNuRadioBTN.isSelected()) {
                        return "Nữ";
                } else if (genderKhacRadioBTN.isSelected()) {
                        return "Khác";
                }
                return null; // Trường hợp không chọn gì (nếu không set mặc định)
        }

        private void fullNameTxtActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_fullNameTxtActionPerformed
                // TODO add your handling code here:
        }// GEN-LAST:event_fullNameTxtActionPerformed

        private void genderNamRadioBTNActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_genderNamRadioBTNActionPerformed
                // TODO add your handling code here:
        }// GEN-LAST:event_genderNamRadioBTNActionPerformed

        private void genderNuRadioBTNActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_genderNuRadioBTNActionPerformed
                // TODO add your handling code here:
        }// GEN-LAST:event_genderNuRadioBTNActionPerformed

        private void genderKhacRadioBTNActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_genderKhacRadioBTNActionPerformed
                // TODO add your handling code here:
        }// GEN-LAST:event_genderKhacRadioBTNActionPerformed

        private void addEMPBtnMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_addEMPBtnMouseClicked
                String fullname = fullNameTxt.getText();
                Date birthDate = new java.sql.Date(jCalendar1.getDate().getTime());
                String gender = getGender();
                String idCard = idCardTxt.getText();
                String phoneNumber = phoneNumberTxt.getText();
                String email = emailAddressTxt.getText();
                String address = addressTxt.getText();
                Integer storeId = getSelectedStoreId();
                BigDecimal hourlyRate = BigDecimal.valueOf(Long.parseLong(hourlyRateTxt.getText()));
                // String status = statusComboBox.getSelectedItem().toString();
                //
                //
                //
                //
                //
                //
                //
                //
                //
                //

                // emp.setFullName(fullname);
                // emp.setGender(gender);
                // emp.setBirthday(birthDate);
                // emp.setIdCard(idCard);
                // emp.setPhone(phoneNumber);
                // emp.setEmail(email);
                // emp.setAddress(address);
                // emp.setStoreId(storeId);
                // emp.setHourlyRate(hourlyRate);
                // emp.setStatus(status);
                if (curEmployee == null) {
                        try {
                                Employee emp = empSrv.createEmployee(fullname, birthDate, gender, idCard, phoneNumber,
                                                email, address,
                                                storeId, hourlyRate);
                                if (emp != null) {
                                        JOptionPane.showMessageDialog(rootPane,
                                                        "Thêm employee thành công ," + fullname);
                                        try {

                                                String barcodeData = util.ShortHash
                                                                .EmployeeBarcodeHash(emp.getIdCard());

                                                util.GenBarcode.genBarcode(barcodeData);

                                        } catch (Exception ex) {
                                        }
                                }
                        } catch (SQLException ex) {
                                System.getLogger(AddEmpDialog.class.getName()).log(System.Logger.Level.ERROR,
                                                (String) null, ex);
                        }
                } else {
                        try {
                                Employee emp = empSrv.updateEmployee(curEmployee.getId(), fullname, birthDate, gender,
                                                idCard,
                                                phoneNumber, email, address, storeId, hourlyRate,
                                                curEmployee.getStatus());
                                if (emp != null) {
                                        JOptionPane.showMessageDialog(rootPane,
                                                        "Cập nhật employee thành công ," + fullname);
                                }
                        } catch (SQLException ex) {
                                System.getLogger(AddEmpDialog.class.getName()).log(System.Logger.Level.ERROR,
                                                (String) null, ex);
                        }
                }
                // setVisible(false);
                dispose();
        }// GEN-LAST:event_addEMPBtnMouseClicked

        private void storeIdComboBoxActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_storeIdComboBoxActionPerformed
                updateSelectedStoreId();
        }// GEN-LAST:event_storeIdComboBoxActionPerformed

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
                        for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager
                                        .getInstalledLookAndFeels()) {
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
                java.awt.EventQueue.invokeLater(() -> new AddEmpDialog().setVisible(true));
        }

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JButton addEMPBtn;
        private javax.swing.JTextField addressTxt;
        private javax.swing.JTextField emailAddressTxt;
        private javax.swing.JTextField fullNameTxt;
        private javax.swing.JRadioButton genderKhacRadioBTN;
        private javax.swing.JRadioButton genderNamRadioBTN;
        private javax.swing.JRadioButton genderNuRadioBTN;
        private javax.swing.JTextField hourlyRateTxt;
        private javax.swing.JTextField idCardTxt;
        private com.toedter.calendar.JCalendar jCalendar1;
        private javax.swing.JLabel jLabel1;
        private javax.swing.JLabel jLabel11;
        private javax.swing.JLabel jLabel2;
        private javax.swing.JLabel jLabel3;
        private javax.swing.JLabel jLabel4;
        private javax.swing.JLabel jLabel5;
        private javax.swing.JLabel jLabel6;
        private javax.swing.JLabel jLabel7;
        private javax.swing.JLabel jLabel8;
        private javax.swing.JLabel jLabel9;
        private javax.swing.JTextField phoneNumberTxt;
        private javax.swing.JComboBox<String> storeIdComboBox;
        // End of variables declaration//GEN-END:variables
}
