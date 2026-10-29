package tn.esprit.studentmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDTO {
    private Long idDepartment;
    private String name;
    private String location;
    private String phone;
    private String head;
}