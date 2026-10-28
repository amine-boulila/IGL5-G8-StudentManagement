package tn.esprit.studentmanagement.controllers;

import lombok.AllArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import tn.esprit.studentmanagement.dto.EnrollmentRequestDTO;
import tn.esprit.studentmanagement.dto.EnrollmentResponseDTO;
import tn.esprit.studentmanagement.entities.Enrollment;
import tn.esprit.studentmanagement.mappers.EnrollmentMapper;
import tn.esprit.studentmanagement.services.IEnrollment;

import java.util.List;

@RestController
@RequestMapping("/Enrollment")
@CrossOrigin(origins = "http://localhost:4200")
@AllArgsConstructor
public class EnrollmentController {
    private final IEnrollment enrollmentService;
    private final EnrollmentMapper enrollmentMapper;
    @GetMapping("/getAllEnrollment")
    public List<EnrollmentResponseDTO> getAllEnrollment() {
        return enrollmentService.getAllEnrollments().stream().map(enrollmentMapper::toResponse).toList();
    }

    @GetMapping("/getEnrollment/{id}")
    public EnrollmentResponseDTO getEnrollment(@PathVariable Long id) {
        return enrollmentMapper.toResponse(enrollmentService.getEnrollmentById(id));
    }

    @PostMapping("/createEnrollment")
    public EnrollmentResponseDTO createEnrollment(@Valid @RequestBody EnrollmentRequestDTO dto) {
        Enrollment enrollment = new Enrollment();
        enrollmentMapper.applyToEntity(dto, enrollment);
        return enrollmentMapper.toResponse(enrollmentService.saveEnrollment(enrollment));
    }

    @PutMapping("/updateEnrollment/{id}")
    public EnrollmentResponseDTO updateEnrollment(@PathVariable Long id, @Valid @RequestBody EnrollmentRequestDTO dto) {
        Enrollment enrollment = enrollmentService.getEnrollmentById(id);
        enrollmentMapper.applyToEntity(dto, enrollment);
        return enrollmentMapper.toResponse(enrollmentService.saveEnrollment(enrollment));
    }

    @DeleteMapping("/deleteEnrollment/{id}")
    public void deleteEnrollment(@PathVariable Long id) {
        enrollmentService.deleteEnrollment(id); }
}
