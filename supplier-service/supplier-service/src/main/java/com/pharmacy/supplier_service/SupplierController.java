package com.pharmacy.supplier_service;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "Supplier Management", description = "APIs for managing pharmacy suppliers")
@RestController
@RequestMapping("/suppliers")
public class SupplierController {

    private static final Logger log = LoggerFactory.getLogger(SupplierController.class);

    @Autowired
    private SupplierRepository supplierRepository;

    @Operation(summary = "Get all suppliers")
    @GetMapping("/all")
    public List<Supplier> getAllSuppliers() {
        log.info("Fetching all suppliers");
        return supplierRepository.findAll();
    }

    @Operation(summary = "Get supplier by ID")
    @GetMapping("/view/{id}")
    public Supplier getSupplierById(@PathVariable int id) {
        log.info("Fetching supplier with id: {}", id);
        return supplierRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Supplier not found with id: {}", id);
                return new ResourceNotFoundException("Supplier not found with id: " + id);
            });
    }

    @Operation(summary = "Add new supplier")
    @PostMapping("/add")
    public Supplier addSupplier(@RequestBody Supplier supplier) {
        log.info("Adding new supplier: {}", supplier.getName());
        Supplier saved = supplierRepository.save(supplier);
        log.info("Supplier added successfully with id: {}", saved.getId());
        return saved;
    }

    @Operation(summary = "Update supplier")
    @PutMapping("/update/{id}")
    public Supplier updateSupplier(@PathVariable int id,
                                   @RequestBody Supplier updatedSupplier) {
        log.info("Updating supplier with id: {}", id);
        Supplier existing = supplierRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Supplier not found with id: {} — cannot update", id);
                return new ResourceNotFoundException("Supplier not found with id: " + id);
            });
        existing.setName(updatedSupplier.getName());
        existing.setContact(updatedSupplier.getContact());
        existing.setEmail(updatedSupplier.getEmail());
        existing.setAddress(updatedSupplier.getAddress());
        Supplier updated = supplierRepository.save(existing);
        log.info("Supplier updated successfully with id: {}", id);
        return updated;
    }

    @Operation(summary = "Delete supplier")
    @DeleteMapping("/delete/{id}")
    public String deleteSupplier(@PathVariable int id) {
        log.info("Deleting supplier with id: {}", id);
        if (!supplierRepository.existsById(id)) {
            log.warn("Supplier not found with id: {} — cannot delete", id);
            throw new ResourceNotFoundException("Supplier not found with id: " + id);
        }
        supplierRepository.deleteById(id);
        log.info("Supplier deleted successfully with id: {}", id);
        return "Supplier deleted with id: " + id;
    }
}