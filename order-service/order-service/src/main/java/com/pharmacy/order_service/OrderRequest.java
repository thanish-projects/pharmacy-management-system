package com.pharmacy.order_service;

// DTO = Data Transfer Object
// This is what the doctor sends in the request body when placing an order
// We use a separate class instead of Order entity
// because doctor shouldn't set status, price, date - system does that
public class OrderRequest {
    private int drugId;
    private int quantity;
    private String doctorEmail;

    public int getDrugId() { return drugId; }
    public int getQuantity() { return quantity; }
    public String getDoctorEmail() { return doctorEmail; }

    public void setDrugId(int drugId) { this.drugId = drugId; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setDoctorEmail(String doctorEmail) { this.doctorEmail = doctorEmail; }
}