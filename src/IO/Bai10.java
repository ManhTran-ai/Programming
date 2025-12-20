package IO;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Bai10 {
    static void packFile(String folder, String packedFile) throws IOException {
        File src = new File(folder);
        if (src.isFile()) {
            return;
        }
        InputStream is;
        RandomAccessFile raf = new RandomAccessFile(packedFile, "rw");
        File[] list = src.listFiles();
        // tạo header trong method tạo header trả về các vị trí của thông tin "pos"
        List<Long> pointerPos = writeHeader(raf, list);
        System.out.println(pointerPos);
        for (int i = 0; i < list.length; i++) {
            if (list[i].isFile()) {
                is = new FileInputStream(list[i]);
                // lấy vị trí của cái "pos" trong list ra để đưa con trỏ về cái vị trí ghi "pos"
                raf.seek(pointerPos.get(i));
                // ghi đè lại "pos" sau khi có pos chính xác
                // pos chính xác ở đây là độ dài của raf
                raf.writeLong(raf.length());
//				System.out.println(raf.getFilePointer());
                // trở về vị trí hiện tại
                raf.seek(raf.length());
                // ghi nd file cần nén vào và lặp lại cho đến hết file
//				writeFile(is, raf, list[i].length());
                writeFile(is, raf);
            }
        }
        raf.close();
    }

    static List<Long> writeHeader(RandomAccessFile raf, File[] list) throws IOException {
        // ghi số lượng file nén
        raf.writeInt(list.length);
        // mảng "pos" để sau này seek con trỏ tới ghi đè thông tin
        List<Long> header = new ArrayList<Long>();
        for (int i = 0; i < list.length; i++) {
            if (list[i].isFile()) {
                // lưu lại vị trí sau này seek tới
                header.add(raf.getFilePointer());
                raf.writeLong(0);
                raf.writeLong(list[i].length());
                raf.writeUTF(list[i].getName());
            }
        }
        return header;
    }

    public static void writeFile(InputStream is, RandomAccessFile raf) {
        byte[] buffer = new byte[1024];
        int byteRead;
        try {
            while ((byteRead = is.read(buffer)) != -1) {
                raf.write(buffer, 0, byteRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // method giải nén
    static void unpackFile(String name, String packedFile, String dest) throws IOException {
        long pos;
        long size;
        String fName;
        RandomAccessFile raf = new RandomAccessFile(packedFile, "r");
        int count = raf.readInt();
        // đọc sl file nén
        while (count > 0) {
            pos = raf.readLong();
            size = raf.readLong();
            fName = raf.readUTF();
            // đọc file các thông tin để dò với name yêu cầu
            if (fName.equalsIgnoreCase(name)) {
                OutputStream os = new FileOutputStream(dest);
                // đúng name thì copy thông tin file đó ra dest
                readFile(os, raf, pos, size);
                return;
            }
            count--;
        }

    }

    static void readFile(OutputStream os, RandomAccessFile raf, long pos, long size) throws IOException {
        // seek tới vị trí của file cần ghi
        raf.seek(pos);
        byte[] buff = new byte[1024];
        int request;
        int buffRead;
        // read write y chang joint split
        while (size > 0) {
            request = (int) (buff.length >= size ? size : buff.length);
            buffRead = raf.read(buff, 0, request);
            if (buffRead == -1) {
                return;
            }
            size -= buffRead;
            os.write(buff, 0, buffRead);
        }

    }

    public static void main(String[] args) throws IOException {
        String foler = "D:\\test\\a";
        String packedFile = "D:\\test\\b\\packed.txt";
        packFile(foler, packedFile);
//		String dest = "E:\\LTM\\Test\\dest.txt";
//		String name = "test2.txt";
//		unpackFile(name, packedFile, dest);
    }
}
