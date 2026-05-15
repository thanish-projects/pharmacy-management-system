package com.pharmacy.order_service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "INVENTORY-SERVICE")
public interface DrugClient {

    @GetMapping("/drugs/view/{id}")
    Drug getDrugById(@PathVariable int id);

    @PutMapping("/drugs/update/{id}")
    Drug updateDrug(@PathVariable int id, @RequestBody Drug drug);
}