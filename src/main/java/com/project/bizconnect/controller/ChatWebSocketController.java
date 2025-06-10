package com.project.bizconnect.controller;

import com.project.bizconnect.dto.ChatMessageDto;
import com.project.bizconnect.entity.ChatMessage;
import com.project.bizconnect.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageDto chatMessageDto) {
        log.info("✉️ WebSocket Message Received - From: {} (ID: {}), Room: {}, Content: '{}'",
                chatMessageDto.getSenderType(), chatMessageDto.getSenderId(),
                chatMessageDto.getChatRoomId(), chatMessageDto.getContent());

        // Validate sender type
        if (!("STORE".equals(chatMessageDto.getSenderType()) || "CUSTOMER".equals(chatMessageDto.getSenderType()))) {
            log.error("Invalid sender type: {}", chatMessageDto.getSenderType());
            return;
        }

        // Save message to database
        ChatMessage savedMessage = chatService.saveMessage(chatMessageDto);
        log.info("✓ Message saved to database with ID: {}", savedMessage.getId());

        // Convert saved message back to DTO
        ChatMessageDto savedDto = ChatMessageDto.builder()
                .id(savedMessage.getId())
                .chatRoomId(savedMessage.getChatRoom().getId())
                .senderType(savedMessage.getSenderType())
                .senderId(savedMessage.getSenderId())
                .senderName(savedMessage.getSenderType() + " " + savedMessage.getSenderId()) // Simplified display name
                .content(savedMessage.getContent())
                .timestamp(savedMessage.getTimestamp())
                .build();

        // Send to specific chat room topic
        String destination = "/topic/chat." + chatMessageDto.getChatRoomId();
        messagingTemplate.convertAndSend(destination, savedDto);
        log.info("➡️ WebSocket Message Sent - To Topic: {}, Message ID: {}", destination, savedMessage.getId());
    }
}
