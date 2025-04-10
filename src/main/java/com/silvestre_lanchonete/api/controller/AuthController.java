package com.silvestre_lanchonete.api.controller;

import com.silvestre_lanchonete.api.DTO.*;
import com.silvestre_lanchonete.api.domain.user.User;
import com.silvestre_lanchonete.api.infra.security.TokenService;
import com.silvestre_lanchonete.api.repositories.UserRepository;
import com.silvestre_lanchonete.api.service.AuthService;
import com.silvestre_lanchonete.api.service.LoginGoogleService;
import com.silvestre_lanchonete.api.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoginGoogleService loginGoogleService;

    @Autowired
    private AuthService authService;

    @Autowired
    private TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity<ResponseDTO> login(HttpServletRequest request, @RequestBody @Valid LoginRequestDTO body) {
        try {
            ResponseDTO response = authService.login(request, body);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseDTO(e.getMessage(), null, null));
        }
    }

    @GetMapping("/login/google")
    public ResponseEntity<Void> redirectGoogle(){
        var url = loginGoogleService.generateUrl();

        var headers = new HttpHeaders();
        headers.setLocation(URI.create(url));

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @GetMapping("/login/google/authorized")
    public ResponseEntity<DataTokenDTO> authenticateUserOAuth(@RequestParam String code){
        var email = loginGoogleService.getEmail(code);

        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String acessToken = tokenService.generateToken((User) authentication.getPrincipal());
        String refreshToken = tokenService.generateRefreshToken((User) authentication.getPrincipal());

        return ResponseEntity.ok(new DataTokenDTO(acessToken, refreshToken));
    }

    @PostMapping("/update-token")
    public ResponseEntity<ResponseDTO> updateToken(@RequestBody @Valid DataRefreshTokenDTO data) {
        try {
            ResponseDTO response = authService.updateToken(data);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseDTO(e.getMessage(), null, null));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseDTO> register(@RequestBody @Valid RegisterRequestDTO body) {
        try {
            ResponseDTO response = authService.register(body);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseDTO(e.getMessage(), null, null));
        }
    }

    @GetMapping("/register/google")

    public ResponseEntity<Void> redirectGoogleRegister(){

        var url = loginGoogleService.generateUrlRegister();

        var headers = new HttpHeaders();

        headers.setLocation(URI.create(url));

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @GetMapping("/register/google/authorized")

    public ResponseEntity<DataTokenDTO> registerUserOAuth(@RequestParam String code){

        var dataUser = loginGoogleService.getDataOAuth(code);

        var responseDTO = authService.register(dataUser);
        var userEntity = userRepository.findByEmail(dataUser.email()).orElseThrow(() -> new RuntimeException("Erro interno"));
        var authentication = new UsernamePasswordAuthenticationToken(userEntity, null, userEntity.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String tokenAcesso = tokenService.generateToken((User) authentication.getPrincipal());
        String refreshToken = tokenService.generateRefreshToken((User) authentication.getPrincipal());

        return ResponseEntity.ok(new DataTokenDTO(tokenAcesso, refreshToken));

    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody PasswordResetRequestDTO request) {
        try {
            passwordResetService.requestPasswordReset(request.email());
            return ResponseEntity.ok("E-mail de recuperação enviado!");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao enviar e-mail de recuperação.");
        }
    }

    @PostMapping("/validate-code")
    public ResponseEntity<String> validateCode(@RequestBody @Valid CodeValidationRequestDTO data) {
        try {
            passwordResetService.validateCode(data.token());
            return ResponseEntity.status(HttpStatus.OK).body("Código válido.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Código inválido ou expirado.");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody PasswordResetTokenDTO request) {
        try {
            passwordResetService.resetPassword(request);
            return ResponseEntity.ok("Senha redefinida com sucesso!");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao redefinir senha.");
        }
    }
}
