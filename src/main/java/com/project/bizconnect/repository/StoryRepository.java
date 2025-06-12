package com.project.bizconnect.repository;

import com.project.bizconnect.entity.Story;
import com.project.bizconnect.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StoryRepository extends JpaRepository<Story, Long> {
    List<Story> findByStoreAndExpiresAtGreaterThan(Store store, LocalDateTime now);

    @Query("SELECT s FROM Story s WHERE s.store.storeId = :storeId AND s.expiresAt > :now")
    List<Story> findActiveStoriesByStoreId(Long storeId, LocalDateTime now);

    @Query("SELECT s FROM Story s WHERE s.store.owner.id = :sellerId AND s.expiresAt > :now")
    List<Story> findActiveStoriesBySellerId(Long sellerId, LocalDateTime now);
}
