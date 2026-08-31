package com.laptophub.auth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "refresh_token_hash", nullable = false, unique = true, length = 64)
    private String refreshTokenHash;

    @Column(name = "family_id", nullable = false, length = 36)
    private String familyId;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "replaced_by_token_id")
    private Long replacedByTokenId;

    @Enumerated(EnumType.STRING)
    @Column(name = "revoke_reason", length = 30)
    private RevokeReason revokeReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    private RefreshToken(Long userId, String tokenHash, String familyId, Instant expiresAt, Instant createdAt) {
        this.userId = Objects.requireNonNull(userId, "userId không được để trống");
        this.refreshTokenHash = Objects.requireNonNull(tokenHash, "refreshTokenHash không được để trống");
        this.familyId = Objects.requireNonNull(familyId, "familyId không được để trống");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt không được để trống");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt không được để trống");
    }

    public static RefreshToken create(Long userId, String tokenHash, String familyId, Instant expiresAt,
                                      Instant createdAt) {
        return new RefreshToken(userId, tokenHash, familyId, expiresAt, createdAt);
    }

    public void revoke(Instant revokedAt, RevokeReason reason) {
        this.revokedAt = Objects.requireNonNull(revokedAt, "revokedAt không được để trống");
        this.revokeReason = Objects.requireNonNull(reason, "reason không được để trống");
    }

    public void markReplacedBy(Long newTokenId) {
        this.replacedByTokenId = Objects.requireNonNull(newTokenId, "newTokenId không được để trống");
    }
}
