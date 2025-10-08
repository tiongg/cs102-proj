package g1t1.testing;

import g1t1.db.attendance.MarkingMethod;
import g1t1.models.ids.StudentID;
import g1t1.models.ids.TeacherID;
import g1t1.models.sessions.ClassSession;
import g1t1.models.sessions.ModuleSection;
import g1t1.models.sessions.SessionAttendance;
import g1t1.models.sessions.SessionStatus;
import g1t1.models.users.FaceData;
import g1t1.models.users.Student;
import g1t1.models.users.Teacher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Mock database class for testing
 */
public class MockDb {
    private final static List<Teacher> teachers = new ArrayList<>();
    private final static Map<TeacherID, List<ModuleSection>> teacherClasses = new HashMap<>();
    private final static Map<TeacherID, List<ClassSession>> pastSessions = new HashMap<>();

    static {
        Teacher newTeacher = new Teacher("123", "Dr Zhang", "ZZY@goat.com", new FaceData());
        teachers.add(newTeacher);

        ModuleSection morningClass = new ModuleSection("CS 102", "G1", 3, "08:00", "11:30");
        ModuleSection afternoonClass = new ModuleSection("CS 102", "G2", 3, "15:30", "18:45");

        teacherClasses.put(newTeacher.getID(), Arrays.asList(morningClass, afternoonClass));

        // Students have to be loaded into the module section first
        loadEnrolledStudents();

        ClassSession morningSession = new ClassSession(morningClass, 1, LocalDateTime.of(2025, 9, 3, 7, 50), SessionStatus.Ended);
        pastSessions.put(newTeacher.getID(),
                new ArrayList<>(Arrays.asList(
                        morningSession,
                        new ClassSession(afternoonClass, 1, LocalDateTime.of(2025, 9, 3, 15, 30), SessionStatus.Ended),
                        new ClassSession(morningClass, 1, LocalDateTime.of(2025, 9, 10, 8, 0), SessionStatus.Ended))));

        for (SessionAttendance attendance : morningSession.getStudentAttendance()) {
            attendance.markPresent(1, MarkingMethod.MANUAL);
        }
    }

    public static List<ModuleSection> getUserModuleSections(TeacherID id) {
        // First one
        return teacherClasses.get(teacherClasses.keySet().iterator().next());
    }

    public static List<ClassSession> getPastSessions(TeacherID id) {
        return pastSessions.get(teacherClasses.keySet().iterator().next());
    }

    private static void loadEnrolledStudents() {
        File baseDir = new File("test-photos");

        if (!baseDir.exists()) {
            return;
        }

        File[] studentDirs = baseDir.listFiles(File::isDirectory);
        if (studentDirs == null) {
            return;
        }

        int studentId = 1;
        // Add all students to both sections for now
        for (File studentDir : studentDirs) {
            try {
                List<byte[]> photos = loadPhotosFromFolder(studentDir.getPath());
                if (photos.isEmpty()) {
                    continue;
                }
                String name = capitalize(studentDir.getName());
                String id = "S" + String.format("%03d", studentId++);

                for (ModuleSection section : teacherClasses.values().stream().flatMap(List::stream).toList()) {
                    Student newStudent = createStudent(id, name, section, photos);
                    section.addStudent(newStudent);
                }
            } catch (IOException e) {
                System.err.println("Error loading photos for " + studentDir.getName() + ": " + e.getMessage());
            }
        }
    }

    private static List<byte[]> loadPhotosFromFolder(String folderPath) throws IOException {
        List<byte[]> photos = new ArrayList<>();
        File folder = new File(folderPath);

        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().matches(".*\\.(jpg|jpeg)$"));

        if (files != null) {
            Arrays.sort(files);
            for (File file : files) {
                try {
                    photos.add(Files.readAllBytes(file.toPath()));
                } catch (IOException e) {
                    System.out.println("[WARNING] Skipped: " + file.getName());
                }
            }
        }

        return photos;
    }

    private static Student createStudent(String id, String name, ModuleSection section, List<byte[]> photos) {
        Student student = new Student(new StudentID(id), name, section, name.toLowerCase() + "@school.edu");
        FaceData faceData = new FaceData();
        faceData.setFaceImages(photos);
        student.setFaceData(faceData);
        return student;
    }

    private static String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
