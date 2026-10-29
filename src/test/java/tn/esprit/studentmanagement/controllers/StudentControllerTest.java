package tn.esprit.studentmanagement.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tn.esprit.studentmanagement.dto.StudentDto;
import tn.esprit.studentmanagement.entities.Department;
import tn.esprit.studentmanagement.entities.Student;
import tn.esprit.studentmanagement.repositories.DepartmentRepository;
import tn.esprit.studentmanagement.services.IStudentService;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StudentControllerTest {

    private IStudentService studentService;
    private DepartmentRepository departmentRepository;
    private StudentController controller;

    @BeforeEach
    void setUp() {
        studentService = mock(IStudentService.class);
        departmentRepository = mock(DepartmentRepository.class);
        controller = new StudentController(studentService, departmentRepository);
    }

    @Test
    void getAllStudentsShouldReturnDtos() {
        when(studentService.getAllStudents()).thenReturn(List.of(student(1L, null)));

        List<StudentDto> result = controller.getAllStudents();

        assertEquals(1, result.size());
        assertEquals("Amina", result.get(0).firstName());
        assertNull(result.get(0).departmentId());
    }

    @Test
    void getStudentShouldMapPresentStudentAndReturnNullWhenMissing() {
        Student present = student(1L, department(7L));
        when(studentService.getStudentById(1L)).thenReturn(present);
        when(studentService.getStudentById(2L)).thenReturn(null);

        assertEquals(7L, controller.getStudent(1L).departmentId());
        assertNull(controller.getStudent(2L));
    }

    @Test
    void createAndUpdateStudentShouldMapDepartmentReference() {
        Department department = department(7L);
        StudentDto dto = new StudentDto(null, "Amina", "Ben Ali", "amina@example.test",
                "111", LocalDate.of(2000, 1, 1), "Tunis", 7L);
        when(departmentRepository.getReferenceById(7L)).thenReturn(department);
        when(studentService.saveStudent(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals(7L, controller.createStudent(dto).departmentId());
        assertEquals("Tunis", controller.updateStudent(dto).address());
        verify(departmentRepository, times(2)).getReferenceById(7L);
    }

    @Test
    void deleteStudentShouldDelegate() {
        controller.deleteStudent(1L);

        verify(studentService).deleteStudent(1L);
    }

    private Student student(Long id, Department department) {
        Student student = new Student();
        student.setIdStudent(id);
        student.setFirstName("Amina");
        student.setLastName("Ben Ali");
        student.setEmail("amina@example.test");
        student.setPhone("111");
        student.setDateOfBirth(LocalDate.of(2000, 1, 1));
        student.setAddress("Tunis");
        student.setDepartment(department);
        return student;
    }

    private Department department(Long id) {
        Department department = new Department();
        department.setIdDepartment(id);
        return department;
    }
}
