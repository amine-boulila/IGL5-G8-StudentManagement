package tn.esprit.studentmanagement.controllers;

import lombok.AllArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import tn.esprit.studentmanagement.dto.StudentRequestDTO;
import tn.esprit.studentmanagement.dto.StudentResponseDTO;
import tn.esprit.studentmanagement.entities.Student;
import tn.esprit.studentmanagement.mappers.StudentMapper;
import tn.esprit.studentmanagement.services.IStudentService;

import java.util.List;

@RestController
@RequestMapping("/students")
@CrossOrigin(origins = "http://localhost:4200")
@AllArgsConstructor
public class StudentController {
    private final IStudentService studentService;
    private final StudentMapper studentMapper;

    @GetMapping("/getAllStudents")
    public List<StudentResponseDTO> getAllStudents() {
        return studentService.getAllStudents().stream().map(studentMapper::toResponse).toList();
    }

    @GetMapping("/getStudent/{id}")
    public StudentResponseDTO getStudent(@PathVariable Long id) {
        return studentMapper.toResponse(studentService.getStudentById(id));
    }

    @PostMapping("/createStudent")
    public StudentResponseDTO createStudent(@Valid @RequestBody StudentRequestDTO dto) {
        Student student = new Student();
        studentMapper.applyToEntity(dto, student);
        return studentMapper.toResponse(studentService.saveStudent(student));
    }

    @PutMapping("/updateStudent/{id}")
    public StudentResponseDTO updateStudent(@PathVariable Long id, @Valid @RequestBody StudentRequestDTO dto) {
        Student student = studentService.getStudentById(id);
        studentMapper.applyToEntity(dto, student);
        return studentMapper.toResponse(studentService.saveStudent(student));
    }

    @DeleteMapping("/deleteStudent/{id}")
    public void deleteStudent(@PathVariable Long id) { studentService.deleteStudent(id); }
}
