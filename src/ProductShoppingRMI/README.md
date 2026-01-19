# Hệ thống Mua bán Sản phẩm (RMI)

Hệ thống Client/Server cho phép tìm kiếm và mua sản phẩm từ xa thông qua **RMI (Remote Method Invocation)**.

## Cấu trúc thư mục

```
src/ProductShoppingRMI/
├── Product.java           # Model sản phẩm (Serializable)
├── ProductService.java    # Interface RMI (Remote interface)
├── ProductServiceImpl.java # Implementation của interface
├── DatabaseManager.java   # Quản lý kết nối Access database
├── Server.java            # RMI Server (port 5918)
├── Client.java            # Ứng dụng Client console
├── server.policy          # Security policy cho RMI
└── README.md              # File hướng dẫn này

shopping.mdb               # Database Access (tự động tạo)
```

## Yêu cầu

### Thư viện cần thiết

Hệ thống sử dụng **UCanAccess** để kết nối với Microsoft Access:

- ucanaccess-x.x.x.jar
- lib/commons-lang3-xx.jar
- lib/commons-logging-xx.jar
- lib/hsqldb-xx.jar
- lib/jackcess-xx.jar

### Cách thêm thư viện trong IntelliJ IDEA

1. Tải UCanAccess từ: http://ucanaccess.sourceforge.net/site.html
2. Giải nén file tải về
3. Trong IntelliJ:
   - File → Project Structure → Libraries
   - Nhấn "+" → "Java"
   - Chọn tất cả các JAR trong thư mục `ucanaccess-x.x.x/lib/`
   - Nhấn OK

## Cách chạy chương trình

### Bước 1: Biên dịch

```bash
javac -cp ".;lib/*" src/ProductShoppingRMI/*.java
```

### Bước 2: Khởi động Server

```bash
java -cp ".;lib/*" -Djava.security.policy=src/ProductShoppingRMI/server.policy ProductShoppingRMI.Server
```

Server sẽ:
- Tạo RMI Registry trên port **5918**
- Đăng ký service với tên: `ProductService`
- Lắng nghe các kết nối từ client

### Bước 3: Khởi động Client

Mở một terminal mới và chạy:

```bash
java -cp ".;lib/*" -Djava.security.policy=src/ProductShoppingRMI/server.policy ProductShoppingRMI.Client
```

## Hướng dẫn sử dụng

### Giai đoạn 1: Đăng nhập (POP3-like)

Sau khi kết nối thành công, client sẽ gọi `getBanner()` để lấy thông điệp chào mừng từ server.

Sử dụng các lệnh sau để đăng nhập:

| Lệnh | Mô tả |
|------|-------|
| `TEN <username>` | Nhập tên đăng nhập |
| `MATKHAU <password>` | Nhập mật khẩu |
| `EXIT` | Thoát |

**Ví dụ:**
```
LOGIN> TEN admin
OK Đã nhận username. Vui lòng nhập mật khẩu: MATKHAU <password>
LOGIN> MATKHAU admin123
OK Đăng nhập thành công! Chào mừng admin!
```

### Giai đoạn 2: Giao dịch (sau khi đăng nhập thành công)

| Lệnh | Mô tả | Ví dụ |
|------|-------|-------|
| `MA <mã sp>` | Tìm sản phẩm theo mã | `MA SP001` |
| `TEN <tên sp>` | Tìm sản phẩm theo tên | `TEN laptop` |
| `MUA <mã1> <mã2>...` | Mua sản phẩm | `MUA SP001 SP002` |
| `QUIT` | Thoát | `QUIT` |

### User mặc định

Hệ thống tự động tạo các user sau khi chạy lần đầu:

| Username | Password |
|----------|----------|
| admin | admin123 |
| user | 123456 |

### Dữ liệu sản phẩm mẫu

Hệ thống tự động tạo 10 sản phẩm mẫu:

| Mã | Tên | Giá |
|----|-----|-----|
| SP001 | Laptop Dell Inspiron 15 | 18.990.000 VNĐ |
| SP002 | Laptop HP Pavilion 14 | 15.990.000 VNĐ |
| SP003 | iPhone 14 Pro Max | 28.990.000 VNĐ |
| SP004 | Samsung Galaxy S23 Ultra | 24.990.000 VNĐ |
| SP005 | Tai nghe Sony WH-1000XM5 | 7.990.000 VNĐ |
| SP006 | Loa JBL Flip 6 | 3.290.000 VNĐ |
| SP007 | Chuột không dây Logitech | 1.290.000 VNĐ |
| SP008 | Bàn phím cơ Corsair | 4.590.000 VNĐ |
| SP009 | Màn hình Samsung 27 inch | 8.990.000 VNĐ |
| SP010 | Ổ cứng SSD Samsung 1TB | 2.490.000 VNĐ |

### Ví dụ sử dụng

```
# Kết nối và đăng nhập
LOGIN> TEN admin
OK Đã nhận username. Vui lòng nhập mật khẩu: MATKHAU <password>
LOGIN> MATKHAU admin123
OK Đăng nhập thành công! Chào mừng admin!

# Tìm sản phẩm theo mã
SHOPPING> MA SP001
OK Tìm thấy sản phẩm:
Mã SP: SP001 | Tên: Laptop Dell Inspiron 15 | Số lượng: 50 | Giá: 18990000.00 VNĐ

# Tìm sản phẩm theo tên
SHOPPING> TEN laptop
OK Tìm thấy 2 sản phẩm:
─────────────────────────────────────────
Mã SP: SP001 | Tên: Laptop Dell Inspiron 15 | Số lượng: 50 | Giá: 18990000.00 VNĐ
Mã SP: SP002 | Tên: Laptop HP Pavilion 14 | Số lượng: 30 | Giá: 15990000.00 VNĐ
─────────────────────────────────────────

# Mua sản phẩm
SHOPPING> MUA SP001 SP003
=== KẾT QUẢ MUA HÀNG ===
Thành công: 2 sản phẩm
Thất bại: 0 sản phẩm
Tổng tiền: 47980000.00 VNĐ

Chi tiết:
  - Laptop Dell Inspiron 15 (SP001): OK - 18990000.00 VNĐ
  - iPhone 14 Pro Max (SP003): OK - 28990000.00 VNĐ

Cảm ơn bạn đã mua hàng!

# Thoát
SHOPPING> QUIT
OK Đã đăng xuất. Tạm biệt!
Đã ngắt kết nối. Tạm biệt!
```

## File Security Policy (server.policy)

RMI yêu cầu Security Manager để hoạt động. File policy sau cho phép:

```
grant {
    permission java.net.SocketPermission "*:1024-65535", "connect,accept,listen";
    permission java.io.FilePermission "<<ALL FILES>>", "read,write,delete,execute";
};
```

## Đặc điểm kỹ thuật

| Thông số | Giá trị |
|----------|---------|
| Giao thức | RMI (Remote Method Invocation) |
| Registry Port | 5918 |
| Service Name | ProductService |
| Service URL | rmi://127.0.0.1:5918/ProductService |
| Database | Microsoft Access (shopping.mdb) |

## Xử lý ngoại lệ

Hệ thống xử lý đầy đủ các ngoại lệ:

- Lỗi kết nối RMI (RemoteException)
- Service chưa được đăng ký (NotBoundException)
- Sản phẩm không tồn tại
- Sản phẩm hết hàng
- Lỗi database

## Các phương thức RMI

| Phương thức | Mô tả |
|-------------|-------|
| `getBanner()` | Lấy thông điệp chào mừng |
| `login(username, password)` | Đăng nhập |
| `logout(username)` | Đăng xuất |
| `findByID(productID)` | Tìm sản phẩm theo mã |
| `findByName(productName)` | Tìm sản phẩm theo tên |
| `buy(username, productIDs)` | Mua sản phẩm |
| `getAllProducts()` | Lấy tất cả sản phẩm |
| `isLoggedIn(username)` | Kiểm tra đăng nhập |

