package com.silvestre_lanchonete.auth_service.service;

import com.silvestre_lanchonete.auth_service.dto.PasswordResetTokenDTO;
import com.silvestre_lanchonete.auth_service.domain.PasswordResetToken;
import com.silvestre_lanchonete.auth_service.domain.User;
import com.silvestre_lanchonete.auth_service.repositories.PasswordResetTokenRepository;
import com.silvestre_lanchonete.auth_service.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class PasswordResetService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String token = String.format("%06d", new Random().nextInt(999999));

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(15));

        passwordResetTokenRepository.save(resetToken);

        String message = "<div style='font-family: Arial, sans-serif; color: #333;'>"
                + "<h2 style='color: #ff6600;'>Olá, " + user.getName() + "!</h2>"
                + "<p>Você solicitou a redefinição da sua senha. Use o código abaixo para continuar:</p>"
                + "<h3 style='font-size: 24px; color: #000000; background-color: #f0f0f0; padding: 10px; display: inline-block;'>"
                + token + "</h3>"
                + "<p><strong>⚠️ Atenção:</strong> Este código expira em <strong>15 minutos</strong>.</p>"
                + "<p>Se você não solicitou esta alteração, ignore este e-mail.</p>"
                + "<br>"
                + "<p>Atenciosamente,</p>"
                + "<p><strong>Silvestre Lanchonete</strong></p>"
                + "<img src='https://amzn-s3-silvestre-lanchonete.s3.us-east-1.amazonaws.com/images/logo.png' alt='Logo da Silvestre Lanchonete' style='width: 150px; margin-top: 10px;'>"
                + "</div>";

        emailService.sendEmail(user.getEmail(), "🔐 Redefinição de Senha", message);
    }

    public PasswordResetToken validateCode(String token) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Código inválido ou inexistente"));

        if  (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Código expirado");
        }
        return resetToken;
    }

    public void resetPassword(PasswordResetTokenDTO dto) {
        PasswordResetToken resetToken = validateCode(dto.token());

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(dto.newPassword()));

        userRepository.save(user);
        passwordResetTokenRepository.delete(resetToken);
    }
}
