package com.laptophub.auth.repository;

import com.laptophub.auth.entity.RefreshToken;
import com.laptophub.auth.entity.RevokeReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByRefreshTokenHash(String refreshToken);


    @Modifying(clearAutomatically = true)
    @Query("UPDATE RefreshToken r SET r.revokedAt = :revokedAt, r.revokeReason = :reason "
            + "WHERE r.userId = :userId AND r.revokedAt IS NULL")
    int revokeActiveByUserId(@Param("userId") Long userId,
                             @Param("revokedAt") Instant revokedAt,
                             @Param("reason") RevokeReason reason);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE RefreshToken r SET r.revokedAt = :revokedAt, r.revokeReason = :reason "
            + "WHERE r.familyId = :familyId AND r.revokedAt IS NULL")
    int revokeActiveByFamilyId(@Param("familyId") String familyId,
                               @Param("revokedAt") Instant revokedAt,
                               @Param("reason") RevokeReason reason);
}
