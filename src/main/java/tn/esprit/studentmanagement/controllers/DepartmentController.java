package tn.esprit.studentmanagement.controllers;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.studentmanagement.entities.Department;
import tn.esprit.studentmanagement.entities.Enrollment;
import tn.esprit.studentmanagement.services.DepartmentService;
import tn.esprit.studentmanagement.services.IDepartmentService;
import tn.esprit.studentmanagement.dto.DepartmentDTO;
import tn.esprit.studentmanagement.dto.DepartmentMapper;

import java.util.List;

@RestController
@RequestMapping("/Depatment")
@CrossOrigin(origins = "http://localhost:4200")
@AllArgsConstructor
public class DepartmentController {
    private IDepartmentService departmentService;

    @GetMapping("/getAllDepartments")
    public List<DepartmentDTO> getAllDepartments() {
        return departmentService.getAllDepartments().stream()
                .map(DepartmentMapper::toDto)
                .toList();
}
   @GetMapping("/getDepartment/{id}")
    public DepartmentDTO getDepartment(@PathVariable Long id) {
        return DepartmentMapper.toDto(departmentService.getDepartmentById(id));
    }

    @PostMapping("/createDepartment")
    public DepartmentDTO createDepartment(@RequestBody DepartmentDTO department) {
        Department saved = departmentService.saveDepartment(DepartmentMapper.toEntity(department));
        return DepartmentMapper.toDto(saved);
    }

    @PutMapping("/updateDepartment")
    public DepartmentDTO updateDepartment(@RequestBody DepartmentDTO department) {
        Department saved = departmentService.saveDepartment(DepartmentMapper.toEntity(department));
        return DepartmentMapper.toDto(saved);
    }

    @DeleteMapping("/deleteDepartment/{id}")
    public void deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
    }
}
