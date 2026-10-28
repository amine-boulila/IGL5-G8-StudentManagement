package tn.esprit.studentmanagement.mappers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.studentmanagement.dto.StudentRequestDTO;
import tn.esprit.studentmanagement.entities.Department;
import tn.esprit.studentmanagement.entities.Student;
import tn.esprit.studentmanagement.repositories.DepartmentRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentMapperTest {
    @Mock DepartmentRepository departments;
    @InjectMocks StudentMapper mapper;

    @Test
    void appliesFieldsAndDepartment() {
        Department department = new Department();
        department.setIdDepartment(4L);
        when(departments.findById(4L)).thenReturn(Optional.of(department));
        StudentRequestDTO dto = new StudentRequestDTO("A", "B", "a@b.test", "123", LocalDate.of(2000, 1, 2), "Here", 4L);
        Student student = new Student();

        mapper.applyToEntity(dto, student);

        assertEquals("A", student.getFirstName());
        assertEquals("B", student.getLastName());
        assertEquals("a@b.test", student.getEmail());
        assertEquals("123", student.getPhone());
        assertEquals(LocalDate.of(2000, 1, 2), student.getDateOfBirth());
        assertEquals("Here", student.getAddress());
        assertSame(department, student.getDepartment());
        var response = mapper.toResponse(student);
        assertEquals(4L, response.getDepartmentId());
    }

    @Test
    void rejectsUnknownDepartment() {
        when(departments.findById(4L)).thenReturn(Optional.empty());
        var exception = assertThrows(ResponseStatusException.class,
                () -> mapper.applyToEntity(new StudentRequestDTO("A", "B", "a@b.test", null, null, null, 4L), new Student()));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void mapsNullDepartmentAndNullStudent() {
        Student student = new Student();
        mapper.applyToEntity(new StudentRequestDTO("A", "B", "a@b.test", null, null, null, null), student);
        assertNull(student.getDepartment());
        assertNull(mapper.toResponse(student).getDepartmentId());
        assertNull(mapper.toResponse(null));
    }
}
