package tn.esprit.studentmanagement.dto;

import tn.esprit.studentmanagement.entities.Department;

public final class DepartmentMapper {
    private DepartmentMapper() {}

    public static DepartmentDTO toDto(Department d) {
        if (d == null) return null;
        return new DepartmentDTO(d.getIdDepartment(), d.getName(),
                d.getLocation(), d.getPhone(), d.getHead());
    }

    public static Department toEntity(DepartmentDTO dto) {
        if (dto == null) return null;
        Department d = new Department();
        d.setIdDepartment(dto.getIdDepartment());
        d.setName(dto.getName());
        d.setLocation(dto.getLocation());
        d.setPhone(dto.getPhone());
        d.setHead(dto.getHead());
        return d;
    }
}