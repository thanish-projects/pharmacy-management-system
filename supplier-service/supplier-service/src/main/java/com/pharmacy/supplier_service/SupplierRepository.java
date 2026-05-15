package com.pharmacy.supplier_service;

import org.springframework.data.jpa.repository.JpaRepository;

// Just like DrugRepository - extend JpaRepository
// Spring automatically gives us findAll(), findById(), save(), deleteById()
// SupplierRepository works with Supplier table, primary key is Integer
public interface SupplierRepository extends JpaRepository<Supplier, Integer> {
    // empty - Spring generates everything automatically
}