# Bài 13: Copy/Move File và Folder

## Mô tả
Chương trình cung cấp 2 phương thức chính để copy/move file và folder sử dụng byte array kết hợp với BufferedInputStream (BIS) và BufferedOutputStream (BOS).

## Các phương thức chính

### 1. fileCopy(String sFile, String destFile, boolean moved)
**Mục đích:** Copy hoặc move một file từ nguồn đến đích

**Tham số:**
- `sFile`: Đường dẫn file nguồn
- `destFile`: Đường dẫn file đích
- `moved`: 
  - `true`: Di chuyển file (xóa file nguồn sau khi copy)
  - `false`: Chỉ copy file (giữ nguyên file nguồn)

**Trả về:** `true` nếu thành công, `false` nếu thất bại

**Đặc điểm:**
- Sử dụng byte array buffer 8KB
- Kết hợp BufferedInputStream và BufferedOutputStream để tối ưu hiệu suất
- Tự động tạo thư mục cha nếu chưa tồn tại
- Kiểm tra file nguồn và đích có trùng nhau không

**Ví dụ:**
```java
// Copy file
FileFolderCopy.fileCopy("source.txt", "destination.txt", false);

// Move file
FileFolderCopy.fileCopy("source.txt", "moved.txt", true);
```

### 2. folderCopy(String sFolder, String destFolder, boolean moved)
**Mục đích:** Copy hoặc move một thư mục từ nguồn đến đích (bao gồm cả các file và thư mục con)

**Tham số:**
- `sFolder`: Đường dẫn thư mục nguồn
- `destFolder`: Đường dẫn thư mục đích
- `moved`:
  - `true`: Di chuyển thư mục (xóa thư mục nguồn sau khi copy)
  - `false`: Chỉ copy thư mục (giữ nguyên thư mục nguồn)

**Trả về:** `true` nếu thành công, `false` nếu thất bại

**Đặc điểm:**
- Copy đệ quy tất cả file và thư mục con
- Sử dụng byte array buffer 8KB cho mỗi file
- Kết hợp BufferedInputStream và BufferedOutputStream
- Kiểm tra thư mục đích không nằm trong thư mục nguồn
- Tự động tạo thư mục đích nếu chưa tồn tại

**Ví dụ:**
```java
// Copy thư mục
FileFolderCopy.folderCopy("source_folder", "destination_folder", false);

// Move thư mục
FileFolderCopy.folderCopy("source_folder", "moved_folder", true);
```

## Cấu trúc dữ liệu

### BufferedInputStream và BufferedOutputStream
- **BufferedInputStream**: Đọc dữ liệu từ file với buffer, giảm số lần truy cập đĩa
- **BufferedOutputStream**: Ghi dữ liệu vào file với buffer, tăng hiệu suất ghi
- **Byte array**: Sử dụng buffer 8KB (8192 bytes) để đọc/ghi dữ liệu

### Luồng xử lý
```
File nguồn → FileInputStream → BufferedInputStream → Byte Array → BufferedOutputStream → FileOutputStream → File đích
```

## Kiểm tra lỗi
Chương trình kiểm tra các trường hợp:
- File/thư mục nguồn không tồn tại
- File/thư mục nguồn và đích trùng nhau
- Thư mục đích nằm trong thư mục nguồn (khi copy folder)
- Không có quyền đọc/ghi file
- Lỗi I/O trong quá trình copy

## Chạy test
Chạy class `TestFileFolderCopy` để test các chức năng:
```bash
java IO.Bai13.TestFileFolderCopy
```

Test case bao gồm:
1. Copy file đơn
2. Move file đơn
3. Copy thư mục (bao gồm thư mục con và nhiều file)
4. Move thư mục

## Ưu điểm
- **Hiệu suất cao**: Sử dụng BufferedInputStream/BufferedOutputStream với byte array
- **Linh hoạt**: Hỗ trợ cả copy và move
- **An toàn**: Kiểm tra nhiều trường hợp lỗi
- **Đệ quy**: Copy cả cấu trúc thư mục phức tạp
- **Tự động**: Tự động tạo thư mục cần thiết

