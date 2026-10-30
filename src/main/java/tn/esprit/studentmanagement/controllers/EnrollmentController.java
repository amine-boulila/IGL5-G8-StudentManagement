package tn.esprit.studentmanagement.controllers;

import org.springframework.web.bind.annotation.*;
import tn.esprit.studentmanagement.entities.Enrollment;
import tn.esprit.studentmanagement.entities.Student;
import tn.esprit.studentmanagement.dto.EnrollmentRequest;
import tn.esprit.studentmanagement.services.ICourseService;
import tn.esprit.studentmanagement.services.IEnrollment;
import tn.esprit.studentmanagement.services.IStudentService;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/Enrollment")
@CrossOrigin(origins = "http://localhost:4200")
public class EnrollmentController {
    private final IEnrollment enrollmentService;
    private final IStudentService studentService;
    private final ICourseService courseService;

    public EnrollmentController(
            IEnrollment enrollmentService,
            IStudentService studentService,
            ICourseService courseService) {
        this.enrollmentService = enrollmentService;
        this.studentService = studentService;
        this.courseService = courseService;
    }

    @GetMapping("/getAllEnrollment")
    public List<Enrollment> getAllEnrollment() { return enrollmentService.getAllEnrollments(); }

    @GetMapping("/getEnrollment/{id}")
    public Enrollment getEnrollment(@PathVariable Long id) { return enrollmentService.getEnrollmentById(id); }

    @PostMapping("/createEnrollment")
    public Enrollment createEnrollment(@RequestBody EnrollmentRequest request) {
        return enrollmentService.saveEnrollment(toEnrollment(request));
    }

    @PutMapping("/updateEnrollment")
    public Enrollment updateEnrollment(@RequestBody EnrollmentRequest request) {
        return enrollmentService.saveEnrollment(toEnrollment(request));
    }

    @DeleteMapping("/deleteEnrollment/{id}")
    public void deleteEnrollment(@PathVariable Long id) {
        enrollmentService.deleteEnrollment(id);
    }

    private Enrollment toEnrollment(EnrollmentRequest request) {
        Enrollment enrollment = new Enrollment();
        enrollment.setIdEnrollment(request.getIdEnrollment());
        enrollment.setEnrollmentDate(request.getEnrollmentDate());
        enrollment.setGrade(request.getGrade());
        enrollment.setStatus(request.getStatus());

        if (request.getStudent() != null) {
            Long studentId = request.getStudent().getIdStudent();
            if (studentId == null) {
                throw new IllegalArgumentException("student.idStudent is required");
            }
            Student student = studentService.getStudentById(studentId);
            if (student == null) {
                throw new NoSuchElementException("Student not found: " + studentId);
            }
            enrollment.setStudent(student);
        }

        if (request.getCourse() != null) {
            Long courseId = request.getCourse().getIdCourse();
            if (courseId == null) {
                throw new IllegalArgumentException("course.idCourse is required");
            }
            enrollment.setCourse(courseService.getCourseById(courseId));
        }
        return enrollment;
    }
}
