package com.project.bizconnect.dto;

import com.project.bizconnect.entity.Story;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoryUploadRequestDto {
    private Long storeId;
    private Story.MediaType mediaType;
}
