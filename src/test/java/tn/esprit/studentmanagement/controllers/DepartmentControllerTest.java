package tn.esprit.studentmanagement.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tn.esprit.studentmanagement.dto.DepartmentRequest;
import tn.esprit.studentmanagement.entities.Department;
import tn.esprit.studentmanagement.services.IDepartmentService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
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
        DepartmentRequest request = new DepartmentRequest();
        request.setName("Computer Science");
        request.setLocation("Block A");
        request.setPhone("111222333");
        request.setHead("Dr Smith");
        Department department = new Department();
        department.setName(request.getName());
        department.setLocation(request.getLocation());
        department.setPhone(request.getPhone());
        department.setHead(request.getHead());
        when(departmentService.saveDepartment(any(Department.class))).thenReturn(department);

        Department result = controller.createDepartment(request);

        assertSame(department, result);
        ArgumentCaptor<Department> captor = ArgumentCaptor.forClass(Department.class);
        verify(departmentService).saveDepartment(captor.capture());
        assertEquals("Computer Science", captor.getValue().getName());
        assertEquals("Block A", captor.getValue().getLocation());
    }

    @Test
    void updateDepartmentDelegatesToService() {
        DepartmentRequest request = new DepartmentRequest();
        request.setIdDepartment(3L);
        request.setName("Applied Physics");
        Department department = new Department();
        department.setIdDepartment(request.getIdDepartment());
        department.setName(request.getName());
        when(departmentService.saveDepartment(any(Department.class))).thenReturn(department);

        assertSame(department, controller.updateDepartment(request));
        ArgumentCaptor<Department> captor = ArgumentCaptor.forClass(Department.class);
        verify(departmentService).saveDepartment(captor.capture());
        assertEquals(3L, captor.getValue().getIdDepartment());
        assertEquals("Applied Physics", captor.getValue().getName());
    }

    @Test
    void deleteDepartmentDelegatesToService() {
        controller.deleteDepartment(3L);

        verify(departmentService).deleteDepartment(3L);
    }
}
