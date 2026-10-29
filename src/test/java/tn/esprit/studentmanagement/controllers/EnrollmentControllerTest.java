package tn.esprit.studentmanagement.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tn.esprit.studentmanagement.dto.EnrollmentDto;
import tn.esprit.studentmanagement.entities.Course;
import tn.esprit.studentmanagement.entities.Enrollment;
import tn.esprit.studentmanagement.entities.Status;
import tn.esprit.studentmanagement.entities.Student;
import tn.esprit.studentmanagement.repositories.CourseRepository;
import tn.esprit.studentmanagement.repositories.StudentRepository;
import tn.esprit.studentmanagement.services.IEnrollment;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EnrollmentControllerTest {

    private IEnrollment enrollmentService;
    private StudentRepository studentRepository;
    private CourseRepository courseRepository;
    private EnrollmentController controller;

    @BeforeEach
    void setUp() {
        enrollmentService = mock(IEnrollment.class);
        studentRepository = mock(StudentRepository.class);
        courseRepository = mock(CourseRepository.class);
        controller = new EnrollmentController(enrollmentService, studentRepository, courseRepository);
    }

    @Test
    void getAllAndGetEnrollmentShouldReturnDtos() {
        Enrollment enrollment = enrollment(1L, student(2L), course(3L));
        when(enrollmentService.getAllEnrollments()).thenReturn(List.of(enrollment));
        when(enrollmentService.getEnrollmentById(1L)).thenReturn(enrollment);

        assertEquals(2L, controller.getAllEnrollment().get(0).studentId());
        assertEquals(3L, controller.getEnrollment(1L).courseId());
    }

    @Test
    void createAndUpdateEnrollmentShouldResolveReferences() {
        EnrollmentDto dto = new EnrollmentDto(null, LocalDate.of(2026, 9, 1), 15.5,
                Status.ACTIVE, 2L, 3L);
        when(studentRepository.getReferenceById(2L)).thenReturn(student(2L));
        when(courseRepository.getReferenceById(3L)).thenReturn(course(3L));
        when(enrollmentService.saveEnrollment(any(Enrollment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals(2L, controller.createEnrollment(dto).studentId());
        assertEquals(3L, controller.updateEnrollment(dto).courseId());
        verify(studentRepository, times(2)).getReferenceById(2L);
        verify(courseRepository, times(2)).getReferenceById(3L);
    }

    @Test
    void enrollmentWithoutReferencesShouldKeepIdsNull() {
        EnrollmentDto dto = new EnrollmentDto(4L, LocalDate.now(), null, Status.ACTIVE, null, null);
        when(enrollmentService.saveEnrollment(any(Enrollment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EnrollmentDto result = controller.createEnrollment(dto);

        assertNull(result.studentId());
        assertNull(result.courseId());
        verifyNoInteractions(studentRepository, courseRepository);
    }

    @Test
    void deleteEnrollmentShouldDelegate() {
        controller.deleteEnrollment(1L);

        verify(enrollmentService).deleteEnrollment(1L);
    }

    private Enrollment enrollment(Long id, Student student, Course course) {
        Enrollment enrollment = new Enrollment();
        enrollment.setIdEnrollment(id);
        enrollment.setEnrollmentDate(LocalDate.of(2026, 9, 1));
        enrollment.setGrade(15.5);
        enrollment.setStatus(Status.ACTIVE);
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        return enrollment;
    }

    private Student student(Long id) {
        Student student = new Student();
        student.setIdStudent(id);
        return student;
    }

    private Course course(Long id) {
        Course course = new Course();
        course.setIdCourse(id);
        return course;
    }
}
