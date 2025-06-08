package com.project.bizconnect.repository;

import com.project.bizconnect.entity.MainOrder;
import com.project.bizconnect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MainOrderRepository extends JpaRepository<MainOrder, Long> {
    List<MainOrder> findByCustomerOrderByCreatedAtDesc(User customer);
}
