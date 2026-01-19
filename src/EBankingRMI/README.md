# NLU e-Banking System - RMI Version

## Tổng quan
Hệ thống ngân hàng điện tử e-Banking sử dụng **RMI (Remote Method Invocation)** với Microsoft Access Database.

## Yêu cầu
- Java JDK 8 trở lên
- Microsoft Access hoặc tạo file .accdb
- UCanAccess JDBC Driver

## Cấu trúc thư mục

```
EBankingRMI/
├── src/
│   ├── Account.java            # Model class cho tài khoản
│   ├── Transaction.java        # Model class cho giao dịch
│   ├── BankService.java        # Remote Interface
│   ├── BankServiceImpl.java    # Implementation
│   ├── DatabaseManager.java    # Quản lý kết nối CSDL (1 kết nối/client)
│   ├── Server.java             # RMI Server với Registry
│   ├── Client.java             # RMI Client
│   ├── server.policy           # Security policy cho RMI
│   └── README.md               # File này
├── ebanking.accdb              # Database file (tự tạo)
└── README.md                   # File này
```

## Cấu trúc Database

### Bảng Accounts
| Column | Type | Description |
|--------|------|-------------|
| username | Text(50) | Tên đăng nhập (Primary Key) |
| password | Text(50) | Mật khẩu |
| accountNumber | Text(20) | Số tài khoản (Unique) |
| balance | Double | Số dư tài khoản |

### Bảng Transactions
| Column | Type | Description |
|--------|------|-------------|
| id | AutoNumber | ID giao dịch |
| accountNumber | Text(20) | Số tài khoản |
| operation | Text(20) | DEPOSIT/WITHDRAW |
| transactionDate | DateTime | Ngày giờ giao dịch |
| amount | Double | Số tiền giao dịch |

## Tài khoản mẫu

| Username | Password | Account Number | Balance |
|----------|----------|----------------|---------|
| user1 | pass123 | 10000001 | 5,000,000 |
| user2 | pass456 | 10000002 | 10,000,000 |
| user3 | pass789 | 10000003 | 2,500,000 |

## Cách chạy

### Bước 1: Tạo Database
1. Mở Microsoft Access
2. Tạo database mới với tên `ebanking.accdb`
3. Lưu vào thư mục `EBankingRMI/`
4. Tạo 2 bảng theo cấu trúc trên

### Bước 2: Biên dịch

```bash
cd D:\WorkSpace(IntelliJ)\NetworkProgramming\src\EBankingRMI
javac -cp "lib/*;." *.java
```

### Bước 3: Chạy Server

```bash
java -cp "lib/*;." EBankingRMI.Server
```

### Bước 4: Chạy Client

```bash
java -cp "lib/*;." EBankingRMI.Client
```

## Các lệnh

### Phase 1: Đăng nhập (POP3-style)

```
USER username     # Nhập tên đăng nhập
PASSWORD password # Nhập mật khẩu
QUIT              # Thoát
```

### Phase 2: Giao dịch (sau khi đăng nhập thành công)

```
DEPOSIT amount    # Gửi tiền vào tài khoản
WITHDRAW amount   # Rút tiền từ tài khoản
BALANCE           # Kiểm tra số dư
REPORT            # Xem nhật ký giao dịch
QUIT              # Đăng xuất
```

## Ví dụ sử dụng

```
=== NLU e-Bank Client ===
Connecting to RMI Server: rmi://localhost:1099/BankService
Connected successfully!
Welcome to NLU e-Bank...

========================================
NLU e-Bank Client
========================================
Phase 1: Login
Commands: USER username, PASSWORD password, QUIT
========================================

> USER user1
+OK 10000001 5000000.0
> PASSWORD pass123
+OK 10000002 5000000.0

========================================
Phase 2: Transactions
Commands:
  DEPOSIT amount   - Gửi tiền
  WITHDRAW amount  - Rút tiền
  BALANCE          - Kiểm tra số dư
  REPORT           - Xem nhật ký giao dịch
  QUIT             - Đăng xuất và thoát
========================================

> DEPOSIT 1000000
+OK Deposit successful
So du tai khoan: 6000000.0

> WITHDRAW 500000
+OK Withdraw successful
So du tai khoan: 5500000.0

> BALANCE
So du tai khoan: 5500000.0

> REPORT
================ Nhat ky giao dich ================
So tai khoan || Ngay thang || Thao tac || Gia tri
-------------------------------------------------
10000001     || 2024-01-19 || GUI       || 1000000
10000001     || 2024-01-19 || RUT       || 500000
-------------------------------------------------
So du tai khoan: 5500000.0
=================================================

> QUIT
+OK Logged out successfully
Client terminated.
```

## Định dạng nhật ký giao dịch

```
Số tài khoản || Ngày tháng || Thao tác || Giá trị
abc          || 1/1/2020  || RUT      || 100

Số dư tài khoản: 200
```

## Xử lý lỗi

| Lỗi | Nguyên nhân | Giải pháp |
|-----|-------------|-----------|
| `-ERR User not found` | Username không tồn tại | Kiểm tra username |
| `-ERR Invalid password` | Sai mật khẩu | Nhập lại password |
| `-ERR Invalid amount` | Số tiền không hợp lệ | Nhập số > 0 |
| `-ERR Insufficient balance` | Số dư không đủ | Kiểm tra BALANCE |
| `java.rmi.ConnectException` | Server chưa chạy | Chạy Server trước |

## Đặc điểm kỹ thuật

| Thông số | Giá trị |
|----------|---------|
| RMI Registry Port | 1099 |
| Service Name | BankService |
| Protocol | Java RMI |
| Database | 1 kết nối/client |
| Remote Interface | BankService |
| Implementation | BankServiceImpl |

## Luồng hoạt động

```
┌─────────────────────────────────────────────────────────────┐
│                    E-Banking RMI Flow                        │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   ┌─────────┐                                            │
│   │  Client │                                            │
│   └────┬────┘                                            │
│        │                                                 │
│        │ connect()                                       │
│        ▼                                                 │
│   ┌─────────────┐    lookup()    ┌─────────────┐         │
│   │ RMI Registry│◄───────────────│   Server    │         │
│   └──────┬──────┘                └──────┬──────┘         │
│          │                               │                 │
│          │ getBanner()                   │                 │
│          │──────────────────────────────►│                 │
│          │                               │                 │
│          │ Phase 1: Login                │                 │
│          │ USER / PASSWORD               │                 │
│          │──────────────────────────────►│                 │
│          │                               │                 │
│          │ Phase 2: Transactions         │                 │
│          │ DEPOSIT / WITHDRAW / BALANCE  │                 │
│          │ / REPORT                      │                 │
│          │──────────────────────────────►│                 │
│          │                               │                 │
│          │ QUIT                          │                 │
│          └──────────────────────────────►│                 │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## Tác giả
Created for Network Programming course

