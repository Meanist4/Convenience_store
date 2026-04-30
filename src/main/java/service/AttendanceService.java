package service;

import repository.AttendanceRepository;
import repository.EmployeeRepository;
import entity.Employee;
import javax.swing.JOptionPane;

public class AttendanceService {

    private final EmployeeRepository employeeDAO;
    private final AttendanceRepository attendanceDAO;

    public AttendanceService() {
        this.employeeDAO = new EmployeeRepository();
        this.attendanceDAO = new AttendanceRepository();
    }

    public void processAttendance(String barcode) {
        Employee emp = employeeDAO.findByBarcode(barcode);

        if (emp == null) {
            JOptionPane.showMessageDialog(null, "Thẻ không hợp lệ hoặc nhân viên không tồn tại!");
            return;
        }

        if (emp.getStatus() != Employee.Status.ACTIVE) {
            JOptionPane.showMessageDialog(null, "Nhân viên " + emp.getFullName() + " hiện không hoạt động!");
            return;
        }

        int empId = emp.getId();

        if (attendanceDAO.isCurrentlyWorking(empId)) {
            if (attendanceDAO.checkOut(empId)) {
                JOptionPane.showMessageDialog(null, "CHECK-OUT THÀNH CÔNG\nTạm biệt: " + emp.getFullName());
            }
        } else {
            if (attendanceDAO.checkIn(empId)) {
                JOptionPane.showMessageDialog(null, "CHECK-IN THÀNH CÔNG\nChào mừng: " + emp.getFullName() + " vào ca.");
            }
        }
    }
}
