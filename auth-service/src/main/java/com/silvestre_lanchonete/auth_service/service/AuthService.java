package com.silvestre_lanchonete.auth_service.service;

import com.silvestre_lanchonete.auth_service.domain.Role;
import com.silvestre_lanchonete.auth_service.dto.DataRefreshTokenDTO;
import com.silvestre_lanchonete.auth_service.dto.LoginRequestDTO;
import com.silvestre_lanchonete.auth_service.dto.RegisterRequestDTO;
import com.silvestre_lanchonete.auth_service.dto.ResponseDTO;
import com.silvestre_lanchonete.auth_service.domain.User;
import com.silvestre_lanchonete.auth_service.infra.exceptions.InvalidTokenException;
import com.silvestre_lanchonete.auth_service.infra.exceptions.UserAlreadyExistsException;
import com.silvestre_lanchonete.auth_service.infra.exceptions.UserNotFoundException;
import com.silvestre_lanchonete.auth_service.infra.security.TokenService;
import com.silvestre_lanchonete.auth_service.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, TokenService tokenService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    public ResponseDTO login(LoginRequestDTO body) {
        User user = this.userRepository.findByEmail(body.email())
                .orElseThrow(() -> new UserNotFoundException("E-mail ou senha incorretos."));
        if (!passwordEncoder.matches(body.password(), user.getPassword())) {
            throw new UserNotFoundException("E-mail ou senha incorretos.");
        }
        String token = this.tokenService.generateToken(user);
        String refreshToken = this.tokenService.generateRefreshToken(user);
        return new ResponseDTO(user.getName(), token, refreshToken);
    }

    public ResponseDTO updateToken(DataRefreshTokenDTO data) {
        String refreshToken = data.refreshToken();
        String subject = tokenService.validateToken(refreshToken);
        if (subject == null || subject.isEmpty()) {
            throw new InvalidTokenException("Refresh Token inválido ou expirado.");
        }

        UUID idUser = UUID.fromString(subject);
        User user = userRepository.findById(idUser)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado para este token."));

        String token = this.tokenService.generateToken(user);
        String newRefreshToken = this.tokenService.generateRefreshToken(user);

        return new ResponseDTO(user.getName(), token, newRefreshToken);
    }

    public ResponseDTO register(RegisterRequestDTO data) {
        if (userRepository.findByEmail(data.email()).isPresent()) {
            throw new UserAlreadyExistsException("O e-mail " + data.email() + " já está em uso.");
        }
        User newUser = new User();
        newUser.setName(data.name());
        newUser.setEmail(data.email());
        newUser.setPassword(passwordEncoder.encode(data.password()));
        if (this.userRepository.count() == 0) {
            newUser.setRole(Role.Administrador);
        } else {
            newUser.setRole(Role.Usuario);
        }
        userRepository.save(newUser);

        String token = this.tokenService.generateToken(newUser);
        String refreshToken = this.tokenService.generateRefreshToken(newUser);
        return new ResponseDTO(data.name(), token, refreshToken);
    }
}
