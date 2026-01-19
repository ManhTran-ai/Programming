# Product Management System - RMI Version

## Tổng quan
Hệ thống quản lý sản phẩm qua mạng sử dụng **RMI (Remote Method Invocation)** với Microsoft Access Database.

## Khác biệt với phiên bản TCP
- **TCP**: Giao tiếp bằng text commands qua socket
- **RMI**: Gọi phương thức Java trực tiếp từ xa (remote method calls)
- **RMI**: Client gọi các phương thức như gọi local methods

## Yêu cầu
- Java JDK 8 trở lên
- Microsoft Access hoặc tạo file .accdb
- UCanAccess JDBC Driver

## Cấu trúc thư mục

```
ProductManagementRMI/
├── src/
│   ├── Product.java              # Model class cho sản phẩm (Serializable)
│   ├── ProductService.java       # Remote Interface (extends Remote)
│   ├── ProductServiceImpl.java   # Implementation (extends UnicastRemoteObject)
│   ├── DatabaseManager.java      # Quản lý kết nối CSDL (Singleton)
│   ├── Server.java               # RMI Server với Registry
│   ├── Client.java               # RMI Client
│   └── server.policy             # Security policy cho RMI
├── product.accdb                 # Database file (tự tạo)
└── README.md                     # File này
```

## Cách chạy

### Bước 1: Tạo Database
1. Mở Microsoft Access
2. Tạo database mới với tên `product.accdb`
3. Lưu vào thư mục `ProductManagementRMI/`
4. Tạo bảng `Products` với các cột:
   - `productID` - Text (Primary Key)
   - `name` - Text
   - `count` - Integer
   - `price` - Double

### Bước 2: Biên dịch

```bash
cd D:\WorkSpace(IntelliJ)\NetworkProgramming\src\ProductManagementRMI

# Biên dịch tất cả file
javac -cp "lib/*;." *.java
```

### Bước 3: Chạy Server

```bash
# Chạy với security manager
java -cp "lib/*;." -Djava.security.policy=server.policy ProductManagementRMI.Server
```

Hoặc không cần policy (chỉ khi chạy local):

```bash
java -cp "lib/*;." ProductManagementRMI.Server
```

### Bước 4: Chạy Client

```bash
java -cp "lib/*;." ProductManagementRMI.Client
```

## Các lệnh (Client)

### ADD - Thêm sản phẩm mới
```
ADD P001 Tên sản phẩm Giá Số lượng
```
Ví dụ:
```
ADD P001 Laptop Dell 50000000 10
```
Phản hồi: `OK` hoặc `ERROR\tmessage`

### BUY - Mua sản phẩm
```
BUY P001 P002 P003
```
Phản hồi: `OK\tP001:OK\tP002:FAIL\t[Success: 1, Failed: 1]`

### PRICE - Tìm theo khoảng giá
```
PRICE 1000000 5000000
```
Phản hồi: Danh sách sản phẩm trong khoảng giá

### NAME - Tìm theo tên
```
NAME Laptop
```
Phản hồi: Danh sách sản phẩm có tên chứa từ khóa

### LIST - Liệt kê tất cả sản phẩm
```
LIST
```

### QUIT - Thoát
```
QUIT
```

## Kiến trúc RMI

```
┌─────────────────────────────────────────────────────────────┐
│                        RMI Architecture                      │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   ┌─────────┐          ┌─────────────┐          ┌─────────┐ │
│   │  Client │◄────────►│ RMI Registry│◄────────►│ Server  │ │
│   └────┬────┘          │  (Port 1099)│          └────┬────┘ │
│        │               └─────────────┘               │       │
│        │                                           │       │
│        │          ┌─────────────────┐              │       │
│        └─────────►│ ProductService  │◄─────────────┘       │
│                   │  (Remote Proxy) │                       │
│                   └────────┬────────┘                       │
│                            │                                │
│                   ┌────────▼────────┐                       │
│                   │ ProductService  │                       │
│                   │    Impl         │                       │
│                   └────────┬────────┘                       │
│                            │                                │
│                   ┌────────▼────────┐                       │
│                   │ DatabaseManager │                       │
│                   │   (Singleton)   │                       │
│                   └────────┬────────┘                       │
│                            │                                │
│                   ┌────────▼────────┐                       │
│                   │ Access Database │                       │
│                   └─────────────────┘                       │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## Đặc điểm kỹ thuật

| Thông số | Giá trị |
|----------|---------|
| RMI Registry Port | 1099 |
| Service Name | ProductService |
| Protocol | Java RMI |
| Database | 1 kết nối Singleton |
| Remote Interface | ProductService |
| Implementation | ProductServiceImpl |

## Ví dụ sử dụng

```
=== Product Management RMI Client ===
Connecting to RMI Server: rmi://localhost:1099/ProductService
Connected successfully!

========================================
Product Management RMI Client
========================================
> LIST
RESULT	10 products:
  ID: P001 | Name: Laptop Dell Inspiron 15 | Count: 10 | Price: 50000000.00
  ID: P002 | Name: Laptop HP ProBook 450 | Count: 15 | Price: 45000000.00
  ...

> ADD P011 Tai nghe Bluetooth 2500000 30
OK

> NAME Laptop
RESULT	2 products found:
  ID: P001 | Name: Laptop Dell Inspiron 15 | Count: 10 | Price: 50000000.00
  ID: P002 | Name: Laptop HP ProBook 450 | Count: 15 | Price: 45000000.00

> PRICE 1000000 10000000
RESULT	3 products found:
  ...

> BUY P001 P003
OK	P001:OK	P003:OK	[Success: 2, Failed: 0]

> QUIT
Disconnecting from server...
Client terminated.
```

## Xử lý lỗi

1. **java.rmi.ConnectException**: Server chưa chạy
2. **java.rmi.NotBoundException**: Service chưa được đăng ký
3. **ClassNotFoundException**: Thiếu UCanAccess driver

## Lưu ý

- RMI yêu cầu **Java Security Manager** khi chạy trong môi trường production
- File `server.policy` cấu quyền cho RMI operations
- Database file nên được đặt cùng thư mục với class files

## Tác giả
Created for Network Programming course

