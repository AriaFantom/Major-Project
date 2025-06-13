package com.project.bizconnect.service.impl;

import com.project.bizconnect.dto.StoryDto;
import com.project.bizconnect.entity.Story;
import com.project.bizconnect.entity.Store;
import com.project.bizconnect.entity.User;
import com.project.bizconnect.repository.StoryRepository;
import com.project.bizconnect.repository.StoreRepository;
import com.project.bizconnect.service.FileStorageService;
import com.project.bizconnect.service.StoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoryServiceImpl implements StoryService {

    private final StoryRepository storyRepository;
    private final StoreRepository storeRepository;
    private final FileStorageService fileStorageService;

    @Override
    public StoryDto uploadStory(Long storeId, MultipartFile mediaFile, Story.MediaType mediaType, String caption) throws Exception {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        if (store.getOwner().getId() != currentUser.getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only add stories to stores you own");
        }

        validateMediaFile(mediaFile, mediaType);
        String mediaUrl = fileStorageService.uploadFile(mediaFile);
        Story story = Story.builder()
                .store(store)
                .mediaUrl(mediaUrl)
                .mediaType(mediaType)
                .caption(caption)
                .build();

        story.prePersist();
        Story savedStory = storyRepository.save(story);
        return mapToStoryDto(savedStory);
    }

    @Override
    public List<StoryDto> getActiveStoriesByStoreId(Long storeId) {
        List<Story> stories = storyRepository.findActiveStoriesByStoreId(storeId, LocalDateTime.now());
        return stories.stream()
                .map(this::mapToStoryDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<StoryDto> getActiveStoriesBySeller() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        // Convert int to Long when calling the repository method
        List<Story> stories = storyRepository.findActiveStoriesBySellerId(
                Long.valueOf(currentUser.getId()), LocalDateTime.now());
        return stories.stream()
                .map(this::mapToStoryDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteStory(Long storyId) {
        // Verify story exists
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Story not found"));

        // Check if authenticated user is the owner of the store
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        if (story.getStore().getOwner().getId() != currentUser.getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only delete stories from stores you own");
        }

        // Delete story
        storyRepository.delete(story);
    }

    private StoryDto mapToStoryDto(Story story) {
        // Get the store image URL
        String storeImageUrl = null;
        if (story.getStore().getImageUrl() != null) {
            storeImageUrl = org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/shop/products/images/")
                    .path(story.getStore().getImageUrl())
                    .toUriString();
        }

        return StoryDto.builder()
                .id(story.getId())
                .storeId(story.getStore().getStoreId())
                .storeName(story.getStore().getStoreName())
                .storeImageUrl(storeImageUrl) // Using the full URL with updated path
                .mediaUrl(story.getMediaUrl())
                .mediaType(story.getMediaType())
                .caption(story.getCaption())
                .createdAt(story.getCreatedAt())
                .expiresAt(story.getExpiresAt())
                .build();
    }

    private void validateMediaFile(MultipartFile file, Story.MediaType mediaType) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File cannot be empty");
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Content type is missing");
        }

        // Validate based on media type
        if (mediaType == Story.MediaType.IMAGE) {
            if (!contentType.startsWith("image/")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid image format. Supported formats: JPEG, PNG, GIF, etc.");
            }
        } else if (mediaType == Story.MediaType.VIDEO) {
            if (!contentType.startsWith("video/")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid video format. Supported formats: MP4, MOV, AVI, etc.");
            }
        }
    }
}
