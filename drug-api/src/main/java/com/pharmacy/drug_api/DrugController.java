package com.pharmacy.drug_api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

// @Tag groups all endpoints in this controller under one section in Swagger UI
@Tag(name = "Drug Management", description = "APIs for managing pharmacy drug inventory")
@RestController
@RequestMapping("/drugs")
public class DrugController {

    @Autowired
    private DrugService service;

    // @Operation adds a summary and description to this endpoint in Swagger
    @Operation(
        summary = "Get all drugs",
        description = "Returns a list of all drugs available in the pharmacy inventory"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved drug list"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid")
    })
    @GetMapping("/all")
    public List<Drug> getAllDrugs() {
        return service.getAllDrugs();
    }

    @Operation(summary = "Get drug by ID", description = "Returns a single drug by its ID")
    @GetMapping("/view/{id}")
    public Drug getDrugById(@PathVariable int id) {
        return service.getDrugById(id);
    }

    @Operation(summary = "Add new drug", description = "Adds a new drug to the inventory. Admin only.")
    @PostMapping("/add")
    public Drug addDrug(@RequestBody Drug drug) {
        return service.addDrug(drug);
    }

    @Operation(summary = "Update drug", description = "Updates an existing drug by ID. Admin only.")
    @PutMapping("/update/{id}")
    public Drug updateDrug(@PathVariable int id, @RequestBody Drug updatedDrug) {
        return service.updateDrug(id, updatedDrug);
    }

    @Operation(summary = "Delete drug", description = "Deletes a drug from inventory by ID. Admin only.")
    @DeleteMapping("/delete/{id}")
    public String deleteDrug(@PathVariable int id) {
        service.deleteDrug(id);
        return "Drug deleted with id: " + id;
    }
}
