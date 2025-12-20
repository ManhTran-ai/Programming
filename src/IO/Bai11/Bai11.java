package IO.Bai11;

import IO.Bai11.Student;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Bai11 {
    private static final String FILE_NAME = "students.dat";
    private static final int HEADER_SIZE = 8; // 4 bytes for studentNumber + 4 bytes for recSize

    private RandomAccessFile raf;
    private File file;

    public Bai11() throws IOException {
        file = new File(FILE_NAME);
        boolean isNewFile = !file.exists();

        raf = new RandomAccessFile(file, "rw");

        if (isNewFile) {
            // Initialize header
            raf.writeInt(0); // studentNumber = 0
            raf.writeInt(Student.RECORD_SIZE); // recSize
        }
    }

    // Read student number from header
    private int getStudentNumber() throws IOException {
        raf.seek(0);
        return raf.readInt();
    }

    // Update student number in header
    private void setStudentNumber(int number) throws IOException {
        raf.seek(0);
        raf.writeInt(number);
    }

    // Calculate file position for a student at given index
    private long getStudentPosition(int index) {
        return HEADER_SIZE + (long) index * Student.RECORD_SIZE;
    }

    // Write a student to file at current position
    private void writeStudentData(Student st) throws IOException {
        raf.writeInt(st.getId());

        // Write name with fixed size
        String name = st.getName();
        if (name.length() > Student.NAME_SIZE) {
            name = name.substring(0, Student.NAME_SIZE);
        }
        StringBuilder sb = new StringBuilder(name);
        sb.setLength(Student.NAME_SIZE); // Pad with null characters
        raf.writeChars(sb.toString());

        raf.writeInt(st.getbYear());
        raf.writeDouble(st.getGrade());
    }

    // Read a student from file at current position
    private Student readStudentData() throws IOException {
        Student st = new Student();

        st.setId(raf.readInt());

        // Read name
        StringBuilder name = new StringBuilder();
        for (int i = 0; i < Student.NAME_SIZE; i++) {
            name.append(raf.readChar());
        }
        st.setName(name.toString().trim());

        st.setbYear(raf.readInt());
        st.setGrade(raf.readDouble());

        return st;
    }

    // Add a new student to the end of file
    public void addStudent(Student st) throws IOException {
        int studentNumber = getStudentNumber();

        // Seek to the position for the new student
        raf.seek(getStudentPosition(studentNumber));

        // Write student data
        writeStudentData(st);

        // Update student count
        setStudentNumber(studentNumber + 1);

        System.out.println("Student added successfully at index " + studentNumber);
    }

    // Get student at specific index
    public Student getStudent(int index) throws IOException {
        int studentNumber = getStudentNumber();

        if (index < 0 || index >= studentNumber) {
            throw new IndexOutOfBoundsException("Index " + index + " is out of bounds. Valid range: 0-" + (studentNumber - 1));
        }

        // Seek to the student position
        raf.seek(getStudentPosition(index));

        // Read and return student
        return readStudentData();
    }

    // Update student at specific index
    public void updateStudent(int index, Student newSt) throws IOException {
        int studentNumber = getStudentNumber();

        if (index < 0 || index >= studentNumber) {
            throw new IndexOutOfBoundsException("Index " + index + " is out of bounds. Valid range: 0-" + (studentNumber - 1));
        }

        // Seek to the student position
        raf.seek(getStudentPosition(index));

        // Write new student data
        writeStudentData(newSt);

        System.out.println("Student at index " + index + " updated successfully");
    }

    // Find student by ID
    public Student findById(int id) throws IOException {
        int studentNumber = getStudentNumber();

        for (int i = 0; i < studentNumber; i++) {
            raf.seek(getStudentPosition(i));
            Student st = readStudentData();

            if (st.getId() == id) {
                System.out.println("Student found at index " + i);
                return st;
            }
        }

        System.out.println("Student with ID " + id + " not found");
        return null;
    }

    // Display all students
    public void displayAllStudents() throws IOException {
        int studentNumber = getStudentNumber();

        System.out.println("\n=== Student List ===");
        System.out.println("Total students: " + studentNumber);
        System.out.println("Record size: " + Student.RECORD_SIZE + " bytes");
        System.out.println("--------------------");

        for (int i = 0; i < studentNumber; i++) {
            Student st = getStudent(i);
            System.out.println("Index " + i + ": " + st);
        }
        System.out.println("====================\n");
    }

    // Close the file
    public void close() throws IOException {
        if (raf != null) {
            raf.close();
        }
    }

    // Main method for testing
    public static void main(String[] args) {
        Bai11 manager = null;

        try {
            manager = new Bai11();

            System.out.println("=== Testing Student Management System ===\n");

            // Test 1: Add students
            System.out.println("1. Adding students...");
            manager.addStudent(new Student(101, "Nguyen Van A", 2003, 8.5));
            manager.addStudent(new Student(102, "Tran Thi B", 2004, 9.0));
            manager.addStudent(new Student(103, "Le Van C", 2003, 7.5));
            manager.addStudent(new Student(104, "Pham Thi D", 2005, 8.8));

            // Test 2: Display all students
            manager.displayAllStudents();

            // Test 3: Get student at specific index
            System.out.println("2. Getting student at index 1:");
            Student st = manager.getStudent(1);
            System.out.println(st + "\n");

            // Test 4: Update student
            System.out.println("3. Updating student at index 2:");
            manager.updateStudent(2, new Student(103, "Le Van C Updated", 2003, 9.5));
            manager.displayAllStudents();

            // Test 5: Find student by ID
            System.out.println("4. Finding student by ID 102:");
            Student foundStudent = manager.findById(102);
            if (foundStudent != null) {
                System.out.println(foundStudent + "\n");
            }

            // Test 6: Find non-existent student
            System.out.println("5. Finding non-existent student (ID 999):");
            manager.findById(999);

            System.out.println("\n=== All tests completed successfully ===");

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (manager != null) {
                    manager.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing file: " + e.getMessage());
            }
        }
    }
}
