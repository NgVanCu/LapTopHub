package com.laptophub.user.service.impl;

import com.laptophub.shared.exception.AppException;
import com.laptophub.shared.exception.ErrorCode;
import com.laptophub.user.entity.User;
import com.laptophub.user.enums.UserRole;
import com.laptophub.user.enums.UserStatus;
import com.laptophub.user.repository.UserRepository;
import com.laptophub.user.service.UserService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;
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

    @Override
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

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    @Transactional
    public User updateProfile(Long userId, String fullName, String phone){
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));
        user.updateProfile(fullName, phone);
        return user;
    }

    @Override
    public Page<User> search(UserRole role, UserStatus status, String keyword, Pageable pageable) {
        return userRepository.search(role, status, keyword, pageable);
    }

    @Override
    @Transactional
    public User blockUser(Long actingAdminId, Long targetUserId) {
        if (Objects.equals(actingAdminId, targetUserId)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR);
        }
        User user = userRepository.findById(targetUserId).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        user.block();
        return user;
    }

    @Override
    @Transactional
    public User activateUser(Long targetUserId) {
        User user = userRepository.findById(targetUserId).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        if (user.getStatus() == UserStatus.PENDING_VERIFICATION) {
            throw new AppException(ErrorCode.VALIDATION_ERROR);
        }
        user.activate();
        return user;
    }

    @Override
    @Transactional
    public User createAdmin(String normalizedEmail, String passwordHash, String fullName) {
        User user = User.create(normalizedEmail, passwordHash, fullName, null, UserRole.ADMIN);
        return userRepository.saveAndFlush(user);
    }
}
