package IO.Bai11;

public class Student {
        private int id;
        private String name;
        private int bYear;
        private double grade;

        // Constants for field sizes
        public static final int ID_SIZE = 4;        // int: 4 bytes
        public static final int NAME_SIZE = 50;     // String: 50 chars * 2 bytes = 100 bytes
        public static final int BYEAR_SIZE = 4;     // int: 4 bytes
        public static final int GRADE_SIZE = 8;     // double: 8 bytes
        public static final int RECORD_SIZE = ID_SIZE + (NAME_SIZE * 2) + BYEAR_SIZE + GRADE_SIZE; // 116 bytes

        public Student() {
            this.id = 0;
            this.name = "";
            this.bYear = 0;
            this.grade = 0.0;
        }

        public Student(int id, String name, int bYear, double grade) {
            this.id = id;
            this.name = name;
            this.bYear = bYear;
            this.grade = grade;
        }

        // Getters and Setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public int getbYear() { return bYear; }
        public void setbYear(int bYear) { this.bYear = bYear; }

        public double getGrade() { return grade; }
        public void setGrade(double grade) { this.grade = grade; }

        @Override
        public String toString() {
            return "Student{id=" + id + ", name='" + name + "', bYear=" + bYear + ", grade=" + grade + '}';
        }
}
