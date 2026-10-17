package com.campnav.backend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {
    private static final String DEFAULT_DEV_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    private final Environment environment;

    @Value("${jwt.secret:${JWT_SECRET:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}}")
    private String secretKey;

    @PostConstruct
    public void validateSecretKey() {
        boolean isProduction = environment.acceptsProfiles(Profiles.of("prod"));

        if (isProduction) {
            if (secretKey == null || secretKey.isBlank() || DEFAULT_DEV_SECRET.equals(secretKey)) {
                throw new IllegalStateException("Production startup failed: A strong JWT_SECRET environment variable is required and cannot be empty or use default development keys.");
            }
            try {
                byte[] keyBytes = Decoders.BASE64.decode(secretKey);
                if (keyBytes.length < 32) {
                    throw new IllegalStateException("Production startup failed: JWT_SECRET must be at least 256 bits (32 bytes) long when base64 decoded.");
                }
            } catch (IllegalArgumentException e) {
                if (secretKey.length() < 32) {
                    throw new IllegalStateException("Production startup failed: JWT_SECRET must be a valid base64 string or at least 32 characters long.");
                }
            }
        } else {
            if (secretKey == null || secretKey.isBlank()) {
                secretKey = DEFAULT_DEV_SECRET;
            }
            if (DEFAULT_DEV_SECRET.equals(secretKey)) {
                System.out.println("WARNING: Using default development JWT secret. Set JWT_SECRET in production!");
            }
        }
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secretKey);
        } catch (Exception e) {
            keyBytes = secretKey.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
