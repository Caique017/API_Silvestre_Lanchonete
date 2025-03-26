package com.silvestre_lanchonete.api.service;

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

            HttpSession session = request.getSession(true);
            session.setAttribute("userEmail", user.getEmail());

            System.out.println("Sessão criada com sucesso para: " + user.getEmail());
            return new ResponseDTO(user.getName(), token);
        }

        throw new RuntimeException("Credenciais inválidas");
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
            return new ResponseDTO(newUser.getName(), token);
        }
        throw new RuntimeException("Usuário já existe");
    }
}
