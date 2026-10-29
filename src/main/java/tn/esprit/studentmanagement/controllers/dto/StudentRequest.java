package tn.esprit.studentmanagement.controllers.dto;

import java.time.LocalDate;

public record StudentRequest(
        Long idStudent,
        String firstName,
        String lastName,
        String email,
        String phone,
        LocalDate dateOfBirth,
        String address,
        Long departmentId
) {
}
