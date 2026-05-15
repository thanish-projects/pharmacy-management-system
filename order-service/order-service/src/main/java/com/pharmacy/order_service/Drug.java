package com.pharmacy.order_service;

// This is a DTO (Data Transfer Object)
// It mirrors the Drug structure from Inventory Service
// Order Service uses this to understand drug data received from Inventory Service
public class Drug {
    private int id;
    private String name;
    private String category;
    private double price;
    private int stock;

    public Drug() {}

    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setCategory(String category) { this.category = category; }
    public void setPrice(double price) { this.price = price; }
    public void setStock(int stock) { this.stock = stock; }
}