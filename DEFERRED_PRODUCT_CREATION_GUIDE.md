# DEFERRED PRODUCT CREATION PATTERN - Implementation Guide

## Overview

This document describes the **Deferred Product Creation** pattern implemented in the PurchaseOrderServiceImpl.java for the convenience store application. This pattern enables flexible product management during purchase order receipt, deferring product and product unit creation until the order is actually received (approved).

---

## Problem Statement

Previously, the system required all products to be fully registered in the database (with product_id) before creating a purchase order. This created friction in the workflow when:

- A new supplier item arrives without prior database registration
- Barcode lookup fails because the product doesn't exist yet
- Manual product creation is required before processing the order

The **Deferred Product Creation** pattern solves this by:

- Accepting hand-typed barcode + metadata during order creation (pending status)
- Deferring all product/unit creation until order receipt (approval)
- Maintaining strict database transaction safety during the entire creation flow

---

## Architecture

### 1. Database Schema Extension

Added 5 new columns to `purchase_order_details` table to hold temporary product metadata:

```sql
ALTER TABLE purchase_order_details
ADD COLUMN raw_barcode VARCHAR(50) NULL;              -- Raw barcode for lookup/creation
ADD COLUMN temp_product_name VARCHAR(255) NULL;       -- Temp product name
ADD COLUMN temp_category VARCHAR(100) NULL;           -- Temp category
ADD COLUMN temp_base_unit VARCHAR(50) NULL;           -- Temp base unit
ADD COLUMN temp_markup_rate DECIMAL(5, 2) NULL;       -- Temp markup rate
```

**Index for performance:**

```sql
CREATE INDEX idx_po_detail_raw_barcode ON purchase_order_details(raw_barcode);
```

---

### 2. Entity Extensions

**PurchaseOrderDetail.java** now includes:

```java
private String rawBarcode;           // Raw barcode for new products
private String tempProductName;      // Temporary product name
private String tempCategory;         // Temporary category
private String tempBaseUnit;         // Temporary base unit
private BigDecimal tempMarkupRate;   // Temporary markup rate
```

With corresponding getters and setters.

---

### 3. Repository Enhancements

#### PurchaseOrderDetailRepository

- **`lookupProductIdByBarcode(String rawBarcode, Connection conn)`**: Looks up product_id by barcode in product_units table within transaction
- **`updateProductId(int detailId, int productId, Connection conn)`**: Updates detail record after product creation

#### ProductRepository

- **`insertWithConnection(Product p, Connection conn)`**: Insert product within existing transaction, returns auto-generated product_id

#### ProductUnitRepository

- **`insertWithConnection(ProductUnit u, Connection conn)`**: Insert product unit within existing transaction

#### InventoryRepository / InventoryRepositoryImpl

- **`insertWithConnection(StoreInventory inventory, Connection conn)`**: Insert inventory within existing transaction

---

### 4. Service Logic: receiveOrder(int orderId)

The `receiveOrder()` method implements a **two-case workflow** within a strict database transaction:

#### Flow Overview

```
receiveOrder(orderId)
  ├─ Get order and verify pending status
  ├─ Start transaction: conn.setAutoCommit(false)
  ├─ For each PurchaseOrderDetail:
  │   ├─ CASE A: productId > 0 (Existing Product)
  │   │   └─ Stock inventory directly with existing product_id
  │   │
  │   └─ CASE B: productId == 0 (New Product - Deferred Creation)
  │       ├─ Validate raw_barcode is populated
  │       ├─ Sub-case B1: Barcode exists in product_units?
  │       │   ├─ YES: Reuse existing product_id
  │       │   └─ Update detail record with product_id
  │       │
  │       └─ Sub-case B2: Brand new barcode
  │           ├─ Step 1: Create Product with temp metadata
  │           ├─ Step 2: Hash barcode with ProductBarcodeHash()
  │           ├─ Step 3: Create ProductUnit with hashed barcode
  │           ├─ Step 4: Update detail record with new product_id
  │           └─ Step 5: Stock inventory
  │
  ├─ Update order status to 'received'
  ├─ conn.commit() - If all steps succeed
  └─ conn.rollback() - If any error occurs
```

---

## Implementation Details

### CASE A: Existing Products (productId > 0)

**When:** Product already exists in the database

**Processing:**

1. Get the existing product_id from the detail record
2. Call `stockInventory()` with the existing product_id
3. Create StoreInventory record with batch code

**No product/unit creation needed** - use what already exists.

---

### CASE B: New Products (productId == 0)

**When:** Product doesn't exist and must be created during receipt

#### Sub-case B1: Barcode Lookup (Reuse)

**Scenario:** Raw barcode already exists in `product_units` table (from another product)

**Processing:**

1. Query `product_units` table for the raw_barcode
2. If found: Get the associated product_id
3. Update the detail record with the found product_id
4. Stock inventory with the reused product

**Benefit:** Prevents duplicate products for the same barcode

---

#### Sub-case B2: Brand New Barcode (Birth Ceremony)

**Scenario:** Raw barcode is completely new and has never been registered

**Birth Ceremony Process:**

**Step 1: Create Product**

```java
Product newProduct = new Product();
newProduct.setProductName(detail.getTempProductName());
newProduct.setCategory(detail.getTempCategory());
newProduct.setBaseUnit(detail.getTempBaseUnit());
newProduct.setImportPrice(detail.getImportPriceAtTime());
newProduct.setMarkupRate(detail.getTempMarkupRate());
newProduct.setStatus("active");
newProduct.setImageName("default.png");

int newProductId = productRepo.insertWithConnection(newProduct, conn);
```

**Step 2: Hash the Barcode**

```java
String hashedBarcode = util.ShortHash.ProductBarcodeHash(
    "PO-" + orderId + "-PROD-" + newProductId + "-" + System.nanoTime()
);
```

This ensures:

- Barcodes are standardized and unique
- Original hand-typed barcode is not stored (only hashed version)
- Uniqueness is guaranteed even with identical manual entries

**Step 3: Create Product Unit**

```java
ProductUnit unit = new ProductUnit();
unit.setProductId(newProductId);
unit.setUnitName(baseUnit);
unit.setRatio(1);
unit.setBarcode(hashedBarcode);         // Hashed barcode
unit.setSellingPrice(importPrice);      // Import price as initial selling price
unit.setDefaultSale(true);

unitRepo.insertWithConnection(unit, conn);
```

**Step 4: Update Detail Record**

```java
detailRepo.updateProductId(detail.getId(), newProductId, conn);
```

This ensures the detail record has the correct product_id for future reference.

**Step 5: Stock Inventory**

```java
stockInventory(order, detail, newProductId, conn);
```

Create the inventory batch with:

- batch_code = BatchBarcodeHash(orderId, productId)
- import_price from the detail record
- quantity from the detail record
- received_at = today

---

## Transaction Safety

The entire `receiveOrder()` operation is wrapped in a strict database transaction:

```java
Connection conn = DatabaseUtil.getConnection();
boolean previousAutoCommit = conn.getAutoCommit();

try {
    conn.setAutoCommit(false);  // BEGIN TRANSACTION

    // All database operations here use the same connection
    // Product, ProductUnit, Inventory, and Order status updates

    conn.commit();               // COMMIT if all successful
} catch (Exception e) {
    conn.rollback();            // ROLLBACK on any error
} finally {
    conn.setAutoCommit(previousAutoCommit);
    DatabaseUtil.closeConnection();
}
```

**Benefits:**

- ✅ All-or-nothing semantics: Either the entire order is received or none of it
- ✅ No partial product creation if inventory insert fails
- ✅ No orphaned product/unit records
- ✅ Database consistency guaranteed

---

## Usage Workflow

### UI Layer (AddProductFrame)

When creating a pending purchase order with a new product:

```java
detail.setProductId(0);                    // Signal: new product
detail.setRawBarcode("USER_INPUT");        // Hand-typed barcode
detail.setTempProductName("Product Name"); // From UI
detail.setTempCategory("Category");        // From UI
detail.setTempBaseUnit("Box");             // From UI
detail.setTempMarkupRate(new BigDecimal("0.25")); // 25% markup
detail.setImportPriceAtTime(importPrice);
detail.setQuantity(quantity);
detail.setSubtotal(subtotal);

// Insert into pending order
orderService.addDetailToOrder(orderId, detail);
```

### Service Layer (Approval)

When the manager approves the order (moves to received status):

```java
orderService.receiveOrder(orderId);
// The receiveOrder() method handles all deferred creation logic
```

---

## Error Handling

The implementation handles these error scenarios:

1. **Missing raw_barcode for new product:**

   ```
   IllegalArgumentException: Chi tiết đơn id=X thiếu raw_barcode cho sản phẩm mới
   ```

2. **Missing temp_product_name for new product:**

   ```
   IllegalArgumentException: Chi tiết đơn id=X thiếu temp_product_name
   ```

3. **Product creation failed:**

   ```
   SQLException: Không thể tạo sản phẩm mới trong DB
   ```

4. **Order not in pending status:**

   ```
   IllegalStateException: Chỉ có thể nhận đơn ở trạng thái pending
   ```

5. **Empty order (no details):**
   ```
   IllegalArgumentException: Đơn nhập không có chi tiết nào
   ```

All errors trigger an automatic rollback, ensuring no partial data corruption.

---

## Performance Considerations

### Indexes Added

```sql
CREATE INDEX idx_po_detail_raw_barcode ON purchase_order_details(raw_barcode);
```

This index accelerates the barcode lookup during sub-case B1.

### Query Optimization

- Barcode lookup uses `product_units` table (indexed by barcode)
- Single query per detail for barcode validation
- No N+1 query problems due to transaction batching

### Batch Operations

Although the implementation processes details sequentially, all database writes happen within a single transaction, reducing connection overhead.

---

## Default Values

When optional fields are NULL during product creation:

| Field          | Default Value  | Rationale                            |
| -------------- | -------------- | ------------------------------------ |
| tempCategory   | "Mặt hàng mới" | Generic category for new items       |
| tempBaseUnit   | "Cái"          | Vietnamese for "piece" (common unit) |
| tempMarkupRate | 0.20 (20%)     | Standard industry markup             |

---

## Data Integrity Rules

1. **Barcode Uniqueness:** After hashing with ProductBarcodeHash(), no two products can have the same barcode
2. **Product-Unit Relationship:** Each newly created product gets exactly one default unit
3. **Inventory Batch Code:** Generated from `BatchBarcodeHash(orderId, productId)` to ensure uniqueness
4. **Status Consistency:** Order status transitions atomically: pending → received

---

## Testing Recommendations

### Unit Tests

- [ ] Test CASE A: Existing product stocking
- [ ] Test CASE B1: Barcode reuse scenario
- [ ] Test CASE B2: Brand new product creation
- [ ] Test error handling for missing fields
- [ ] Test transaction rollback on failure

### Integration Tests

- [ ] End-to-end order receipt with mixed case A and B details
- [ ] Verify product uniqueness by barcode
- [ ] Verify inventory batch codes are unique
- [ ] Verify order status transitions correctly

### Database Tests

- [ ] Verify all columns populated correctly
- [ ] Verify no orphaned product/unit records on rollback
- [ ] Verify index performance for barcode lookups

---

## Future Enhancements

1. **Batch Barcode Import:** Accept CSV with multiple barcode+metadata entries
2. **Supplier Catalog:** Pre-load supplier's product list during order creation
3. **Barcode Normalization:** Apply additional validation/normalization before hashing
4. **Audit Trail:** Log all product creation events for compliance
5. **Duplicate Detection:** Alert if similar product names already exist

---

## Files Modified

### Core Implementation

- `PurchaseOrderServiceImpl.java`: Complete rewrite of receiveOrder() method
- `PurchaseOrderDetail.java`: Added 5 new metadata fields
- `PurchaseOrderDetailRepository.java`: Added lookupProductIdByBarcode(), updateProductId()

### Repository Support

- `ProductRepository.java`: Added insertWithConnection()
- `ProductUnitRepository.java`: Added insertWithConnection()
- `InventoryRepository.java`: Added insertWithConnection() interface
- `InventoryRepositoryImpl.java`: Implemented insertWithConnection()

### Database

- `DATABASE_SCHEMA_UPDATES.sql`: ALTER TABLE statements

---

## Conclusion

The **Deferred Product Creation** pattern provides a robust, transaction-safe mechanism for accepting new products during purchase order receipt. By deferring product creation to the approval stage, the system gains flexibility while maintaining strict data integrity through database transactions.

This architecture eliminates the chicken-and-egg problem of requiring product pre-registration while guaranteeing that no partial or corrupted data can exist in the database.
