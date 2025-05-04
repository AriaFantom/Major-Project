package com.project.bizconnect.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;
import java.util.*;

@Getter
@Setter
@Entity
@Table(name = "order")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @Column(nullable = false)
    private Date orderDate;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Double totalAmount;

    // Many orders can be placed by one user (customer)
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    // Many orders belong to one store (the seller fulfilling the order)
    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    // If a saved payment method was used
//    @ManyToOne
//    @JoinColumn(name = "payment_method_id")
//    private PaymentMethod paymentMethod;

    // One order has many order items
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items = new ArrayList<>();

}