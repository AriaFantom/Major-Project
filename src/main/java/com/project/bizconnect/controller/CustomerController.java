package com.project.bizconnect.controller;

import com.project.bizconnect.dto.AddressDto;
import com.project.bizconnect.entity.User;
import com.project.bizconnect.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final AddressService addressService;

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
}
