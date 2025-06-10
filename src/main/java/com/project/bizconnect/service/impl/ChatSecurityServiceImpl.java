package com.project.bizconnect.service.impl;

import com.project.bizconnect.entity.ChatRoom;
import com.project.bizconnect.entity.Role;
import com.project.bizconnect.entity.User;
import com.project.bizconnect.repository.ChatRoomRepository;
import com.project.bizconnect.service.ChatSecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatSecurityServiceImpl implements ChatSecurityService {

    private final ChatRoomRepository chatRoomRepository;

    @Override
    public boolean canAccessChatRoom(User user, ChatRoom chatRoom) {
        if (user == null || chatRoom == null) {
            return false;
        }

        // Convert user ID to Long for comparison
        Long userId = Long.valueOf(user.getId());

        // Check if the user is either the customer or the store owner
        if (user.getRole() == Role.CUSTOMER) {
            return chatRoom.getCustomerId().equals(userId);
        } else if (user.getRole() == Role.SELLER) {
            return chatRoom.getStoreId().equals(userId);
        }

        return false;
    }

    @Override
    public boolean canCreateChatRoom(Long customerId, Long storeId) {
        // Check if a chat room already exists for this customer and store
        ChatRoom existingRoom = chatRoomRepository.findByStoreIdAndCustomerId(storeId, customerId);

        // If a room doesn't exist, the customer can create one
        return existingRoom == null;
    }

    @Override
    public boolean canSendMessage(User user, Long chatRoomId) {
        if (user == null || chatRoomId == null) {
            return false;
        }

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElse(null);
        if (chatRoom == null) {
            return false;
        }

        return canAccessChatRoom(user, chatRoom);
    }
}
