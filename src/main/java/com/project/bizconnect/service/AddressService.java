package com.project.bizconnect.service;

import com.project.bizconnect.dto.AddressDto;
import com.project.bizconnect.entity.Address;
import com.project.bizconnect.entity.User;

import java.util.List;

public interface AddressService {
    List<AddressDto> getAllAddressesByUser(User user);
    AddressDto getAddressById(Integer addressId, User user);
    AddressDto createAddress(AddressDto addressDto, User user);
    AddressDto updateAddress(Integer addressId, AddressDto addressDto, User user);
    void deleteAddress(Integer addressId, User user);
    AddressDto setDefaultAddress(Integer addressId, User user);
}
