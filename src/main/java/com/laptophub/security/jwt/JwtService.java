package com.laptophub.security.jwt;

import com.laptophub.security.service.UserPrincipal;
import com.laptophub.shared.properties.JwtProperties;
import com.laptophub.user.entity.User;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class JwtService {
    private final JwtProperties jwtProperties;
    private final JwtEncoder jwtEncoder;
    public JwtService(JwtProperties jwtProperties, JwtEncoder jwtEncoder) {
        this.jwtProperties = jwtProperties;
        this.jwtEncoder = jwtEncoder;
    }

    public String generateAccessToken(UserPrincipal principal) {
        Instant now = Instant.now();
        String email = principal.getUsername();
        String scope = principal.getRole().name();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(email)
                .id(UUID.randomUUID().toString())
                .issuer("self")
                .issuedAt(now)
                .claim("userId", principal.getId())
                .expiresAt(now.plus(jwtProperties.accessTokenExpiration()))
                .claim("scope", scope)
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(header,claims)).getTokenValue();
    }

    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }
}
