package com.laptophub.user.dto.response;

import com.laptophub.user.entity.Address;

import java.time.Instant;

public record AddressResponse(
        Long id,
        String recipientName,
        String phone,
        String province,
        String ward,
        String streetAddress,
        boolean isDefault,
        Instant createdAt,
        Instant updatedAt) {

    public static AddressResponse from(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getRecipientName(),
                address.getPhone(),
                address.getProvince(),
                address.getWard(),
                address.getStreetAddress(),
                address.isDefault(),
                address.getCreatedAt(),
                address.getUpdatedAt());
    }
}
