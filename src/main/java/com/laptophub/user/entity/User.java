package com.laptophub.user.entity;

import com.laptophub.shared.entity.BaseEntity;
import com.laptophub.user.enums.UserRole;
import com.laptophub.user.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Column(name = "email", nullable = false, length = 255, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String password;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status;

    private User(String email, String passwordHash, String fullName, String phone, UserRole role) {
        this.email = Objects.requireNonNull(email, "Email không được để trống");
        this.password = Objects.requireNonNull(passwordHash, "Mật khẩu không được để trống");
        this.fullName = Objects.requireNonNull(fullName, "Họ và tên không được để trống");
        this.phone = phone;
        this.role = Objects.requireNonNull(role, "Vai trò không được để trống");
        this.status = UserStatus.ACTIVE;
    }

    public static User create(String email, String passwordHash, String fullName, String phone, UserRole role) {
        return new User(email, passwordHash, fullName, phone, role);
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    public void block() {
        this.status = UserStatus.BLOCKED;
    }

    public void changePasswordHash(String newPasswordHash) {
        if (newPasswordHash == null || newPasswordHash.isBlank()) {
            throw new IllegalArgumentException("Mật khẩu mới không được để trống");
        }
        this.password = newPasswordHash;
    }

    public void updateProfile(String fullName, String phone) {
        this.fullName = Objects.requireNonNull(fullName, "Họ và tên không được để trống");
        this.phone = phone;
    }
}
