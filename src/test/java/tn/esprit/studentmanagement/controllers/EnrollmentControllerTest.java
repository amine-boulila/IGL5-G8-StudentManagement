package tn.esprit.studentmanagement.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tn.esprit.studentmanagement.dto.EnrollmentRequest;
import tn.esprit.studentmanagement.entities.Course;
import tn.esprit.studentmanagement.entities.Enrollment;
import tn.esprit.studentmanagement.entities.Student;
import tn.esprit.studentmanagement.services.ICourseService;
import tn.esprit.studentmanagement.services.IEnrollment;
import tn.esprit.studentmanagement.services.IStudentService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

class EnrollmentControllerTest {

    private IEnrollment enrollmentService;
    private IStudentService studentService;
    private ICourseService courseService;
    private EnrollmentController controller;

    @BeforeEach
    void setUp() {
        enrollmentService = mock(IEnrollment.class);
        studentService = mock(IStudentService.class);
        courseService = mock(ICourseService.class);
        controller = new EnrollmentController(enrollmentService, studentService, courseService);
    }

    @Test
    void getAllEnrollmentsDelegatesToService() {
        List<Enrollment> enrollments = List.of(new Enrollment());
        when(enrollmentService.getAllEnrollments()).thenReturn(enrollments);

        assertSame(enrollments, controller.getAllEnrollment());
        verify(enrollmentService).getAllEnrollments();
    }

    @Test
    void getEnrollmentDelegatesToService() {
        Enrollment enrollment = new Enrollment();
        when(enrollmentService.getEnrollmentById(4L)).thenReturn(enrollment);

        assertSame(enrollment, controller.getEnrollment(4L));
        verify(enrollmentService).getEnrollmentById(4L);
    }

    @Test
    void createEnrollmentDelegatesToService() {
        EnrollmentRequest request = new EnrollmentRequest();
        request.setGrade(16.0);
        EnrollmentRequest.StudentReference studentReference = new EnrollmentRequest.StudentReference();
        studentReference.setIdStudent(5L);
        request.setStudent(studentReference);
        EnrollmentRequest.CourseReference courseReference = new EnrollmentRequest.CourseReference();
        courseReference.setIdCourse(6L);
        request.setCourse(courseReference);
        Student student = new Student();
        Course course = new Course();
        when(studentService.getStudentById(5L)).thenReturn(student);
        when(courseService.getCourseById(6L)).thenReturn(course);
        Enrollment savedEnrollment = new Enrollment();
        savedEnrollment.setGrade(16.0);
        when(enrollmentService.saveEnrollment(any(Enrollment.class))).thenReturn(savedEnrollment);

        assertSame(savedEnrollment, controller.createEnrollment(request));
        ArgumentCaptor<Enrollment> captor = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollmentService).saveEnrollment(captor.capture());
        assertEquals(16.0, captor.getValue().getGrade());
        assertSame(student, captor.getValue().getStudent());
        assertSame(course, captor.getValue().getCourse());
        verify(studentService).getStudentById(5L);
        verify(courseService).getCourseById(6L);
    }

    @Test
    void updateEnrollmentDelegatesToService() {
        EnrollmentRequest request = new EnrollmentRequest();
        request.setIdEnrollment(9L);
        Enrollment savedEnrollment = new Enrollment();
        savedEnrollment.setIdEnrollment(9L);
        when(enrollmentService.saveEnrollment(any(Enrollment.class))).thenReturn(savedEnrollment);

        assertSame(savedEnrollment, controller.updateEnrollment(request));
        verify(enrollmentService).saveEnrollment(any(Enrollment.class));
    }

    @Test
    void deleteEnrollmentDelegatesToService() {
        controller.deleteEnrollment(4L);

        verify(enrollmentService).deleteEnrollment(4L);
    }
}
