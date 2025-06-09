package com.project.bizconnect.repository;

import com.project.bizconnect.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProductProductId(Long productId);
}
