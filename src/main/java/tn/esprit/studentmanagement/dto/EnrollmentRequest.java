package tn.esprit.studentmanagement.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tn.esprit.studentmanagement.entities.Status;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class EnrollmentRequest {
    private Long idEnrollment;
    private LocalDate enrollmentDate;
    private Double grade;
    private Status status;
    private StudentReference student;
    private CourseReference course;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class StudentReference {
        private Long idStudent;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class CourseReference {
        private Long idCourse;
    }
}
