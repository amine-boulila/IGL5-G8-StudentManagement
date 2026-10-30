package tn.esprit.studentmanagement.controllers;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.studentmanagement.dto.DepartmentDTO;
import tn.esprit.studentmanagement.entities.Department;
import tn.esprit.studentmanagement.services.IDepartmentService;

import java.util.List;

@RestController
@RequestMapping("/Depatment")
@CrossOrigin(origins = "http://localhost:4200")
@AllArgsConstructor
public class DepartmentController {
    private IDepartmentService departmentService;

    @GetMapping("/getAllDepartment")
    public List<Department> getAllDepartment() { return departmentService.getAllDepartments(); }

    @GetMapping("/getDepartment/{id}")
    public Department getDepartment(@PathVariable Long id) { return departmentService.getDepartmentById(id); }

    @PostMapping("/createDepartment")
    public Department createDepartment(@RequestBody DepartmentDTO department) { return departmentService.saveDepartment(department.toEntity()); }

    @PutMapping("/updateDepartment")
    public Department updateDepartment(@RequestBody DepartmentDTO department) {
        return departmentService.saveDepartment(department.toEntity());
    }

    @DeleteMapping("/deleteDepartment/{id}")
    public void deleteDepartment(@PathVariable Long id) {
      departmentService.deleteDepartment(id); }
}
