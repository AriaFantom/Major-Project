package com.project.bizconnect.dto;

import com.project.bizconnect.entity.Story;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoryDto {
    private Long id;
    private Long storeId;
    private String storeName;
    private String storeImageUrl; // Added field for store image
    private String mediaUrl;
    private Story.MediaType mediaType;
    private String caption;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}
