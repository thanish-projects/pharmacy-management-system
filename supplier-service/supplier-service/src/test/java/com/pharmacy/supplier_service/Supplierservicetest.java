package com.pharmacy.supplier_service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private SupplierController supplierController;

    private Supplier sampleSupplier;

    @BeforeEach
    void setUp() {
        sampleSupplier = new Supplier("MedCorp", "9876543210", "medcorp@email.com", "123 MedStreet");
        sampleSupplier.setId(1);
    }

    // ===== getAllSuppliers() tests =====

    @Test
    void getAllSuppliers_ShouldReturnAllSuppliers() {
        Supplier supplier2 = new Supplier("PharmaPlus", "9123456780", "pharmaplus@email.com", "456 PharmaAve");
        supplier2.setId(2);

        when(supplierRepository.findAll()).thenReturn(Arrays.asList(sampleSupplier, supplier2));

        List<Supplier> result = supplierController.getAllSuppliers();

        assertEquals(2, result.size());
        assertEquals("MedCorp", result.get(0).getName());
        assertEquals("PharmaPlus", result.get(1).getName());
        verify(supplierRepository, times(1)).findAll();
    }

    @Test
    void getAllSuppliers_WhenEmpty_ShouldReturnEmptyList() {
        when(supplierRepository.findAll()).thenReturn(Arrays.asList());

        List<Supplier> result = supplierController.getAllSuppliers();

        assertTrue(result.isEmpty());
        verify(supplierRepository, times(1)).findAll();
    }

    // ===== getSupplierById() tests =====

    @Test
    void getSupplierById_WhenExists_ShouldReturnSupplier() {
        when(supplierRepository.findById(1)).thenReturn(Optional.of(sampleSupplier));

        Supplier result = supplierController.getSupplierById(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("MedCorp", result.getName());
        assertEquals("9876543210", result.getContact());
        assertEquals("medcorp@email.com", result.getEmail());
        assertEquals("123 MedStreet", result.getAddress());
    }

    @Test
    void getSupplierById_WhenNotFound_ShouldThrowResourceNotFoundException() {
        when(supplierRepository.findById(99)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
            ResourceNotFoundException.class,
            () -> supplierController.getSupplierById(99)
        );

        assertTrue(ex.getMessage().contains("99"));

        // Repository was queried but nothing was found — no save should happen
        verify(supplierRepository, never()).save(any());
    }

    // ===== addSupplier() tests =====

    @Test
    void addSupplier_ShouldSaveAndReturnSupplier() {
        Supplier input = new Supplier("NewSupplier", "1111111111", "new@email.com", "789 NewRoad");
        Supplier saved = new Supplier("NewSupplier", "1111111111", "new@email.com", "789 NewRoad");
        saved.setId(3);

        when(supplierRepository.save(any(Supplier.class))).thenReturn(saved);

        Supplier result = supplierController.addSupplier(input);

        assertNotNull(result);
        assertEquals(3, result.getId());
        assertEquals("NewSupplier", result.getName());
        assertEquals("new@email.com", result.getEmail());

        // Verify save() was called exactly once — not zero, not twice
        verify(supplierRepository, times(1)).save(any(Supplier.class));
    }

    @Test
    void addSupplier_ShouldPersistAllFields() {
        // Verify that all fields passed in are actually stored — not just name
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> {
            Supplier s = invocation.getArgument(0);
            s.setId(5);
            return s;
        });

        Supplier input = new Supplier("TestCo", "9999999999", "test@co.com", "Test City");
        Supplier result = supplierController.addSupplier(input);

        assertEquals("TestCo", result.getName());
        assertEquals("9999999999", result.getContact());
        assertEquals("test@co.com", result.getEmail());
        assertEquals("Test City", result.getAddress());
    }

    // ===== updateSupplier() tests =====

    @Test
    void updateSupplier_WhenExists_ShouldUpdateAllFieldsAndReturn() {
        Supplier updatePayload = new Supplier("UpdatedCorp", "0000000000", "updated@email.com", "New Address");

        when(supplierRepository.findById(1)).thenReturn(Optional.of(sampleSupplier));
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Supplier result = supplierController.updateSupplier(1, updatePayload);

        // All fields should be updated on the existing entity
        assertEquals("UpdatedCorp", result.getName());
        assertEquals("0000000000", result.getContact());
        assertEquals("updated@email.com", result.getEmail());
        assertEquals("New Address", result.getAddress());

        verify(supplierRepository, times(1)).findById(1);
        verify(supplierRepository, times(1)).save(any(Supplier.class));
    }

    @Test
    void updateSupplier_WhenNotFound_ShouldThrowResourceNotFoundException() {
        Supplier updatePayload = new Supplier("X", "X", "x@x.com", "X");

        when(supplierRepository.findById(99)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
            ResourceNotFoundException.class,
            () -> supplierController.updateSupplier(99, updatePayload)
        );

        assertTrue(ex.getMessage().contains("99"));

        // If supplier doesn't exist, save() must never be called
        verify(supplierRepository, never()).save(any());
    }

    // ===== deleteSupplier() tests =====

    @Test
    void deleteSupplier_WhenExists_ShouldDeleteAndReturnMessage() {
        when(supplierRepository.existsById(1)).thenReturn(true);
        doNothing().when(supplierRepository).deleteById(1);

        String result = supplierController.deleteSupplier(1);

        assertEquals("Supplier deleted with id: 1", result);

        // Verify deleteById was actually called — not just existsById
        verify(supplierRepository, times(1)).deleteById(1);
    }

    @Test
    void deleteSupplier_WhenNotFound_ShouldThrowResourceNotFoundException() {
        when(supplierRepository.existsById(99)).thenReturn(false);

        ResourceNotFoundException ex = assertThrows(
            ResourceNotFoundException.class,
            () -> supplierController.deleteSupplier(99)
        );

        assertTrue(ex.getMessage().contains("99"));

        // If supplier doesn't exist, deleteById must never be called
        verify(supplierRepository, never()).deleteById(anyInt());
    }

    @Test
    void deleteSupplier_ShouldNotCallDeleteIfExistsFails() {
        // Double-check: existsById returns false → deleteById must be skipped entirely
        when(supplierRepository.existsById(5)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> supplierController.deleteSupplier(5));

        verify(supplierRepository, never()).deleteById(5);
    }
}