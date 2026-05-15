package com.pharmacy.report_service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface SalesReportRepository extends JpaRepository<SalesOrder, Integer> {

    // Spring generates: SELECT * FROM orders WHERE status = 'PICKED_UP'
    // Only completed (picked up) orders count as sales
    List<SalesOrder> findByStatus(OrderStatus status);

    // Custom JPQL query to calculate total revenue
    // SUM(o.totalPrice) adds up all totalPrice values
    // WHERE o.status = 'PICKED_UP' only counts completed orders
    @Query("SELECT SUM(o.totalPrice) FROM SalesOrder o WHERE o.status = 'PICKED_UP'")
    Double getTotalRevenue();

    // Get sales for a specific drug by name
    List<SalesOrder> findByDrugNameAndStatus(String drugName, OrderStatus status);
}
