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
public class ChatRoomDto {
    private Long id;
    private Long storeId;
    private String storeName;
    private Long customerId;
    private String customerName;
    private LocalDateTime createdAt;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
}
