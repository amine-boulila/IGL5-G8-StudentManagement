package tn.esprit.studentmanagement.dto;

import tn.esprit.studentmanagement.entities.Department;
import tn.esprit.studentmanagement.entities.Student;

import java.time.LocalDate;

/**
 * Objet recu par l'API pour creer / modifier un etudiant (regle Sonar java:S4684).
 * Le departement est reference par son id uniquement.
 */
public record StudentDTO(Long idStudent, String firstName, String lastName, String email, String phone,
                         LocalDate dateOfBirth, String address, Long departmentId) {

    public Student toEntity() {
        Student student = new Student();
        student.setIdStudent(idStudent);
        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setEmail(email);
        student.setPhone(phone);
        student.setDateOfBirth(dateOfBirth);
        student.setAddress(address);
        if (departmentId != null) {
            Department department = new Department();
            department.setIdDepartment(departmentId);
            student.setDepartment(department);
        }
        return student;
    }
}
