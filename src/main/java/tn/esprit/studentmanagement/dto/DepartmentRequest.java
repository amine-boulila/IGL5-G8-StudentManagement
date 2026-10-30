package tn.esprit.studentmanagement.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DepartmentRequest {
    private Long idDepartment;
    private String name;
    private String location;
    private String phone;
    private String head;
}
