package tn.esprit.studentmanagement.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.studentmanagement.mappers.EnrollmentMapper;
import tn.esprit.studentmanagement.services.IEnrollment;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EnrollmentController.class)
class EnrollmentControllerTest {
    @Autowired MockMvc mvc;
    @MockBean IEnrollment service;
    @MockBean EnrollmentMapper mapper;

    @Test
    void lists() throws Exception {
        when(service.getAllEnrollments()).thenReturn(List.of());
        mvc.perform(get("/Enrollment/getAllEnrollment")).andExpect(status().isOk());
    }

    @Test
    void gets() throws Exception {
        mvc.perform(get("/Enrollment/getEnrollment/1")).andExpect(status().isOk());
    }

    @Test
    void creates() throws Exception {
        mvc.perform(post("/Enrollment/createEnrollment").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void updates() throws Exception {
        mvc.perform(put("/Enrollment/updateEnrollment/1").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void deletes() throws Exception {
        mvc.perform(delete("/Enrollment/deleteEnrollment/1")).andExpect(status().isOk());
    }
}
