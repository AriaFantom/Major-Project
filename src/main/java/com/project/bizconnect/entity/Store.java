package com.project.bizconnect.entity;

import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "store")
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long storeId;

    @Column(name = "store_name")
    private String storeName;

    private String description;

    private boolean isVerified = false;

    @OneToOne
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User owner;




}
