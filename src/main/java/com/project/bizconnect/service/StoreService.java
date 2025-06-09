package com.project.bizconnect.service;

import com.project.bizconnect.dto.StoreDto;
import com.project.bizconnect.entity.User;

import java.util.List;

public interface StoreService {
    StoreDto createStore(StoreDto storeDto);
    List<StoreDto> getStoresByAuthenticatedSeller();
    boolean isAuthenticatedUserStoreOwner(Long storeId);
    List<StoreDto> getAllVerifiedStores();
    List<StoreDto> getStoresByIds(List<Long> storeIds, User currentUser);
    List<StoreDto> getAllStoresWithFollowerCounts();
}
