package tn.esprit.studentmanagement.controllers;

import org.junit.jupiter.api.Test;
import tn.esprit.studentmanagement.controllers.dto.DepartmentRequest;
import tn.esprit.studentmanagement.entities.Department;
import tn.esprit.studentmanagement.services.IDepartmentService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.any;
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
        DepartmentRequest request = new DepartmentRequest(null, "Computer Science", "Block A", "123", "Dr Smith");
        when(departmentService.saveDepartment(any(Department.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Department result = controller.createDepartment(request);

        assertEquals("Computer Science", result.getName());
        assertEquals("Block A", result.getLocation());
        verify(departmentService).saveDepartment(any(Department.class));
        verifyNoMoreInteractions(departmentService);
    }

    @Test
    void updateDepartmentShouldSaveRequestBody() {
        DepartmentRequest request = new DepartmentRequest(7L, "Mathematics", "Block B", "456", "Dr Brown");
        when(departmentService.saveDepartment(any(Department.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Department result = controller.updateDepartment(request);

        assertEquals(7L, result.getIdDepartment());
        assertEquals("Mathematics", result.getName());
        verify(departmentService).saveDepartment(any(Department.class));
        verifyNoMoreInteractions(departmentService);
    }

    @Test
    void deleteDepartmentShouldDelegateId() {
        controller.deleteDepartment(7L);

        verify(departmentService).deleteDepartment(7L);
        verifyNoMoreInteractions(departmentService);
    }
}
