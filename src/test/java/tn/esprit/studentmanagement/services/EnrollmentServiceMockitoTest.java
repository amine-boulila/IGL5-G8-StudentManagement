package tn.esprit.studentmanagement.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.studentmanagement.entities.Enrollment;
import tn.esprit.studentmanagement.entities.Status;
import tn.esprit.studentmanagement.repositories.EnrollmentRepository;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceMockitoTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private EnrollmentService enrollmentService;

    // CREATE
    @Test
    void saveEnrollmentShouldReturnSavedEnrollment() {
        Enrollment enrollment = createEnrollment(null, 15.5, Status.ACTIVE);
        Enrollment savedEnrollment = createEnrollment(1L, 15.5, Status.ACTIVE);
        when(enrollmentRepository.save(enrollment)).thenReturn(savedEnrollment);

        Enrollment result = enrollmentService.saveEnrollment(enrollment);

        assertNotNull(result);
        assertEquals(1L, result.getIdEnrollment());
        assertEquals(15.5, result.getGrade());
        assertEquals(Status.ACTIVE, result.getStatus());
        verify(enrollmentRepository).save(enrollment);
        verifyNoMoreInteractions(enrollmentRepository);
    }

    // READ
    @Test
    void getEnrollmentByIdShouldReturnEnrollmentWhenPresent() {
        Enrollment enrollment = createEnrollment(1L, 12.0, Status.COMPLETED);
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment));

        Enrollment result = enrollmentService.getEnrollmentById(1L);

        assertEquals(enrollment, result);
        assertEquals(Status.COMPLETED, result.getStatus());
        verify(enrollmentRepository).findById(1L);
        verifyNoMoreInteractions(enrollmentRepository);
    }

    // UPDATE (save sur une entité qui a déjà un id)
    @Test
    void saveEnrollmentShouldUpdateExistingEnrollment() {
        Enrollment enrollment = createEnrollment(1L, 18.0, Status.COMPLETED);
        when(enrollmentRepository.save(enrollment)).thenReturn(enrollment);

        Enrollment result = enrollmentService.saveEnrollment(enrollment);

        assertEquals(1L, result.getIdEnrollment());
        assertEquals(18.0, result.getGrade());
        assertEquals(Status.COMPLETED, result.getStatus());
        verify(enrollmentRepository).save(enrollment);
        verifyNoMoreInteractions(enrollmentRepository);
    }

    // DELETE
    @Test
    void deleteEnrollmentShouldDelegateToRepository() {
        enrollmentService.deleteEnrollment(1L);

        verify(enrollmentRepository).deleteById(1L);
        verifyNoMoreInteractions(enrollmentRepository);
    }

    // Cas d'erreur bonus : le service utilise Optional.get()
    @Test
    void getEnrollmentByIdShouldThrowWhenMissing() {
        when(enrollmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> enrollmentService.getEnrollmentById(99L));

        verify(enrollmentRepository).findById(99L);
        verifyNoMoreInteractions(enrollmentRepository);
    }

    private Enrollment createEnrollment(Long id, Double grade, Status status) {
        Enrollment enrollment = new Enrollment();
        enrollment.setIdEnrollment(id);
        enrollment.setEnrollmentDate(LocalDate.of(2026, 9, 1));
        enrollment.setGrade(grade);
        enrollment.setStatus(status);
        return enrollment;
    }
}
