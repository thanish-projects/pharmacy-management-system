package com.pharmacy.drug_api;

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
import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class) — tells JUnit 5 to activate Mockito for this test class
// Without this, @Mock and @InjectMocks won't work
@ExtendWith(MockitoExtension.class)
class DrugServiceTest {

    // @Mock creates a FAKE DrugRepository — no real DB calls happen
    // Think of it as a stunt double that you can program to behave however you want
    @Mock
    private DrugRepository drugRepository;

    // @InjectMocks creates a REAL DrugService, but injects the fake repo into it
    // So DrugService thinks it has a real repo, but it's actually talking to our mock
    @InjectMocks
    private DrugService drugService;

    // A reusable Drug object for all tests
    private Drug sampleDrug;

    // @BeforeEach runs before EVERY single test method — fresh setup each time
    @BeforeEach
    void setUp() {
        sampleDrug = new Drug();
        sampleDrug.setId(1);
        sampleDrug.setName("Paracetamol");
        sampleDrug.setCategory("Painkiller");
        sampleDrug.setPrice(50.0);
        sampleDrug.setStock(100);
    }

    // ===== getAllDrugs() tests =====

    @Test
    void getAllDrugs_ShouldReturnListOfDrugs() {
        // ARRANGE — tell the mock what to return when findAll() is called
        when(drugRepository.findAll()).thenReturn(Arrays.asList(sampleDrug));

        // ACT — call the real method
        List<Drug> result = drugService.getAllDrugs();

        // ASSERT — verify the result is what we expect
        assertEquals(1, result.size());
        assertEquals("Paracetamol", result.get(0).getName());

        // Verify that findAll() was actually called exactly once
        // This catches bugs where the method returns hardcoded data and never hits the DB
        verify(drugRepository, times(1)).findAll();
    }

    @Test
    void getAllDrugs_WhenEmpty_ShouldReturnEmptyList() {
        when(drugRepository.findAll()).thenReturn(Arrays.asList());

        List<Drug> result = drugService.getAllDrugs();

        assertTrue(result.isEmpty());
    }

    // ===== getDrugById() tests =====

    @Test
    void getDrugById_WhenExists_ShouldReturnDrug() {
        // Optional.of() — simulates the repo finding the drug
        when(drugRepository.findById(1)).thenReturn(Optional.of(sampleDrug));

        Drug result = drugService.getDrugById(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Paracetamol", result.getName());
    }

    @Test
    void getDrugById_WhenNotFound_ShouldThrowException() {
        // Optional.empty() — simulates the drug not existing in DB
        when(drugRepository.findById(99)).thenReturn(Optional.empty());

        // assertThrows verifies that calling getDrugById(99) actually throws this exception
        // If it doesn't throw, the test FAILS — this is how you test error paths
        ResourceNotFoundException ex = assertThrows(
            ResourceNotFoundException.class,
            () -> drugService.getDrugById(99)
        );

        assertTrue(ex.getMessage().contains("99"));
    }

    // ===== addDrug() tests =====

    @Test
    void addDrug_ShouldSaveAndReturnDrug() {
        // When save() is called with any Drug object, return our sampleDrug
        // any(Drug.class) — Mockito matcher, matches any Drug instance
        when(drugRepository.save(any(Drug.class))).thenReturn(sampleDrug);

        Drug result = drugService.addDrug(sampleDrug);

        assertNotNull(result);
        assertEquals("Paracetamol", result.getName());
        verify(drugRepository, times(1)).save(sampleDrug);
    }

    // ===== deleteDrug() tests =====

    @Test
    void deleteDrug_WhenExists_ShouldDeleteSuccessfully() {
        // existsById returns true — drug exists
        when(drugRepository.existsById(1)).thenReturn(true);

        // deleteById returns void, so we don't need when() — just call and verify
        drugService.deleteDrug(1);

        // Confirm deleteById was actually called with id=1
        verify(drugRepository, times(1)).deleteById(1);
    }

    @Test
    void deleteDrug_WhenNotFound_ShouldThrowException() {
        when(drugRepository.existsById(99)).thenReturn(false);

        assertThrows(
            ResourceNotFoundException.class,
            () -> drugService.deleteDrug(99)
        );

        // Confirm deleteById was NEVER called — no point deleting something that doesn't exist
        verify(drugRepository, never()).deleteById(99);
    }

    // ===== updateDrug() tests =====

    @Test
    void updateDrug_WhenExists_ShouldUpdateAndReturn() {
        Drug updatedData = new Drug();
        updatedData.setName("Ibuprofen");
        updatedData.setCategory("Anti-inflammatory");
        updatedData.setPrice(80.0);
        updatedData.setStock(50);

        when(drugRepository.findById(1)).thenReturn(Optional.of(sampleDrug));
        when(drugRepository.save(any(Drug.class))).thenReturn(sampleDrug);

        Drug result = drugService.updateDrug(1, updatedData);

        assertNotNull(result);
        // The existing drug object was mutated with new values before save
        verify(drugRepository, times(1)).save(sampleDrug);
    }

    @Test
    void updateDrug_WhenNotFound_ShouldThrowException() {
        when(drugRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> drugService.updateDrug(99, sampleDrug)
        );

        // save() should never be reached if drug doesn't exist
        verify(drugRepository, never()).save(any());
    }
}