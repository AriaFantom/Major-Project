package com.project.bizconnect.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "stories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Story {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    private String mediaUrl;

    @Enumerated(EnumType.STRING)
    private MediaType mediaType;

    @Column(length = 500)
    private String caption;

    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    // Stores will expire stories after 24 hours
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.expiresAt = createdAt.plusHours(24);
    }

    public enum MediaType {
        IMAGE, VIDEO
    }
}
