package com.laptophub.user.repository;

import com.laptophub.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByEmailVerificationTokenHash(String emailVerificationTokenHash);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.status = 'ACTIVE', u.emailVerificationTokenHash = NULL, "
            + "u.emailVerificationExpiresAt = NULL WHERE u.id = :id AND u.status = 'PENDING_VERIFICATION'")
    int verifyEmailIfPending(@Param("id") Long id);
}
