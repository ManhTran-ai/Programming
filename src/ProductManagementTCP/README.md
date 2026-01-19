# Product Management System - TCP Version

## Tổng quan
Hệ thống quản lý sản phẩm qua mạng sử dụng TCP connection với Microsoft Access Database.

## Yêu cầu
- Java JDK 8 trở lên
- Microsoft Access hoặc tạo file .accdb trống
- UCanAccess JDBC Driver (đã bao gồm trong lib/)

## Cấu trúc thư mục

```
ProductManagementTCP/
├── src/
│   ├── Product.java          # Model class cho sản phẩm
│   ├── DatabaseManager.java  # Quản lý kết nối CSDL (Singleton)
│   ├── ClientHandler.java    # Xử lý từng client connection
│   ├── Server.java           # TCP Server đa luồng
│   └── Client.java           # Client application
├── lib/                       # Thư viện UCanAccess
│   ├── ucanaccess-5.0.1.jar
│   ├── lib/commons-lang3-3.12.0.jar
│   ├── lib/commons-logging-1.2.jar
│   ├── lib/h2-2.2.224.jar
│   └── lib/jackcess-3.0.1.jar
├── product.accdb             # Database file (tự tạo)
└── README.md                 # File này
```

## Cách chạy

### Bước 1: Tạo Database
1. Mở Microsoft Access
2. Tạo database mới với tên `product.accdb`
3. Lưu vào thư mục `ProductManagementTCP/`
4. Tạo bảng `Products` với các cột:
   - `productID` - Text (Primary Key)
   - `name` - Text
   - `count` - Integer
   - `price` - Double

### Bước 2: Chuẩn bị thư viện
Tải UCanAccess từ: http://ucanaccess.sourceforge.net/site.html
Hoặc sử dụng Maven dependency:

```xml
<dependency>
    <groupId>net.sf.ucanaccess</groupId>
    <artifactId>ucanaccess</artifactId>
    <version>5.0.1</version>
</dependency>
```

### Bước 3: Biên dịch và chạy Server

```bash
# Biên dịch
cd D:\WorkSpace(IntelliJ)\NetworkProgramming\src\ProductManagementTCP
javac -cp "lib/*;." *.java

# Chạy Server
java -cp "lib/*;." Server
```

### Bước 4: Chạy Client

```bash
# Chạy Client
java -cp "lib/*;." Client
```

### Bước 5: Test bằng Telnet

```bash
telnet 127.0.0.1 1080
```

## Các lệnh

### ADD - Thêm sản phẩm mới
```
ADD\tP001\tTên sản phẩm\tGiá\tSố lượng
```
Ví dụ:
```
ADD	P001	Laptop Dell	50000000	10
```
Phản hồi: `OK` hoặc `ERROR\tProduct ID already exists`

### BUY - Mua sản phẩm
```
BUY\tP001\tP002\tP003
```
Phản hồi: `OK\tP001:OK\tP002:FAIL\tP003:OK`

### PRICE - Tìm theo khoảng giá
```
PRICE\t1000000\t5000000
```
Phản hồi: Danh sách sản phẩm trong khoảng giá

### NAME - Tìm theo tên
```
NAME\tLaptop
```
Phản hồi: Danh sách sản phẩm có tên chứa từ khóa

### QUIT - Thoát
```
QUIT
```

## Đặc điểm kỹ thuật

- **Port**: 1080
- **Host**: 127.0.0.1
- **Protocol**: TCP, Command/Response
- **Connection**: Multi-threaded, nhiều client đồng thời
- **Database**: 1 kết nối duy nhất (Singleton pattern)

## Ví dụ sử dụng

```
Welcome to Product Management System ...

> ADD	P001	Laptop Dell	50000000	10
OK

> ADD	P002	Mouse Logitech	500000	50
OK

> PRICE	100000	1000000
RESULT	2 products found
P002	Mouse Logitech	50	500000.00

> NAME	Laptop
RESULT	1 products found
P001	Laptop Dell	10	50000000.00

> BUY	P001
OK

> QUIT
BYE
```

## Xử lý lỗi

1. **Không kết nối được server**: Đảm bảo server đang chạy
2. **Lỗi database**: Kiểm tra file product.accdb tồn tại
3. **Lỗi command**: Kiểm tra cú pháp lệnh

## Tác giả
Created for Network Programming course

