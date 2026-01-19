# Hệ thống E-Banking qua mạng TCP

## Tổng quan
Hệ thống Client/Server cung cấp dịch vụ ngân hàng trực tuyến e-Banking với các chức năng:
- Đăng nhập/Đăng xuất tài khoản
- Gửi tiền vào tài khoản
- Rút tiền từ tài khoản
- Kiể tra số dư
- Xem nhật ký giao dịch

## Cấu trúc thư mục

```
src/EBankingTCP/
├── Account.java          # Model tài khoản ngân hàng
├── Transaction.java      # Model giao dịch
├── DatabaseManager.java  # Quản lý kết nối và thao tác CSDL
├── Server.java           # Server TCP phục vụ nhiều client
├── Client.java           # Client kết nối đến server
├── DatabaseSetup.java    # Tạo CSDL mẫu để test
├── README.md             # File hướng dẫn này
└── ebanking.accdb        # CSDL Microsoft Access (tự động tạo)
```

## Yêu cầu

1. Java Development Kit (JDK) 8 trở lên
2. Microsoft Access Database Engine (cho kết nối Access)
3. Thư viện UCanAccess (đã bao gồm trong dependencies)

## Cách chạy hệ thống

### Bước 1: Thiết lập cơ sở dữ liệu
```bash
# Biên dịch và chạy DatabaseSetup để tạo dữ liệu mẫu
javac -cp ".;lib/*" EBankingTCP/DatabaseSetup.java
java -cp ".;lib/*" EBankingTCP.DatabaseSetup
```

### Bước 2: Khởi động Server
```bash
# Mở terminal mới và chạy Server
javac -cp ".;lib/*" EBankingTCP/Server.java
java -cp ".;lib/*" EBankingTCP.Server
```

Server sẽ lắng nghe tại địa chỉ `127.0.0.1:1099`

### Bước 3: Kết nối Client
```bash
# Mở terminal mới và chạy Client
javac -cp ".;lib/*" EBankingTCP/Client.java
java -cp ".;lib/*" EBankingTCP.Client
```

## Các lệnh hệ thống

### Giai đoạn Đăng nhập (POP3-like)

| Lệnh | Mô tả | Ví dụ |
|------|-------|-------|
| USER | Nhập tên đăng nhập | `USER user1` |
| PASSWORD | Nhập mật khẩu | `PASSWORD password123` |
| QUIT | Hủy đăng nhập | `QUIT` |

### Giai đoạn Giao dịch (Sau khi đăng nhập thành công)

| Lệnh | Mô tả | Ví dụ |
|------|-------|-------|
| DEPOSIT | Gửi tiền vào tài khoản | `DEPOSIT 50000` |
| WITHDRAW | Rút tiền từ tài khoản | `WITHDRAW 20000` |
| BALANCE | Kiểm tra số dư | `BALANCE` |
| REPORT | Xem nhật ký giao dịch | `REPORT` |
| QUIT | Đăng xuất và kết thúc | `QUIT` |

## Tài khoản mẫu để test

| Tên đăng nhập | Mật khẩu | Số tài khoản | Số dư ban đầu |
|---------------|----------|--------------|---------------|
| user1 | password123 | ACC001 | 100,000 |
| user2 | password456 | ACC002 | 50,000 |
| admin | admin888 | ACC003 | 200,000 |

## Ví dụ sử dụng

```
# Kết nối và đăng nhập
NHAP LENH> USER user1
+OK ACC001 100000.0
NHAP LENH> PASSWORD password123
+OK ACC001 100000.0

# Giao dịch
NHAP LENH> DEPOSIT 50000
+OK Deposit successful | So du hien tai: 150000.0
NHAP LENH> WITHDRAW 20000
+OK Withdraw successful | So du hien tai: 130000.0
NHAP LENH> BALANCE
+OK So du tai khoan: 130000.0
NHAP LENH> REPORT
+OK 
So tai khoan	|| Ngay thang	|| Thao tac	|| Gia tri
------------------------------------------------------------
ACC001	|| 2025-01-19	|| RUT	|| 20000
ACC001	|| 2025-01-19	|| GUI	|| 50000
ACC001	|| 2025-01-14	|| RUT	|| 20000
ACC001	|| 2025-01-14	|| GUI	|| 50000
------------------------------------------------------------
So du tai khoan: 130000.0

NHAP LENH> QUIT
+OK Goodbye
```

## Xử lý lỗi

| Mã lỗi | Nguyên nhân |
|--------|-------------|
| -ERR User not found | Tên đăng nhập không tồn tại |
| -ERR Invalid password | Mật khẩu không đúng |
| -ERR Insufficient balance | Số dư không đủ để rút tiền |
| -ERR Invalid amount | Số tiền không hợp lệ |
| -ERR Not logged in | Chưa đăng nhập |

## Đặc điểm kỹ thuật

- **Giao thức**: TCP/IP
- **Cổng**: 1099
- **Mô hình**: Command/Response
- **Hỗ trợ đa client**: Server sử dụng ThreadPool để phục vụ đồng thời nhiều client
- **Kết nối CSDL**: Mỗi client có một kết nối độc lập xuống Microsoft Access
- **Bảo mật**: Xác thực hai bước (username + password)

## Lưu ý

1. Đảm bảo Microsoft Access Database Engine đã được cài đặt
2. File `ebanking.accdb` sẽ được tự động tạo khi chạy `DatabaseSetup`
3. Server phải được khởi động trước khi chạy Client
4. Có thể chạy nhiều Client đồng thời để test hệ thống đa người dùng

