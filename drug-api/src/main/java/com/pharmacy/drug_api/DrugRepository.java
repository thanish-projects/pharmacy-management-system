package com.pharmacy.drug_api;

import org.springframework.data.jpa.repository.JpaRepository;

// JpaRepository<Drug, Integer> means:
// - Drug = the entity/table this repository works with
// - Integer = the data type of the primary key (our id is int)

// Just by extending JpaRepository, Spring automatically gives us:
// findAll(), findById(), save(), deleteById() and many more
// We don't write anything inside - Spring handles it all!

public interface DrugRepository extends JpaRepository<Drug, Integer> {
    // empty - Spring generates all methods automatically
}