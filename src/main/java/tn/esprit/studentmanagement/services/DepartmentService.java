package tn.esprit.studentmanagement.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.studentmanagement.entities.Department;
import tn.esprit.studentmanagement.repositories.DepartmentRepository;

import java.util.List;

@Service

public class DepartmentService implements IDepartmentService {
    private static final Logger logger = LogManager.getLogger(DepartmentService.class);

    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    public List<Department> getAllDepartments() {
        logger.info("Fetching all departments");
        try {
            List<Department> departments = departmentRepository.findAll();
            logger.debug("Found {} departments", departments.size());
            return departments;
        } catch (RuntimeException exception) {
            logger.error("Failed to fetch departments", exception);
            throw exception;
        }
    }

    @Override
    public Department getDepartmentById(Long idDepartment) {
        logger.info("Fetching department with id {}", idDepartment);
        try {
            Department department = departmentRepository.findById(idDepartment).get();
            logger.debug("Found department with id {}", idDepartment);
            return department;
        } catch (RuntimeException exception) {
            logger.error("Failed to fetch department with id {}", idDepartment, exception);
            throw exception;
        }
    }

    @Override
    public Department saveDepartment(Department department) {
        logger.info("Saving department");
        try {
            Department savedDepartment = departmentRepository.save(department);
            logger.debug("Saved department with id {}", savedDepartment.getIdDepartment());
            return savedDepartment;
        } catch (RuntimeException exception) {
            logger.error("Failed to save department", exception);
            throw exception;
        }
    }

    @Override
    public void deleteDepartment(Long idDepartment) {
        logger.info("Deleting department with id {}", idDepartment);
        try {
            departmentRepository.deleteById(idDepartment);
            logger.debug("Deleted department with id {}", idDepartment);
        } catch (RuntimeException exception) {
            logger.error("Failed to delete department with id {}", idDepartment, exception);
            throw exception;
        }
    }
}
