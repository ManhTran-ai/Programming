package IO.Bai12;

public class Student {
    public int sid;
    public String name;
    public int bYear;
    public double grade;

    public Student(int sid, String name, int bYear, double grade) {
        this.sid = sid;
        this.name = name;
        this.bYear = bYear;
        this.grade = grade;
    }

    @Override
    public String toString() {
        return String.format("Student{sid=%d, name='%s', bYear=%d, grade=%.2f}",
                sid, name, bYear, grade);
    }
}

