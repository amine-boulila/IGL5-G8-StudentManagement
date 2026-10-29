package tn.esprit.studentmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tn.esprit.studentmanagement.entities.Status;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentRequestDTO {
    private LocalDate enrollmentDate;
    private Double grade;
    private Status status;
    private Long studentId;
    private Long courseId;
}
