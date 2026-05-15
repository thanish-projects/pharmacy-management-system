package com.pharmacy.drug_api;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

// @Entity tells JPA - "this class is a database table"
// JPA will automatically create a 'drug' table in pharmacydb
@Entity
public class Drug {

    // @Id tells JPA - "this is the primary key of the table"
    @Id
    // @GeneratedValue tells JPA - "auto increment this ID"
    // So you don't manually set ID - database handles it
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;       // becomes a column in the table
    private String category;   // becomes a column in the table
    private double price;  
    private int stock;// becomes a column in the table

    // JPA needs an empty constructor to create objects internally
    public Drug() {}

    // Constructor to create Drug with all fields
    public Drug(String name, String category, double price) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock=stock;
    }

    // Getters - Spring needs these to convert to JSON
    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }

    // Setters - JPA needs these to set values when fetching from database
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setCategory(String category) { this.category = category; }
    public void setPrice(double price) { this.price = price; }
    public void setStock(int stock) { this.stock = stock; }  
}