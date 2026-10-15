package tn.esprit.studentmanagement.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.studentmanagement.entities.Student;
import tn.esprit.studentmanagement.repositories.StudentRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class StudentServiceNoMockTest {

    @Autowired
    private IStudentService studentService;

    @Autowired
    private StudentRepository studentRepository;

    @Test
    void saveStudentShouldPersistAndGenerateId() {
        Student savedStudent = studentService.saveStudent(createStudent("Amina", "Ben Ali"));

        assertNotNull(savedStudent.getIdStudent());
        assertEquals("Amina", savedStudent.getFirstName());
        assertEquals("Ben Ali", savedStudent.getLastName());
        Student persistedStudent = studentRepository.findById(savedStudent.getIdStudent()).orElseThrow();
        assertEquals(savedStudent.getIdStudent(), persistedStudent.getIdStudent());
        assertEquals("Amina", persistedStudent.getFirstName());
    }

    @Test
    void getAllStudentsShouldReturnPersistedStudents() {
        Student first = studentService.saveStudent(createStudent("Amina", "Ben Ali"));
        Student second = studentService.saveStudent(createStudent("Youssef", "Trabelsi"));

        List<Student> result = studentService.getAllStudents();

        assertEquals(2, result.size());
        List<Long> resultIds = result.stream().map(Student::getIdStudent).toList();
        assertTrue(resultIds.containsAll(List.of(first.getIdStudent(), second.getIdStudent())));
    }

    @Test
    void getStudentByIdShouldReturnNullForUnknownId() {
        assertNull(studentService.getStudentById(Long.MAX_VALUE));
    }

    @Test
    void saveStudentShouldUpdateExistingStudent() {
        Student student = studentService.saveStudent(createStudent("Amina", "Ben Ali"));
        student.setLastName("Mansour");

        Student updatedStudent = studentService.saveStudent(student);

        assertEquals(student.getIdStudent(), updatedStudent.getIdStudent());
        assertEquals("Mansour", studentRepository.findById(student.getIdStudent()).orElseThrow().getLastName());
    }

    @Test
    void deleteStudentShouldRemovePersistedStudent() {
        Student student = studentService.saveStudent(createStudent("Amina", "Ben Ali"));

        studentService.deleteStudent(student.getIdStudent());

        assertFalse(studentRepository.existsById(student.getIdStudent()));
    }

    private Student createStudent(String firstName, String lastName) {
        Student student = new Student();
        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setEmail(firstName.toLowerCase() + "." + lastName.toLowerCase().replace(" ", "") + "@example.test");
        return student;
    }
}
