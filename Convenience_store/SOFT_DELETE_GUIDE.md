# 🗑️ Soft Delete Implementation Guide

## Overview

This project implements **Soft Delete** pattern for data preservation and audit trails. Instead of permanently deleting data, records are marked as deleted with timestamps.

---

## Database Schema Updates

### Tables with Soft Delete Columns

#### 1. **Stores**

```sql
ALTER TABLE stores ADD COLUMN is_deleted TINYINT(1) DEFAULT 0;
ALTER TABLE stores ADD COLUMN deleted_at TIMESTAMP NULL;
```

#### 2. **Employees**

```sql
ALTER TABLE employees ADD COLUMN is_deleted TINYINT(1) DEFAULT 0;
ALTER TABLE employees ADD COLUMN deleted_at TIMESTAMP NULL;
```

#### 3. **Admins**

```sql
ALTER TABLE admins ADD COLUMN is_deleted TINYINT(1) DEFAULT 0;
ALTER TABLE admins ADD COLUMN deleted_at TIMESTAMP NULL;
```

#### 4. **Managers**

```sql
ALTER TABLE managers ADD COLUMN is_deleted TINYINT(1) DEFAULT 0;
ALTER TABLE managers ADD COLUMN deleted_at TIMESTAMP NULL;
```

#### 5. **Products**

```sql
ALTER TABLE products ADD COLUMN is_deleted TINYINT(1) DEFAULT 0;
ALTER TABLE products ADD COLUMN deleted_at TIMESTAMP NULL;
```

#### 6. **Product Units**

```sql
ALTER TABLE product_units ADD COLUMN is_deleted TINYINT(1) DEFAULT 0;
ALTER TABLE product_units ADD COLUMN deleted_at TIMESTAMP NULL;
```

### Tables with Status Instead of Soft Delete

#### 7. **Attendance** (Status Pattern)

```sql
ALTER TABLE attendance ADD COLUMN status ENUM('valid', 'invalid', 'rejected') DEFAULT 'valid';
-- Instead of deleting, mark as 'invalid' or 'rejected'
```

#### 8. **Invoices** (Status Pattern)

```sql
ALTER TABLE invoices ADD COLUMN status ENUM('completed', 'cancelled') DEFAULT 'completed';
-- Instead of deleting, mark as 'cancelled'
```

#### 9. **Store Inventory**

```sql
-- No soft delete needed - tracks real quantities
-- Deletions handled through inventory transactions
```

---

## Entity Updates

### Soft Delete Columns in Entities

All entities that support soft delete now have:

```java
private boolean isDeleted;
private Timestamp deletedAt;

// Getters and setters
public boolean isIsDeleted() { return isDeleted; }
public void setIsDeleted(boolean isDeleted) { this.isDeleted = isDeleted; }
public Timestamp getDeletedAt() { return deletedAt; }
public void setDeletedAt(Timestamp deletedAt) { this.deletedAt = deletedAt; }
```

### Status Enum in Entities

**Attendance**:

```java
public enum Status {
    VALID,      // Normal, accepted attendance
    INVALID,    // Flagged as suspicious/error
    REJECTED    // Manager rejected this record
}
```

**Invoice**:

```java
public enum Status {
    COMPLETED,  // Normal, finalized invoice
    CANCELLED   // Invoice was cancelled/voided
}
```

---

## Service Layer Implementation

### Soft Delete Methods

All services supporting soft delete now provide:

```java
// Soft delete - marks record as deleted
boolean softDeleteRecord(int recordId)

// Restore - unmarks deleted record
boolean restoreRecord(int recordId)
```

### Query Filtering

All queries automatically filter out deleted records:

```java
// In repository methods:
WHERE is_deleted = 0   // Only return active records
```

### Service Examples

#### StoreService

```java
// Soft delete
storeService.softDeleteStore(storeId);

// Restore
storeService.restoreStore(storeId);
```

#### EmployeeService

```java
// Soft delete
employeeService.softDeleteEmployee(employeeId);

// Restore
employeeService.restoreEmployee(employeeId);
```

#### InvoiceService

```java
// Cancel invoice (instead of delete)
invoiceService.cancelInvoice(invoiceId);

// Get cancelled invoice for audit
invoiceService.getCancelledInvoice(invoiceId);
```

#### AttendanceService

```java
// Mark as invalid
attendanceService.markAsInvalid(attendanceId);

// Mark as rejected
attendanceService.markAsRejected(attendanceId);

// Restore
attendanceService.restoreAttendance(attendanceId);
```

---

## Usage Patterns

### Pattern 1: Soft Delete

```java
// Delete without data loss
storeService.softDeleteStore(123);

// Later, view all deleted stores (admin only)
List<Store> deleted = storeRepository.findDeleted();

// Restore if needed
storeService.restoreStore(123);
```

### Pattern 2: Status Management

```java
// Cancel invoice
invoiceService.cancelInvoice(invoiceId);

// View cancelled invoices for reporting
Invoice cancelled = invoiceService.getCancelledInvoice(invoiceId);

// Revenue calculation automatically excludes cancelled invoices
BigDecimal revenue = invoiceService.calculateTotalRevenue(invoices);
```

### Pattern 3: Attendance Audit Trail

```java
// If attendance record seems wrong
attendanceService.markAsInvalid(attendanceId);

// Status is preserved for audit trail
// Manager can review and restore if needed
attendanceService.restoreAttendance(attendanceId);
```

---

## Query Examples

### SQL Queries

**Get all active stores**:

```sql
SELECT * FROM stores WHERE is_deleted = 0;
```

**Get all deleted stores (audit)**:

```sql
SELECT * FROM stores WHERE is_deleted = 1;
```

**Permanently delete (after archive)**:

```sql
DELETE FROM stores WHERE is_deleted = 1 AND deleted_at < DATE_SUB(NOW(), INTERVAL 1 YEAR);
```

**Get completed invoices only**:

```sql
SELECT * FROM invoices WHERE status = 'completed';
```

**Get cancelled invoices for audit**:

```sql
SELECT * FROM invoices WHERE status = 'cancelled';
```

---

## Repository Layer

All repositories must be updated to:

1. **Filter deleted records** in SELECT queries:

   ```java
   private static final String FIND_ALL_SQL = "SELECT * FROM employees WHERE is_deleted = 0";
   ```

2. **Include soft delete in UPDATE**:

   ```java
   public boolean softDelete(int id) {
       String sql = "UPDATE employees SET is_deleted = 1, deleted_at = NOW() WHERE id = ?";
       // Execute query
   }
   ```

3. **Provide admin methods** to view deleted records:
   ```java
   public List<T> findDeleted() {
       String sql = "SELECT * FROM table WHERE is_deleted = 1";
       // Execute query
   }
   ```

---

## Benefits

✅ **Data Preservation**: No data loss, can always restore
✅ **Audit Trail**: Know when and what was deleted
✅ **Compliance**: Meets GDPR requirements for data retention
✅ **Reversibility**: Mistakes can be easily corrected
✅ **Analytics**: Can analyze deleted data trends
✅ **Business Logic**: Supports "undo" operations
✅ **Reporting**: Historical data preserved

---

## Migration Strategy

### Phase 1: Database Migration

1. Add soft delete columns to all tables
2. Ensure all queries include `WHERE is_deleted = 0`
3. Test with existing data

### Phase 2: Entity Updates

1. Add `isDeleted` and `deletedAt` fields
2. Update constructors and toString()
3. Generate getters/setters

### Phase 3: Repository Updates

1. Update all SELECT queries with soft delete filter
2. Add `softDelete()` method replacing `delete()`
3. Add `restore()` method
4. Add `findDeleted()` for admin queries

### Phase 4: Service Updates

1. Update service delete methods to call `softDelete()`
2. Add `restore()` methods to all services
3. Update business logic to exclude deleted records
4. Add validation checks for `isDeleted`

---

## Admin Utilities (To Be Created)

### SoftDeleteManager (Recommended)

```java
public class SoftDeleteManager {
    // List all deleted records by entity type
    public List<T> listDeleted(Class<T> entityClass)

    // Permanently delete records older than X days
    public void permanentlyDeleteOldRecords(Class<T> entityClass, int days)

    // Generate audit report
    public AuditReport generateDeleteAuditReport(LocalDate start, LocalDate end)

    // Restore multiple deleted records
    public int bulkRestore(List<Integer> ids, Class<T> entityClass)
}
```

---

## Testing Considerations

### Test Cases for Soft Delete

1. **Create and Delete**

   ```java
   storeService.addStore(store);
   storeService.softDeleteStore(storeId);
   assertNull(storeService.getStore(storeId));  // Should return null (marked deleted)
   ```

2. **Restore**

   ```java
   storeService.restoreStore(storeId);
   assertNotNull(storeService.getStore(storeId));  // Should return restored
   ```

3. **Query Filtering**

   ```java
   storeService.softDeleteStore(storeId);
   List<Store> stores = storeService.getAllStores();
   assertFalse(stores.contains(deletedStore));  // Should not appear in list
   ```

4. **Audit Trail**
   ```java
   storeService.softDeleteStore(storeId);
   Store store = storeRepository.findById(storeId);  // Raw access
   assertTrue(store.isIsDeleted());
   assertNotNull(store.getDeletedAt());
   ```

---

## Important Notes

⚠️ **Version 1.0**: Services include soft delete support
⚠️ **Not Yet Implemented**: Admin deletion APIs (to be added)
⚠️ **Database**: Ensure backup before adding soft delete columns
⚠️ **Backwards Compatibility**: Old hard delete calls should be replaced

---

**Status**: ✅ Entity & Service Layer Ready
**Next**: Repository Layer Implementation & Testing
