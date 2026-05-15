package com.pharmacy.order_service;

// Enum means this field can only have these 3 values
// PENDING = just placed, not verified yet
// VERIFIED = admin verified it's valid
// PICKED_UP = doctor picked up the drugs
public enum OrderStatus {
    PENDING,
    VERIFIED,
    PICKED_UP
}