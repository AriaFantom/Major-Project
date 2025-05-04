package com.project.bizconnect.entity;
import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "product_variants")
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long variantId;

    private String name;    // e.g. "Red, Size M"

    private Double price;

    @Column(nullable = false)
    private int stockQuantity;

    private String sku;

    // Many variants belong to one product
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
