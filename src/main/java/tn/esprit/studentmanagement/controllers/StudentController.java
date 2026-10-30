package tn.esprit.studentmanagement.controllers;

import org.springframework.web.bind.annotation.*;
import tn.esprit.studentmanagement.entities.Student;
import tn.esprit.studentmanagement.dto.StudentRequest;
import tn.esprit.studentmanagement.services.IDepartmentService;
import tn.esprit.studentmanagement.services.IStudentService;

import java.util.List;

@RestController
@RequestMapping("/students")
@CrossOrigin(origins = "http://localhost:4200")
public class StudentController {
    private final IStudentService studentService;
    private final IDepartmentService departmentService;

    public StudentController(IStudentService studentService, IDepartmentService departmentService) {
        this.studentService = studentService;
        this.departmentService = departmentService;
    }

    @GetMapping("/getAllStudents")
    public List<Student> getAllStudents() { return studentService.getAllStudents(); }

    @GetMapping("/getStudent/{id}")
    public Student getStudent(@PathVariable Long id) { return studentService.getStudentById(id); }

    @PostMapping("/createStudent")
    public Student createStudent(@RequestBody StudentRequest request) {
        return studentService.saveStudent(toStudent(request));
    }

    @PutMapping("/updateStudent")
    public Student updateStudent(@RequestBody StudentRequest request) {
        return studentService.saveStudent(toStudent(request));
    }

    @DeleteMapping("/deleteStudent/{id}")
    public void deleteStudent(@PathVariable Long id) { studentService.deleteStudent(id); }

    private Student toStudent(StudentRequest request) {
        Student student = new Student();
        student.setIdStudent(request.getIdStudent());
        student.setFirstName(request.getFirstName());
        student.setLastName(request.getLastName());
        student.setEmail(request.getEmail());
        student.setPhone(request.getPhone());
        student.setDateOfBirth(request.getDateOfBirth());
        student.setAddress(request.getAddress());

        if (request.getDepartment() != null) {
            Long departmentId = request.getDepartment().getIdDepartment();
            if (departmentId == null) {
                throw new IllegalArgumentException("department.idDepartment is required");
            }
            student.setDepartment(departmentService.getDepartmentById(departmentId));
        }
        return student;
    }
}
