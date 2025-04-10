package com.silvestre_lanchonete.api.service;

import com.silvestre_lanchonete.api.DTO.DataRefreshTokenDTO;
import com.silvestre_lanchonete.api.DTO.LoginRequestDTO;
import com.silvestre_lanchonete.api.DTO.RegisterRequestDTO;
import com.silvestre_lanchonete.api.DTO.ResponseDTO;
import com.silvestre_lanchonete.api.infra.security.TokenService;
import com.silvestre_lanchonete.api.domain.user.User;
import com.silvestre_lanchonete.api.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    TokenService tokenService;

    @Autowired
    PasswordEncoder passwordEncoder;

    public ResponseDTO login(HttpServletRequest request, LoginRequestDTO body) {
        User user = this.userRepository.findByEmail(body.email())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (passwordEncoder.matches(body.password(), user.getPassword())) {
            String token = this.tokenService.generateToken(user);
            String refreshToken = this.tokenService.generateRefreshToken(user);

            HttpSession session = request.getSession(true);
            session.setAttribute("userEmail", user.getEmail());

            return new ResponseDTO(user.getName(), token, refreshToken);
        }

        throw new RuntimeException("Credenciais inválidas");
    }

    public ResponseDTO updateToken(DataRefreshTokenDTO data) {
        var refreshToken = data.refreshToken();
        UUID idUser = UUID.fromString(tokenService.validateToken(refreshToken));
        var user = userRepository.findById(idUser).orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        String token = this.tokenService.generateToken(user);
        String updateToken = this.tokenService.generateRefreshToken(user);

        return  new ResponseDTO(user.getName(), token, updateToken);
    }

    public ResponseDTO register(RegisterRequestDTO body) {
        Optional<User> user = this.userRepository.findByEmail(body.email());

        if (user.isEmpty()) {
            User newUser = new User();

            if (this.userRepository.count() == 0) {
                newUser.setRole(User.Role.Administrador);
            } else {
                newUser.setRole(User.Role.Usuario);
            }
            newUser.setPassword(passwordEncoder.encode(body.password()));
            newUser.setEmail(body.email());
            newUser.setName(body.name());
            this.userRepository.save(newUser);

            String token = this.tokenService.generateToken(newUser);
            String refreshToken = this.tokenService.generateRefreshToken(newUser);
            return new ResponseDTO(newUser.getName(), token, refreshToken);
        }
        throw new RuntimeException("Usuário já existe");
    }
}
