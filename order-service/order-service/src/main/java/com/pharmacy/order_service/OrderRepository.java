package com.pharmacy.order_service;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    // Custom method - find all orders by a specific doctor
    // Spring generates SQL: SELECT * FROM orders WHERE doctor_email = ?
    List<Order> findByDoctorEmail(String doctorEmail);

    // Custom method - find all orders by status
    // Spring generates SQL: SELECT * FROM orders WHERE status = ?
    List<Order> findByStatus(OrderStatus status);
}