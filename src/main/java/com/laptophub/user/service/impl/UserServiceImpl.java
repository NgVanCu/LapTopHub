package com.laptophub.user.service.impl;

import com.laptophub.shared.exception.AppException;
import com.laptophub.shared.exception.ErrorCode;
import com.laptophub.user.entity.User;
import com.laptophub.user.repository.UserRepository;
import com.laptophub.user.service.UserService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public User createCustomer(String normalizedEmail, String passwordHash, String fullName, String phone,
                               String emailVerificationTokenHash, Instant emailVerificationExpiresAt) {
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        User user = User.createPendingVerification(normalizedEmail, passwordHash, fullName, phone,
                emailVerificationTokenHash, emailVerificationExpiresAt);

        try {
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }

    @Override
    public Optional<User> findByEmailVerificationTokenHash(String emailVerificationTokenHash) {
        return userRepository.findByEmailVerificationTokenHash(emailVerificationTokenHash);
    }

    @Transactional
    public int verifyEmailIfPending(Long userId) {
        return userRepository.verifyEmailIfPending(userId);
    }

    public Optional<User> findByNormalizedEmail(String normalizedEmail) {
        return userRepository.findByEmail(normalizedEmail);
    }

    @Transactional
    public User reissueEmailVerificationToken(Long userId, String emailVerificationTokenHash,
                                              Instant emailVerificationExpiresAt) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        user.reissueEmailVerificationToken(emailVerificationTokenHash, emailVerificationExpiresAt);
        return userRepository.saveAndFlush(user);
    }
}
