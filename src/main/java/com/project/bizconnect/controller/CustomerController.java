package com.project.bizconnect.controller;

import com.project.bizconnect.dto.AddressDto;
import com.project.bizconnect.dto.ChatRoomDto;
import com.project.bizconnect.dto.StoreDto;
import com.project.bizconnect.dto.StoryDto;
import com.project.bizconnect.entity.ChatRoom;
import com.project.bizconnect.entity.User;
import com.project.bizconnect.service.AddressService;
import com.project.bizconnect.service.ChatService;
import com.project.bizconnect.service.FollowerService;
import com.project.bizconnect.service.StoreService;
import com.project.bizconnect.service.StoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final AddressService addressService;
    private final FollowerService followerService;
    private final StoreService storeService;
    private final ChatService chatService;
    private final StoryService storyService;

    @GetMapping()
    public ResponseEntity<String> getCustomer() {
        return ResponseEntity.ok("customer route");
    }

    // Address management endpoints
    @GetMapping("/addresses")
    public ResponseEntity<List<AddressDto>> getAllAddresses(@AuthenticationPrincipal User user) {
        List<AddressDto> addresses = addressService.getAllAddressesByUser(user);
        return ResponseEntity.ok(addresses);
    }

    @GetMapping("/addresses/{id}")
    public ResponseEntity<AddressDto> getAddressById(@PathVariable Integer id, @AuthenticationPrincipal User user) {
        AddressDto address = addressService.getAddressById(id, user);
        return ResponseEntity.ok(address);
    }

    @PostMapping("/addresses")
    public ResponseEntity<AddressDto> createAddress(@Valid @RequestBody AddressDto addressDto, @AuthenticationPrincipal User user) {
        AddressDto createdAddress = addressService.createAddress(addressDto, user);
        return new ResponseEntity<>(createdAddress, HttpStatus.CREATED);
    }

    @PutMapping("/addresses/{id}")
    public ResponseEntity<AddressDto> updateAddress(@PathVariable Integer id, @Valid @RequestBody AddressDto addressDto,
                                                   @AuthenticationPrincipal User user) {
        AddressDto updatedAddress = addressService.updateAddress(id, addressDto, user);
        return ResponseEntity.ok(updatedAddress);
    }

    @DeleteMapping("/addresses/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Integer id, @AuthenticationPrincipal User user) {
        addressService.deleteAddress(id, user);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/addresses/{id}/default")
    public ResponseEntity<AddressDto> setDefaultAddress(@PathVariable Integer id, @AuthenticationPrincipal User user) {
        AddressDto updatedAddress = addressService.setDefaultAddress(id, user);
        return ResponseEntity.ok(updatedAddress);
    }

    // Store following endpoints
    @PostMapping("/stores/{storeId}/follow")
    public ResponseEntity<Map<String, String>> followStore(
            @PathVariable Long storeId,
            @AuthenticationPrincipal User currentUser) {
        followerService.followStore(storeId, currentUser);
        return ResponseEntity.ok(Map.of("message", "Store followed successfully"));
    }

    @DeleteMapping("/stores/{storeId}/unfollow")
    public ResponseEntity<Map<String, String>> unfollowStore(
            @PathVariable Long storeId,
            @AuthenticationPrincipal User currentUser) {
        followerService.unfollowStore(storeId, currentUser);
        return ResponseEntity.ok(Map.of("message", "Store unfollowed successfully"));
    }

    @GetMapping("/stores/following")
    public ResponseEntity<List<StoreDto>> getFollowedStores(@AuthenticationPrincipal User currentUser) {
        List<Long> followedStoreIds = followerService.getFollowedStoreIds(currentUser);
        List<StoreDto> followedStores = storeService.getStoresByIds(followedStoreIds, currentUser);
        return ResponseEntity.ok(followedStores);
    }

    @GetMapping("/stores/{storeId}/following")
    public ResponseEntity<Map<String, Boolean>> checkFollowingStatus(
            @PathVariable Long storeId,
            @AuthenticationPrincipal User currentUser) {
        boolean isFollowing = followerService.isFollowingStore(storeId, currentUser);
        return ResponseEntity.ok(Map.of("following", isFollowing));
    }

    // New endpoint to fetch stories from followed stores
    @GetMapping("/stories/following")
    public ResponseEntity<List<StoryDto>> getFollowedStoresStories(@AuthenticationPrincipal User currentUser) {
        // Get all store IDs that the current user is following
        List<Long> followedStoreIds = followerService.getFollowedStoreIds(currentUser);

        // Use the StoryService to fetch active stories from these stores
        List<StoryDto> followedStoresStories = new ArrayList<>();
        for (Long storeId : followedStoreIds) {
            List<StoryDto> storeStories = storyService.getActiveStoriesByStoreId(storeId);
            followedStoresStories.addAll(storeStories);
        }

        return ResponseEntity.ok(followedStoresStories);
    }

    // Chat related endpoints

    // Endpoint for customer to create a chat room with a store
    @PostMapping("/chat/rooms/create")
    public ResponseEntity<?> createChatRoom(
            @RequestParam Long storeId,
            @AuthenticationPrincipal User currentUser
    ) {
        Long customerId = Long.valueOf(currentUser.getId());

        // Check if a chat room already exists
        ChatRoom existingRoom = chatService.getChatRoomByStoreAndCustomer(storeId, customerId);
        if (existingRoom != null) {
            return ResponseEntity.ok(existingRoom);
        }

        // Create new chat room
        ChatRoom chatRoom = chatService.createChatRoom(storeId, customerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(chatRoom);
    }

    // Get all chat rooms for the authenticated customer
    @GetMapping("/chat/rooms")
    public ResponseEntity<List<ChatRoomDto>> getCustomerChatRooms(
            @AuthenticationPrincipal User currentUser
    ) {
        Long customerId = Long.valueOf(currentUser.getId());
        List<ChatRoomDto> chatRooms = chatService.getCustomerChatRooms(customerId);
        return ResponseEntity.ok(chatRooms);
    }
}
