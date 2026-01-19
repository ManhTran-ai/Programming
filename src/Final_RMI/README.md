# Remote File Manager - Hệ thống Quản lý File Từ xa

## Mô tả
Hệ thống Remote File Manager sử dụng Java RMI theo mô hình Command/Response tương tự giao thức POP3, cho phép người dùng đăng nhập và thực hiện thao tác upload/download file giữa Client và Server.

## Yêu cầu kỹ thuật
- Java RMI (Remote Method Invocation)
- Microsoft Access Database (file .mdb)
- JDBC Driver (UCanAccess)
- Port 1099 cho RMI Registry

## Cấu trúc Package
```
Final_RMI/
├── LoginService.java          # Interface cho việc đăng nhập
├── FileSession.java           # Interface cho thao tác file
├── DatabaseManager.java       # Quản lý CSDL
├── FileManager.java          # Quản lý thao tác file
├── LoginServiceImpl.java     # Implementation của LoginService
├── FileSessionImpl.java      # Implementation của FileSession
├── Server.java               # Server chính
├── Client.java               # Client với giao diện command line
└── README.md                 # Tài liệu hướng dẫn
```

## Thiết kế RMI (Factory Pattern)
- **LoginService**: Xử lý UNAME/PASS, tạo FileSession khi đăng nhập thành công
- **FileSession**: Xử lý SS_DIR, UPLOAD, DOWNLOAD sau khi đăng nhập

## Cơ sở dữ liệu
- **Driver**: UCanAccess JDBC Driver
- **File**: users.mdb
- **Bảng USERS**: USERNAME (Text, Primary Key), PASSWORD (Text)

## Thư mục làm việc
- **Server**: E://server (tự động tạo)
- **Client**: E://client (tự động tạo)

## Cách chạy

### 1. Chuẩn bị
```bash
# Đảm bảo có thư mục E:/
# Download UCanAccess JDBC driver và thêm vào classpath
# Hoặc sử dụng Maven/Gradle để quản lý dependencies
```

### 2. Biên dịch
```bash
# Biên dịch tất cả file Java
javac -cp ".:ucanaccess-5.0.1.jar" Final_RMI/*.java

# Hoặc sử dụng Maven/Gradle
```

### 3. Chạy Server
```bash
java -cp ".:ucanaccess-5.0.1.jar" Final_RMI.Server
```

### 4. Chạy Client (trong terminal khác)
```bash
java -cp ".:ucanaccess-5.0.1.jar" Final_RMI.Client
```

## Lệnh sử dụng

### Trước đăng nhập:
```
UNAME <username>    # Gửi tên đăng nhập
PASS <password>     # Gửi mật khẩu
EXIT               # Thoát
```

### Sau đăng nhập:
```
SS_DIR <folder>     # Thay đổi thư mục server
SC_DIR <folder>     # Thay đổi thư mục client
UPLOAD <local> <server>    # Upload file
DOWNLOAD <server> <local>  # Download file
EXIT               # Đăng xuất
```

## User mẫu
- admin/123456
- user1/password
- test/test123

## Lưu ý
- Server tự động tạo database và user mẫu nếu chưa có
- Client và Server tự động tạo thư mục E://client và E://server
- Sử dụng PreparedStatement để tránh SQL Injection
- Session management đảm bảo tính riêng tư cho từng client
- File upload/download sử dụng byte array với buffer hợp lý
