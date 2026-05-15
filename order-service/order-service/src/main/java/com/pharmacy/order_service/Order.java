package com.pharmacy.order_service;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders") // 'order' is a reserved word in MySQL so we use 'orders'
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // Who placed the order - doctor's email from JWT token
    private String doctorEmail;

    // Which drug was ordered
    private int drugId;

    // Drug name - stored for easy report generation
    private String drugName;

    // How many units ordered
    private int quantity;

    // Total price = drug price x quantity
    private double totalPrice;

    // Order status - PENDING, VERIFIED, PICKED_UP
    @Enumerated(EnumType.STRING) // saves as text in DB not number
    private OrderStatus status;

    // When order was placed
    private LocalDateTime orderDate;

    // Empty constructor for JPA
    public Order() {}

    // Getters
    public int getId() { return id; }
    public String getDoctorEmail() { return doctorEmail; }
    public int getDrugId() { return drugId; }
    public String getDrugName() { return drugName; }
    public int getQuantity() { return quantity; }
    public double getTotalPrice() { return totalPrice; }
    public OrderStatus getStatus() { return status; }
    public LocalDateTime getOrderDate() { return orderDate; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setDoctorEmail(String doctorEmail) { this.doctorEmail = doctorEmail; }
    public void setDrugId(int drugId) { this.drugId = drugId; }
    public void setDrugName(String drugName) { this.drugName = drugName; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
}