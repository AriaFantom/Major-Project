package com.project.bizconnect.controller;

import com.project.bizconnect.dto.StoreDto;
import com.project.bizconnect.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seller")
@RequiredArgsConstructor
public class SellerController {

    private final StoreService storeService;

    @PostMapping("/stores")
    public ResponseEntity<StoreDto> createStore(@RequestBody StoreDto storeDto) {
        StoreDto created = storeService.createStore(storeDto);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<String> getSeller() {
        return ResponseEntity.ok("seller route");
    }
}
