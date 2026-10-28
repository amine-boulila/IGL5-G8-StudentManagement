package tn.esprit.studentmanagement.mappers;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.studentmanagement.dto.StudentRequestDTO;
import tn.esprit.studentmanagement.dto.StudentResponseDTO;
import tn.esprit.studentmanagement.entities.Department;
import tn.esprit.studentmanagement.entities.Student;
import tn.esprit.studentmanagement.repositories.DepartmentRepository;

@Component
@AllArgsConstructor
public class StudentMapper {
    private final DepartmentRepository departmentRepository;

    public void applyToEntity(StudentRequestDTO dto, Student target) {
        target.setFirstName(dto.getFirstName());
        target.setLastName(dto.getLastName());
        target.setEmail(dto.getEmail());
        target.setPhone(dto.getPhone());
        target.setDateOfBirth(dto.getDateOfBirth());
        target.setAddress(dto.getAddress());
        if (dto.getDepartmentId() == null) {
            target.setDepartment(null);
        } else {
            Department department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Department not found"));
            target.setDepartment(department);
        }
    }

    public StudentResponseDTO toResponse(Student student) {
        if (student == null) return null;
        return new StudentResponseDTO(student.getIdStudent(), student.getFirstName(), student.getLastName(),
                student.getEmail(), student.getPhone(), student.getDateOfBirth(), student.getAddress(),
                student.getDepartment() == null ? null : student.getDepartment().getIdDepartment());
    }
}
