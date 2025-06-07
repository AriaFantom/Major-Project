package com.project.bizconnect.entity;

public enum OrderStatus {
    PENDING,     // Order is created but not yet processed
    PROCESSING,  // After payment is complete, being prepared
    SHIPPED,     // Order has been shipped
    DELIVERED,   // Order has been delivered
    CANCELLED    // Order was cancelled
}
