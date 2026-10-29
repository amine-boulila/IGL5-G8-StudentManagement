package tn.esprit.studentmanagement.dto;

public record DepartmentDto(
        Long idDepartment,
        String name,
        String location,
        String phone,
        String head) {
}
