package g1t1.testing;

import java.util.List;

import g1t1.features.report.*;
import g1t1.models.users.*;
import g1t1.models.sessions.*;

public class ReportTesting {
    public static void main(String[] args) {
        // filepath for testing
        String filePath = "./src/main/java/g1t1/testing/outputs/";

        

        // sample session
        List<ClassSession> sessions = SampleClassSessions.generateSampleData();
        ClassSession session1 = sessions.get(0);
        ClassSession session2 = sessions.get(1);
        ClassSession session3 = sessions.get(2);


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