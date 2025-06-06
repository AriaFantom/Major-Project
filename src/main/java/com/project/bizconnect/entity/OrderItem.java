package com.project.bizconnect.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "order_item")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderItemId;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private Double price;

    // Many order items belong to one order
    @ManyToOne
    @JoinColumn(name = "orders_id", nullable = false)
    private Order order;

    // The product that was ordered
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}