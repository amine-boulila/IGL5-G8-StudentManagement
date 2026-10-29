package tn.esprit.studentmanagement.controllers;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.studentmanagement.dto.StudentDto;
import tn.esprit.studentmanagement.entities.Student;
import tn.esprit.studentmanagement.repositories.DepartmentRepository;
import tn.esprit.studentmanagement.services.IStudentService;

import java.util.List;

@RestController
@RequestMapping("/students")
@CrossOrigin(origins = "http://localhost:4200")
@AllArgsConstructor
public class StudentController {
    private final IStudentService studentService;
    private final DepartmentRepository departmentRepository;

    @GetMapping("/getAllStudents")
    public List<StudentDto> getAllStudents() {
        return studentService.getAllStudents().stream()
                .map(this::toDto)
                .toList();
    }

    @GetMapping("/getStudent/{id}")
    public StudentDto getStudent(@PathVariable Long id) {
        Student student = studentService.getStudentById(id);
        return student == null ? null : toDto(student);
    }

    @PostMapping("/createStudent")
    public StudentDto createStudent(@RequestBody StudentDto student) {
        return toDto(studentService.saveStudent(toEntity(student)));
    }

    @PutMapping("/updateStudent")
    public StudentDto updateStudent(@RequestBody StudentDto student) {
        return toDto(studentService.saveStudent(toEntity(student)));
    }

    @DeleteMapping("/deleteStudent/{id}")
    public void deleteStudent(@PathVariable Long id) { studentService.deleteStudent(id); }

    private StudentDto toDto(Student student) {
        return new StudentDto(
                student.getIdStudent(),
                student.getFirstName(),
                student.getLastName(),
                student.getEmail(),
                student.getPhone(),
                student.getDateOfBirth(),
                student.getAddress(),
                student.getDepartment() == null ? null : student.getDepartment().getIdDepartment());
    }

    private Student toEntity(StudentDto student) {
        Student entity = new Student();
        entity.setIdStudent(student.idStudent());
        entity.setFirstName(student.firstName());
        entity.setLastName(student.lastName());
        entity.setEmail(student.email());
        entity.setPhone(student.phone());
        entity.setDateOfBirth(student.dateOfBirth());
        entity.setAddress(student.address());
        if (student.departmentId() != null) {
            entity.setDepartment(departmentRepository.getReferenceById(student.departmentId()));
        }
        return entity;
    }
}
