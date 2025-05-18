package com.project.bizconnect.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Double totalAmount;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Many orders can be placed by one user (customer)
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    // Many orders belong to one store (the seller fulfilling the order)
    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;


    // One order has many order items
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items = new ArrayList<>();

}