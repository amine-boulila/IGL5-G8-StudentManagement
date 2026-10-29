package tn.esprit.studentmanagement.dto;

import tn.esprit.studentmanagement.entities.Status;

import java.time.LocalDate;

public record EnrollmentDto(
        Long idEnrollment,
        LocalDate enrollmentDate,
        Double grade,
        Status status,
        Long studentId,
        Long courseId) {
}
