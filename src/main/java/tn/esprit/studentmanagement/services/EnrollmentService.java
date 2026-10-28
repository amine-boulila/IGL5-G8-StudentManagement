package tn.esprit.studentmanagement.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.studentmanagement.entities.Enrollment;
import tn.esprit.studentmanagement.repositories.EnrollmentRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class EnrollmentService implements IEnrollment {
    private static final Logger logger = LogManager.getLogger(EnrollmentService.class);

    private final EnrollmentRepository enrollmentRepository;

    @Override
    public List<Enrollment> getAllEnrollments() {
        logger.info("Fetching all enrollments");
        try {
            List<Enrollment> enrollments = enrollmentRepository.findAll();
            logger.debug("Found {} enrollments", enrollments.size());
            return enrollments;
        } catch (RuntimeException exception) {
            logger.error("Failed to fetch enrollments", exception);
            throw exception;
        }
    }

    @Override
    public Enrollment getEnrollmentById(Long idEnrollment) {
        logger.info("Fetching enrollment with id {}", idEnrollment);
        try {
            Enrollment enrollment = enrollmentRepository.findById(idEnrollment)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Enrollment not found with id " + idEnrollment));
            logger.debug("Found enrollment with id {}", idEnrollment);
            return enrollment;
        } catch (RuntimeException exception) {
            logger.error("Failed to fetch enrollment with id {}", idEnrollment, exception);
            throw exception;
        }
    }

    @Override
    public Enrollment saveEnrollment(Enrollment enrollment) {
        logger.info("Saving enrollment");
        try {
            Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
            logger.debug("Saved enrollment with id {}", savedEnrollment.getIdEnrollment());
            return savedEnrollment;
        } catch (RuntimeException exception) {
            logger.error("Failed to save enrollment", exception);
            throw exception;
        }
    }

    @Override
    public void deleteEnrollment(Long idEnrollment) {
        logger.info("Deleting enrollment with id {}", idEnrollment);
        try {
            enrollmentRepository.deleteById(idEnrollment);
            logger.debug("Deleted enrollment with id {}", idEnrollment);
        } catch (RuntimeException exception) {
            logger.error("Failed to delete enrollment with id {}", idEnrollment, exception);
            throw exception;
        }
    }
}
