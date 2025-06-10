package com.project.bizconnect.service;

import com.project.bizconnect.entity.ChatRoom;
import com.project.bizconnect.entity.User;

public interface ChatSecurityService {

    // Validate if the user is allowed to access a specific chat room
    boolean canAccessChatRoom(User user, ChatRoom chatRoom);

    // Validate if the customer can create a chat room with the store
    boolean canCreateChatRoom(Long customerId, Long storeId);

    // Validate if the user can send messages in the chat room
    boolean canSendMessage(User user, Long chatRoomId);
}
