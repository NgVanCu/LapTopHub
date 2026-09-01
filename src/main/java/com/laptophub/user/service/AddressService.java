package com.laptophub.user.service;

import com.laptophub.user.dto.request.AddressCreateRequest;
import com.laptophub.user.dto.request.AddressUpdateRequest;
import com.laptophub.user.entity.Address;

import java.util.List;

public interface AddressService {
    List<Address> list();

    Address getOwned(Long addressId);

    Address create(AddressCreateRequest request);

    Address update(Long addressId, AddressUpdateRequest request);

    void delete(Long addressId);

    Address setDefault(Long addressId);
}
