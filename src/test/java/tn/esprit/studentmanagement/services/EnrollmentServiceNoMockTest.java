package tn.esprit.studentmanagement.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.studentmanagement.entities.Enrollment;
import tn.esprit.studentmanagement.entities.Status;
import tn.esprit.studentmanagement.repositories.EnrollmentRepository;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional // rollback automatique après chaque test : la base H2 reste propre
class EnrollmentServiceNoMockTest {

    @Autowired
    private IEnrollment enrollmentService;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    // CREATE
    @Test
    void saveEnrollmentShouldPersistAndGenerateId() {
        Enrollment saved = enrollmentService.saveEnrollment(createEnrollment(15.5, Status.ACTIVE));

        assertNotNull(saved.getIdEnrollment());
        Enrollment persisted = enrollmentRepository.findById(saved.getIdEnrollment()).orElseThrow();
        assertEquals(15.5, persisted.getGrade());
        assertEquals(Status.ACTIVE, persisted.getStatus());
        assertEquals(LocalDate.of(2026, 9, 1), persisted.getEnrollmentDate());
    }

    // READ
    @Test
    void getEnrollmentByIdShouldReturnPersistedEnrollment() {
        Enrollment saved = enrollmentService.saveEnrollment(createEnrollment(12.0, Status.COMPLETED));

        Enrollment found = enrollmentService.getEnrollmentById(saved.getIdEnrollment());

        assertEquals(saved.getIdEnrollment(), found.getIdEnrollment());
        assertEquals(12.0, found.getGrade());
        assertEquals(Status.COMPLETED, found.getStatus());
    }

    // UPDATE
    @Test
    void saveEnrollmentShouldUpdateExistingEnrollment() {
        Enrollment enrollment = enrollmentService.saveEnrollment(createEnrollment(10.0, Status.ACTIVE));
        enrollment.setGrade(18.0);
        enrollment.setStatus(Status.COMPLETED);

        Enrollment updated = enrollmentService.saveEnrollment(enrollment);

        assertEquals(enrollment.getIdEnrollment(), updated.getIdEnrollment());
        Enrollment persisted = enrollmentRepository.findById(enrollment.getIdEnrollment()).orElseThrow();
        assertEquals(18.0, persisted.getGrade());
        assertEquals(Status.COMPLETED, persisted.getStatus());
    }

    // DELETE
    @Test
    void deleteEnrollmentShouldRemovePersistedEnrollment() {
        Enrollment enrollment = enrollmentService.saveEnrollment(createEnrollment(9.0, Status.DROPPED));
        Long id = enrollment.getIdEnrollment();

        enrollmentService.deleteEnrollment(id);

        assertFalse(enrollmentRepository.existsById(id));
    }

    private Enrollment createEnrollment(Double grade, Status status) {
        Enrollment enrollment = new Enrollment();
        enrollment.setEnrollmentDate(LocalDate.of(2026, 9, 1));
        enrollment.setGrade(grade);
        enrollment.setStatus(status);
        return enrollment;
    }
}
