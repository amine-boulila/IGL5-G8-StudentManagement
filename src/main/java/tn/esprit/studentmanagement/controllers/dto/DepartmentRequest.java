package tn.esprit.studentmanagement.controllers.dto;

public record DepartmentRequest(
        Long idDepartment,
        String name,
        String location,
        String phone,
        String head
) {
}
