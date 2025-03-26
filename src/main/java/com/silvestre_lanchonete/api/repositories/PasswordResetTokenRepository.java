package com.silvestre_lanchonete.api.repositories;

import com.silvestre_lanchonete.api.domain.passwordResetToken.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, String> {
    Optional<PasswordResetToken> findByToken(String token);
}
