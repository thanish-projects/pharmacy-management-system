package com.pharmacy.report_service;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
// This maps to the EXISTING 'orders' table created by Order Service
// We are only reading from it, not creating a new table
@Table(name = "orders")
public class SalesOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String doctorEmail;   // Which doctor placed the order
    private int drugId;           // Drug that was ordered
    private String drugName;      // Drug name for easy display in report
    private int quantity;         // How many units
    private double totalPrice;    // Revenue from this order

    @Enumerated(EnumType.STRING)
    private OrderStatus status;   // PENDING, VERIFIED, PICKED_UP

    private LocalDateTime orderDate;  // When order was placed

    public SalesOrder() {}

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
