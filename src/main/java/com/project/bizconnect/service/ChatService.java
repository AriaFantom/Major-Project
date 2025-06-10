package com.project.bizconnect.service;

import com.project.bizconnect.dto.ChatMessageDto;
import com.project.bizconnect.dto.ChatRoomDto;
import com.project.bizconnect.entity.ChatMessage;
import com.project.bizconnect.entity.ChatRoom;

import java.util.List;

public interface ChatService {

    // Chat room operations
    ChatRoom createChatRoom(Long storeId, Long customerId);
    List<ChatRoomDto> getCustomerChatRooms(Long customerId);
    List<ChatRoomDto> getStoreChatRooms(Long storeId);
    ChatRoom getChatRoomByStoreAndCustomer(Long storeId, Long customerId);

    // Chat message operations
    ChatMessage saveMessage(ChatMessageDto messageDto);
    List<ChatMessageDto> getChatHistory(Long chatRoomId);
}
