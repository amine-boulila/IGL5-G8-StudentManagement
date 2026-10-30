package tn.esprit.studentmanagement.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tn.esprit.studentmanagement.entities.Student;
import tn.esprit.studentmanagement.services.IStudentService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StudentControllerTest {

    private IStudentService studentService;
    private StudentController controller;

    @BeforeEach
    void setUp() {
        studentService = mock(IStudentService.class);
        controller = new StudentController(studentService);
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
        Student student = new Student();
        when(studentService.saveStudent(student)).thenReturn(student);

        assertSame(student, controller.createStudent(student));
        verify(studentService).saveStudent(student);
    }

    @Test
    void updateStudentDelegatesToService() {
        Student student = new Student();
        when(studentService.saveStudent(student)).thenReturn(student);

        assertSame(student, controller.updateStudent(student));
        verify(studentService).saveStudent(student);
    }

    @Test
    void deleteStudentDelegatesToService() {
        controller.deleteStudent(7L);

        verify(studentService).deleteStudent(7L);
    }
}
