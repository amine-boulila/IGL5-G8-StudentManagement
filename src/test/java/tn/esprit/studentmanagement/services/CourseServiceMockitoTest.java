package tn.esprit.studentmanagement.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.studentmanagement.entities.Course;
import tn.esprit.studentmanagement.repositories.CourseRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceMockitoTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseService courseService;

    @Test
    void getAllCoursesShouldReturnRepositoryResults() {
        List<Course> courses = List.of(createCourse(1L, "Algorithms", "CS101", 5));
        when(courseRepository.findAll()).thenReturn(courses);

        List<Course> result = courseService.getAllCourses();

        assertEquals(courses, result);
        verify(courseRepository).findAll();
        verifyNoMoreInteractions(courseRepository);
    }

    @Test
    void getCourseByIdShouldReturnCourseWhenPresent() {
        Course course = createCourse(1L, "Algorithms", "CS101", 5);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        Course result = courseService.getCourseById(1L);

        assertEquals(course, result);
        verify(courseRepository).findById(1L);
        verifyNoMoreInteractions(courseRepository);
    }

    @Test
    void getCourseByIdShouldThrowWhenMissing() {
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> courseService.getCourseById(99L));

        verify(courseRepository).findById(99L);
        verifyNoMoreInteractions(courseRepository);
    }

    @Test
    void saveCourseShouldReturnSavedCourse() {
        Course course = createCourse(null, "Algorithms", "CS101", 5);
        Course savedCourse = createCourse(1L, "Algorithms", "CS101", 5);
        when(courseRepository.save(course)).thenReturn(savedCourse);

        Course result = courseService.saveCourse(course);

        assertEquals(savedCourse, result);
        verify(courseRepository).save(course);
        verifyNoMoreInteractions(courseRepository);
    }

    @Test
    void deleteCourseShouldDelegateToRepository() {
        courseService.deleteCourse(1L);

        verify(courseRepository).deleteById(1L);
        verifyNoMoreInteractions(courseRepository);
    }

    private Course createCourse(Long id, String name, String code, int credit) {
        Course course = new Course();
        course.setIdCourse(id);
        course.setName(name);
        course.setCode(code);
        course.setCredit(credit);
        return course;
    }
}
