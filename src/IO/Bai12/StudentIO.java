package IO.Bai12;

import java.io.*;
import java.util.*;

public class StudentIO {
    /**
     * loadData: Đọc dữ liệu sinh viên từ hai file sv.txt và diem.txt với bảng mã cho trước.
     *
     * Bước 1: Đọc file sv.txt, mỗi dòng gồm MSSV, HọTên, Năm sinh.
     *         - Tách từng dòng thành các phần tử.
     *         - Lưu vào studentMap với key là MSSV, value là Student (chưa có điểm).
     *
     * Bước 2: Đọc file diem.txt, mỗi dòng gồm MSSV, các điểm.
     *         - Tách từng dòng, lấy MSSV và các điểm.
     *         - Tính điểm trung bình cho từng MSSV, lưu vào gradeMap.
     *
     * Bước 3: Gán điểm trung bình từ gradeMap vào các đối tượng Student trong studentMap.
     *
     * Bước 4: Trả về danh sách Student.
     *
     * @param stFile     File chứa thông tin sinh viên (MSSV, Họ Tên, Năm sinh)
     * @param gradeFile  File chứa điểm các môn của sinh viên
     * @param charset    Bảng mã (ví dụ: UTF-16BE, UTF-8, etc.)
     * @return           Danh sách sinh viên đầy đủ thông tin
     */
    public static List<Student> loadData(String stFile, String gradeFile, String charset) throws IOException {
        Map<Integer, Student> studentMap = new LinkedHashMap<>();

        // Đọc file sv.txt
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(stFile), charset))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue; // Bỏ qua dòng trắng
                String[] parts = line.split("\t");
                if (parts.length >= 3) {
                    int sid = Integer.parseInt(parts[0].trim());
                    String name = parts[1].trim();
                    int bYear = Integer.parseInt(parts[2].trim());
                    studentMap.put(sid, new Student(sid, name, bYear, 0));
                } else {
                    // Debug: In ra dòng lỗi
                    System.err.println("Dòng sv.txt không hợp lệ: " + line);
                }
            }
        }

        // Đọc file diem.txt
        Map<Integer, Double> gradeMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(gradeFile), charset))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue; // Bỏ qua dòng trắng
                String[] parts = line.split("\t");
                if (parts.length >= 2) {
                    int sid = Integer.parseInt(parts[0].trim());
                    double sum = 0;
                    int count = 0;
                    for (int i = 1; i < parts.length; i++) {
                        String p = parts[i].trim();
                        if (!p.isEmpty()) {
                            sum += Double.parseDouble(p);
                            count++;
                        }
                    }
                    if (count > 0) {
                        gradeMap.put(sid, sum / count);
                    }
                } else {
                    // Debug: In ra dòng lỗi
                    System.err.println("Dòng diem.txt không hợp lệ: " + line);
                }
            }
        }
        // Gán điểm vào student
        for (Student st : studentMap.values()) {
            if (gradeMap.containsKey(st.sid)) {
                st.grade = gradeMap.get(st.sid);
            }
        }
        return new ArrayList<>(studentMap.values());
    }

    /**
     * export: Ghi danh sách sinh viên ra file text với bảng mã cho trước.
     *         File này có thể import vào Microsoft Excel.
     *
     * Bước 1: Mở file xuất với encoding được chỉ định.
     *
     * Bước 2: Duyệt từng Student trong list, ghi ra file theo định dạng:
     *         MSSV \t HọTên \t Năm sinh \t Điểm trung bình
     *
     * Bước 3: Đóng file.
     *
     * @param list      Danh sách sinh viên cần xuất
     * @param textFile  Đường dẫn file xuất
     * @param charset   Bảng mã (ví dụ: UTF-16BE, UTF-8, etc.)
     */
    public static void export(List<Student> list, String textFile, String charset) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(textFile), charset))) {
            for (Student st : list) {
                bw.write(st.sid + "\t" + st.name + "\t" + st.bYear + "\t" + String.format("%.2f", st.grade));
                bw.newLine();
            }
        }
        System.out.println("Đã xuất " + list.size() + " sinh viên vào file: " + textFile);
    }

}
