package tn.esprit.studentmanagement.dto;

import org.junit.jupiter.api.Test;
import tn.esprit.studentmanagement.entities.Department;
import tn.esprit.studentmanagement.entities.Enrollment;
import tn.esprit.studentmanagement.entities.Status;
import tn.esprit.studentmanagement.entities.Student;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Verifie la conversion DTO -> entite JPA (methode toEntity()).
 */
class DtoMappingTest {

    @Test
    void departmentDtoShouldMapAllFields() {
        DepartmentDTO dto = new DepartmentDTO(1L, "Computer Science", "Block A", "111222333", "Dr Smith");

        Department department = dto.toEntity();

        assertEquals(1L, department.getIdDepartment());
        assertEquals("Computer Science", department.getName());
        assertEquals("Block A", department.getLocation());
        assertEquals("111222333", department.getPhone());
        assertEquals("Dr Smith", department.getHead());
    }

    @Test
    void studentDtoShouldMapFieldsAndDepartmentReference() {
        LocalDate birthDate = LocalDate.of(2003, 5, 10);
        StudentDTO dto = new StudentDTO(2L, "Amina", "Ben Ali", "amina@example.test", "22111333",
                birthDate, "Tunis", 7L);

        Student student = dto.toEntity();

        assertEquals(2L, student.getIdStudent());
        assertEquals("Amina", student.getFirstName());
        assertEquals("Ben Ali", student.getLastName());
        assertEquals("amina@example.test", student.getEmail());
        assertEquals("22111333", student.getPhone());
        assertEquals(birthDate, student.getDateOfBirth());
        assertEquals("Tunis", student.getAddress());
        assertEquals(7L, student.getDepartment().getIdDepartment());
    }

    @Test
    void studentDtoWithoutDepartmentShouldLeaveDepartmentNull() {
        StudentDTO dto = new StudentDTO(null, "Youssef", "Trabelsi", null, null, null, null, null);

        Student student = dto.toEntity();

        assertNull(student.getIdStudent());
        assertNull(student.getDepartment());
    }

    @Test
    void enrollmentDtoShouldMapFieldsAndReferences() {
        LocalDate date = LocalDate.of(2026, 9, 1);
        EnrollmentDTO dto = new EnrollmentDTO(3L, date, 15.5, Status.ACTIVE, 4L, 5L);

        Enrollment enrollment = dto.toEntity();

        assertEquals(3L, enrollment.getIdEnrollment());
        assertEquals(date, enrollment.getEnrollmentDate());
        assertEquals(15.5, enrollment.getGrade());
        assertEquals(Status.ACTIVE, enrollment.getStatus());
        assertEquals(4L, enrollment.getStudent().getIdStudent());
        assertEquals(5L, enrollment.getCourse().getIdCourse());
    }

    @Test
    void enrollmentDtoWithoutReferencesShouldLeaveRelationsNull() {
        EnrollmentDTO dto = new EnrollmentDTO(null, null, null, Status.DROPPED, null, null);

        Enrollment enrollment = dto.toEntity();

        assertNull(enrollment.getStudent());
        assertNull(enrollment.getCourse());
        assertEquals(Status.DROPPED, enrollment.getStatus());
    }
}
