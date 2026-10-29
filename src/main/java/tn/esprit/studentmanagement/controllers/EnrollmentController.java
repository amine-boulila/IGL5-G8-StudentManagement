package tn.esprit.studentmanagement.controllers;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.studentmanagement.entities.Course;
import tn.esprit.studentmanagement.entities.Enrollment;
import tn.esprit.studentmanagement.entities.Student;
import tn.esprit.studentmanagement.controllers.dto.EnrollmentRequest;
import tn.esprit.studentmanagement.repositories.CourseRepository;
import tn.esprit.studentmanagement.repositories.StudentRepository;
import tn.esprit.studentmanagement.services.IEnrollment;

import java.util.List;

@RestController
@RequestMapping("/Enrollment")
@CrossOrigin(origins = "http://localhost:4200")
@AllArgsConstructor
public class EnrollmentController {
    IEnrollment enrollmentService;
    StudentRepository studentRepository;
    CourseRepository courseRepository;
    @GetMapping("/getAllEnrollment")
    public List<Enrollment> getAllEnrollment() { return enrollmentService.getAllEnrollments(); }

    @GetMapping("/getEnrollment/{id}")
    public Enrollment getEnrollment(@PathVariable Long id) { return enrollmentService.getEnrollmentById(id); }

    @PostMapping("/createEnrollment")
    public Enrollment createEnrollment(@RequestBody EnrollmentRequest request) {
        return enrollmentService.saveEnrollment(toEnrollment(request, false));
    }

    @PutMapping("/updateEnrollment")
    public Enrollment updateEnrollment(@RequestBody EnrollmentRequest request) {
        return enrollmentService.saveEnrollment(toEnrollment(request, true));
    }

    @DeleteMapping("/deleteEnrollment/{id}")
    public void deleteEnrollment(@PathVariable Long id) {
        enrollmentService.deleteEnrollment(id); }

    private Enrollment toEnrollment(EnrollmentRequest request, boolean includeId) {
        Enrollment enrollment = new Enrollment();
        if (includeId) {
            enrollment.setIdEnrollment(request.idEnrollment());
        }
        enrollment.setEnrollmentDate(request.enrollmentDate());
        enrollment.setGrade(request.grade());
        enrollment.setStatus(request.status());
        if (request.studentId() != null) {
            Student student = studentRepository.findById(request.studentId()).orElseThrow();
            enrollment.setStudent(student);
        }
        if (request.courseId() != null) {
            Course course = courseRepository.findById(request.courseId()).orElseThrow();
            enrollment.setCourse(course);
        }
        return enrollment;
    }
}
