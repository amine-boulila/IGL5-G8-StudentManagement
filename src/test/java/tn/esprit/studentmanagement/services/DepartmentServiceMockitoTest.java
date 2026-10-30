package tn.esprit.studentmanagement.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.studentmanagement.entities.Department;
import tn.esprit.studentmanagement.repositories.DepartmentRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceMockitoTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DepartmentService departmentService;

    @Test
    void saveDepartmentShouldReturnSavedDepartment() {
        Department department = createDepartment(null, "Computer Science", "Block A", "111222333", "Dr Smith");
        Department savedDepartment = createDepartment(1L, "Computer Science", "Block A", "111222333", "Dr Smith");
        when(departmentRepository.save(department)).thenReturn(savedDepartment);

        Department result = departmentService.saveDepartment(department);

        assertNotNull(result);
        assertNotNull(result.getIdDepartment());
        assertEquals(1L, result.getIdDepartment());
        assertEquals("Computer Science", result.getName());
        assertEquals("Block A", result.getLocation());
        verify(departmentRepository).save(department);
        verifyNoMoreInteractions(departmentRepository);
    }

    @Test
    void getDepartmentByIdShouldReturnExpectedDepartment() {
        Long departmentId = 1L;
        Department expectedDepartment = createDepartment(departmentId, "Mathematics", "Block B", "222333444", "Dr Brown");
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(expectedDepartment));

        Department result = departmentService.getDepartmentById(departmentId);

        assertNotNull(result);
        assertEquals(departmentId, result.getIdDepartment());
        assertEquals("Mathematics", result.getName());
        assertEquals("Block B", result.getLocation());
        assertEquals("222333444", result.getPhone());
        assertEquals("Dr Brown", result.getHead());
        verify(departmentRepository).findById(departmentId);
        verifyNoMoreInteractions(departmentRepository);
    }

    @Test
    void saveDepartmentShouldUpdateExistingDepartment() {
        Department department = createDepartment(1L, "Applied Physics", "Research Center", "444555666", "Dr Wilson");
        when(departmentRepository.save(department)).thenReturn(department);

        Department result = departmentService.saveDepartment(department);

        assertNotNull(result);
        assertEquals(1L, result.getIdDepartment());
        assertEquals("Applied Physics", result.getName());
        assertEquals("Research Center", result.getLocation());
        assertEquals("444555666", result.getPhone());
        assertEquals("Dr Wilson", result.getHead());
        verify(departmentRepository).save(department);
        verifyNoMoreInteractions(departmentRepository);
    }

    @Test
    void deleteDepartmentShouldRemoveDepartment() {
        Long departmentId = 1L;
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.empty());

        departmentService.deleteDepartment(departmentId);

        assertFalse(departmentRepository.findById(departmentId).isPresent());
        assertThrows(NoSuchElementException.class, () -> departmentService.getDepartmentById(departmentId));
        verify(departmentRepository).deleteById(departmentId);
        verify(departmentRepository, times(2)).findById(departmentId);
        verifyNoMoreInteractions(departmentRepository);
    }

    @Test
    void getAllDepartmentsShouldReturnRepositoryResults() {
        List<Department> departments = List.of(createDepartment(1L, "Computer Science", "Block A", "111222333", "Dr Smith"));
        when(departmentRepository.findAll()).thenReturn(departments);

        assertEquals(departments, departmentService.getAllDepartments());

        verify(departmentRepository).findAll();
        verifyNoMoreInteractions(departmentRepository);
    }

    @Test
    void getAllDepartmentsShouldRethrowRepositoryFailure() {
        IllegalStateException failure = new IllegalStateException("database unavailable");
        when(departmentRepository.findAll()).thenThrow(failure);

        assertEquals(failure, assertThrows(IllegalStateException.class, () -> departmentService.getAllDepartments()));

        verify(departmentRepository).findAll();
        verifyNoMoreInteractions(departmentRepository);
    }

    @Test
    void saveDepartmentShouldRethrowRepositoryFailure() {
        Department department = createDepartment(null, "Computer Science", "Block A", "111222333", "Dr Smith");
        IllegalStateException failure = new IllegalStateException("database unavailable");
        when(departmentRepository.save(department)).thenThrow(failure);

        assertEquals(failure, assertThrows(IllegalStateException.class, () -> departmentService.saveDepartment(department)));

        verify(departmentRepository).save(department);
        verifyNoMoreInteractions(departmentRepository);
    }

    @Test
    void deleteDepartmentShouldRethrowRepositoryFailure() {
        IllegalStateException failure = new IllegalStateException("database unavailable");
        doThrow(failure).when(departmentRepository).deleteById(7L);

        assertEquals(failure, assertThrows(IllegalStateException.class, () -> departmentService.deleteDepartment(7L)));

        verify(departmentRepository).deleteById(7L);
        verifyNoMoreInteractions(departmentRepository);
    }

    private Department createDepartment(Long idDepartment, String name, String location, String phone, String head) {
        Department department = new Department();
        department.setIdDepartment(idDepartment);
        department.setName(name);
        department.setLocation(location);
        department.setPhone(phone);
        department.setHead(head);
        return department;
    }
}
