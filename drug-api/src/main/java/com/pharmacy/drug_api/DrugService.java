package com.pharmacy.drug_api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DrugService {

    // Logger instance for this class
    private static final Logger log = LoggerFactory.getLogger(DrugService.class);

    @Autowired
    private DrugRepository drugRepository;

    public List<Drug> getAllDrugs() {
        log.info("Fetching all drugs from database");
        return drugRepository.findAll();
    }

    public Drug addDrug(Drug drug) {
        log.info("Adding new drug: {}", drug.getName());
        Drug saved = drugRepository.save(drug);
        log.info("Drug added successfully with id: {}", saved.getId());
        return saved;
    }

    public void deleteDrug(int id) {
        log.info("Attempting to delete drug with id: {}", id);
        if (!drugRepository.existsById(id)) {
            log.warn("Drug not found with id: {} — throwing exception", id);
            throw new ResourceNotFoundException("Drug not found with id: " + id);
        }
        drugRepository.deleteById(id);
        log.info("Drug deleted successfully with id: {}", id);
    }

    public Drug getDrugById(int id) {
        log.info("Fetching drug with id: {}", id);
        return drugRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Drug not found with id: {}", id);
                return new ResourceNotFoundException("Drug not found with id: " + id);
            });
    }

    public Drug updateDrug(int id, Drug updatedDrug) {
        log.info("Updating drug with id: {}", id);
        Drug existing = drugRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Drug not found with id: {} — cannot update", id);
                return new ResourceNotFoundException("Drug not found with id: " + id);
            });
        existing.setName(updatedDrug.getName());
        existing.setCategory(updatedDrug.getCategory());
        existing.setPrice(updatedDrug.getPrice());
        existing.setStock(updatedDrug.getStock());
        Drug updated = drugRepository.save(existing);
        log.info("Drug updated successfully with id: {}", id);
        return updated;
    }
}