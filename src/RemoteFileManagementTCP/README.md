# Hệ thống Remote File Management

Hệ thống Client/Server cho phép quản lý file từ xa thông qua kết nối TCP.

## Cấu trúc thư mục

```
src/RemoteFileManagement/
├── Server.java          # Server chính (lắng nghe trên port 55555)
├── Client.java          # Ứng dụng Client console
├── ClientHandler.java   # Xử lý từng kết nối client
├── DatabaseManager.java # Quản lý kết nối Access database
└── README.md            # File hướng dẫn này

server_files/            # Thư mục lưu trữ file trên server (tự động tạo)
users.mdb                # Database Access (tự động tạo)
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

### Bước 1: Khởi động Server

```bash
# Biên dịch
javac -cp ".;lib/*" src/RemoteFileManagement/*.java

# Chạy Server
java -cp ".;lib/*" RemoteFileManagement.Server
```

Server sẽ lắng nghe trên:
- **IP**: 127.0.0.1
- **Port**: 55555

### Bước 2: Khởi động Client

Mở một terminal mới và chạy:

```bash
java -cp ".;lib/*" RemoteFileManagement.Client
```

## Hướng dẫn sử dụng

### Giai đoạn 1: Đăng nhập (POP3-like)

Khi kết nối thành công, server sẽ hiển thị thông điệp chào mừng:
```
Welcome to File management
```

Sử dụng các lệnh sau để đăng nhập:

| Lệnh | Mô tả |
|------|-------|
| `USER \| <username>` | Nhập tên đăng nhập |
| `PASS \| <password>` | Nhập mật khẩu |
| `QUIT` | Thoát |

**Ví dụ:**
```
USER | admin
PASS | admin123
```

### Giai đoạn 2: Quản lý File (sau khi đăng nhập thành công)

Sau khi đăng nhập, bạn có thể sử dụng các lệnh sau:

| Lệnh | Mô tả | Ví dụ |
|------|-------|-------|
| `SET FOLDER \| <path>` | Đặt thư mục làm việc | `SET FOLDER \| docs` |
| `VIEW \| <file/path>` | Xem nội dung file/thư mục | `VIEW \| readme.txt` |
| `COPY \| <src> \| <dest>` | Sao chép file | `COPY \| a.txt \| b.txt` |
| `MOVE \| <src> \| <dest>` | Di chuyển file | `MOVE \| a.txt \| docs/` |
| `RENAME \| <src> \| <dest>` | Đổi tên file | `RENAME \| old.txt \| new.txt` |
| `QUIT` | Thoát | `QUIT` |

### User mặc định

Hệ thống tự động tạo các user sau khi chạy lần đầu:

| Username | Password |
|----------|----------|
| admin | admin123 |
| user | 123456 |

### Ví dụ sử dụng

```
# Kết nối và đăng nhập
LOGIN> USER | admin
OK User accepted. Vui lòng nhập mật khẩu: PASS | <password>
LOGIN> PASS | admin123
OK Đăng nhập thành công! Chào mừng admin.

# Xem danh sách file trong thư mục hiện tại
FILE-MGT> VIEW | .
OK Danh sách trong thư mục server_files:
  [DIR]  docs
  [FILE] readme.txt 1024 bytes

# Tạo thư mục mới
FILE-MGT> SET FOLDER | backup

# Sao chép file
FILE-MGT> COPY | ../readme.txt | backup_readme.txt
OK Đã sao chép readme.txt -> backup_readme.txt

# Đổi tên file
FILE-MGT> RENAME | backup_readme.txt | backup_readme_old.txt
OK Đã đổi tên backup_readme.txt -> backup_readme_old.txt

# Thoát
FILE-MGT> QUIT
BYE Đã ngắt kết nối. Tạm biệt!
```

## Xử lý ngoại lệ

Hệ thống xử lý đầy đủ các ngoại lệ:

- Lỗi kết nối mạng
- File không tồn tại
- Đường dẫn không hợp lệ
- Lỗi quyền truy cập
- Lỗi database

## Đặc điểm kỹ thuật

- **Giao thức**: TCP
- **Địa chỉ**: 127.0.0.1:55555
- **Dữ liệu**: Text-based (sử dụng BufferedReader/PrintWriter)
- **Mô hình**: Command/Response
- **Đa luồng**: Server hỗ trợ nhiều client đồng thời
- **Bảo mật**: Kiểm tra đường dẫn để ngăn truy cập ngoài thư mục server

