package com.project.bizconnect.service.impl;

import com.project.bizconnect.dto.StoreDto;
import com.project.bizconnect.entity.Store;
import com.project.bizconnect.entity.User;
import com.project.bizconnect.repository.StoreRepository;
import com.project.bizconnect.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;

    private StoreDto mapToDto(Store store) {
        StoreDto dto = new StoreDto();
        dto.setStoreId(store.getStoreId());
        dto.setStoreName(store.getStoreName());
        dto.setDescription(store.getDescription());
        dto.setVerified(store.isVerified());
        dto.setOwnerUserId(Long.valueOf(store.getOwner().getId()));
        dto.setEmail(store.getEmail());
        dto.setPhoneNumber(store.getPhoneNumber());
        dto.setAddress(store.getAddress());
        dto.setWebsiteUrl(store.getWebsiteUrl());
        dto.setCreatedAt(store.getCreatedAt());
        dto.setUpdatedAt(store.getUpdatedAt());
        return dto;
    }

    @Override
    public StoreDto createStore(StoreDto storeDto) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof User)) {
            throw new IllegalStateException("Unauthorized");
        }
        User user = (User) principal;
        Store store = new Store();
        store.setStoreName(storeDto.getStoreName());
        store.setDescription(storeDto.getDescription());
        store.setOwner(user);
        store.setEmail(storeDto.getEmail());
        store.setPhoneNumber(storeDto.getPhoneNumber());
        store.setAddress(storeDto.getAddress());
        store.setWebsiteUrl(storeDto.getWebsiteUrl());
        Store saved = storeRepository.save(store);
        return mapToDto(saved);
    }
}
