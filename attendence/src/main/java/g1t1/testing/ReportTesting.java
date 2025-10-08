package g1t1.testing;

import g1t1.features.report.*;
import g1t1.models.users.*;
import g1t1.models.sessions.*;
import g1t1.models.ids.*;

public class ReportTesting {
    public static void main(String[] args) {
        // sample module
        ModuleSection modulesection1 = new ModuleSection("CS 102", "G1", 3, "08:00", "11:30");
        ModuleSection modulesection2 = new ModuleSection("CS 102", "G2", 3, "15:30", "18:45");

        // sample students
        // Using all String constructor
        Student stu1 = new Student(new StudentID("S001"), "Alice", modulesection1, "alice@example.com", "555-1234");
        // Using ModuleSection object directly
        Student stu2 = new Student(new StudentID("S002"), "Bob", modulesection1, "bob@example.com", "555-2345");
        // Using StudentID object + String module/section
        Student stu3 = new Student(new StudentID("S003"), "Charlie", modulesection2, "charlie@example.com", "555-3456");
        // Using StudentID + ModuleSection object
        Student stu4 = new Student(new StudentID("S004"), "Diana", modulesection2, "diana@example.com", "555-4567");

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