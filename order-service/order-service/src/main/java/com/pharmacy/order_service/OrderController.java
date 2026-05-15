package com.pharmacy.order_service;

import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@Tag(name = "Order Management", description = "APIs for placing and managing drug orders")
@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // POST /orders - Doctor places an order
    // Request body contains drugId, quantity, doctorEmail
    @Operation(summary = "Place order", description = "Doctor places a drug order")
    @PostMapping
    public Order placeOrder(@RequestBody OrderRequest request) {
        return orderService.placeOrder(request);
    }

    // GET /orders - Admin views all orders
    @Operation(summary = "Get all orders", description = "Admin views all orders")
    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    // GET /orders/1 - Get one order by id
    @Operation(summary = "Get order by ID")
    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable int id) {
        return orderService.getOrderById(id);
    }

    // GET /orders/doctor?email=doctor@pharmacy.com
    // Doctor views their own orders
    @Operation(summary = "Get orders by doctor email")
    @GetMapping("/doctor")
    public List<Order> getOrdersByDoctor(@RequestParam String email) {
        return orderService.getOrdersByDoctor(email);
    }

    // PUT /orders/1/verify - Admin verifies an order
    @Operation(summary = "Verify order", description = "Admin verifies a PENDING order")
    @PutMapping("/{id}/verify")
    public Order verifyOrder(@PathVariable int id) {
        return orderService.verifyOrder(id);
    }

    // PUT /orders/1/pickup - Admin marks order as picked up
    @Operation(summary = "Mark as picked up", description = "Admin marks VERIFIED order as PICKED_UP")
    @PutMapping("/{id}/pickup")
    public Order markAsPickedUp(@PathVariable int id) {
        return orderService.markAsPickedUp(id);
    }
}