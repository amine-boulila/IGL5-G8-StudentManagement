package tn.esprit.studentmanagement.controllers;

import org.junit.jupiter.api.Test;
import tn.esprit.studentmanagement.entities.Enrollment;
import tn.esprit.studentmanagement.services.IEnrollment;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class EnrollmentControllerTest {

    private final IEnrollment enrollmentService = mock(IEnrollment.class);
    private final EnrollmentController controller = new EnrollmentController(enrollmentService);

    @Test
    void getAllEnrollmentShouldReturnServiceResults() {
        List<Enrollment> enrollments = List.of(new Enrollment());
        when(enrollmentService.getAllEnrollments()).thenReturn(enrollments);

        assertEquals(enrollments, controller.getAllEnrollment());

        verify(enrollmentService).getAllEnrollments();
        verifyNoMoreInteractions(enrollmentService);
    }

    @Test
    void getEnrollmentShouldReturnServiceResult() {
        Enrollment enrollment = new Enrollment();
        when(enrollmentService.getEnrollmentById(7L)).thenReturn(enrollment);

        assertEquals(enrollment, controller.getEnrollment(7L));

        verify(enrollmentService).getEnrollmentById(7L);
        verifyNoMoreInteractions(enrollmentService);
    }

    @Test
    void createEnrollmentShouldSaveRequestBody() {
        Enrollment enrollment = new Enrollment();
        when(enrollmentService.saveEnrollment(enrollment)).thenReturn(enrollment);

        assertEquals(enrollment, controller.createEnrollment(enrollment));

        verify(enrollmentService).saveEnrollment(enrollment);
        verifyNoMoreInteractions(enrollmentService);
    }

    @Test
    void updateEnrollmentShouldSaveRequestBody() {
        Enrollment enrollment = new Enrollment();
        when(enrollmentService.saveEnrollment(enrollment)).thenReturn(enrollment);

        assertEquals(enrollment, controller.updateEnrollment(enrollment));

        verify(enrollmentService).saveEnrollment(enrollment);
        verifyNoMoreInteractions(enrollmentService);
    }

    @Test
    void deleteEnrollmentShouldDelegateId() {
        controller.deleteEnrollment(7L);

        verify(enrollmentService).deleteEnrollment(7L);
        verifyNoMoreInteractions(enrollmentService);
    }
}
