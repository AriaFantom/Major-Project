package com.project.bizconnect.service.impl;

import com.project.bizconnect.dto.AddressDto;
import com.project.bizconnect.entity.Address;
import com.project.bizconnect.entity.User;
import com.project.bizconnect.repository.AddressRepository;
import com.project.bizconnect.service.AddressService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;

    @Override
    public List<AddressDto> getAllAddressesByUser(User user) {
        return addressRepository.findByUser(user)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public AddressDto getAddressById(Integer addressId, User user) {
        Address address = addressRepository.findByIdAndUser(addressId, user)
                .orElseThrow(() -> new EntityNotFoundException("Address not found with id: " + addressId));
        return convertToDto(address);
    }

    @Override
    @Transactional
    public AddressDto createAddress(AddressDto addressDto, User user) {
        Address address = convertToEntity(addressDto, user);

        // Handle default address
        if (Boolean.TRUE.equals(addressDto.getDefaultAddress())) {
            resetDefaultAddresses(user);
        }

        Address savedAddress = addressRepository.save(address);
        return convertToDto(savedAddress);
    }

    @Override
    @Transactional
    public AddressDto updateAddress(Integer addressId, AddressDto addressDto, User user) {
        Address address = addressRepository.findByIdAndUser(addressId, user)
                .orElseThrow(() -> new EntityNotFoundException("Address not found with id: " + addressId));

        address.setFullName(addressDto.getFullName());
        address.setStreetAddress(addressDto.getStreetAddress());
        address.setCity(addressDto.getCity());
        address.setState(addressDto.getState());
        address.setZipCode(addressDto.getZipCode());
        address.setCountry(addressDto.getCountry());

        // Handle default address
        if (Boolean.TRUE.equals(addressDto.getDefaultAddress()) && !address.getDefaultAddress()) {
            resetDefaultAddresses(user);
            address.setDefaultAddress(true);
        }

        Address savedAddress = addressRepository.save(address);
        return convertToDto(savedAddress);
    }

    @Override
    @Transactional
    public void deleteAddress(Integer addressId, User user) {
        Address address = addressRepository.findByIdAndUser(addressId, user)
                .orElseThrow(() -> new EntityNotFoundException("Address not found with id: " + addressId));

        addressRepository.delete(address);
    }

    @Override
    @Transactional
    public AddressDto setDefaultAddress(Integer addressId, User user) {
        resetDefaultAddresses(user);

        Address address = addressRepository.findByIdAndUser(addressId, user)
                .orElseThrow(() -> new EntityNotFoundException("Address not found with id: " + addressId));

        address.setDefaultAddress(true);
        Address savedAddress = addressRepository.save(address);

        return convertToDto(savedAddress);
    }

    private void resetDefaultAddresses(User user) {
        addressRepository.findByUserAndDefaultAddressIsTrue(user)
                .ifPresent(defaultAddress -> {
                    defaultAddress.setDefaultAddress(false);
                    addressRepository.save(defaultAddress);
                });
    }

    private AddressDto convertToDto(Address address) {
        AddressDto addressDto = new AddressDto();
        addressDto.setId(address.getId());
        addressDto.setFullName(address.getFullName());
        addressDto.setStreetAddress(address.getStreetAddress());
        addressDto.setCity(address.getCity());
        addressDto.setState(address.getState());
        addressDto.setZipCode(address.getZipCode());
        addressDto.setCountry(address.getCountry());
        addressDto.setDefaultAddress(address.getDefaultAddress());
        return addressDto;
    }

    private Address convertToEntity(AddressDto addressDto, User user) {
        Address address = new Address();
        address.setFullName(addressDto.getFullName());
        address.setStreetAddress(addressDto.getStreetAddress());
        address.setCity(addressDto.getCity());
        address.setState(addressDto.getState());
        address.setZipCode(addressDto.getZipCode());
        address.setCountry(addressDto.getCountry());
        address.setDefaultAddress(addressDto.getDefaultAddress());
        address.setUser(user);
        return address;
    }
}
