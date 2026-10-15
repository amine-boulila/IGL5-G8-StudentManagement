package tn.esprit.studentmanagement.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.studentmanagement.entities.Student;
import tn.esprit.studentmanagement.repositories.StudentRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceMockitoTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentService studentService;

    @Test
    void getAllStudentsShouldReturnRepositoryResults() {
        List<Student> students = List.of(createStudent(1L, "Amina", "Ben Ali"));
        when(studentRepository.findAll()).thenReturn(students);

        List<Student> result = studentService.getAllStudents();

        assertEquals(students, result);
        verify(studentRepository).findAll();
        verifyNoMoreInteractions(studentRepository);
    }

    @Test
    void getStudentByIdShouldReturnStudentWhenPresent() {
        Student student = createStudent(1L, "Amina", "Ben Ali");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        Student result = studentService.getStudentById(1L);

        assertEquals(student, result);
        verify(studentRepository).findById(1L);
        verifyNoMoreInteractions(studentRepository);
    }

    @Test
    void getStudentByIdShouldReturnNullWhenMissing() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(studentService.getStudentById(99L));

        verify(studentRepository).findById(99L);
        verifyNoMoreInteractions(studentRepository);
    }

    @Test
    void saveStudentShouldReturnSavedStudent() {
        Student student = createStudent(null, "Amina", "Ben Ali");
        Student savedStudent = createStudent(1L, "Amina", "Ben Ali");
        when(studentRepository.save(student)).thenReturn(savedStudent);

        Student result = studentService.saveStudent(student);

        assertEquals(1L, result.getIdStudent());
        verify(studentRepository).save(student);
        verifyNoMoreInteractions(studentRepository);
    }

    @Test
    void deleteStudentShouldDelegateToRepository() {
        studentService.deleteStudent(1L);

        verify(studentRepository).deleteById(1L);
        verifyNoMoreInteractions(studentRepository);
    }

    @Test
    void saveStudentShouldRethrowRepositoryFailure() {
        Student student = createStudent(null, "Amina", "Ben Ali");
        IllegalStateException failure = new IllegalStateException("database unavailable");
        when(studentRepository.save(student)).thenThrow(failure);

        IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                () -> studentService.saveStudent(student));

        assertEquals(failure, thrown);
        verify(studentRepository).save(student);
        verifyNoMoreInteractions(studentRepository);
    }

    private Student createStudent(Long id, String firstName, String lastName) {
        Student student = new Student();
        student.setIdStudent(id);
        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setEmail(firstName.toLowerCase() + "@example.test");
        return student;
    }
}
