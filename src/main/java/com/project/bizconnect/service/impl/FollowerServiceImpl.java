package com.project.bizconnect.service.impl;

import com.project.bizconnect.dto.FollowerDto;
import com.project.bizconnect.entity.Store;
import com.project.bizconnect.entity.StoreFollower;
import com.project.bizconnect.entity.User;
import com.project.bizconnect.repository.StoreFollowerRepository;
import com.project.bizconnect.repository.StoreRepository;
import com.project.bizconnect.service.FollowerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowerServiceImpl implements FollowerService {

    private final StoreFollowerRepository followerRepository;
    private final StoreRepository storeRepository;

    @Override
    @Transactional
    public void followStore(Long storeId, User currentUser) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found"));

        // Check if the user already follows this store
        boolean alreadyFollowing = followerRepository.findByStoreAndUser(store, currentUser).isPresent();
        if (alreadyFollowing) {
            // User already follows this store, no action needed
            return;
        }

        // Create new follower relationship
        StoreFollower follower = new StoreFollower();
        follower.setStore(store);
        follower.setUser(currentUser);
        followerRepository.save(follower);
    }

    @Override
    @Transactional
    public void unfollowStore(Long storeId, User currentUser) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found"));

        followerRepository.findByStoreAndUser(store, currentUser)
                .ifPresent(followerRepository::delete);
    }

    @Override
    public boolean isFollowingStore(Long storeId, User currentUser) {
        if (currentUser == null) {
            return false;
        }

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found"));

        return followerRepository.findByStoreAndUser(store, currentUser).isPresent();
    }

    @Override
    public long getFollowerCount(Long storeId) {
        return followerRepository.countByStoreStoreId(storeId);
    }

    @Override
    public List<FollowerDto> getStoreFollowers(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found"));

        return followerRepository.findByStore(store)
                .stream()
                .map(this::mapToFollowerDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> getFollowedStoreIds(User currentUser) {
        return followerRepository.findByUser(currentUser)
                .stream()
                .map(follower -> follower.getStore().getStoreId())
                .collect(Collectors.toList());
    }

    private FollowerDto mapToFollowerDto(StoreFollower follower) {
        FollowerDto dto = new FollowerDto();
        dto.setId(follower.getId());
        dto.setUserId(Long.valueOf(follower.getUser().getId())); // Convert int to Long
        dto.setUsername(follower.getUser().getUsername());
        dto.setEmail(follower.getUser().getEmail());
        dto.setFollowedAt(follower.getFollowedAt());
        return dto;
    }
}
