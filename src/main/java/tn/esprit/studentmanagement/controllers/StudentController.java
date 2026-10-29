package tn.esprit.studentmanagement.controllers;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tn.esprit.studentmanagement.entities.Department;
import tn.esprit.studentmanagement.entities.Student;
import tn.esprit.studentmanagement.controllers.dto.StudentRequest;
import tn.esprit.studentmanagement.repositories.DepartmentRepository;
import tn.esprit.studentmanagement.services.IStudentService;

import java.util.List;

@RestController
@RequestMapping("/students")
@CrossOrigin(origins = "http://localhost:4200")
@AllArgsConstructor
public class StudentController {
    IStudentService studentService;
    DepartmentRepository departmentRepository;

    @GetMapping("/getAllStudents")
    public List<Student> getAllStudents() { return studentService.getAllStudents(); }

    @GetMapping("/getStudent/{id}")
    public Student getStudent(@PathVariable Long id) { return studentService.getStudentById(id); }

    @PostMapping("/createStudent")
    public Student createStudent(@RequestBody StudentRequest request) {
        return studentService.saveStudent(toStudent(request, false));
    }

    @PutMapping("/updateStudent")
    public Student updateStudent(@RequestBody StudentRequest request) {
        return studentService.saveStudent(toStudent(request, true));
    }

    @DeleteMapping("/deleteStudent/{id}")
    public void deleteStudent(@PathVariable Long id) { studentService.deleteStudent(id); }

    private Student toStudent(StudentRequest request, boolean includeId) {
        Student student = new Student();
        if (includeId) {
            student.setIdStudent(request.idStudent());
        }
        student.setFirstName(request.firstName());
        student.setLastName(request.lastName());
        student.setEmail(request.email());
        student.setPhone(request.phone());
        student.setDateOfBirth(request.dateOfBirth());
        student.setAddress(request.address());
        if (request.departmentId() != null) {
            Department department = departmentRepository.findById(request.departmentId()).orElseThrow();
            student.setDepartment(department);
        }
        return student;
    }
}
