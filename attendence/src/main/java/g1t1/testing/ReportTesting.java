package g1t1.testing;

import g1t1.features.report.Report;
import g1t1.features.report.ReportBuilder;
import g1t1.models.ids.StudentID;
import g1t1.models.sessions.ModuleSection;
import g1t1.models.users.Student;

public class ReportTesting {
    public static void main(String[] args) {
        // filepath for testing
        String filePath = "./src/main/java/g1t1/testing/outputs/";

        // sample module
        ModuleSection modulesection1 = new ModuleSection("CS 102", "G1", "AY25-26T1", "SCIS1 2-4", 3, "08:00", "11:30");
        ModuleSection modulesection2 = new ModuleSection("CS 102", "G2", "AY25-26T1", "SCIS1 2-4", 3, "15:30", "18:45");

        // sample students
        // Using all String constructor
        Student stu1 = new Student(new StudentID("S001"), "Alice", modulesection1, "alice@example.com");
        // Using ModuleSection object directly
        Student stu2 = new Student(new StudentID("S002"), "Bob", modulesection1, "bob@example.com");
        // Using StudentID object + String module/section
        Student stu3 = new Student(new StudentID("S003"), "Charlie", modulesection2, "charlie@example.com");
        // Using StudentID + ModuleSection object
        Student stu4 = new Student(new StudentID("S004"), "Diana", modulesection2, "diana@example.com");

        // adding students
        modulesection1.addStudent(stu1);
        modulesection1.addStudent(stu2);
        modulesection2.addStudent(stu3);
        modulesection2.addStudent(stu4);

        // sample session
        ClassSession session1 = new ClassSession(modulesection1, 1);

        // Reportbuilder
        ReportBuilder builder = new ReportBuilder().includeReport(session1).withTimeStamp().withNotes().withConfidence()
                .withMethod();

        // Report
        Report report = new Report(builder); // generates the report

        // List all student IDs
        for (Student s : report.getClassSession().getModuleSection().getStudents()) {
            System.out.println("Student ID: " + s.getId());
        }

        CSVReport csvreport = new CSVReport(filePath + "csvfile.csv");
        csvreport.generate(report);
    }
}