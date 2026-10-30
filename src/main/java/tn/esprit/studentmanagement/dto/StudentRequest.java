package tn.esprit.studentmanagement.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class StudentRequest {
    private Long idStudent;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private String address;
    private DepartmentReference department;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class DepartmentReference {
        private Long idDepartment;
    }
}
