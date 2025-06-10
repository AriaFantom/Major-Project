package com.project.bizconnect.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long storeId;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}

