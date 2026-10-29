package tn.esprit.studentmanagement.controllers;

import org.junit.jupiter.api.Test;
import tn.esprit.studentmanagement.entities.Student;
import tn.esprit.studentmanagement.services.IStudentService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class StudentControllerTest {

    private final IStudentService studentService = mock(IStudentService.class);
    private final StudentController controller = new StudentController(studentService);

    @Test
    void getAllStudentsShouldReturnServiceResults() {
        List<Student> students = List.of(new Student());
        when(studentService.getAllStudents()).thenReturn(students);

        assertEquals(students, controller.getAllStudents());

        verify(studentService).getAllStudents();
        verifyNoMoreInteractions(studentService);
    }

    @Test
    void getStudentShouldReturnServiceResult() {
        Student student = new Student();
        when(studentService.getStudentById(7L)).thenReturn(student);

        assertEquals(student, controller.getStudent(7L));

        verify(studentService).getStudentById(7L);
        verifyNoMoreInteractions(studentService);
    }

    @Test
    void createStudentShouldSaveRequestBody() {
        Student student = new Student();
        when(studentService.saveStudent(student)).thenReturn(student);

        assertEquals(student, controller.createStudent(student));

        verify(studentService).saveStudent(student);
        verifyNoMoreInteractions(studentService);
    }

    @Test
    void updateStudentShouldSaveRequestBody() {
        Student student = new Student();
        when(studentService.saveStudent(student)).thenReturn(student);

        assertEquals(student, controller.updateStudent(student));

        verify(studentService).saveStudent(student);
        verifyNoMoreInteractions(studentService);
    }

    @Test
    void deleteStudentShouldDelegateId() {
        controller.deleteStudent(7L);

        verify(studentService).deleteStudent(7L);
        verifyNoMoreInteractions(studentService);
    }
}
