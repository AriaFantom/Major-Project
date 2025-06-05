package com.project.bizconnect.service;

import com.project.bizconnect.dto.StoreDto;

import java.util.List;

public interface StoreService {
    StoreDto createStore(StoreDto storeDto);
    List<StoreDto> getStoresByAuthenticatedSeller();
}
