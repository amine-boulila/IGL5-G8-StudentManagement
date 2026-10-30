package tn.esprit.studentmanagement.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tn.esprit.studentmanagement.entities.Department;
import tn.esprit.studentmanagement.services.IDepartmentService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DepartmentControllerTest {

    private IDepartmentService departmentService;
    private DepartmentController controller;

    @BeforeEach
    void setUp() {
        departmentService = mock(IDepartmentService.class);
        controller = new DepartmentController(departmentService);
    }

    @Test
    void getAllDepartmentsDelegatesToService() {
        List<Department> departments = List.of(new Department());
        when(departmentService.getAllDepartments()).thenReturn(departments);

        assertSame(departments, controller.getAllDepartment());
        verify(departmentService).getAllDepartments();
    }

    @Test
    void getDepartmentDelegatesToService() {
        Department department = new Department();
        when(departmentService.getDepartmentById(3L)).thenReturn(department);

        assertSame(department, controller.getDepartment(3L));
        verify(departmentService).getDepartmentById(3L);
    }

    @Test
    void createDepartmentDelegatesToService() {
        Department department = new Department();
        when(departmentService.saveDepartment(department)).thenReturn(department);

        assertSame(department, controller.createDepartment(department));
        verify(departmentService).saveDepartment(department);
    }

    @Test
    void updateDepartmentDelegatesToService() {
        Department department = new Department();
        when(departmentService.saveDepartment(department)).thenReturn(department);

        assertSame(department, controller.updateDepartment(department));
        verify(departmentService).saveDepartment(department);
    }

    @Test
    void deleteDepartmentDelegatesToService() {
        controller.deleteDepartment(3L);

        verify(departmentService).deleteDepartment(3L);
    }
}
