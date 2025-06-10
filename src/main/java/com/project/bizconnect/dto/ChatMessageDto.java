package com.project.bizconnect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {
    private Long id;
    private Long chatRoomId;
    private String senderType; // "STORE" or "CUSTOMER"
    private Long senderId;
    private String senderName; // For display purposes
    private String content;
    private LocalDateTime timestamp;
}
