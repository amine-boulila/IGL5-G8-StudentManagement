package tn.esprit.studentmanagement.controllers;

import org.junit.jupiter.api.Test;
import tn.esprit.studentmanagement.controllers.dto.StudentRequest;
import tn.esprit.studentmanagement.entities.Student;
import tn.esprit.studentmanagement.repositories.DepartmentRepository;
import tn.esprit.studentmanagement.services.IStudentService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class StudentControllerTest {

    private final IStudentService studentService = mock(IStudentService.class);
    private final DepartmentRepository departmentRepository = mock(DepartmentRepository.class);
    private final StudentController controller = new StudentController(studentService, departmentRepository);

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
        StudentRequest request = new StudentRequest(null, "Amina", "Ben Ali", "amina@example.test",
                "123", null, "Tunis", null);
        when(studentService.saveStudent(any(Student.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Student result = controller.createStudent(request);

        assertEquals("Amina", result.getFirstName());
        assertEquals("Ben Ali", result.getLastName());
        verify(studentService).saveStudent(any(Student.class));
        verifyNoMoreInteractions(studentService);
        verifyNoMoreInteractions(departmentRepository);
    }

    @Test
    void updateStudentShouldSaveRequestBody() {
        StudentRequest request = new StudentRequest(7L, "Amina", "Mansour", "amina@example.test",
                "123", null, "Tunis", null);
        when(studentService.saveStudent(any(Student.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Student result = controller.updateStudent(request);

        assertEquals(7L, result.getIdStudent());
        assertEquals("Mansour", result.getLastName());
        verify(studentService).saveStudent(any(Student.class));
        verifyNoMoreInteractions(studentService);
        verifyNoMoreInteractions(departmentRepository);
    }

    @Test
    void deleteStudentShouldDelegateId() {
        controller.deleteStudent(7L);

        verify(studentService).deleteStudent(7L);
        verifyNoMoreInteractions(studentService);
    }
}
