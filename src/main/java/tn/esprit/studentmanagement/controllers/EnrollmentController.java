package tn.esprit.studentmanagement.controllers;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.studentmanagement.dto.EnrollmentDto;
import tn.esprit.studentmanagement.entities.Enrollment;
import tn.esprit.studentmanagement.repositories.CourseRepository;
import tn.esprit.studentmanagement.repositories.StudentRepository;
import tn.esprit.studentmanagement.services.IEnrollment;

import java.util.List;

@RestController
@RequestMapping("/Enrollment")
@CrossOrigin(origins = "http://localhost:4200")
@AllArgsConstructor
public class EnrollmentController {
    private final IEnrollment enrollmentService;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    @GetMapping("/getAllEnrollment")
    public List<EnrollmentDto> getAllEnrollment() {
        return enrollmentService.getAllEnrollments().stream()
                .map(this::toDto)
                .toList();
    }

    @GetMapping("/getEnrollment/{id}")
    public EnrollmentDto getEnrollment(@PathVariable Long id) {
        return toDto(enrollmentService.getEnrollmentById(id));
    }

    @PostMapping("/createEnrollment")
    public EnrollmentDto createEnrollment(@RequestBody EnrollmentDto enrollment) {
        return toDto(enrollmentService.saveEnrollment(toEntity(enrollment)));
    }

    @PutMapping("/updateEnrollment")
    public EnrollmentDto updateEnrollment(@RequestBody EnrollmentDto enrollment) {
        return toDto(enrollmentService.saveEnrollment(toEntity(enrollment)));
    }

    @DeleteMapping("/deleteEnrollment/{id}")
    public void deleteEnrollment(@PathVariable Long id) {
        enrollmentService.deleteEnrollment(id); }

    private EnrollmentDto toDto(Enrollment enrollment) {
        return new EnrollmentDto(
                enrollment.getIdEnrollment(),
                enrollment.getEnrollmentDate(),
                enrollment.getGrade(),
                enrollment.getStatus(),
                enrollment.getStudent() == null ? null : enrollment.getStudent().getIdStudent(),
                enrollment.getCourse() == null ? null : enrollment.getCourse().getIdCourse());
    }

    private Enrollment toEntity(EnrollmentDto enrollment) {
        Enrollment entity = new Enrollment();
        entity.setIdEnrollment(enrollment.idEnrollment());
        entity.setEnrollmentDate(enrollment.enrollmentDate());
        entity.setGrade(enrollment.grade());
        entity.setStatus(enrollment.status());
        if (enrollment.studentId() != null) {
            entity.setStudent(studentRepository.getReferenceById(enrollment.studentId()));
        }
        if (enrollment.courseId() != null) {
            entity.setCourse(courseRepository.getReferenceById(enrollment.courseId()));
        }
        return entity;
    }
}
