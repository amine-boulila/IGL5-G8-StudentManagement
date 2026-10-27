package tn.esprit.studentmanagement.services;

import tn.esprit.studentmanagement.entities.Course;

import java.util.List;

public interface ICourseService {
    List<Course> getAllCourses();
    Course getCourseById(Long idCourse);
    Course saveCourse(Course course);
    void deleteCourse(Long idCourse);
}
