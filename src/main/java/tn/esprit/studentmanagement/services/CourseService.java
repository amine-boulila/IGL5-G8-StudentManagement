package tn.esprit.studentmanagement.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.studentmanagement.entities.Course;
import tn.esprit.studentmanagement.repositories.CourseRepository;

import java.util.List;

@Service
public class CourseService implements ICourseService {
    private static final Logger logger = LogManager.getLogger(CourseService.class);

    @Autowired
    private CourseRepository courseRepository;

    @Override
    public List<Course> getAllCourses() {
        logger.info("Fetching all courses");
        try {
            List<Course> courses = courseRepository.findAll();
            logger.debug("Found {} courses", courses.size());
            return courses;
        } catch (RuntimeException exception) {
            logger.error("Failed to fetch courses", exception);
            throw exception;
        }
    }

    @Override
    public Course getCourseById(Long idCourse) {
        logger.info("Fetching course with id {}", idCourse);
        try {
            Course course = courseRepository.findById(idCourse).orElseThrow();
            logger.debug("Found course with id {}", idCourse);
            return course;
        } catch (RuntimeException exception) {
            logger.error("Failed to fetch course with id {}", idCourse, exception);
            throw exception;
        }
    }

    @Override
    public Course saveCourse(Course course) {
        logger.info("Saving course");
        try {
            Course savedCourse = courseRepository.save(course);
            logger.debug("Saved course with id {}", savedCourse.getIdCourse());
            return savedCourse;
        } catch (RuntimeException exception) {
            logger.error("Failed to save course", exception);
            throw exception;
        }
    }

    @Override
    public void deleteCourse(Long idCourse) {
        logger.info("Deleting course with id {}", idCourse);
        try {
            courseRepository.deleteById(idCourse);
            logger.debug("Deleted course with id {}", idCourse);
        } catch (RuntimeException exception) {
            logger.error("Failed to delete course with id {}", idCourse, exception);
            throw exception;
        }
    }
}
