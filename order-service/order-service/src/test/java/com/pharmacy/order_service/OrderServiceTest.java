package com.pharmacy.order_service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    // DrugClient is a Feign interface — mocking it means no real HTTP calls
    // to Inventory Service happen during tests. Order Service tests should
    // not depend on another service being up and running.
    @Mock
    private DrugClient drugClient;

    @InjectMocks
    private OrderService orderService;

    private Drug sampleDrug;
    private Order sampleOrder;
    private OrderRequest sampleRequest;

    @BeforeEach
    void setUp() {
        // Sample drug returned by Feign client
        sampleDrug = new Drug();
        sampleDrug.setId(1);
        sampleDrug.setName("Paracetamol");
        sampleDrug.setCategory("Painkiller");
        sampleDrug.setPrice(50.0);
        sampleDrug.setStock(100);

        // Sample order request from doctor
        sampleRequest = new OrderRequest();
        sampleRequest.setDrugId(1);
        sampleRequest.setQuantity(5);
        sampleRequest.setDoctorEmail("doctor@pharmacy.com");

        // Sample saved order
        sampleOrder = new Order();
        sampleOrder.setId(1);
        sampleOrder.setDoctorEmail("doctor@pharmacy.com");
        sampleOrder.setDrugId(1);
        sampleOrder.setDrugName("Paracetamol");
        sampleOrder.setQuantity(5);
        sampleOrder.setTotalPrice(250.0);
        sampleOrder.setStatus(OrderStatus.PENDING);
        sampleOrder.setOrderDate(LocalDateTime.now());
    }

    // ===== placeOrder() tests =====

    @Test
    void placeOrder_WhenStockSufficient_ShouldCreatePendingOrder() {
        // Drug has 100 stock, request is for 5 — sufficient
        when(drugClient.getDrugById(1)).thenReturn(sampleDrug);
        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);

        Order result = orderService.placeOrder(sampleRequest);

        assertNotNull(result);
        assertEquals(OrderStatus.PENDING, result.getStatus());
        assertEquals("doctor@pharmacy.com", result.getDoctorEmail());
        assertEquals(250.0, result.getTotalPrice());

        // Verify stock was updated in Inventory Service via Feign
        // This is critical — if someone removes the stock update call,
        // stock never gets reduced and drugs can be over-ordered
        verify(drugClient, times(1)).updateDrug(eq(1), any(Drug.class));
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void placeOrder_WhenStockInsufficient_ShouldThrowException() {
        // Only 3 stock available, doctor requests 5
        sampleDrug.setStock(3);
        when(drugClient.getDrugById(1)).thenReturn(sampleDrug);

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> orderService.placeOrder(sampleRequest)
        );

        assertTrue(ex.getMessage().contains("Not enough stock"));

        // If stock insufficient, order should never be saved
        verify(orderRepository, never()).save(any());

        // Stock should never be updated either
        verify(drugClient, never()).updateDrug(anyInt(), any(Drug.class));
    }

    @Test
    void placeOrder_ShouldCalculateTotalPriceCorrectly() {
        // price=50, quantity=5, expected total=250
        when(drugClient.getDrugById(1)).thenReturn(sampleDrug);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            // invocation.getArgument(0) captures the actual Order object passed to save()
            // This lets us inspect what was built before saving
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(1);
            return savedOrder;
        });

        Order result = orderService.placeOrder(sampleRequest);

        // 50.0 price x 5 quantity = 250.0
        assertEquals(250.0, result.getTotalPrice());
    }

    @Test
    void placeOrder_ShouldReduceStockByQuantity() {
        when(drugClient.getDrugById(1)).thenReturn(sampleDrug);
        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);

        orderService.placeOrder(sampleRequest);

        // Capture what drug was sent to updateDrug()
        // Verify stock was reduced from 100 to 95 (100 - 5)
        verify(drugClient).updateDrug(eq(1), argThat(drug -> drug.getStock() == 95));
    }

    // ===== getAllOrders() tests =====

    @Test
    void getAllOrders_ShouldReturnAllOrders() {
        when(orderRepository.findAll()).thenReturn(Arrays.asList(sampleOrder));

        List<Order> result = orderService.getAllOrders();

        assertEquals(1, result.size());
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void getAllOrders_WhenEmpty_ShouldReturnEmptyList() {
        when(orderRepository.findAll()).thenReturn(Arrays.asList());

        List<Order> result = orderService.getAllOrders();

        assertTrue(result.isEmpty());
    }

    // ===== getOrderById() tests =====

    @Test
    void getOrderById_WhenExists_ShouldReturnOrder() {
        when(orderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));

        Order result = orderService.getOrderById(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(OrderStatus.PENDING, result.getStatus());
    }

    @Test
    void getOrderById_WhenNotFound_ShouldThrowException() {
        when(orderRepository.findById(99)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
            ResourceNotFoundException.class,
            () -> orderService.getOrderById(99)
        );

        assertTrue(ex.getMessage().contains("99"));
    }

    // ===== getOrdersByDoctor() tests =====

    @Test
    void getOrdersByDoctor_ShouldReturnDoctorOrders() {
        when(orderRepository.findByDoctorEmail("doctor@pharmacy.com"))
            .thenReturn(Arrays.asList(sampleOrder));

        List<Order> result = orderService.getOrdersByDoctor("doctor@pharmacy.com");

        assertEquals(1, result.size());
        assertEquals("doctor@pharmacy.com", result.get(0).getDoctorEmail());
        verify(orderRepository, times(1)).findByDoctorEmail("doctor@pharmacy.com");
    }

    // ===== verifyOrder() tests =====

    @Test
    void verifyOrder_WhenPending_ShouldChangeStatusToVerified() {
        // Order is PENDING — can be verified
        sampleOrder.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);

        Order result = orderService.verifyOrder(1);

        // After verifyOrder, status should be VERIFIED
        assertEquals(OrderStatus.VERIFIED, result.getStatus());
        verify(orderRepository, times(1)).save(sampleOrder);
    }

    @Test
    void verifyOrder_WhenAlreadyVerified_ShouldThrowException() {
        // Order is already VERIFIED — cannot verify again
        sampleOrder.setStatus(OrderStatus.VERIFIED);
        when(orderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> orderService.verifyOrder(1)
        );

        assertTrue(ex.getMessage().contains("VERIFIED"));

        // save() should never be called if status transition is invalid
        verify(orderRepository, never()).save(any());
    }

    @Test
    void verifyOrder_WhenPickedUp_ShouldThrowException() {
        // Already PICKED_UP — cannot go back to VERIFIED
        sampleOrder.setStatus(OrderStatus.PICKED_UP);
        when(orderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));

        assertThrows(
            IllegalArgumentException.class,
            () -> orderService.verifyOrder(1)
        );

        verify(orderRepository, never()).save(any());
    }

    @Test
    void verifyOrder_WhenNotFound_ShouldThrowException() {
        when(orderRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> orderService.verifyOrder(99)
        );
    }

    // ===== markAsPickedUp() tests =====

    @Test
    void markAsPickedUp_WhenVerified_ShouldChangeStatusToPickedUp() {
        // Only VERIFIED orders can be picked up
        sampleOrder.setStatus(OrderStatus.VERIFIED);
        when(orderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);

        Order result = orderService.markAsPickedUp(1);

        assertEquals(OrderStatus.PICKED_UP, result.getStatus());
        verify(orderRepository, times(1)).save(sampleOrder);
    }

    @Test
    void markAsPickedUp_WhenPending_ShouldThrowException() {
        // PENDING cannot skip to PICKED_UP — must be VERIFIED first
        sampleOrder.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> orderService.markAsPickedUp(1)
        );

        assertTrue(ex.getMessage().contains("PENDING"));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void markAsPickedUp_WhenNotFound_ShouldThrowException() {
        when(orderRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> orderService.markAsPickedUp(99)
        );
    }
}