package tn.esprit.studentmanagement.dto;

import java.time.LocalDate;

public record StudentDto(
        Long idStudent,
        String firstName,
        String lastName,
        String email,
        String phone,
        LocalDate dateOfBirth,
        String address,
        Long departmentId) {
}
