package com.project.bizconnect.repository;

import com.project.bizconnect.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    List<ChatRoom> findByStoreId(Long storeId);
    List<ChatRoom> findByCustomerId(Long customerId);
    ChatRoom findByStoreIdAndCustomerId(Long storeId, Long customerId);
}

