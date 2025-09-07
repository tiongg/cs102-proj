package g1t1;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.opencv_java;
import org.bytedeco.opencv.global.opencv_core;

import g1t1.models.Student;

public class App {
    public static void main(String[] args) {
        Student s = new Student("Jeff");
        System.out.println(s);

        Loader.load(opencv_java.class);
        System.out.println("OpenCV version: " + opencv_core.CV_VERSION);
    }
}