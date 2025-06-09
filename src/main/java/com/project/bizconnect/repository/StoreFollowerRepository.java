package com.project.bizconnect.repository;

import com.project.bizconnect.entity.Store;
import com.project.bizconnect.entity.StoreFollower;
import com.project.bizconnect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StoreFollowerRepository extends JpaRepository<StoreFollower, Long> {

    // Find if a user follows a store
    Optional<StoreFollower> findByStoreAndUser(Store store, User user);

    // Count followers for a store
    long countByStore(Store store);

    // Count followers for a store by ID
    long countByStoreStoreId(Long storeId);

    // Get all stores followed by a user
    List<StoreFollower> findByUser(User user);

    // Get all followers for a store
    List<StoreFollower> findByStore(Store store);

    // Delete a follow relationship
    void deleteByStoreAndUser(Store store, User user);
}
