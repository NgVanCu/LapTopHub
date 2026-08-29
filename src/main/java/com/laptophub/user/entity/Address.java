package com.laptophub.user.entity;

import com.laptophub.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Table(name = "addresses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "recipient_name", nullable = false, length = 255)
    private String recipientName;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "province", nullable = false, length = 255)
    private String province;

    @Column(name = "ward", nullable = false, length = 255)
    private String ward;

    @Column(name = "street_address", nullable = false, length = 500)
    private String streetAddress;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    private Address(Long userId, String recipientName, String phone, String province,
                    String ward, String streetAddress, boolean isDefault) {
        this.userId = Objects.requireNonNull(userId, "ID người dùng không được để trống");
        this.recipientName = Objects.requireNonNull(recipientName,  "Tên người nhận không được để trống");
        this.phone = Objects.requireNonNull(phone, "Số điện thoại không được để trống");
        this.province = Objects.requireNonNull(province, "Tỉnh/thành phố không được để trống");
        this.ward = Objects.requireNonNull(ward, "Phường/xã không được để trống");
        this.streetAddress = Objects.requireNonNull(streetAddress, "Địa chỉ đường phố không được để trống");
        this.isDefault = isDefault;
    }

    public static Address create(Long userId, String recipientName, String phone, String province,
                                  String ward, String streetAddress, boolean isDefault) {
        return new Address(userId, recipientName, phone, province, ward, streetAddress, isDefault);
    }

    public void update(String recipientName, String phone, String province,  String ward,
                       String streetAddress) {
        this.recipientName = Objects.requireNonNull(recipientName,  "Tên người nhận không được để trống");
        this.phone = Objects.requireNonNull(phone, "Số điện thoại không được để trống");
        this.province = Objects.requireNonNull(province, "Tỉnh/thành phố không được để trống");
        this.ward = Objects.requireNonNull(ward, "Phường/xã không được để trống");
        this.streetAddress = Objects.requireNonNull(streetAddress, "Địa chỉ đường phố không được để trống");
    }

    public void markAsDefault() {
        this.isDefault = true;
    }

    public void unmarkAsDefault() {
        this.isDefault = false;
    }
}
