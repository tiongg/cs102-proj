package g1t1.testing;

import g1t1.models.sessions.*;
import g1t1.models.users.*;
import g1t1.models.ids.StudentID;
import g1t1.db.attendance.AttendanceStatus;
import g1t1.db.attendance.MarkingMethod;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SampleClassSessions {

    public static List<ClassSession> generateSampleData() {
        // 1️⃣ Fixed ModuleSections
        ModuleSection cs102 = new ModuleSection("CS102", "G1", 1, "09:00", "11:00");
        ModuleSection cs203 = new ModuleSection("CS203", "G2", 3, "10:00", "12:00");
        ModuleSection cs301 = new ModuleSection("CS301", "G3", 5, "14:00", "16:00");

        // 2️⃣ Fixed Students (20 total, 7 per section except last with 6)
        List<Student> students = new ArrayList<>();

        // CS102 students
        students.add(new Student(new StudentID("S00001"), "Alice Tan", cs102, "a@mail.com", "11111111"));
        students.add(new Student(new StudentID("S00002"), "Bob Lim", cs102, "b@mail.com", "11111112"));
        students.add(new Student(new StudentID("S00003"), "Charlie Ng", cs102, "c@mail.com", "11111113"));
        students.add(new Student(new StudentID("S00004"), "Darren Lee", cs102, "d@mail.com", "11111114"));
        students.add(new Student(new StudentID("S00005"), "Eve Wong", cs102, "e@mail.com", "11111115"));
        students.add(new Student(new StudentID("S00006"), "Fiona Koh", cs102, "f@mail.com", "11111116"));
        students.add(new Student(new StudentID("S00007"), "George Yeo", cs102, "g@mail.com", "11111117"));

        // CS203 students
        students.add(new Student(new StudentID("S00008"), "Hannah Peh", cs203, "h@mail.com", "11111118"));
        students.add(new Student(new StudentID("S00009"), "Ian Chong", cs203, "i@mail.com", "11111119"));
        students.add(new Student(new StudentID("S00010"), "Jasmine Toh", cs203, "j@mail.com", "11111120"));
        students.add(new Student(new StudentID("S00011"), "Ken Ng", cs203, "k@mail.com", "11111121"));
        students.add(new Student(new StudentID("S00012"), "Laura Tan", cs203, "l@mail.com", "11111122"));
        students.add(new Student(new StudentID("S00013"), "Matt Lim", cs203, "m@mail.com", "11111123"));
        students.add(new Student(new StudentID("S00014"), "Nina Lee", cs203, "n@mail.com", "11111124"));

        // CS301 students
        students.add(new Student(new StudentID("S00015"), "Oscar Wong", cs301, "o@mail.com", "11111125"));
        students.add(new Student(new StudentID("S00016"), "Pam Koh", cs301, "p@mail.com", "11111126"));
        students.add(new Student(new StudentID("S00017"), "Quinn Yeo", cs301, "q@mail.com", "11111127"));
        students.add(new Student(new StudentID("S00018"), "Rachel Peh", cs301, "r@mail.com", "11111128"));
        students.add(new Student(new StudentID("S00019"), "Sam Chong", cs301, "s@mail.com", "11111129"));
        students.add(new Student(new StudentID("S00020"), "Tina Toh", cs301, "t@mail.com", "11111130"));

        // 3️⃣ Add students to their respective moduleSection
        for (Student s : students) {
            s.getModuleSection().addStudent(s);
        }

        // 4️⃣ Create 3 ClassSessions
        ClassSession session1 = new ClassSession(cs102, 3, LocalDateTime.now().minusDays(10), SessionStatus.Ended);
        ClassSession session2 = new ClassSession(cs203, 5, LocalDateTime.now().minusDays(5), SessionStatus.Ended);
        ClassSession session3 = new ClassSession(cs301, 7, LocalDateTime.now(), SessionStatus.Active);

        // 5️⃣ Add fixed attendance patterns
        markAttendance(session1);
        markAttendance(session2);
        markAttendance(session3);

        return List.of(session1, session2, session3);
    }

    private static void markAttendance(ClassSession session) {
        int i = 0;
        for (SessionAttendance sa : session.getStudentAttendance()) {
            if (i % 5 == 0) {
                sa.markLate(0.75, MarkingMethod.MANUAL);
            } else if (i % 4 == 0) {
                sa.excuseStudent();
            } else {
                sa.markPresent(0.95, MarkingMethod.AUTOMATIC);
            }
            i++;
        }
    }

    // Test Main
    public static void main(String[] args) {
        List<ClassSession> sessions = generateSampleData();
        for (ClassSession cs : sessions) {
            System.out.println("=== " + cs.getModuleSection().getModule() + " " + cs.getModuleSection().getSection() + " ===");
            System.out.println("Week: " + cs.getWeek());
            for (SessionAttendance sa : cs.getStudentAttendance()) {
                System.out.printf("  %-15s %-10s (%.2f)\n",
                        sa.getStudent().getName(),
                        sa.getStatus(),
                        sa.getConfidence());
            }
            System.out.println();
        }
    }
}
