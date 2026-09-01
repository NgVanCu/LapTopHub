package com.laptophub.user.service.impl;

import com.laptophub.security.currentuser.CurrentUserProvider;
import com.laptophub.shared.exception.AppException;
import com.laptophub.shared.exception.ErrorCode;
import com.laptophub.user.dto.request.AddressCreateRequest;
import com.laptophub.user.dto.request.AddressUpdateRequest;
import com.laptophub.user.entity.Address;
import com.laptophub.user.repository.AddressRepository;
import com.laptophub.user.service.AddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final CurrentUserProvider currentUserProvider;
    public AddressServiceImpl(AddressRepository addressRepository, CurrentUserProvider currentUserProvider) {
        this.addressRepository = addressRepository;
        this.currentUserProvider = currentUserProvider;
    }

    public List<Address> list() {
        return addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(currentUserProvider.getCurrentUser().userId());
    }

    public Address getOwned(Long addressId) {
        return addressRepository.findByIdAndUserId(addressId, currentUserProvider.getCurrentUser().userId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Transactional
    public Address create(AddressCreateRequest request) {
        Long userId = currentUserProvider.getCurrentUser().userId();
        boolean shouldBeDefault = Boolean.TRUE.equals(request.isDefault()) || addressRepository.countByUserId(userId) == 0;

        if (shouldBeDefault) {
            addressRepository.findByUserIdAndIsDefaultTrue(userId).ifPresent(Address::unmarkAsDefault);
        }

        Address address = Address.create(userId, request.recipientName(), request.phone(), request.province(),
                 request.ward(), request.streetAddress(), shouldBeDefault);
        return addressRepository.save(address);
    }

    @Transactional
    public Address update(Long addressId, AddressUpdateRequest request) {
        Address address = getOwned(addressId);
        address.update(request.recipientName(), request.phone(), request.province(),
                request.ward(), request.streetAddress());
        return address;
    }

    @Transactional
    public void delete(Long addressId) {
        Address address = getOwned(addressId);
        addressRepository.delete(address);
    }

    @Transactional
    public Address setDefault(Long addressId) {
        Long userId =  currentUserProvider.getCurrentUser().userId();
        Address target = getOwned(addressId);

        addressRepository.findByUserIdAndIsDefaultTrue(userId)
                .filter(current -> !current.getId().equals(target.getId()))
                .ifPresent(Address::unmarkAsDefault);

        target.markAsDefault();
        return target;
    }
}
