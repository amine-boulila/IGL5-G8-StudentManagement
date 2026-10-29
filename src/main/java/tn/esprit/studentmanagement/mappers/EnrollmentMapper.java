package tn.esprit.studentmanagement.mappers;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.studentmanagement.dto.EnrollmentRequestDTO;
import tn.esprit.studentmanagement.dto.EnrollmentResponseDTO;
import tn.esprit.studentmanagement.entities.Course;
import tn.esprit.studentmanagement.entities.Enrollment;
import tn.esprit.studentmanagement.entities.Student;
import tn.esprit.studentmanagement.repositories.CourseRepository;
import tn.esprit.studentmanagement.repositories.StudentRepository;

@Component
@AllArgsConstructor
public class EnrollmentMapper {
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    public void applyToEntity(EnrollmentRequestDTO dto, Enrollment target) {
        target.setEnrollmentDate(dto.getEnrollmentDate());
        target.setGrade(dto.getGrade());
        target.setStatus(dto.getStatus());
        if (dto.getStudentId() == null) {
            target.setStudent(null);
        } else {
            Student student = studentRepository.findById(dto.getStudentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student not found"));
            target.setStudent(student);
        }
        if (dto.getCourseId() == null) {
            target.setCourse(null);
        } else {
            Course course = courseRepository.findById(dto.getCourseId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Course not found"));
            target.setCourse(course);
        }
    }

    public EnrollmentResponseDTO toResponse(Enrollment enrollment) {
        if (enrollment == null) return null;
        return new EnrollmentResponseDTO(enrollment.getIdEnrollment(), enrollment.getEnrollmentDate(),
                enrollment.getGrade(), enrollment.getStatus(),
                enrollment.getStudent() == null ? null : enrollment.getStudent().getIdStudent(),
                enrollment.getCourse() == null ? null : enrollment.getCourse().getIdCourse());
    }
}
