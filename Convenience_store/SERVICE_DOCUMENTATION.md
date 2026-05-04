# 📚 Service Layer Documentation

## Quick Reference Guide

---

## 1️⃣ AdminService

**File**: `src/main/java/service/AdminService.java`  
**Purpose**: User authentication and admin account management

### Key Methods:

```java
boolean authenticate(String username, String password)
  → Xác thực người dùng admin

Admin login(String username, String password)
  → Đăng nhập và lấy thông tin admin

boolean changePassword(String username, String oldPassword, String newPassword)
  → Thay đổi mật khẩu
```

### Security:

- ✅ Argon2 password hashing
- ✅ Input validation
- ✅ Logging for audit trail

---

## 2️⃣ EmployeeService

**File**: `src/main/java/service/EmployeeService.java`  
**Purpose**: Employee management and information

### Key Methods:

```java
Employee findByBarcode(String barcode)
  → Tìm nhân viên theo mã barcode (chấm công)

Employee getEmployeeById(int employeeId)
  → Lấy thông tin nhân viên

List<Employee> getEmployeesByStore(int storeId)
  → Danh sách nhân viên của cửa hàng

boolean updateHourlyRate(int employeeId, BigDecimal hourlyRate)
  → Cập nhật lương giờ

boolean updateEmployeeStatus(int employeeId, Employee.Status status)
  → Thay đổi trạng thái (active/inactive/on_leave)

int addEmployee(Employee employee)
  → Thêm nhân viên mới

boolean updateEmployee(Employee employee)
  → Cập nhật thông tin nhân viên

boolean deleteEmployee(int employeeId)
  → Xóa nhân viên

List<Employee> getAllEmployees()
  → Danh sách tất cả nhân viên
```

### Features:

- ✅ Check-in/check-out barcode lookup
- ✅ Hourly rate management
- ✅ Status tracking (active/inactive/on_leave)
- ✅ Complete CRUD operations

---

## 3️⃣ ManagerService

**File**: `src/main/java/service/ManagerService.java`  
**Purpose**: Manager account management and permissions

### Key Methods:

```java
int createManager(int employeeId, String username, String password,
                  String managementLevel, BigDecimal allowance)
  → Tạo tài khoản quản lý từ nhân viên

Manager authenticate(String username, String password)
  → Xác thực đăng nhập quản lý

boolean changePassword(int managerId, String oldPassword, String newPassword)
  → Thay đổi mật khẩu quản lý

boolean updateAllowance(int managerId, BigDecimal newAllowance)
  → Cập nhật phụ cấp

boolean updateManagementLevel(int managerId, String managementLevel)
  → Cập nhật cấp độ quản lý

List<Manager> getAllManagers()
  → Danh sách tất cả quản lý

boolean deleteManager(int managerId)
  → Xóa tài khoản quản lý
```

### Features:

- ✅ 1:1 relationship with Employee
- ✅ Argon2 password hashing
- ✅ Allowance management
- ✅ Management level tracking

---

## 4️⃣ ProductService

**File**: `src/main/java/service/ProductService.java`  
**Purpose**: Product management and pricing

### Key Methods:

```java
Object[] getProductByBarcode(String barcode)
  → Tìm sản phẩm theo mã barcode bán hàng

Product getProductById(int productId)
  → Lấy thông tin sản phẩm

BigDecimal calculateSellingPrice(BigDecimal importPrice, BigDecimal markupRate)
  → Tính giá bán = importPrice * (1 + markupRate)

boolean updateImportPrice(int productId, BigDecimal newImportPrice)
  → Cập nhật giá nhập

boolean updateMarkupRate(int productId, BigDecimal newMarkupRate)
  → Cập nhật tỷ lệ lãi

int addProduct(Product product)
  → Thêm sản phẩm mới

boolean updateProduct(Product product)
  → Cập nhật sản phẩm

boolean deleteProduct(int productId)
  → Xóa sản phẩm

List<Product> getAllProducts()
  → Danh sách tất cả sản phẩm

List<Product> getProductsByCategory(String category)
  → Sản phẩm theo danh mục
```

### Features:

- ✅ Barcode-based lookup
- ✅ Flexible pricing: import price + markup rate
- ✅ Category filtering
- ✅ Full CRUD operations

---

## 5️⃣ InventoryService

**File**: `src/main/java/service/InventoryService.java`  
**Purpose**: Stock management per store

### Key Methods:

```java
int getStock(int storeId, int productId)
  → Lấy tồn kho hiện tại

boolean hasEnoughStock(int storeId, int productId, int quantityNeeded)
  → Kiểm tra đủ hàng

boolean updateStock(int storeId, int productId, int quantity)
  → Cập nhật tồn kho (+/- dương:nhập, âm:bán)

boolean importStock(int storeId, int productId, int quantityToImport)
  → Nhập kho (tăng tồn)

boolean sellStock(int storeId, int productId, int quantityToSell)
  → Bán hàng (giảm tồn, kiểm tra đủ hàng)

int countOutOfStockProducts(int storeId)
  → Đếm sản phẩm hết hàng

boolean isLowStock(int storeId, int productId, int minimumThreshold)
  → Kiểm tra sắp hết hàng

BigDecimal getTotalInventoryValue(int storeId)
  → Tổng giá trị tồn kho
```

### Features:

- ✅ Per-store inventory tracking
- ✅ Stock validation (no negative)
- ✅ Import/sell operations
- ✅ Low stock alerts
- ✅ Inventory valuation

---

## 6️⃣ StoreService

**File**: `src/main/java/service/StoreService.java`  
**Purpose**: Store management

### Key Methods:

```java
Store getStore(int storeId)
  → Lấy thông tin cửa hàng

List<Store> getAllStores()
  → Danh sách tất cả cửa hàng

List<Store> getActiveStores()
  → Danh sách cửa hàng hoạt động

int addStore(Store store)
  → Thêm cửa hàng mới

boolean updateStore(Store store)
  → Cập nhật thông tin

boolean assignManager(int storeId, int managerId)
  → Gán quản lý cho cửa hàng

boolean updateStoreStatus(int storeId, String status)
  → Thay đổi trạng thái (active/inactive)

boolean deleteStore(int storeId)
  → Xóa cửa hàng

int getStoreCount()
  → Tổng số cửa hàng

int getActiveStoreCount()
  → Tổng số cửa hàng hoạt động
```

### Features:

- ✅ Multi-store support
- ✅ Manager assignment
- ✅ Status management
- ✅ Statistics tracking

---

## 7️⃣ PayrollService

**File**: `src/main/java/service/PayrollService.java`  
**Purpose**: Salary calculation and payroll management

### Key Methods:

```java
BigDecimal calculateBaseSalary(BigDecimal totalHours, BigDecimal hourlyRate)
  → Tính lương cơ bản = giờ * lương/giờ

int createPayroll(int employeeId, LocalDate periodStart, LocalDate periodEnd)
  → Tạo bảng lương cho kỳ 15 ngày

boolean addAllowance(int payrollId, BigDecimal allowanceAmount)
  → Thêm phụ cấp quản lý

boolean addBonusOrPenalty(int payrollId, BigDecimal bonusOrPenalty)
  → Thêm thưởng (+ ) hoặc phạt (- )

boolean markAsPaid(int payrollId)
  → Xác nhận thanh toán

Payroll getPayroll(int payrollId)
  → Lấy thông tin bảng lương

List<Payroll> getEmployeePayrolls(int employeeId)
  → Danh sách bảng lương của nhân viên

List<Payroll> getPendingPayrolls()
  → Danh sách chưa thanh toán

BigDecimal calculateTotalPayroll(List<Payroll> payrolls)
  → Tổng lương phải trả
```

### Features:

- ✅ Hourly-based calculation
- ✅ Allowance tracking
- ✅ Bonus/Penalty system
- ✅ Period-based (15-day cycles)
- ✅ Payment status tracking

### Calculation Formula:

```
Final Salary = Base Amount + Allowance + Bonus/Penalty
Where:
  Base Amount = Total Hours × Hourly Rate
  Allowance = Monthly allowance (for managers)
  Bonus/Penalty = Additional earnings/deductions
```

---

## 8️⃣ InvoiceService

**File**: `src/main/java/service/InvoiceService.java`  
**Purpose**: Sales invoice management

### Key Methods:

```java
int createInvoice(int storeId, int employeeId)
  → Tạo hóa đơn mới

boolean addInvoiceDetail(int invoiceId, int productId, int unitId,
                         int quantity, BigDecimal priceAtSale)
  → Thêm chi tiết sản phẩm vào hóa đơn

Invoice getInvoice(int invoiceId)
  → Lấy thông tin hóa đơn

List<InvoiceDetail> getInvoiceDetails(int invoiceId)
  → Lấy danh sách chi tiết hóa đơn

boolean updateInvoiceTotal(int invoiceId)
  → Cập nhật tổng tiền hóa đơn

boolean finalizeInvoice(int invoiceId)
  → Hoàn tất hóa đơn

List<Invoice> getInvoicesByStore(int storeId)
  → Danh sách hóa đơn của cửa hàng

List<Invoice> getInvoicesByEmployee(int employeeId)
  → Danh sách hóa đơn của nhân viên

BigDecimal calculateTotalRevenue(List<Invoice> invoices)
  → Tính doanh thu tổng cộng

boolean deleteInvoice(int invoiceId)
  → Xóa hóa đơn
```

### Features:

- ✅ Invoice creation
- ✅ Line item management
- ✅ Price tracking (historical)
- ✅ Total calculation
- ✅ Revenue reporting
- ✅ Per-store/per-employee tracking

---

## 9️⃣ AttendanceService

**File**: `src/main/java/service/AttendanceService.java`  
**Purpose**: Employee check-in/check-out

### Key Methods:

```java
void processAttendance(String barcode)
  → Xử lý chấm công (check-in/out từ barcode)
```

### Features:

- ✅ Barcode-based check-in/out
- ✅ Automatic detection (in or out)
- ✅ Status validation
- ✅ User notifications

---

## 🔟 SaleService

**File**: `src/main/java/service/SaleService.java`  
**Purpose**: Sales transaction processing

### Key Methods:

```java
void processSale(int storeId, String barcode, int quantityToSell)
  → Xử lý bán hàng (quét mã, kiểm tra tồn, trừ kho)

void checkout(int storeId, int employeeId, BigDecimal totalAmount,
              List<Object[]> cartItems)
  → Thanh toán giỏ hàng (tạo hóa đơn & lưu)
```

### Features:

- ✅ Barcode scanning
- ✅ Stock validation
- ✅ Cart management
- ✅ Invoice generation

---

## 🎯 Common Usage Patterns

### Pattern 1: Login & Authentication

```java
AdminService adminService = new AdminService();
if (adminService.authenticate("username", "password")) {
    // User authenticated
}
```

### Pattern 2: Add & Manage Employee

```java
EmployeeService empService = new EmployeeService();
Employee emp = new Employee();
emp.setFullName("Nguyễn Văn A");
emp.setHourlyRate(new BigDecimal("50000"));
int empId = empService.addEmployee(emp);
```

### Pattern 3: Create & Process Payroll

```java
PayrollService payrollService = new PayrollService();
int payrollId = payrollService.createPayroll(employeeId,
    LocalDate.of(2024, 5, 1),
    LocalDate.of(2024, 5, 15));

if (isManager) {
    payrollService.addAllowance(payrollId, new BigDecimal("1000000"));
}
payrollService.markAsPaid(payrollId);
```

### Pattern 4: Inventory Management

```java
InventoryService inventoryService = new InventoryService();

// Check stock before sale
if (inventoryService.hasEnoughStock(storeId, productId, quantity)) {
    inventoryService.sellStock(storeId, productId, quantity);
} else {
    System.out.println("Out of stock!");
}

// Import new stock
inventoryService.importStock(storeId, productId, 100);
```

### Pattern 5: Sales & Invoice

```java
InvoiceService invoiceService = new InvoiceService();
int invoiceId = invoiceService.createInvoice(storeId, employeeId);

for (Product product : cart) {
    invoiceService.addInvoiceDetail(invoiceId, product.getId(),
        unitId, quantity, priceAtSale);
}

invoiceService.finalizeInvoice(invoiceId);
```

---

## 🔒 Security & Validation

All services implement:

- ✅ **Null checks** - Prevent NullPointerException
- ✅ **Range validation** - ID > 0, prices >= 0
- ✅ **String validation** - Not empty, trim whitespace
- ✅ **Logging** - Warning/Error/Info levels
- ✅ **Exception handling** - Try-catch with logging
- ✅ **Password security** - Argon2 hashing

---

## 📊 Data Types & Units

| Field         | Type       | Unit       | Example    |
| ------------- | ---------- | ---------- | ---------- |
| Hourly Rate   | BigDecimal | VND/hour   | 50,000     |
| Import Price  | BigDecimal | VND        | 100,000    |
| Selling Price | BigDecimal | VND        | 120,000    |
| Markup Rate   | BigDecimal | Percentage | 0.20 (20%) |
| Total Hours   | BigDecimal | Hours      | 8.5        |
| Quantity      | Integer    | Units      | 100        |
| Allowance     | BigDecimal | VND        | 1,000,000  |

---

**Last Updated**: 2024  
**Version**: 1.0  
**Status**: ✅ Complete & Ready for Testing
