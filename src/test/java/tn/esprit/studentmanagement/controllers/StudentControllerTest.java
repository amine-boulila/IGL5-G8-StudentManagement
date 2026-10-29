package tn.esprit.studentmanagement.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.studentmanagement.mappers.StudentMapper;
import tn.esprit.studentmanagement.services.IStudentService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StudentController.class)
class StudentControllerTest {
    @Autowired MockMvc mvc;
    @MockBean IStudentService service;
    @MockBean StudentMapper mapper;

    @Test
    void lists() throws Exception {
        when(service.getAllStudents()).thenReturn(List.of());
        mvc.perform(get("/students/getAllStudents")).andExpect(status().isOk());
    }

    @Test
    void gets() throws Exception {
        mvc.perform(get("/students/getStudent/1")).andExpect(status().isOk());
    }

    @Test
    void creates() throws Exception {
        mvc.perform(post("/students/createStudent").contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"A\",\"lastName\":\"B\",\"email\":\"a@b.test\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsInvalidCreate() throws Exception {
        mvc.perform(post("/students/createStudent").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updates() throws Exception {
        mvc.perform(put("/students/updateStudent/1").contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"A\",\"lastName\":\"B\",\"email\":\"a@b.test\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void deletes() throws Exception {
        mvc.perform(delete("/students/deleteStudent/1")).andExpect(status().isOk());
    }
}
