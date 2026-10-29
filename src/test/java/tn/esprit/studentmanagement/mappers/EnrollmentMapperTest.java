package tn.esprit.studentmanagement.mappers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.studentmanagement.dto.EnrollmentRequestDTO;
import tn.esprit.studentmanagement.entities.Course;
import tn.esprit.studentmanagement.entities.Enrollment;
import tn.esprit.studentmanagement.entities.Status;
import tn.esprit.studentmanagement.entities.Student;
import tn.esprit.studentmanagement.repositories.CourseRepository;
import tn.esprit.studentmanagement.repositories.StudentRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnrollmentMapperTest {
    @Mock StudentRepository students;
    @Mock CourseRepository courses;
    @InjectMocks EnrollmentMapper mapper;

    @Test
    void appliesFieldsAndRelations() {
        Student student = new Student(); student.setIdStudent(1L);
        Course course = new Course(); course.setIdCourse(2L);
        when(students.findById(1L)).thenReturn(Optional.of(student));
        when(courses.findById(2L)).thenReturn(Optional.of(course));
        var date = LocalDate.of(2026, 1, 2);
        Enrollment enrollment = new Enrollment();

        mapper.applyToEntity(new EnrollmentRequestDTO(date, 16.0, Status.ACTIVE, 1L, 2L), enrollment);

        assertEquals(date, enrollment.getEnrollmentDate());
        assertEquals(16.0, enrollment.getGrade());
        assertEquals(Status.ACTIVE, enrollment.getStatus());
        assertSame(student, enrollment.getStudent());
        assertSame(course, enrollment.getCourse());
        var response = mapper.toResponse(enrollment);
        assertEquals(1L, response.getStudentId());
        assertEquals(2L, response.getCourseId());
    }

    @Test
    void rejectsUnknownStudent() {
        when(students.findById(1L)).thenReturn(Optional.empty());
        var exception = assertThrows(ResponseStatusException.class,
                () -> mapper.applyToEntity(new EnrollmentRequestDTO(null, null, null, 1L, 2L), new Enrollment()));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void mapsNullRelations() {
        Enrollment enrollment = new Enrollment();
        mapper.applyToEntity(new EnrollmentRequestDTO(null, null, null, null, null), enrollment);
        var response = mapper.toResponse(enrollment);
        assertNull(response.getStudentId());
        assertNull(response.getCourseId());
        assertNull(mapper.toResponse(null));
    }
}
