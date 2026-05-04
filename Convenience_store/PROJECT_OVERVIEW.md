# 📋 PROJECT OVERVIEW: Convenience Store Management System

## 🎯 Project Objectives

Manage all operations of a convenience store chain including:

- **HR & Payroll**: Employee management, attendance tracking, salary calculation
- **Inventory**: Product management, stock tracking per store
- **Sales**: Invoice generation, point-of-sale transactions
- **Administration**: User authentication, system management

---

## 📊 Database Architecture

### Core Entities & Relationships

#### 1. **Store Management**

```
stores (Cửa hàng)
├── id (PRIMARY KEY)
├── store_name
├── location
├── manager_id (FOREIGN KEY → managers)
├── status (active/inactive)
└── created_at
```

#### 2. **Employee Management**

```
employees (Nhân viên)
├── id (PRIMARY KEY)
├── full_name
├── birthday
├── gender (Nam/Nữ/Khác)
├── id_card (UNIQUE - CCCD)
├── employee_barcode (UNIQUE - Quét chấm công)
├── phone, email, address
├── store_id (FOREIGN KEY → stores)
├── hourly_rate (Lương theo giờ)
├── status (active/inactive/on_leave)
└── timestamps

managers (Quản lý cửa hàng - 1:1 với employees)
├── id (PRIMARY KEY)
├── employee_id (UNIQUE FK → employees)
├── username (UNIQUE)
├── password_hash (Argon2)
├── management_level
└── allowance (Phụ cấp hàng tháng/kỳ)
```

#### 3. **Admin & Authentication**

```
admins (Quản trị viên hệ thống)
├── id (PRIMARY KEY)
├── username (UNIQUE)
├── password_hash (Argon2)
├── full_name
├── role (super_admin/system_admin)
└── created_at
```

#### 4. **Product & Inventory**

```
products (Sản phẩm)
├── id (PRIMARY KEY)
├── product_name
├── category
├── base_unit (Đơn vị cơ bản - Cái)
├── import_price (Giá nhập)
├── markup_rate (Tỷ lệ lãi - VD: 0.20 = 20%)
├── status (active/inactive)
└── created_at

product_units (Đơn vị quy đổi & Giá bán)
├── id (PRIMARY KEY)
├── product_id (FK → products)
├── unit_name (Thùng, Lốc, Lon...)
├── ratio (Quy đổi - VD: 1 Thùng = 24 Cái)
├── barcode (UNIQUE - Quét mã bán hàng)
├── selling_price (Giá bán của đơn vị này)
└── is_default_sale

store_inventory (Tồn kho theo cửa hàng)
├── store_id (FK → stores)
├── product_id (FK → products)
├── quantity
└── PRIMARY KEY (store_id, product_id)

inventory_imports (Nhật ký nhập kho)
├── id (PRIMARY KEY)
├── store_id, product_id
├── quantity
├── import_price_at_time
├── supplier_name
├── received_by_employee_id
└── import_date
```

#### 5. **Attendance & Payroll**

```
attendance (Chấm công)
├── id (PRIMARY KEY)
├── employee_id (FK)
├── work_date
├── check_in (DATETIME)
├── break_start/break_end (DATETIME)
├── check_out (DATETIME)
└── total_work_hours = (check_out - check_in) - (break_end - break_start)

payrolls (Bảng lương - Kỳ 15 ngày)
├── id (PRIMARY KEY)
├── employee_id (FK)
├── period_start (Ngày 1 hoặc 16)
├── period_end (Ngày 15 hoặc cuối tháng)
├── total_hours (Tổng giờ làm)
├── base_amount (Lương cơ bản = giờ * hourly_rate)
├── allowance_amount (Phụ cấp)
├── bonus_deduction (Thưởng/Phạt)
├── final_salary (Số tiền chuyển)
├── payment_status (pending/paid)
└── paid_at (TIMESTAMP)
```

#### 6. **Sales & Invoices**

```
invoices (Hóa đơn bán hàng)
├── id (PRIMARY KEY)
├── store_id (FK → stores)
├── employee_id (FK → employees)
├── total_amount (Tổng tiền hóa đơn)
└── created_at

invoice_details (Chi tiết hóa đơn)
├── id (PRIMARY KEY)
├── invoice_id (FK → invoices) [CASCADE DELETE]
├── product_id (FK → products)
├── unit_id (FK → product_units)
├── quantity
├── price_at_sale (Giá bán tại thời điểm đó - để lưu lịch sử)
└── subtotal = quantity * price_at_sale
```

---

## 🏗️ System Architecture

### Service Layer (Business Logic)

```
SERVICE LAYER
├── AdminService              ✅ Đăng nhập, xác thực, quản lý admin
├── EmployeeService           ✅ CRUD nhân viên, quản lý lương giờ
├── ManagerService            ✅ Tài khoản quản lý, phụ cấp
├── ProductService            ✅ Quản lý sản phẩm, giá, tỷ lệ lãi
├── InventoryService          ✅ Quản lý tồn kho (nhập/bán/kiểm tra)
├── StoreService              ✅ Quản lý cửa hàng, gán quản lý
├── PayrollService            ✅ Tính lương, thưởng/phạt, thanh toán
├── InvoiceService            ✅ Tạo/quản lý hóa đơn, doanh thu
├── AttendanceService         ✅ Chấm công (check-in/out)
└── SaleService               ✅ Xử lý bán hàng, thanh toán
```

### Repository Layer (Data Access)

```
REPOSITORY LAYER
├── AdminRepository           - Quản lý tài khoản admin
├── EmployeeRepository        - CRUD nhân viên
├── ManagerRepository         - CRUD quản lý
├── ProductRepository         - CRUD sản phẩm
├── InventoryRepository       - Quản lý tồn kho
├── StoreRepository           - CRUD cửa hàng
├── PayrollRepository         - CRUD bảng lương
├── InvoiceRepository         - CRUD hóa đơn
├── InvoiceDetailRepository   - CRUD chi tiết hóa đơn
├── AttendanceRepository      - CRUD chấm công
└── OrderRepository           - Tạo đơn hàng
```

### Entity Layer (Data Models)

```
ENTITY LAYER (10 classes)
├── Admin                     - Thông tin admin
├── Employee                  - Thông tin nhân viên
├── Manager                   - Thông tin quản lý
├── Store                     - Thông tin cửa hàng
├── Product                   - Thông tin sản phẩm
├── ProductUnit               - Đơn vị sản phẩm
├── Attendance                - Record chấm công
├── Invoice                   - Header hóa đơn
├── InvoiceDetail             - Chi tiết hóa đơn
└── Payroll                   - Bảng lương
```

---

## 🔐 Authentication & Authorization

### Password Security

- **Algorithm**: Argon2 hashing (bcrypt alternative)
- **Lib**: `argon2-jvm` (v2.11)
- **Used by**: AdminService, ManagerService

### Users

- **Admin**: Quản trị viên hệ thống (role: super_admin/system_admin)
- **Manager**: Quản lý cửa hàng (tài khoản riêng, 1:1 với nhân viên)
- **Employee**: Nhân viên (không có tài khoản - chỉ barcode)

---

## 🔄 Key Business Workflows

### 1. **Chấm Công (Attendance)**

```
Employee → Quét barcode → Check-in/Check-out
                ↓
        AttendanceService.processAttendance()
                ↓
        Lưu check-in/check-out time
                ↓
        Tính tổng giờ làm (bao gồm break)
```

### 2. **Bán Hàng (Sales)**

```
Product → Quét barcode → Xác định số lượng
                ↓
    SaleService.processSale()
                ↓
    Kiểm tra tồn kho (InventoryService)
                ↓
    Trừ kho + Tạo hóa đơn
                ↓
    SaleService.checkout() → InvoiceService
```

### 3. **Tính Lương (Payroll)**

```
Kỳ thanh toán (15 ngày) → PayrollService.createPayroll()
                ↓
    Tổng giờ từ AttendanceService
                ↓
    Lương cơ bản = giờ × hourly_rate
                ↓
    + Phụ cấp (nếu là quản lý)
                ↓
    + Thưởng/Phạt
                ↓
    = Lương cuối cùng
                ↓
    PayrollService.markAsPaid()
```

### 4. **Nhập Kho (Inventory Import)**

```
Nhân viên nhập hàng → InventoryService.importStock()
                ↓
    Cập nhật store_inventory
                ↓
    Lưu nhật ký trong inventory_imports
                ↓
    Cập nhật giá nhập (nếu khác)
```

### 5. **Quản lý Giá (Pricing)**

```
import_price (Giá nhập gốc)
        ↓
markup_rate (VD: 0.20 = 20%)
        ↓
selling_price = import_price × (1 + markup_rate)
        ↓
Cho phép lưu giá khác cho từng đơn vị (Lon/Thùng)
```

---

## 💾 Technology Stack

| Layer          | Technology               |
| -------------- | ------------------------ |
| **Language**   | Java 25                  |
| **Database**   | MySQL 8.3                |
| **Security**   | Argon2 (argon2-jvm 2.11) |
| **Build Tool** | Maven                    |
| **JDBC**       | MySQL Connector/J 8.3.0  |

---

## 📦 Dependencies

```xml
<dependency>
    <groupId>de.mkammerer</groupId>
    <artifactId>argon2-jvm</artifactId>
    <version>2.11</version>
</dependency>

<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.3.0</version>
</dependency>
```

---

## 🎯 Current Implementation Status

### ✅ Completed

**Entities (10)** - All core entities implemented with proper fields

- Employee, Manager, Admin, Store
- Product, ProductUnit
- Attendance, Invoice, InvoiceDetail, Payroll

**Repositories (11)** - All CRUD repositories ready

- All basic SQL operations implemented
- Proper SQL parameterization (prevent SQL injection)
- Connection pooling via DatabaseConnection

**Services (10)** - All business logic services created

1. ✅ **AdminService** - Login & authentication
2. ✅ **EmployeeService** - Employee CRUD & management
3. ✅ **ManagerService** - Manager accounts & permissions
4. ✅ **ProductService** - Product & pricing management
5. ✅ **InventoryService** - Stock tracking & updates
6. ✅ **StoreService** - Store management
7. ✅ **PayrollService** - Salary calculation & processing
8. ✅ **InvoiceService** - Invoice generation & management
9. ✅ **AttendanceService** - Check-in/check-out
10. ✅ **SaleService** - Sales transactions

**Utilities**

- ✅ Argon2Hasher - Password hashing/verification
- ✅ DatabaseConnection - Connection management

---

## 📝 Project Structure

```
Convenience_store/
├── pom.xml                                  [Maven config]
├── src/main/java/
│   ├── convenience_store/
│   │   ├── Argon2Hasher.java               [Security]
│   │   ├── Convenience_store.java           [Main entry]
│   │   └── DatabaseConnection.java          [DB connection]
│   │
│   ├── entity/                              [Data Models - 10 classes]
│   │   ├── Admin.java
│   │   ├── Employee.java
│   │   ├── Manager.java
│   │   ├── Store.java
│   │   ├── Product.java
│   │   ├── ProductUnit.java
│   │   ├── Attendance.java
│   │   ├── Invoice.java
│   │   ├── InvoiceDetail.java
│   │   └── Payroll.java
│   │
│   ├── repository/                          [Data Access - 11 classes]
│   │   ├── AdminRepository.java
│   │   ├── EmployeeRepository.java
│   │   ├── ManagerRepository.java
│   │   ├── StoreRepository.java
│   │   ├── ProductRepository.java
│   │   ├── InventoryRepository.java
│   │   ├── PayrollRepository.java
│   │   ├── InvoiceRepository.java
│   │   ├── InvoiceDetailRepository.java
│   │   ├── AttendanceRepository.java
│   │   └── OrderRepository.java
│   │
│   └── service/                             [Business Logic - 10 classes]
│       ├── AdminService.java                [Auth & admin mgmt]
│       ├── EmployeeService.java             [Employee mgmt]
│       ├── ManagerService.java              [Manager accounts]
│       ├── ProductService.java              [Product mgmt]
│       ├── InventoryService.java            [Stock mgmt]
│       ├── StoreService.java                [Store mgmt]
│       ├── PayrollService.java              [Salary mgmt]
│       ├── InvoiceService.java              [Invoice mgmt]
│       ├── AttendanceService.java           [Check-in/out]
│       └── SaleService.java                 [Sales transactions]
│
└── src/test/java/
    └── [Test classes to be implemented]
```

---

## 🚀 Next Steps

### Phase 1: Validation & Testing

1. Create unit tests for each service
2. Create integration tests
3. Validate database schema creation
4. Test JDBC connections

### Phase 2: UI/Frontend (Optional)

1. Create Swing GUI for:
   - Admin login screen
   - Manager dashboard
   - POS (Point of Sale) system
   - Inventory management interface
   - Payroll management interface

### Phase 3: Reports & Analytics

1. Sales reports (daily/monthly)
2. Payroll reports
3. Inventory reports
4. Employee performance reports

### Phase 4: Advanced Features

1. Multi-store analytics
2. Purchase order management
3. Supplier management
4. Return & refund handling

---

## 📞 Usage Examples

### Example 1: Employee Check-in/Check-out

```java
AttendanceService attendanceService = new AttendanceService();
attendanceService.processAttendance("BARCODE123");  // Check-in/out via barcode
```

### Example 2: Create and Process Sale

```java
InventoryService inventoryService = new InventoryService();
if (inventoryService.hasEnoughStock(storeId, productId, quantity)) {
    inventoryService.sellStock(storeId, productId, quantity);
    InvoiceService invoiceService = new InvoiceService();
    invoiceService.createInvoice(storeId, employeeId);
}
```

### Example 3: Calculate Payroll

```java
PayrollService payrollService = new PayrollService();
int payrollId = payrollService.createPayroll(employeeId,
    LocalDate.of(2024, 5, 1),
    LocalDate.of(2024, 5, 15));
payrollService.addAllowance(payrollId, new BigDecimal("500000"));
payrollService.markAsPaid(payrollId);
```

### Example 4: Admin Login

```java
AdminService adminService = new AdminService();
if (adminService.authenticate("admin", "password123")) {
    System.out.println("Login successful!");
}
```

---

## 🔍 Key Design Patterns

1. **DAO Pattern** - Repository classes for data access
2. **Service Pattern** - Business logic separation
3. **Layered Architecture** - Entity → Repository → Service
4. **Password Hashing** - Argon2 for security
5. **Exception Handling** - Try-catch with logging
6. **SQL Parameterization** - Prevent SQL injection
7. **Enum Usage** - Type-safe status fields

---

**Version**: 1.0  
**Last Updated**: 2024  
**Status**: Core implementation complete ✅
