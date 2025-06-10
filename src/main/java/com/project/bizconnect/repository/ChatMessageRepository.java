package com.project.bizconnect.repository;

import com.project.bizconnect.entity.ChatMessage;
import com.project.bizconnect.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatRoomOrderByTimestampAsc(ChatRoom chatRoom);
}

