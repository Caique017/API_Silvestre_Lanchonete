package com.silvestre_lanchonete.api.infra.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.silvestre_lanchonete.api.domain.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {

    private final String secret;

    public TokenService(@Value("${token-secret}") String secret) {
        this.secret = secret;
    }

    public String generateToken(User user){
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            String token = JWT.create()
                    .withIssuer("login-auth-api")
                    .withClaim("role", user.getRole().name())
                    .withSubject(user.getEmail())
                    .withExpiresAt(this.generateExpirationDate(1))
                    .sign(algorithm);
            return token;
        } catch (JWTCreationException exception){
            throw new RuntimeException("Error while authenticating");
        }
    }

    public String generateRefreshToken(User user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            String token = JWT.create()
                    .withIssuer("login-auth-api")
                    .withClaim("role", user.getRole().name())
                    .withSubject(user.getId().toString())
                    .withExpiresAt(this.generateExpirationDate(3))
                    .sign(algorithm);
            return token;
        } catch (JWTCreationException exception){
            throw new RuntimeException("Error while authenticating");
        }
    }

        public String validateToken (String token){
            try {
                Algorithm algorithm = Algorithm.HMAC256(secret);
                return JWT.require(algorithm)
                        .withIssuer("login-auth-api")
                        .build()
                        .verify(token)
                        .getSubject();
            } catch (JWTVerificationException exception) {
                return null;
            }
        }

        private Instant generateExpirationDate (Integer hours) {
            return LocalDateTime.now().plusHours(hours).toInstant(ZoneOffset.of("-03:00"));
        }
    }