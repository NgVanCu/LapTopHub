package com.laptophub.security.service;

import com.laptophub.user.entity.User;
import com.laptophub.user.enums.UserRole;
import com.laptophub.user.enums.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public final class UserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String passwordHash;
    private final UserRole role;
    private final boolean enabled;
    private final boolean pendingVerification;

    private UserPrincipal(Long id, String email, String passwordHash, UserRole role, boolean enabled,
                          boolean pendingVerification) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.enabled = enabled;
        this.pendingVerification = pendingVerification;
    }

    public static UserPrincipal from(User user) {
        // Chỉ BLOCKED mới disabled — PENDING_VERIFICATION vẫn "enabled" theo
        // nghĩa Spring Security để DaoAuthenticationProvider vẫn so mật khẩu
        // bình thường (sai mật khẩu ra đúng BadCredentialsException/
        // INVALID_CREDENTIALS, không bị che bởi lỗi trạng thái tài khoản).
        // LoginService tự kiểm tra pendingVerification tường minh SAU KHI
        // authenticate() thành công, TRƯỚC KHI cấp JWT — không dùng cơ chế
        // isAccountNonLocked()/LockedException của Spring Security cho việc
        // này (giữ nguyên semantics UserDetails sẵn có).
        boolean enabled = user.getStatus() != UserStatus.BLOCKED;
        boolean pendingVerification = user.getStatus() == UserStatus.PENDING_VERIFICATION;
        return new UserPrincipal(user.getId(), user.getEmail(), user.getPassword(), user.getRole(), enabled,
                pendingVerification);
    }


    public Long getId() {
        return id;
    }

    public UserRole getRole() {
        return role;
    }


    public boolean isPendingVerification() {
        return pendingVerification;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
