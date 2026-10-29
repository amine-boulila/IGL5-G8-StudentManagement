package tn.esprit.studentmanagement.controllers;

import org.junit.jupiter.api.Test;
import tn.esprit.studentmanagement.controllers.dto.EnrollmentRequest;
import tn.esprit.studentmanagement.entities.Enrollment;
import tn.esprit.studentmanagement.repositories.CourseRepository;
import tn.esprit.studentmanagement.repositories.StudentRepository;
import tn.esprit.studentmanagement.services.IEnrollment;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class EnrollmentControllerTest {

    private final IEnrollment enrollmentService = mock(IEnrollment.class);
    private final StudentRepository studentRepository = mock(StudentRepository.class);
    private final CourseRepository courseRepository = mock(CourseRepository.class);
    private final EnrollmentController controller = new EnrollmentController(
            enrollmentService, studentRepository, courseRepository);

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
        EnrollmentRequest request = new EnrollmentRequest(null, null, 15.5, null, null, null);
        when(enrollmentService.saveEnrollment(any(Enrollment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Enrollment result = controller.createEnrollment(request);

        assertEquals(15.5, result.getGrade());
        verify(enrollmentService).saveEnrollment(any(Enrollment.class));
        verifyNoMoreInteractions(enrollmentService);
        verifyNoMoreInteractions(studentRepository, courseRepository);
    }

    @Test
    void updateEnrollmentShouldSaveRequestBody() {
        EnrollmentRequest request = new EnrollmentRequest(7L, null, 18.0, null, null, null);
        when(enrollmentService.saveEnrollment(any(Enrollment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Enrollment result = controller.updateEnrollment(request);

        assertEquals(7L, result.getIdEnrollment());
        assertEquals(18.0, result.getGrade());
        verify(enrollmentService).saveEnrollment(any(Enrollment.class));
        verifyNoMoreInteractions(enrollmentService);
        verifyNoMoreInteractions(studentRepository, courseRepository);
    }

    @Test
    void deleteEnrollmentShouldDelegateId() {
        controller.deleteEnrollment(7L);

        verify(enrollmentService).deleteEnrollment(7L);
        verifyNoMoreInteractions(enrollmentService);
    }
}
