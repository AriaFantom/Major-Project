package com.project.bizconnect.service.impl;

import com.project.bizconnect.dto.ChatMessageDto;
import com.project.bizconnect.dto.ChatRoomDto;
import com.project.bizconnect.entity.ChatMessage;
import com.project.bizconnect.entity.ChatRoom;
import com.project.bizconnect.entity.Role;
import com.project.bizconnect.entity.User;
import com.project.bizconnect.repository.ChatMessageRepository;
import com.project.bizconnect.repository.ChatRoomRepository;
import com.project.bizconnect.repository.UsersRepository;
import com.project.bizconnect.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UsersRepository usersRepository;

    @Override
    @Transactional
    public ChatRoom createChatRoom(Long storeId, Long customerId) {
        // Check if a chat room already exists between this store and customer
        ChatRoom existingRoom = chatRoomRepository.findByStoreIdAndCustomerId(storeId, customerId);
        if (existingRoom != null) {
            return existingRoom;
        }

        // Create new chat room
        ChatRoom chatRoom = ChatRoom.builder()
                .storeId(storeId)
                .customerId(customerId)
                .createdAt(LocalDateTime.now())
                .build();

        return chatRoomRepository.save(chatRoom);
    }

    @Override
    public List<ChatRoomDto> getCustomerChatRooms(Long customerId) {
        List<ChatRoom> chatRooms = chatRoomRepository.findByCustomerId(customerId);
        return convertToChatRoomDtos(chatRooms);
    }

    @Override
    public List<ChatRoomDto> getStoreChatRooms(Long storeId) {
        List<ChatRoom> chatRooms = chatRoomRepository.findByStoreId(storeId);
        return convertToChatRoomDtos(chatRooms);
    }

    @Override
    public ChatRoom getChatRoomByStoreAndCustomer(Long storeId, Long customerId) {
        return chatRoomRepository.findByStoreIdAndCustomerId(storeId, customerId);
    }

    @Override
    @Transactional
    public ChatMessage saveMessage(ChatMessageDto messageDto) {
        ChatRoom chatRoom = chatRoomRepository.findById(messageDto.getChatRoomId())
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .senderType(messageDto.getSenderType())
                .senderId(messageDto.getSenderId())
                .content(messageDto.getContent())
                .timestamp(LocalDateTime.now())
                .build();

        return chatMessageRepository.save(chatMessage);
    }

    @Override
    public List<ChatMessageDto> getChatHistory(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        List<ChatMessage> messages = chatMessageRepository.findByChatRoomOrderByTimestampAsc(chatRoom);

        return messages.stream()
                .map(this::convertToMessageDto)
                .collect(Collectors.toList());
    }

    private List<ChatRoomDto> convertToChatRoomDtos(List<ChatRoom> chatRooms) {
        List<ChatRoomDto> chatRoomDtos = new ArrayList<>();

        for (ChatRoom chatRoom : chatRooms) {
            // Get the latest message (if any)
            List<ChatMessage> messages = chatMessageRepository.findByChatRoomOrderByTimestampAsc(chatRoom);
            String lastMessage = "";
            LocalDateTime lastMessageTime = chatRoom.getCreatedAt();

            if (!messages.isEmpty()) {
                ChatMessage latestMessage = messages.get(messages.size() - 1);
                lastMessage = latestMessage.getContent();
                lastMessageTime = latestMessage.getTimestamp();
            }

            // Find user names for better display
            String customerName = "Customer " + chatRoom.getCustomerId();
            String storeName = "Store " + chatRoom.getStoreId();

            ChatRoomDto dto = ChatRoomDto.builder()
                    .id(chatRoom.getId())
                    .storeId(chatRoom.getStoreId())
                    .customerId(chatRoom.getCustomerId())
                    .storeName(storeName)
                    .customerName(customerName)
                    .createdAt(chatRoom.getCreatedAt())
                    .lastMessage(lastMessage)
                    .lastMessageTime(lastMessageTime)
                    .build();

            chatRoomDtos.add(dto);
        }

        return chatRoomDtos;
    }

    private ChatMessageDto convertToMessageDto(ChatMessage message) {
        // Get sender name based on type and ID
        String senderName = message.getSenderType().equals("STORE")
            ? "Store " + message.getSenderId()
            : "Customer " + message.getSenderId();

        return ChatMessageDto.builder()
                .id(message.getId())
                .chatRoomId(message.getChatRoom().getId())
                .senderType(message.getSenderType())
                .senderId(message.getSenderId())
                .senderName(senderName)
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .build();
    }
}
