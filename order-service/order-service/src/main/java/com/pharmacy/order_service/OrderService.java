package com.pharmacy.order_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private DrugClient drugClient;

    public Order placeOrder(OrderRequest request) {
        log.info("Doctor {} is placing order for drug id: {}",
            request.getDoctorEmail(), request.getDrugId());

        Drug drug = drugClient.getDrugById(request.getDrugId());
        log.info("Fetched drug: {} with stock: {}", drug.getName(), drug.getStock());

        if (drug.getStock() < request.getQuantity()) {
            log.warn("Insufficient stock for drug: {}. Available: {}, Requested: {}",
                drug.getName(), drug.getStock(), request.getQuantity());
            throw new IllegalArgumentException(
                "Not enough stock! Available: " + drug.getStock() +
                ", Requested: " + request.getQuantity());
        }

        Order order = new Order();
        order.setDoctorEmail(request.getDoctorEmail());
        order.setDrugId(drug.getId());
        order.setDrugName(drug.getName());
        order.setQuantity(request.getQuantity());
        order.setTotalPrice(drug.getPrice() * request.getQuantity());
        order.setStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());

        drug.setStock(drug.getStock() - request.getQuantity());
        drugClient.updateDrug(drug.getId(), drug);
        log.info("Stock reduced for drug: {}. New stock: {}", drug.getName(), drug.getStock());

        Order saved = orderRepository.save(order);
        log.info("Order placed successfully with id: {} and status: PENDING", saved.getId());
        return saved;
    }

    public List<Order> getAllOrders() {
        log.info("Fetching all orders");
        return orderRepository.findAll();
    }

    public Order getOrderById(int id) {
        log.info("Fetching order with id: {}", id);
        return orderRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Order not found with id: {}", id);
                return new ResourceNotFoundException("Order not found with id: " + id);
            });
    }

    public List<Order> getOrdersByDoctor(String doctorEmail) {
        log.info("Fetching orders for doctor: {}", doctorEmail);
        return orderRepository.findByDoctorEmail(doctorEmail);
    }

    public Order verifyOrder(int id) {
        log.info("Admin verifying order with id: {}", id);
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Order not found with id: {}", id);
                return new ResourceNotFoundException("Order not found with id: " + id);
            });

        if (order.getStatus() != OrderStatus.PENDING) {
            log.warn("Cannot verify order id: {}. Current status: {}", id, order.getStatus());
            throw new IllegalArgumentException(
                "Order cannot be verified. Current status: " + order.getStatus() +
                ". Only PENDING orders can be verified.");
        }

        order.setStatus(OrderStatus.VERIFIED);
        Order updated = orderRepository.save(order);
        log.info("Order id: {} verified successfully", id);
        return updated;
    }

    public Order markAsPickedUp(int id) {
        log.info("Admin marking order id: {} as picked up", id);
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Order not found with id: {}", id);
                return new ResourceNotFoundException("Order not found with id: " + id);
            });

        if (order.getStatus() != OrderStatus.VERIFIED) {
            log.warn("Cannot mark order id: {} as picked up. Current status: {}",
                id, order.getStatus());
            throw new IllegalArgumentException(
                "Order cannot be marked as picked up. Current status: " + order.getStatus() +
                ". Only VERIFIED orders can be picked up.");
        }

        order.setStatus(OrderStatus.PICKED_UP);
        Order updated = orderRepository.save(order);
        log.info("Order id: {} marked as PICKED_UP successfully", id);
        return updated;
    }
}