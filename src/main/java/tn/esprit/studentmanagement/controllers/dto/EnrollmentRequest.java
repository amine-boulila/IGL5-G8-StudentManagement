package tn.esprit.studentmanagement.controllers.dto;

import tn.esprit.studentmanagement.entities.Status;

import java.time.LocalDate;

public record EnrollmentRequest(
        Long idEnrollment,
        LocalDate enrollmentDate,
        Double grade,
        Status status,
        Long studentId,
        Long courseId
) {
}
