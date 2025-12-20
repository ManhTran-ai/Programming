package IO.Bai12;

import java.io.*;
import java.util.*;

public class TestStudentIO {

    /**
     * Tạo file sv.txt mẫu với encoding UTF-16BE
     */
    public static void createSampleStudentFile(String path, String charset) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(path), charset))) {
            // MSSV \t Họ Và Tên \t Năm sinh
            bw.write("101\tNguyễn Văn An\t2003"); bw.newLine();
            bw.write("102\tTrần Thị Bình\t2004"); bw.newLine();
            bw.write("103\tLê Văn Cường\t2003"); bw.newLine();
            bw.write("104\tPhạm Thị Dung\t2005"); bw.newLine();
            bw.write("105\tHoàng Văn Em\t2004"); bw.newLine();
            bw.write("106\tVũ Thị Phương\t2003"); bw.newLine();
        }
        System.out.println("Đã tạo file: " + path + " (" + charset + ")");
    }

    /**
     * Tạo file diem.txt mẫu với encoding UTF-16BE
     * Số lượng điểm của mỗi sinh viên khác nhau
     */
    public static void createSampleGradeFile(String path, String charset) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(path), charset))) {
            // MSSV \t Điểm 1 \t Điểm 2 \t ... \t Điểm n
            bw.write("101\t8.5\t7.0\t9.0"); bw.newLine();                    // 3 điểm
            bw.write("102\t9.5\t8.0\t9.0\t8.5"); bw.newLine();              // 4 điểm
            bw.write("103\t7.0\t6.5"); bw.newLine();                        // 2 điểm
            bw.write("104\t8.0\t9.0\t7.5\t8.5\t9.5"); bw.newLine();        // 5 điểm
            bw.write("105\t6.0\t7.0\t8.0"); bw.newLine();                   // 3 điểm
            // Chú ý: Sinh viên 106 không có trong file điểm
        }
        System.out.println("Đã tạo file: " + path + " (" + charset + ")");
    }

    /**
     * Hiển thị danh sách sinh viên
     */
    public static void displayStudents(List<Student> students) {
        System.out.println("\n=== DANH SÁCH SINH VIÊN ===");
        System.out.println("Tổng số: " + students.size() + " sinh viên");
        System.out.println("-----------------------------------------------------------");
        System.out.printf("%-10s %-25s %-10s %-10s%n", "MSSV", "Họ Và Tên", "Năm Sinh", "Điểm TB");
        System.out.println("-----------------------------------------------------------");
        for (Student st : students) {
            System.out.printf("%-10d %-25s %-10d %-10.2f%n",
                    st.sid, st.name, st.bYear, st.grade);
        }
        System.out.println("-----------------------------------------------------------\n");
    }

    public static void main(String[] args) {
        try {
            System.out.println("=== TEST CHƯƠNG TRÌNH IMPORT/EXPORT SINH VIÊN ===\n");

            // Bước 1: Tạo file dữ liệu mẫu
            System.out.println("BƯỚC 1: Tạo file dữ liệu mẫu");
            String charset = "UTF-16BE";
            createSampleStudentFile("sv.txt", charset);
            createSampleGradeFile("diem.txt", charset);

            // Bước 2: Test loadData với UTF-16BE
            System.out.println("\nBƯỚC 2: Đọc dữ liệu từ sv.txt và diem.txt (UTF-16BE)");
            List<Student> students = StudentIO.loadData("sv.txt", "diem.txt", charset);
            displayStudents(students);

            // Bước 3: Test export với UTF-16BE
            System.out.println("BƯỚC 3: Xuất dữ liệu ra file output_utf16be.txt (UTF-16BE)");
            StudentIO.export(students, "output_utf16be.txt", "UTF-16BE");

            // Bước 4: Test export với UTF-8
            System.out.println("\nBƯỚC 4: Xuất dữ liệu ra file output_utf8.txt (UTF-8)");
            StudentIO.export(students, "output_utf8.txt", "UTF-8");

            // Bước 5: Tạo file dữ liệu UTF-8 để test
            System.out.println("\nBƯỚC 5: Tạo file dữ liệu mẫu với UTF-8");
            createSampleStudentFile("sv_utf8.txt", "UTF-8");
            createSampleGradeFile("diem_utf8.txt", "UTF-8");

            // Bước 6: Test loadData với UTF-8
            System.out.println("\nBƯỚC 6: Đọc dữ liệu từ sv_utf8.txt và diem_utf8.txt (UTF-8)");
            List<Student> students2 = StudentIO.loadData("sv_utf8.txt", "diem_utf8.txt", "UTF-8");
            displayStudents(students2);

            // Bước 7: Kiểm tra sinh viên không có điểm
            System.out.println("BƯỚC 7: Kiểm tra sinh viên không có điểm (MSSV 106)");
            Student st106 = students.stream()
                    .filter(s -> s.sid == 106)
                    .findFirst()
                    .orElse(null);
            if (st106 != null) {
                System.out.println("Sinh viên 106: " + st106);
                System.out.println("Lưu ý: Điểm = 0.00 vì không có trong file diem.txt");
            }

            // Bước 8: Thống kê
            System.out.println("\nBƯỚC 8: Thống kê");
            double avgGrade = students.stream()
                    .filter(s -> s.grade > 0)
                    .mapToDouble(s -> s.grade)
                    .average()
                    .orElse(0.0);
            System.out.printf("Điểm trung bình chung: %.2f%n", avgGrade);

            long studentsWithGrade = students.stream()
                    .filter(s -> s.grade > 0)
                    .count();
            System.out.println("Số sinh viên có điểm: " + studentsWithGrade);
            System.out.println("Số sinh viên không có điểm: " + (students.size() - studentsWithGrade));

            System.out.println("\n=== TEST HOÀN TẤT THÀNH CÔNG ===");
            System.out.println("\nCác file đã được tạo:");
            System.out.println("- sv.txt (UTF-16BE) - File sinh viên");
            System.out.println("- diem.txt (UTF-16BE) - File điểm");
            System.out.println("- output_utf16be.txt (UTF-16BE) - File xuất, có thể import vào Excel");
            System.out.println("- output_utf8.txt (UTF-8) - File xuất, có thể import vào Excel");
            System.out.println("- sv_utf8.txt (UTF-8) - File sinh viên");
            System.out.println("- diem_utf8.txt (UTF-8) - File điểm");

            System.out.println("\nHướng dẫn import vào Excel:");
            System.out.println("1. Mở Microsoft Excel");
            System.out.println("2. Data -> From Text/CSV");
            System.out.println("3. Chọn file output_utf16be.txt hoặc output_utf8.txt");
            System.out.println("4. Chọn File Origin: Unicode (UTF-8) hoặc Unicode");
            System.out.println("5. Chọn Delimiter: Tab");
            System.out.println("6. Click Load");

        } catch (IOException e) {
            System.err.println("Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

