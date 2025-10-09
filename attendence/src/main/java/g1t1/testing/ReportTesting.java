package g1t1.testing;

import g1t1.features.report.Report;
import g1t1.features.report.ReportBuilder;
import g1t1.models.ids.StudentID;
import g1t1.models.sessions.ModuleSection;
import g1t1.models.users.Student;

public class ReportTesting {
    public static void main(String[] args) {
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

        // Reportbuilder
        ReportBuilder builder = new ReportBuilder().includeReport(modulesection1).withTimeStamp().withNotes();

        // Report
        Report report = new Report(builder); // generates the report

        // Access info from report to verify
        System.out.println("Include Student Id: " + builder.isIncludeStudentId());
        System.out.println("Include TimeStamp: " + builder.isIncludeTimeStamp());
        System.out.println("Include Notes: " + builder.isIncludeNotes());

        ModuleSection reportSection = builder.getModuleSection();
        System.out.println("Report Module: " + reportSection.getModule());
        System.out.println("Report Section: " + reportSection.getSection());
        System.out.println("Number of students: " + reportSection.getStudents().size());

        // List all student IDs
        for (Student s : reportSection.getStudents()) {
            System.out.println("Student ID: " + s.getId());
        }

    }
}