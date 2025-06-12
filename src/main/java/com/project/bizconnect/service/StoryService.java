package com.project.bizconnect.service;

import com.project.bizconnect.dto.StoryDto;
import com.project.bizconnect.entity.Story;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StoryService {
    StoryDto uploadStory(Long storeId, MultipartFile mediaFile, Story.MediaType mediaType, String caption) throws Exception;
    List<StoryDto> getActiveStoriesByStoreId(Long storeId);
    List<StoryDto> getActiveStoriesBySeller();
    void deleteStory(Long storyId);
}
