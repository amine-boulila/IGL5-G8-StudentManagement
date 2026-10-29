package tn.esprit.studentmanagement.dto;

import org.junit.jupiter.api.Test;
import tn.esprit.studentmanagement.entities.Department;

import static org.junit.jupiter.api.Assertions.*;

class DepartmentMapperTest {
    @Test
    void mapsBothWaysAndHandlesNull() {
        DepartmentDTO dto = new DepartmentDTO(3L, "CS", "A", "123", "Dr A");
        Department entity = DepartmentMapper.toEntity(dto);
        assertEquals(3L, entity.getIdDepartment());
        assertEquals("CS", entity.getName());
        assertEquals("A", entity.getLocation());
        assertEquals("123", entity.getPhone());
        assertEquals("Dr A", entity.getHead());
        assertEquals(dto.getName(), DepartmentMapper.toDto(entity).getName());
        assertNull(DepartmentMapper.toDto(null));
        assertNull(DepartmentMapper.toEntity(null));
    }
}
