package tn.esprit.studentmanagement.dto;

import tn.esprit.studentmanagement.entities.Course;
import tn.esprit.studentmanagement.entities.Enrollment;
import tn.esprit.studentmanagement.entities.Status;
import tn.esprit.studentmanagement.entities.Student;

import java.time.LocalDate;

/**
 * Objet recu par l'API pour creer / modifier une inscription (regle Sonar java:S4684).
 * L'etudiant et le cours sont references par leur id uniquement.
 */
public record EnrollmentDTO(Long idEnrollment, LocalDate enrollmentDate, Double grade, Status status,
                            Long studentId, Long courseId) {

    public Enrollment toEntity() {
        Enrollment enrollment = new Enrollment();
        enrollment.setIdEnrollment(idEnrollment);
        enrollment.setEnrollmentDate(enrollmentDate);
        enrollment.setGrade(grade);
        enrollment.setStatus(status);
        if (studentId != null) {
            Student student = new Student();
            student.setIdStudent(studentId);
            enrollment.setStudent(student);
        }
        if (courseId != null) {
            Course course = new Course();
            course.setIdCourse(courseId);
            enrollment.setCourse(course);
        }
        return enrollment;
    }
}
