package com.project.bizconnect.repository;

import com.project.bizconnect.entity.Category;
import com.project.bizconnect.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByStore(Store store);

    // Search method for categories by name (case insensitive, partial match)
    @Query("SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Category> findByNameContainingIgnoreCase(@Param("searchTerm") String searchTerm);
}
