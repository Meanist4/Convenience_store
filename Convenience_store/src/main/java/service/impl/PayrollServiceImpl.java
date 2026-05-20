package service.impl;


import service.*;
import repository.EmployeeRepository;
import repository.PayrollRepository;
import repository.PayrollRepository.PayrollRecord;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

public class PayrollServiceImpl implements PayrollService {
    private final PayrollRepository payrollRepo = new PayrollRepository();
    private final EmployeeRepository employeeRepo = new EmployeeRepository();
    @Override public PayrollRecord calculateForEmployee(int employeeId, int year, int month) throws SQLException { validateYearMonth(year, month); employeeRepo.findById(employeeId).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên id=" + employeeId)); PayrollRecord rec = payrollRepo.calculateForEmployee(employeeId, year, month); if (rec == null) throw new IllegalStateException("Không thể tính lương cho nhân viên id=" + employeeId); return rec; }
    @Override public List<PayrollRecord> calculateByStore(int storeId, int year, int month) throws SQLException { validateYearMonth(year, month); return payrollRepo.calculateByStore(storeId, year, month); }
    @Override public BigDecimal getTotalPayroll(int year, int month) throws SQLException { validateYearMonth(year, month); return payrollRepo.totalPayrollByMonth(year, month); }
    @Override public Map<String, BigDecimal> getWorkHourHistory(int employeeId, int months) throws SQLException { if (months <= 0) throw new IllegalArgumentException("Số tháng phải > 0"); employeeRepo.findById(employeeId).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên id=" + employeeId)); return payrollRepo.workHourHistory(employeeId, months); }
    @Override public PayrollRecord calculateCurrentMonth(int employeeId) throws SQLException { YearMonth now = YearMonth.now(); return calculateForEmployee(employeeId, now.getYear(), now.getMonthValue()); }
    private void validateYearMonth(int year, int month) { if (year < 2000 || year > 2100) throw new IllegalArgumentException("Năm không hợp lệ: " + year); if (month < 1 || month > 12) throw new IllegalArgumentException("Tháng không hợp lệ: " + month); }
}

