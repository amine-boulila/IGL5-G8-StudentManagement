package tn.esprit.studentmanagement.controllers;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.studentmanagement.entities.Department;
import tn.esprit.studentmanagement.dto.DepartmentDto;
import tn.esprit.studentmanagement.services.IDepartmentService;

import java.util.List;

@RestController
@RequestMapping("/Depatment")
@CrossOrigin(origins = "http://localhost:4200")
@AllArgsConstructor
public class DepartmentController {
    private final IDepartmentService departmentService;

    @GetMapping("/getAllDepartment")
    public List<DepartmentDto> getAllDepartment() {
        return departmentService.getAllDepartments().stream()
                .map(this::toDto)
                .toList();
    }

    @GetMapping("/getDepartment/{id}")
    public DepartmentDto getDepartment(@PathVariable Long id) {
        return toDto(departmentService.getDepartmentById(id));
    }

    @PostMapping("/createDepartment")
    public DepartmentDto createDepartment(@RequestBody DepartmentDto department) {
        return toDto(departmentService.saveDepartment(toEntity(department)));
    }

    @PutMapping("/updateDepartment")
    public DepartmentDto updateDepartment(@RequestBody DepartmentDto department) {
        return toDto(departmentService.saveDepartment(toEntity(department)));
    }

    @DeleteMapping("/deleteDepartment/{id}")
    public void deleteDepartment(@PathVariable Long id) {
      departmentService.deleteDepartment(id); }

    private DepartmentDto toDto(Department department) {
        return new DepartmentDto(
                department.getIdDepartment(),
                department.getName(),
                department.getLocation(),
                department.getPhone(),
                department.getHead());
    }

    private Department toEntity(DepartmentDto department) {
        Department entity = new Department();
        entity.setIdDepartment(department.idDepartment());
        entity.setName(department.name());
        entity.setLocation(department.location());
        entity.setPhone(department.phone());
        entity.setHead(department.head());
        return entity;
    }
}
