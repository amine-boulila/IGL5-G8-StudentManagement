package tn.esprit.studentmanagement.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tn.esprit.studentmanagement.dto.DepartmentDto;
import tn.esprit.studentmanagement.entities.Department;
import tn.esprit.studentmanagement.services.IDepartmentService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class DepartmentControllerTest {

    private IDepartmentService departmentService;
    private DepartmentController controller;

    @BeforeEach
    void setUp() {
        departmentService = mock(IDepartmentService.class);
        controller = new DepartmentController(departmentService);
    }

    @Test
    void getAllDepartmentShouldReturnDtos() {
        Department department = department(1L);
        when(departmentService.getAllDepartments()).thenReturn(List.of(department));

        List<DepartmentDto> result = controller.getAllDepartment();

        assertEquals(1, result.size());
        assertEquals("Computer Science", result.get(0).name());
    }

    @Test
    void getDepartmentShouldReturnDto() {
        when(departmentService.getDepartmentById(1L)).thenReturn(department(1L));

        DepartmentDto result = controller.getDepartment(1L);

        assertEquals(1L, result.idDepartment());
        verify(departmentService).getDepartmentById(1L);
    }

    @Test
    void createAndUpdateDepartmentShouldMapDto() {
        DepartmentDto dto = new DepartmentDto(null, "Computer Science", "Block A", "111", "Dr Smith");
        Department saved = department(2L);
        when(departmentService.saveDepartment(any(Department.class))).thenReturn(saved);

        assertEquals(2L, controller.createDepartment(dto).idDepartment());
        assertEquals(2L, controller.updateDepartment(dto).idDepartment());
        verify(departmentService, times(2)).saveDepartment(any(Department.class));
    }

    @Test
    void deleteDepartmentShouldDelegate() {
        controller.deleteDepartment(3L);

        verify(departmentService).deleteDepartment(3L);
    }

    private Department department(Long id) {
        Department department = new Department();
        department.setIdDepartment(id);
        department.setName("Computer Science");
        department.setLocation("Block A");
        department.setPhone("111");
        department.setHead("Dr Smith");
        return department;
    }
}
