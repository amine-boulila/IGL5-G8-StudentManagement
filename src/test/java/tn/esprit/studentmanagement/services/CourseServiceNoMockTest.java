package tn.esprit.studentmanagement.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.studentmanagement.entities.Course;
import tn.esprit.studentmanagement.repositories.CourseRepository;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class CourseServiceNoMockTest {

    @Autowired
    private ICourseService courseService;

    @Autowired
    private CourseRepository courseRepository;

    @Test
    void saveCourseShouldPersistAndGenerateId() {
        Course savedCourse = courseService.saveCourse(createCourse("Algorithms", "CS101", 5));

        assertNotNull(savedCourse.getIdCourse());
        assertEquals("Algorithms", savedCourse.getName());
        assertTrue(courseRepository.existsById(savedCourse.getIdCourse()));
    }

    @Test
    void getAllCoursesShouldReturnPersistedCourses() {
        Course first = courseService.saveCourse(createCourse("Algorithms", "CS101", 5));
        Course second = courseService.saveCourse(createCourse("Databases", "CS202", 4));

        List<Course> result = courseService.getAllCourses();

        assertEquals(2, result.size());
        List<Long> resultIds = result.stream().map(Course::getIdCourse).toList();
        assertTrue(resultIds.containsAll(List.of(first.getIdCourse(), second.getIdCourse())));
    }

    @Test
    void getCourseByIdShouldReturnPersistedCourse() {
        Course savedCourse = courseService.saveCourse(createCourse("Algorithms", "CS101", 5));

        Course result = courseService.getCourseById(savedCourse.getIdCourse());

        assertEquals(savedCourse.getIdCourse(), result.getIdCourse());
        assertEquals("CS101", result.getCode());
        assertEquals(5, result.getCredit());
    }

    @Test
    void getCourseByIdShouldThrowForUnknownId() {
        assertThrows(NoSuchElementException.class, () -> courseService.getCourseById(Long.MAX_VALUE));
    }

    @Test
    void deleteCourseShouldRemovePersistedCourse() {
        Course savedCourse = courseService.saveCourse(createCourse("Algorithms", "CS101", 5));

        courseService.deleteCourse(savedCourse.getIdCourse());

        assertFalse(courseRepository.existsById(savedCourse.getIdCourse()));
    }

    private Course createCourse(String name, String code, int credit) {
        Course course = new Course();
        course.setName(name);
        course.setCode(code);
        course.setCredit(credit);
        return course;
    }
}
