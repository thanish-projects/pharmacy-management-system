package com.pharmacy.supplier_service;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

// @Entity tells JPA - create a 'supplier' table in pharmacydb
@Entity
public class Supplier {

    // Primary key, auto incremented by database
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // These become columns in the supplier table
    private String name;        // supplier company name
    private String contact;     // phone number
    private String email;       // email address
    private String address;     // physical address

    // Empty constructor - JPA needs this to create objects internally
    public Supplier() {}

    // Constructor with all fields
    public Supplier(String name, String contact, String email, String address) {
        this.name = name;
        this.contact = contact;
        this.email = email;
        this.address = address;
    }

    // Getters - Spring needs these to convert to JSON
    public int getId() { return id; }
    public String getName() { return name; }
    public String getContact() { return contact; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }

    // Setters - JPA needs these to fill values when reading from DB
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setContact(String contact) { this.contact = contact; }
    public void setEmail(String email) { this.email = email; }
    public void setAddress(String address) { this.address = address; }
}