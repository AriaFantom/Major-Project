package com.project.bizconnect.entity;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private int stockQuantity;

    // Many products belong to one store
    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    // Many products belong to one category (nullable)
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    // One product can have multiple images
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    // Reviews for this product
//    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
//    private List<Review> reviews = new ArrayList<>();
}
