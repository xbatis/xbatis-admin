package com.xbatis.auth.security;

import com.xbatis.commons.exception.BusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtTokenService {

    @Value("${app.security.jwt-secret}")
    private String jwtSecret;

    @Value("${app.security.access-token-expire-minutes:720}")
    private long expireMinutes;

    private SecretKey secretKey;

    @PostConstruct
    void init() {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public TokenPayload createToken(Long userId, String username) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(expireMinutes, ChronoUnit.MINUTES);
        String token = Jwts.builder()
                .subject(username)
                .claim("uid", userId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
        return new TokenPayload(token, expiresAt.toEpochMilli());
    }

    public Long parseUserId(String token) {
        return parseClaims(token).get("uid", Long.class);
    }

    public String parseUsername(String token) {
        return parseClaims(token).getSubject();
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException ex) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, 4011, "登录已过期，请重新登录");
        } catch (JwtException | IllegalArgumentException ex) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, 4012, "无效的访问令牌");
        }
    }

    public record TokenPayload(String accessToken, long expiresAt) {
    }
}
