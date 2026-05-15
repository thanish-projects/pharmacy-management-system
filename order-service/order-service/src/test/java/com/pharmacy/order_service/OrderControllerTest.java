package com.pharmacy.order_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = OrderController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class
    }
)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    private Order sampleOrder;
    private OrderRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleOrder = new Order();
        sampleOrder.setId(1);
        sampleOrder.setDoctorEmail("doctor@pharmacy.com");
        sampleOrder.setDrugId(1);
        sampleOrder.setDrugName("Paracetamol");
        sampleOrder.setQuantity(5);
        sampleOrder.setTotalPrice(250.0);
        sampleOrder.setStatus(OrderStatus.PENDING);
        sampleOrder.setOrderDate(LocalDateTime.now());

        sampleRequest = new OrderRequest();
        sampleRequest.setDrugId(1);
        sampleRequest.setQuantity(5);
        sampleRequest.setDoctorEmail("doctor@pharmacy.com");
    }

    @Test
    void placeOrder_ShouldReturn200WithOrder() throws Exception {
        when(orderService.placeOrder(any(OrderRequest.class))).thenReturn(sampleOrder);

        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andExpect(jsonPath("$.doctorEmail").value("doctor@pharmacy.com"))
            .andExpect(jsonPath("$.totalPrice").value(250.0));
    }

    @Test
    void placeOrder_WhenInsufficientStock_ShouldReturn400() throws Exception {
        when(orderService.placeOrder(any(OrderRequest.class)))
            .thenThrow(new IllegalArgumentException("Not enough stock! Available: 3, Requested: 5"));

        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getAllOrders_ShouldReturn200WithList() throws Exception {
        when(orderService.getAllOrders()).thenReturn(Arrays.asList(sampleOrder));

        mockMvc.perform(get("/orders"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void getOrderById_WhenExists_ShouldReturn200() throws Exception {
        when(orderService.getOrderById(1)).thenReturn(sampleOrder);

        mockMvc.perform(get("/orders/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.drugName").value("Paracetamol"));
    }

    @Test
    void getOrderById_WhenNotFound_ShouldReturn404() throws Exception {
        when(orderService.getOrderById(99))
            .thenThrow(new ResourceNotFoundException("Order not found with id: 99"));

        mockMvc.perform(get("/orders/99"))
            .andExpect(status().isNotFound());
    }

    @Test
    void getOrdersByDoctor_ShouldReturn200WithList() throws Exception {
        when(orderService.getOrdersByDoctor("doctor@pharmacy.com"))
            .thenReturn(Arrays.asList(sampleOrder));

        // @RequestParam email — passed as query param ?email=...
        mockMvc.perform(get("/orders/doctor")
                .param("email", "doctor@pharmacy.com"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].doctorEmail").value("doctor@pharmacy.com"));
    }

    @Test
    void verifyOrder_WhenPending_ShouldReturn200() throws Exception {
        sampleOrder.setStatus(OrderStatus.VERIFIED);
        when(orderService.verifyOrder(1)).thenReturn(sampleOrder);

        mockMvc.perform(put("/orders/1/verify"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("VERIFIED"));
    }

    @Test
    void verifyOrder_WhenInvalidStatus_ShouldReturn400() throws Exception {
        when(orderService.verifyOrder(1))
            .thenThrow(new IllegalArgumentException(
                "Order cannot be verified. Current status: VERIFIED"));

        mockMvc.perform(put("/orders/1/verify"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void markAsPickedUp_WhenVerified_ShouldReturn200() throws Exception {
        sampleOrder.setStatus(OrderStatus.PICKED_UP);
        when(orderService.markAsPickedUp(1)).thenReturn(sampleOrder);

        mockMvc.perform(put("/orders/1/pickup"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PICKED_UP"));
    }

    @Test
    void markAsPickedUp_WhenInvalidStatus_ShouldReturn400() throws Exception {
        when(orderService.markAsPickedUp(1))
            .thenThrow(new IllegalArgumentException(
                "Order cannot be marked as picked up. Current status: PENDING"));

        mockMvc.perform(put("/orders/1/pickup"))
            .andExpect(status().isBadRequest());
    }
}