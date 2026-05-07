package service;

import repository.PayrollRepository.PayrollRecord;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface PayrollService {
    PayrollRecord calculateForEmployee(int employeeId, int year, int month) throws SQLException;
    List<PayrollRecord> calculateByStore(int storeId, int year, int month) throws SQLException;
    BigDecimal getTotalPayroll(int year, int month) throws SQLException;
    Map<String, BigDecimal> getWorkHourHistory(int employeeId, int months) throws SQLException;
    PayrollRecord calculateCurrentMonth(int employeeId) throws SQLException;
}
