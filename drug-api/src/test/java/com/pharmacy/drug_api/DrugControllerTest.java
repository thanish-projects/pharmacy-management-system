package com.pharmacy.drug_api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// exclude= tells Spring NOT to load security or JPA auto-config during this test
// This prevents Eureka + DB from starting up just to test HTTP endpoints
@WebMvcTest(
    controllers = DrugController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class
    }
)
class DrugControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DrugService drugService;

    @Autowired
    private ObjectMapper objectMapper;

    private Drug sampleDrug;

    @BeforeEach
    void setUp() {
        sampleDrug = new Drug();
        sampleDrug.setId(1);
        sampleDrug.setName("Paracetamol");
        sampleDrug.setCategory("Painkiller");
        sampleDrug.setPrice(50.0);
        sampleDrug.setStock(100);
    }

    @Test
    void getAllDrugs_ShouldReturn200WithList() throws Exception {
        when(drugService.getAllDrugs()).thenReturn(Arrays.asList(sampleDrug));

        mockMvc.perform(get("/drugs/all"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Paracetamol"))
            .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getDrugById_WhenExists_ShouldReturn200() throws Exception {
        when(drugService.getDrugById(1)).thenReturn(sampleDrug);

        mockMvc.perform(get("/drugs/view/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Paracetamol"))
            .andExpect(jsonPath("$.category").value("Painkiller"));
    }

    @Test
    void addDrug_ShouldReturn200WithSavedDrug() throws Exception {
        when(drugService.addDrug(any(Drug.class))).thenReturn(sampleDrug);

        mockMvc.perform(post("/drugs/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleDrug)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Paracetamol"));
    }

    @Test
    void updateDrug_ShouldReturn200WithUpdatedDrug() throws Exception {
        when(drugService.updateDrug(eq(1), any(Drug.class))).thenReturn(sampleDrug);

        mockMvc.perform(put("/drugs/update/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleDrug)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Paracetamol"));
    }

    @Test
    void deleteDrug_ShouldReturn200WithMessage() throws Exception {
        doNothing().when(drugService).deleteDrug(1);

        mockMvc.perform(delete("/drugs/delete/1"))
            .andExpect(status().isOk())
            .andExpect(content().string("Drug deleted with id: 1"));
    }
}