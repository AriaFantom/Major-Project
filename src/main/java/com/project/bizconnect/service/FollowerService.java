package com.project.bizconnect.service;

import com.project.bizconnect.dto.FollowerDto;
import com.project.bizconnect.entity.User;

import java.util.List;

public interface FollowerService {
    // Follow a store
    void followStore(Long storeId, User currentUser);

    // Unfollow a store
    void unfollowStore(Long storeId, User currentUser);

    // Check if current user follows a store
    boolean isFollowingStore(Long storeId, User currentUser);

    // Get follower count for a store
    long getFollowerCount(Long storeId);

    // Get list of followers for a store (for store owners and admins)
    List<FollowerDto> getStoreFollowers(Long storeId);

    // Get list of stores followed by current user
    List<Long> getFollowedStoreIds(User currentUser);
}
