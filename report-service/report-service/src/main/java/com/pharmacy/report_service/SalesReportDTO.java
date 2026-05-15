package com.pharmacy.report_service;

import java.util.List;

// DTO = Data Transfer Object
// This is what gets sent back when someone requests a sales report
// It wraps the list of sales + total revenue in one object
public class SalesReportDTO {

    private List<SalesOrder> sales;       // All completed orders
    private double totalRevenue;           // Sum of all sales
    private int totalOrdersCompleted;      // Count of completed orders

    public SalesReportDTO() {}

    public SalesReportDTO(List<SalesOrder> sales, double totalRevenue) {
        this.sales = sales;
        this.totalRevenue = totalRevenue;
        this.totalOrdersCompleted = sales.size();
    }

    // Getters
    public List<SalesOrder> getSales() { return sales; }
    public double getTotalRevenue() { return totalRevenue; }
    public int getTotalOrdersCompleted() { return totalOrdersCompleted; }

    // Setters
    public void setSales(List<SalesOrder> sales) { this.sales = sales; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }
    public void setTotalOrdersCompleted(int total) { this.totalOrdersCompleted = total; }
}
