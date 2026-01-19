# Hệ thống Remote File Management (RMI)

Hệ thống Client/Server cho phép quản lý file từ xa thông qua **RMI (Remote Method Invocation)**.

## Cấu trúc thư mục

```
src/RemoteFileManagementRMI/
├── RemoteFileManager.java       # Interface RMI (Remote interface)
├── RemoteFileManagerImpl.java   # Implementation của interface
├── Server.java                  # RMI Server (đăng ký service)
├── Client.java                  # Ứng dụng Client console
├── DatabaseManager.java         # Quản lý kết nối Access database
├── README.md                    # File hướng dẫn này
└── server.policy                # Security policy cho RMI

server_files_rmi/                # Thư mục lưu trữ file (tự động tạo)
users_rmi.mdb                    # Database Access (tự động tạo)
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
javac -cp ".;lib/*" src/RemoteFileManagementRMI/*.java
```

### Bước 2: Khởi động Server

```bash
java -cp ".;lib/*" -Djava.security.policy=src/RemoteFileManagementRMI/server.policy RemoteFileManagementRMI.Server
```

Server sẽ:
- Tạo RMI Registry trên port **55555**
- Đăng ký service với tên: `RemoteFileManager`
- Lắng nghe các kết nối từ client

### Bước 3: Khởi động Client

Mở một terminal mới và chạy:

```bash
java -cp ".;lib/*" -Djava.security.policy=src/RemoteFileManagementRMI/server.policy RemoteFileManagementRMI.Client
```

## Hướng dẫn sử dụng

### Giai đoạn 1: Đăng nhập (POP3-like)

Sử dụng các lệnh sau để đăng nhập:

| Lệnh | Mô tả |
|------|-------|
| `USER \| <username>` | Nhập tên đăng nhập |
| `PASS \| <password>` | Nhập mật khẩu |
| `QUIT` | Thoát |

**Ví dụ:**
```
LOGIN> USER | admin
OK User accepted. Vui lòng nhập mật khẩu: PASS | <password>
LOGIN> PASS | admin123
OK Đăng nhập thành công! Chào mừng admin.
```

### Giai đoạn 2: Quản lý File (sau khi đăng nhập thành công)

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
OK Danh sách trong thư mục server_files_rmi:
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
OK Đã đăng xuất. Tạm biệt!
Đã ngắt kết nối. Tạm biệt!
```

## File Security Policy (server.policy)

RMI yêu cầu Security Manager để hoạt động. File policy sau cho phép:

```
grant {
    permission java.net.SocketPermission "*:1024-65535", "connect,accept";
    permission java.io.FilePermission "<<ALL FILES>>", "read,write,delete,execute";
};
```

## Đặc điểm kỹ thuật

| Thông số | Giá trị |
|----------|---------|
| Giao thức | RMI (Remote Method Invocation) |
| Registry Port | 55555 |
| Service Name | RemoteFileManager |
| Service URL | rmi://127.0.0.1/RemoteFileManager |
| Database | Microsoft Access (users_rmi.mdb) |
| Thư mục gốc | server_files_rmi |

## Xử lý ngoại lệ

Hệ thống xử lý đầy đủ các ngoại lệ:

- Lỗi kết nối RMI (RemoteException)
- Service chưa được đăng ký (NotBoundException)
- File không tồn tại
- Đường dẫn không hợp lệ
- Lỗi quyền truy cập
- Lỗi database

## So sánh với phiên bản TCP

| Tính năng | TCP | RMI |
|-----------|-----|-----|
| Cách giao tiếp | Streams (Input/Output streams) | Gọi phương thức từ xa |
| Dữ liệu | Text lines | Object serialization |
| Xử lý lệnh | Server tự parse command | Client gọi method trực tiếp |
| Độ phức tạp | Cao (tự xử lý protocol) | Thấp (RMI lo protocol) |
| Hiệu năng | Tốt hơn | Tốt (overhead nhỏ) |
| Bảo mật | Tự quản lý | Security Manager |

## Khắc phục lỗi thường gặp

### Lỗi "Connection refused"

Đảm bảo Server đang chạy trước khi chạy Client.

### Lỗi "NotBoundException"

Service chưa được đăng ký. Kiểm tra Server đã chạy chưa.

### Lỗi "ClassNotFoundException"

Thiếu thư viện UCanAccess. Kiểm tra lại classpath.

### Lỗi Security

Đảm bảo sử dụng `-Djava.security.policy` khi chạy.

