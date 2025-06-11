package com.project.bizconnect.service;

import com.project.bizconnect.dto.StoreDto;
import com.project.bizconnect.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface StoreService {
    StoreDto createStore(StoreDto storeDto);
    StoreDto createStoreWithImage(StoreDto storeDto, MultipartFile image) throws Exception;
    List<StoreDto> getStoresByAuthenticatedSeller();
    boolean isAuthenticatedUserStoreOwner(Long storeId);
    List<StoreDto> getAllVerifiedStores();
    List<StoreDto> getStoresByIds(List<Long> storeIds, User currentUser);
    List<StoreDto> getAllStoresWithFollowerCounts();
    Optional<StoreDto> getVerifiedStoreById(Long storeId); // New method for getting a specific verified store
    List<StoreDto> getStoresByMostFollowers(Integer limit); // New method to get stores ordered by follower count
}
