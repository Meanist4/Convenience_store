# Quick Reference: Production-Ready Code

## Location 1: ApproveProductFrame.java (Lines 229-355)

### Method Signature

```java
private void executeApproval()
```

### Key Features

- ✅ **Validates empty orders** before processing
- ✅ **Extracts correct JTable columns:**
  - Column 0: Product ID
  - Column 1: Quantity (raw, e.g., 2)
  - Column 2: Import Price (raw, e.g., 650000.00)
  - Column 3: SKIPPED
  - Column 4: Batch Code
  - Column 5: Expiry Date (yyyy-MM-dd)
- ✅ **Validates all inputs** with NumberFormatException handling
- ✅ **All JOptionPane calls** have 4 required arguments
- ✅ **Separate error handling** for SQLException and general exceptions
- ✅ **Proper logging** using logger.log()

### Entry Point

```java
// User clicks "Approve" button
btnApprove.addActionListener(e -> executeApproval());
```

### Exit Conditions

- ✅ Success: `"Duyệt đơn nhập kho thành công!"` message, UI refreshed
- ✗ Error: User-friendly error message with line number/detail
- ✗ Validation Fail: Specific error message about what failed

---

## Location 2: PurchaseOrderServiceImpl.java (Lines 448-549)

### Method Signature

```java
@Override
public void approveAndImportInventory(int orderId, int storeId, List<InventoryImportDTO> importItems)
        throws SQLException
```

### Parameters

- `orderId`: ID of the purchase order to approve
- `storeId`: ID of the store receiving inventory
- `importItems`: List of InventoryImportDTO with:
  - `productId`: Product ID (validated > 0)
  - `quantity`: Raw quantity from JTable (validated > 0)
  - `importPrice`: Raw price from JTable (validated >= 0)
  - `batchCode`: User-entered batch code (validated non-empty)
  - `expiryDate`: User-entered expiry date (validated non-null)
  - `conversionRatio`: Always 1 (hardcoded)

### SQL Operations

**Step 1: Update Order Status**

```sql
UPDATE purchase_orders
SET status = 'received', received_at = NOW()
WHERE id = ?
```

**Step 2: Insert into Store Inventory** (for each item)

```sql
INSERT INTO store_inventory (store_id, product_id, batch_code, quantity, import_price, expiry_date, received_at, min_stock_level)
VALUES (?, ?, ?, ?, ?, ?, NOW(), 5)
```

### Critical Points

- ✅ **NO calculations**: quantity and import_price passed raw
- ✅ **Correct columns**: Uses `import_price` NOT `price`
- ✅ **Complete validation**: All fields checked before insert
- ✅ **Transaction atomicity**: All-or-nothing using BEGIN/COMMIT/ROLLBACK
- ✅ **Resource cleanup**: All statements and connections properly closed
- ✅ **Exception safety**: Rollback on any error

### Return Value

- Success: Void (no exception thrown)
- Error: SQLException with detailed message

---

## Database Schema (VERIFIED)

### Table: store_inventory (Critical for this operation)

```
Column Name          Type          Constraints
id                   INT           AUTO_INCREMENT PRIMARY KEY
store_id             INT           FOREIGN KEY → stores.id
product_id           INT           FOREIGN KEY → products.id
batch_code           VARCHAR(50)   NOT NULL
quantity             INT           NOT NULL
import_price         DECIMAL(12,2) NOT NULL ← CORRECT COLUMN NAME
expiry_date          DATE          NULLABLE
received_at          TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
min_stock_level      INT           DEFAULT 5
```

### Common Mistakes (Avoid!)

```
❌ INSERT INTO store_inventory (... price ...) ← WRONG! Column doesn't exist
❌ INSERT INTO store_inventory (... Price ...) ← WRONG! Case-sensitive
❌ INSERT INTO products (... price ...) ← WRONG! Use 'import_price'
❌ quantity = quantity * conversionRatio ← WRONG! Pass raw value
❌ importPrice = importPrice / ratio ← WRONG! Pass raw value
```

---

## Testing Script

### Test Case 1: Valid Approval

```
1. Create a purchase order with 1+ items
2. Click "Approve" button
3. Enter batch codes for all items
4. Enter expiry dates in yyyy-MM-dd format
5. Click "Approve" again

Expected Result:
- ✅ "Duyệt đơn nhập kho thành công!" message
- ✅ purchase_orders.status = 'received'
- ✅ store_inventory has new rows with correct values
- ✅ No SQL exceptions
```

### Test Case 2: Missing Batch Code

```
1. Create a purchase order
2. Click "Approve" button
3. Leave batch code empty
4. Click "Approve" again

Expected Result:
- ✅ "Vui lòng nhập Mã Lô tại dòng X." error message
- ✅ Transaction NOT executed
- ✅ Database unchanged
```

### Test Case 3: Invalid Expiry Date Format

```
1. Create a purchase order
2. Click "Approve" button
3. Enter batch codes
4. Enter expiry date as "2025/12/31" (wrong format)
5. Click "Approve" again

Expected Result:
- ✅ "Định dạng Hạn Sử Dụng... yyyy-MM-dd" error message
- ✅ Transaction NOT executed
- ✅ Database unchanged
```

### Test Case 4: Negative Quantity

```
1. Create a purchase order
2. Click "Approve" button
3. Manually edit quantity to -5
4. Click "Approve" again

Expected Result:
- ✅ "Số lượng tại dòng X phải lớn hơn 0." error message
- ✅ Transaction NOT executed
- ✅ Database unchanged
```

### Test Case 5: Invalid Price Format

```
1. Create a purchase order
2. Click "Approve" button
3. Manually edit price to "abc"
4. Click "Approve" again

Expected Result:
- ✅ "Giá nhập tại dòng X không hợp lệ." error message
- ✅ Transaction NOT executed
- ✅ Database unchanged
```

---

## Deployment Checklist

- [ ] Backup database before deployment
- [ ] Verify all compilation errors are 0
- [ ] Run Test Cases 1-5 above
- [ ] Verify store_inventory.import_price is populated correctly
- [ ] Verify purchase_orders.status = 'received' after approval
- [ ] Verify no old 'price' column references remain
- [ ] Monitor logs for Level.SEVERE messages
- [ ] Test with actual data volume
- [ ] Verify UI refresh works after approval
- [ ] Document any schema differences from spec

---

## Troubleshooting

### Error: "Unknown column 'price'"

- ✅ **FIXED**: Refactored code now uses `'import_price'`
- Check if other parts of code still use wrong column name

### Error: "Incorrect quantity calculation"

- ✅ **FIXED**: Raw values passed directly, no calculations
- Verify InventoryImportDTO.quantity = JTable value

### Error: "Expiry date not saved"

- ✅ **FIXED**: Correct column mapping in SQL
- Verify format is yyyy-MM-dd

### Error: "Transaction never commits"

- ✅ **FIXED**: Clear commit logic in finally block
- Check database connection permissions

### Error: "Previous data still visible"

- ✅ **FIXED**: Proper rollback on any error
- Verify database didn't create uncommitted locks

---

## Support References

1. **REFACTORING_SUMMARY.md** - Complete overview of all changes
2. **BEFORE_AFTER_COMPARISON.md** - Detailed side-by-side comparison
3. **DATABASE_SCHEMA_UPDATES.sql** - Verify table structure
4. **Service Layer**: PurchaseOrderServiceImpl.java
5. **UI Layer**: ApproveProductFrame.java
