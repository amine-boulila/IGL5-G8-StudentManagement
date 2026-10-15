package tn.esprit.studentmanagement.services;

import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.studentmanagement.entities.Student;
import tn.esprit.studentmanagement.repositories.StudentRepository;

import java.util.List;

@Service
public class StudentService implements IStudentService {
    private static final Logger logger = LogManager.getLogger(StudentService.class);

    @Autowired
    private StudentRepository studentRepository;

    public List<Student> getAllStudents() {
        logger.info("Fetching all students");
        try {
            List<Student> students = studentRepository.findAll();
            logger.debug("Found {} students", students.size());
            return students;
        } catch (RuntimeException exception) {
            logger.error("Failed to fetch students", exception);
            throw exception;
        }
    }

    public Student getStudentById(Long id) {
        logger.info("Fetching student with id {}", id);
        try {
            Student student = studentRepository.findById(id).orElse(null);
            if (student == null) {
                logger.debug("No student found with id {}", id);
            }
            return student;
        } catch (RuntimeException exception) {
            logger.error("Failed to fetch student with id {}", id, exception);
            throw exception;
        }
    }

    public Student saveStudent(Student student) {
        logger.info("Saving student");
        try {
            Student savedStudent = studentRepository.save(student);
            logger.debug("Saved student with id {}", savedStudent.getIdStudent());
            return savedStudent;
        } catch (RuntimeException exception) {
            logger.error("Failed to save student", exception);
            throw exception;
        }
    }

    public void deleteStudent(Long id) {
        logger.info("Deleting student with id {}", id);
        try {
            studentRepository.deleteById(id);
            logger.debug("Deleted student with id {}", id);
        } catch (RuntimeException exception) {
            logger.error("Failed to delete student with id {}", id, exception);
            throw exception;
        }
    }

}
