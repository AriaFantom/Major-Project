package com.project.bizconnect.controller;

import com.project.bizconnect.dto.ChatMessageDto;
import com.project.bizconnect.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // Get chat history for a specific chat room
    @GetMapping("/history/{chatRoomId}")
    public ResponseEntity<List<ChatMessageDto>> getChatHistory(
            @PathVariable Long chatRoomId
    ) {
        List<ChatMessageDto> messages = chatService.getChatHistory(chatRoomId);
        return ResponseEntity.ok(messages);
    }
}
