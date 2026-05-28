-- ========================================================
-- DATABASE SCHEMA UPDATE: DEFERRED PRODUCT CREATION
-- ========================================================
-- Add columns to purchase_order_details table to support
-- the "Deferred Product Creation" pattern during order receipt
-- ========================================================

USE store_management;

-- Step 1: Add deferred creation metadata columns to purchase_order_details
ALTER TABLE purchase_order_details 
ADD COLUMN raw_barcode VARCHAR(50) NULL AFTER product_id;

ALTER TABLE purchase_order_details 
ADD COLUMN temp_product_name VARCHAR(255) NULL AFTER raw_barcode;

ALTER TABLE purchase_order_details 
ADD COLUMN temp_category VARCHAR(100) NULL AFTER temp_product_name;

ALTER TABLE purchase_order_details 
ADD COLUMN temp_base_unit VARCHAR(50) NULL AFTER temp_category;

ALTER TABLE purchase_order_details 
ADD COLUMN temp_markup_rate DECIMAL(5, 2) NULL AFTER temp_base_unit;

-- Step 2: Verify the columns were added successfully
DESC purchase_order_details;

-- Step 3: Create an index on raw_barcode for faster lookups during receipt
CREATE INDEX idx_po_detail_raw_barcode ON purchase_order_details(raw_barcode);

-- Step 4: Verify the index was created
SHOW INDEXES FROM purchase_order_details;

-- ========================================================
-- MIGRATION COMPLETE
-- ========================================================
-- The purchase_order_details table now supports:
-- 
-- CASE A (Existing Products):
--   - productId > 0: Uses existing product, deferred fields NULL
--
-- CASE B (New Products):
--   - productId = 0: Temporary fields hold raw metadata:
--     * raw_barcode: Hand-typed barcode to lookup or use for hashing
--     * temp_product_name: Product name for creation
--     * temp_category: Product category for creation
--     * temp_base_unit: Product base unit for creation
--     * temp_markup_rate: Markup rate for the new product
--
-- During receiveOrder(), the system:
-- 1. Checks if raw_barcode exists in product_units (reuse if found)
-- 2. Creates new product+unit if barcode is brand new
-- 3. Hashes the barcode with ProductBarcodeHash()
-- 4. Stocks inventory within a single transaction
-- 5. Updates productId on the detail record for future reference
-- ========================================================
