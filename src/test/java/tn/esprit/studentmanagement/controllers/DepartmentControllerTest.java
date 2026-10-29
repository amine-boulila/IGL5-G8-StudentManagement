package tn.esprit.studentmanagement.controllers;

import org.junit.jupiter.api.Test;
import tn.esprit.studentmanagement.entities.Department;
import tn.esprit.studentmanagement.services.IDepartmentService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class DepartmentControllerTest {

    private final IDepartmentService departmentService = mock(IDepartmentService.class);
    private final DepartmentController controller = new DepartmentController(departmentService);

    @Test
    void getAllDepartmentShouldReturnServiceResults() {
        List<Department> departments = List.of(new Department());
        when(departmentService.getAllDepartments()).thenReturn(departments);

        assertEquals(departments, controller.getAllDepartment());

        verify(departmentService).getAllDepartments();
        verifyNoMoreInteractions(departmentService);
    }

    @Test
    void getDepartmentShouldReturnServiceResult() {
        Department department = new Department();
        when(departmentService.getDepartmentById(7L)).thenReturn(department);

        assertEquals(department, controller.getDepartment(7L));

        verify(departmentService).getDepartmentById(7L);
        verifyNoMoreInteractions(departmentService);
    }

    @Test
    void createDepartmentShouldSaveRequestBody() {
        Department department = new Department();
        when(departmentService.saveDepartment(department)).thenReturn(department);

        assertEquals(department, controller.createDepartment(department));

        verify(departmentService).saveDepartment(department);
        verifyNoMoreInteractions(departmentService);
    }

    @Test
    void updateDepartmentShouldSaveRequestBody() {
        Department department = new Department();
        when(departmentService.saveDepartment(department)).thenReturn(department);

        assertEquals(department, controller.updateDepartment(department));

        verify(departmentService).saveDepartment(department);
        verifyNoMoreInteractions(departmentService);
    }

    @Test
    void deleteDepartmentShouldDelegateId() {
        controller.deleteDepartment(7L);

        verify(departmentService).deleteDepartment(7L);
        verifyNoMoreInteractions(departmentService);
    }
}
