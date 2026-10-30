package tn.esprit.studentmanagement.dto;

import tn.esprit.studentmanagement.entities.Department;

/**
 * Objet recu par l'API pour creer / modifier un departement.
 * On n'expose pas directement l'entite JPA (regle Sonar java:S4684) :
 * le client ne peut envoyer que ces champs (pas la liste des etudiants, etc.).
 */
public record DepartmentDTO(Long idDepartment, String name, String location, String phone, String head) {

    public Department toEntity() {
        Department department = new Department();
        department.setIdDepartment(idDepartment);
        department.setName(name);
        department.setLocation(location);
        department.setPhone(phone);
        department.setHead(head);
        return department;
    }
}
