package com.laptophub.user.service;

import com.laptophub.user.entity.User;
import com.laptophub.user.enums.UserRole;
import com.laptophub.user.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Optional;

public interface UserService {
    User createCustomer(String normalizedEmail, String passwordHash, String fullName, String phone,
                        String emailVerificationTokenHash, Instant emailVerificationExpiresAt);

    Optional<User> findByEmailVerificationTokenHash(String emailVerificationTokenHash);

    int verifyEmailIfPending(Long userId);

    Optional<User> findByNormalizedEmail(String normalizedEmail);

    User reissueEmailVerificationToken(Long userId, String emailVerificationTokenHash,
                                       Instant emailVerificationExpiresAt);
    Optional<User> findById(Long id);

    User updateProfile(Long userId, String fullName, String phone);

    Page<User> search(UserRole role, UserStatus status, String keyword, Pageable pageable);

    User blockUser(Long actingAdminId, Long targetUserId);

    User activateUser(Long targetUserId);

    User createAdmin(String normalizedEmail, String passwordHash, String fullName);
}
