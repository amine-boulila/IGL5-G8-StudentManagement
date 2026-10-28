package tn.esprit.studentmanagement.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.studentmanagement.services.IDepartmentService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DepartmentController.class)
class DepartmentControllerTest {
    @Autowired MockMvc mvc;
    @MockBean IDepartmentService service;

    @Test
    void lists() throws Exception {
        when(service.getAllDepartments()).thenReturn(List.of());
        mvc.perform(get("/Depatment/getAllDepartments")).andExpect(status().isOk());
    }

    @Test
    void gets() throws Exception {
        mvc.perform(get("/Depatment/getDepartment/1")).andExpect(status().isOk());
    }

    @Test
    void creates() throws Exception {
        mvc.perform(post("/Depatment/createDepartment").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void updates() throws Exception {
        mvc.perform(put("/Depatment/updateDepartment").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void deletes() throws Exception {
        mvc.perform(delete("/Depatment/deleteDepartment/1")).andExpect(status().isOk());
    }
}
