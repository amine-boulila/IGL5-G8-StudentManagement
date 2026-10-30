package tn.esprit.studentmanagement.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.studentmanagement.dto.DepartmentDTO;
import tn.esprit.studentmanagement.dto.EnrollmentDTO;
import tn.esprit.studentmanagement.dto.StudentDTO;
import tn.esprit.studentmanagement.entities.Department;
import tn.esprit.studentmanagement.entities.Enrollment;
import tn.esprit.studentmanagement.entities.Status;
import tn.esprit.studentmanagement.entities.Student;
import tn.esprit.studentmanagement.services.IDepartmentService;
import tn.esprit.studentmanagement.services.IEnrollment;
import tn.esprit.studentmanagement.services.IStudentService;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires des contrôleurs : le DTO recu est converti en entite puis transmis au service.
 */
@ExtendWith(MockitoExtension.class)
class ControllersMockitoTest {

    @Mock
    private IEnrollment enrollmentService;

    @Mock
    private IStudentService studentService;

    @Mock
    private IDepartmentService departmentService;

    @Test
    void createEnrollmentShouldConvertDtoAndCallService() {
        EnrollmentController controller = new EnrollmentController(enrollmentService);
        Enrollment saved = new Enrollment();
        when(enrollmentService.saveEnrollment(any(Enrollment.class))).thenReturn(saved);

        Enrollment result = controller.createEnrollment(
                new EnrollmentDTO(null, LocalDate.of(2026, 9, 1), 14.0, Status.ACTIVE, 1L, 2L));

        assertSame(saved, result);
        ArgumentCaptor<Enrollment> captor = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollmentService).saveEnrollment(captor.capture());
        assertEquals(14.0, captor.getValue().getGrade());
        assertEquals(1L, captor.getValue().getStudent().getIdStudent());
    }

    @Test
    void updateEnrollmentShouldConvertDtoAndCallService() {
        EnrollmentController controller = new EnrollmentController(enrollmentService);
        Enrollment saved = new Enrollment();
        when(enrollmentService.saveEnrollment(any(Enrollment.class))).thenReturn(saved);

        Enrollment result = controller.updateEnrollment(
                new EnrollmentDTO(5L, null, 18.0, Status.COMPLETED, null, null));

        assertSame(saved, result);
        ArgumentCaptor<Enrollment> captor = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollmentService).saveEnrollment(captor.capture());
        assertEquals(5L, captor.getValue().getIdEnrollment());
    }

    @Test
    void createAndUpdateStudentShouldConvertDtoAndCallService() {
        StudentController controller = new StudentController(studentService);
        Student saved = new Student();
        when(studentService.saveStudent(any(Student.class))).thenReturn(saved);

        assertSame(saved, controller.createStudent(
                new StudentDTO(null, "Amina", "Ben Ali", null, null, null, null, 1L)));
        assertSame(saved, controller.updateStudent(
                new StudentDTO(2L, "Amina", "Mansour", null, null, null, null, null)));
    }

    @Test
    void createAndUpdateDepartmentShouldConvertDtoAndCallService() {
        DepartmentController controller = new DepartmentController(departmentService);
        Department saved = new Department();
        when(departmentService.saveDepartment(any(Department.class))).thenReturn(saved);

        assertSame(saved, controller.createDepartment(
                new DepartmentDTO(null, "Math", "Block B", null, null)));
        assertSame(saved, controller.updateDepartment(
                new DepartmentDTO(1L, "Applied Math", "Block B", null, null)));
    }
}
