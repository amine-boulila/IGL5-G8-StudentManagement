package tn.esprit.studentmanagement.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tn.esprit.studentmanagement.entities.Department;
import tn.esprit.studentmanagement.dto.StudentRequest;
import tn.esprit.studentmanagement.entities.Student;
import tn.esprit.studentmanagement.services.IDepartmentService;
import tn.esprit.studentmanagement.services.IStudentService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

class StudentControllerTest {

    private IStudentService studentService;
    private IDepartmentService departmentService;
    private StudentController controller;

    @BeforeEach
    void setUp() {
        studentService = mock(IStudentService.class);
        departmentService = mock(IDepartmentService.class);
        controller = new StudentController(studentService, departmentService);
    }

    @Test
    void getAllStudentsDelegatesToService() {
        List<Student> students = List.of(new Student());
        when(studentService.getAllStudents()).thenReturn(students);

        assertSame(students, controller.getAllStudents());
        verify(studentService).getAllStudents();
    }

    @Test
    void getStudentDelegatesToService() {
        Student student = new Student();
        when(studentService.getStudentById(7L)).thenReturn(student);

        assertSame(student, controller.getStudent(7L));
        verify(studentService).getStudentById(7L);
    }

    @Test
    void createStudentDelegatesToService() {
        StudentRequest request = new StudentRequest();
        request.setFirstName("Amina");
        request.setLastName("Ben Ali");
        StudentRequest.DepartmentReference departmentReference = new StudentRequest.DepartmentReference();
        departmentReference.setIdDepartment(2L);
        request.setDepartment(departmentReference);
        Department department = new Department();
        when(departmentService.getDepartmentById(2L)).thenReturn(department);
        Student savedStudent = new Student();
        savedStudent.setFirstName("Amina");
        savedStudent.setLastName("Ben Ali");
        when(studentService.saveStudent(any(Student.class))).thenReturn(savedStudent);

        assertSame(savedStudent, controller.createStudent(request));
        ArgumentCaptor<Student> captor = ArgumentCaptor.forClass(Student.class);
        verify(studentService).saveStudent(captor.capture());
        assertEquals("Amina", captor.getValue().getFirstName());
        assertSame(department, captor.getValue().getDepartment());
        verify(departmentService).getDepartmentById(2L);
    }

    @Test
    void updateStudentDelegatesToService() {
        StudentRequest request = new StudentRequest();
        request.setIdStudent(8L);
        request.setEmail("amina@example.test");
        Student savedStudent = new Student();
        savedStudent.setIdStudent(8L);
        savedStudent.setEmail("amina@example.test");
        when(studentService.saveStudent(any(Student.class))).thenReturn(savedStudent);

        assertSame(savedStudent, controller.updateStudent(request));
        verify(studentService).saveStudent(any(Student.class));
    }

    @Test
    void deleteStudentDelegatesToService() {
        controller.deleteStudent(7L);

        verify(studentService).deleteStudent(7L);
    }
}
