package tn.esprit.studentmanagement.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tn.esprit.studentmanagement.entities.Department;
import tn.esprit.studentmanagement.repositories.DepartmentRepository;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class DepartmentServiceImplTest {

    @Autowired
    private IDepartmentService departmentService;

    @Autowired
    private DepartmentRepository departmentRepository;

    private final Set<Long> departmentIdsToCleanUp = new HashSet<>();

    @AfterEach
    void cleanUp() {
        departmentIdsToCleanUp.stream()
                .filter(Objects::nonNull)
                .filter(departmentRepository::existsById)
                .forEach(departmentRepository::deleteById);
        departmentIdsToCleanUp.clear();
    }

    @Test
    void saveDepartmentShouldPersistDepartmentWithGeneratedId() {
        Department department = createDepartment("Computer Science", "Block A", "111222333", "Dr Smith");

        Department savedDepartment = departmentService.saveDepartment(department);
        departmentIdsToCleanUp.add(savedDepartment.getIdDepartment());

        assertNotNull(savedDepartment);
        assertNotNull(savedDepartment.getIdDepartment());
        assertTrue(departmentRepository.existsById(savedDepartment.getIdDepartment()));
        assertEquals("Computer Science", savedDepartment.getName());
        assertEquals("Block A", savedDepartment.getLocation());
        assertEquals("111222333", savedDepartment.getPhone());
        assertEquals("Dr Smith", savedDepartment.getHead());
    }

    @Test
    void getDepartmentByIdShouldReturnExpectedDepartment() {
        Department savedDepartment = saveDepartmentForTest("Mathematics", "Block B", "222333444", "Dr Brown");

        Department foundDepartment = departmentService.getDepartmentById(savedDepartment.getIdDepartment());

        assertNotNull(foundDepartment);
        assertEquals(savedDepartment.getIdDepartment(), foundDepartment.getIdDepartment());
        assertEquals("Mathematics", foundDepartment.getName());
        assertEquals("Block B", foundDepartment.getLocation());
        assertEquals("222333444", foundDepartment.getPhone());
        assertEquals("Dr Brown", foundDepartment.getHead());
    }

    @Test
    void saveDepartmentShouldUpdateExistingDepartment() {
        Department savedDepartment = saveDepartmentForTest("Physics", "Block C", "333444555", "Dr Taylor");

        savedDepartment.setName("Applied Physics");
        savedDepartment.setLocation("Research Center");
        savedDepartment.setPhone("444555666");
        savedDepartment.setHead("Dr Wilson");
        Department updatedDepartment = departmentService.saveDepartment(savedDepartment);

        assertNotNull(updatedDepartment);
        assertEquals(savedDepartment.getIdDepartment(), updatedDepartment.getIdDepartment());
        assertEquals("Applied Physics", updatedDepartment.getName());
        assertEquals("Research Center", updatedDepartment.getLocation());
        assertEquals("444555666", updatedDepartment.getPhone());
        assertEquals("Dr Wilson", updatedDepartment.getHead());

        Department persistedDepartment = departmentService.getDepartmentById(savedDepartment.getIdDepartment());
        assertEquals("Applied Physics", persistedDepartment.getName());
        assertEquals("Research Center", persistedDepartment.getLocation());
        assertTrue(departmentRepository.existsById(savedDepartment.getIdDepartment()));
    }

    @Test
    void deleteDepartmentShouldRemoveDepartment() {
        Department savedDepartment = saveDepartmentForTest("Chemistry", "Block D", "555666777", "Dr Davis");
        Long departmentId = savedDepartment.getIdDepartment();

        departmentService.deleteDepartment(departmentId);
        departmentIdsToCleanUp.remove(departmentId);

        assertFalse(departmentRepository.existsById(departmentId));
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> departmentService.getDepartmentById(departmentId));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    private Department saveDepartmentForTest(String name, String location, String phone, String head) {
        Department savedDepartment = departmentService.saveDepartment(createDepartment(name, location, phone, head));
        departmentIdsToCleanUp.add(savedDepartment.getIdDepartment());
        return savedDepartment;
    }

    private Department createDepartment(String name, String location, String phone, String head) {
        Department department = new Department();
        department.setName(name);
        department.setLocation(location);
        department.setPhone(phone);
        department.setHead(head);
        return department;
    }
}
