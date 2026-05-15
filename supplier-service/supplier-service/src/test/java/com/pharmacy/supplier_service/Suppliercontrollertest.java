package com.pharmacy.supplier_service;

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
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = SupplierController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class
    }
)
class SupplierControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SupplierRepository supplierRepository;

    private Supplier sampleSupplier;

    @BeforeEach
    void setUp() {
        sampleSupplier = new Supplier("MedCorp", "9876543210", "medcorp@email.com", "123 MedStreet");
        sampleSupplier.setId(1);
    }

    // ===== GET /suppliers/all =====

    @Test
    void getAllSuppliers_ShouldReturn200WithList() throws Exception {
        Supplier supplier2 = new Supplier("PharmaPlus", "9123456780", "pharmaplus@email.com", "456 PharmaAve");
        supplier2.setId(2);

        when(supplierRepository.findAll()).thenReturn(Arrays.asList(sampleSupplier, supplier2));

        mockMvc.perform(get("/suppliers/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("MedCorp"))
                .andExpect(jsonPath("$[1].name").value("PharmaPlus"));
    }

    @Test
    void getAllSuppliers_WhenEmpty_ShouldReturn200WithEmptyList() throws Exception {
        when(supplierRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/suppliers/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ===== GET /suppliers/view/{id} =====

    @Test
    void getSupplierById_WhenExists_ShouldReturn200() throws Exception {
        when(supplierRepository.findById(1)).thenReturn(Optional.of(sampleSupplier));

        mockMvc.perform(get("/suppliers/view/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("MedCorp"))
                .andExpect(jsonPath("$.contact").value("9876543210"))
                .andExpect(jsonPath("$.email").value("medcorp@email.com"))
                .andExpect(jsonPath("$.address").value("123 MedStreet"));
    }

    @Test
    void getSupplierById_WhenNotFound_ShouldReturn404() throws Exception {
        when(supplierRepository.findById(99)).thenReturn(Optional.empty());

        mockMvc.perform(get("/suppliers/view/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Supplier not found with id: 99"));
    }

    // ===== POST /suppliers/add =====

    @Test
    void addSupplier_ValidSupplier_ShouldReturn200WithSavedSupplier() throws Exception {
        Supplier input = new Supplier("NewSupplier", "1111111111", "new@email.com", "789 NewRoad");
        Supplier saved = new Supplier("NewSupplier", "1111111111", "new@email.com", "789 NewRoad");
        saved.setId(3);

        when(supplierRepository.save(any(Supplier.class))).thenReturn(saved);

        mockMvc.perform(post("/suppliers/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("NewSupplier"))
                .andExpect(jsonPath("$.contact").value("1111111111"))
                .andExpect(jsonPath("$.email").value("new@email.com"))
                .andExpect(jsonPath("$.address").value("789 NewRoad"));
    }

    // ===== PUT /suppliers/update/{id} =====

    @Test
    void updateSupplier_WhenExists_ShouldReturn200WithUpdatedSupplier() throws Exception {
        Supplier updatePayload = new Supplier("UpdatedCorp", "0000000000", "updated@email.com", "New Address");
        Supplier updated = new Supplier("UpdatedCorp", "0000000000", "updated@email.com", "New Address");
        updated.setId(1);

        when(supplierRepository.findById(1)).thenReturn(Optional.of(sampleSupplier));
        when(supplierRepository.save(any(Supplier.class))).thenReturn(updated);

        mockMvc.perform(put("/suppliers/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("UpdatedCorp"))
                .andExpect(jsonPath("$.contact").value("0000000000"))
                .andExpect(jsonPath("$.email").value("updated@email.com"))
                .andExpect(jsonPath("$.address").value("New Address"));
    }

    @Test
    void updateSupplier_WhenNotFound_ShouldReturn404() throws Exception {
        Supplier updatePayload = new Supplier("X", "X", "x@x.com", "X");

        when(supplierRepository.findById(99)).thenReturn(Optional.empty());

        mockMvc.perform(put("/suppliers/update/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePayload)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Supplier not found with id: 99"));
    }

    // ===== DELETE /suppliers/delete/{id} =====

    @Test
    void deleteSupplier_WhenExists_ShouldReturn200WithMessage() throws Exception {
        when(supplierRepository.existsById(1)).thenReturn(true);
        doNothing().when(supplierRepository).deleteById(1);

        mockMvc.perform(delete("/suppliers/delete/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Supplier deleted with id: 1"));
    }

    @Test
    void deleteSupplier_WhenNotFound_ShouldReturn404() throws Exception {
        when(supplierRepository.existsById(99)).thenReturn(false);

        mockMvc.perform(delete("/suppliers/delete/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Supplier not found with id: 99"));
    }
}