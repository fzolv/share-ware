package com.fzolv.shareware.hull.security;

import com.fzolv.shareware.core.annotations.NoLogging;
import com.fzolv.shareware.data.entities.UserEntity;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {

    private final KeyProvider keyProvider;

    public JwtService(KeyProvider keyProvider) {
        this.keyProvider = keyProvider;
    }

    public String issueToken(UserEntity user, long ttlSeconds) {
        try {
            Instant now = Instant.now();
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .jwtID(UUID.randomUUID().toString())
                    .subject(user.getEmail())
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plusSeconds(ttlSeconds)))
                    .claim("uid", user.getId().toString())
                    .claim("role", user.getRole() == null ? "MEMBER" : user.getRole().name())
                    .build();

            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .type(JOSEObjectType.JWT)
                    .build();

            SignedJWT jwt = new SignedJWT(header, claims);
            RSASSASigner signer = new RSASSASigner(keyProvider.getPrivateKey());
            jwt.sign(signer);
            return jwt.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("Failed to create JWT", e);
        }
    }

    @NoLogging
    public Map<String, Object> verifyAndParse(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            RSASSAVerifier verifier = new RSASSAVerifier(keyProvider.getPublicKey());
            if (!jwt.verify(verifier)) {
                throw new IllegalArgumentException("Invalid signature");
            }
            if (jwt.getJWTClaimsSet().getExpirationTime() == null || jwt.getJWTClaimsSet().getExpirationTime().before(new Date())) {
                throw new IllegalArgumentException("Token expired");
            }
            return jwt.getJWTClaimsSet().getClaims();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid token", e);
        }
    }
}


