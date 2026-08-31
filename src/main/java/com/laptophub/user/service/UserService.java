package com.laptophub.user.service;

import com.laptophub.user.entity.User;

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
}
