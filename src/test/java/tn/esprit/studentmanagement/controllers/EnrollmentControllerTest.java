package tn.esprit.studentmanagement.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tn.esprit.studentmanagement.entities.Enrollment;
import tn.esprit.studentmanagement.services.IEnrollment;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EnrollmentControllerTest {

    private IEnrollment enrollmentService;
    private EnrollmentController controller;

    @BeforeEach
    void setUp() {
        enrollmentService = mock(IEnrollment.class);
        controller = new EnrollmentController(enrollmentService);
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
        Enrollment enrollment = new Enrollment();
        when(enrollmentService.saveEnrollment(enrollment)).thenReturn(enrollment);

        assertSame(enrollment, controller.createEnrollment(enrollment));
        verify(enrollmentService).saveEnrollment(enrollment);
    }

    @Test
    void updateEnrollmentDelegatesToService() {
        Enrollment enrollment = new Enrollment();
        when(enrollmentService.saveEnrollment(enrollment)).thenReturn(enrollment);

        assertSame(enrollment, controller.updateEnrollment(enrollment));
        verify(enrollmentService).saveEnrollment(enrollment);
    }

    @Test
    void deleteEnrollmentDelegatesToService() {
        controller.deleteEnrollment(4L);

        verify(enrollmentService).deleteEnrollment(4L);
    }
}
