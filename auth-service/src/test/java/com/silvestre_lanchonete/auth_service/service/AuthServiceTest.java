package com.silvestre_lanchonete.auth_service.service;

import com.silvestre_lanchonete.auth_service.domain.Role;
import com.silvestre_lanchonete.auth_service.domain.User;
import com.silvestre_lanchonete.auth_service.dto.DataRefreshTokenDTO;
import com.silvestre_lanchonete.auth_service.dto.LoginRequestDTO;
import com.silvestre_lanchonete.auth_service.dto.RegisterRequestDTO;
import com.silvestre_lanchonete.auth_service.dto.ResponseDTO;
import com.silvestre_lanchonete.auth_service.infra.exceptions.InvalidTokenException;
import com.silvestre_lanchonete.auth_service.infra.exceptions.UserAlreadyExistsException;
import com.silvestre_lanchonete.auth_service.infra.exceptions.UserNotFoundException;
import com.silvestre_lanchonete.auth_service.infra.security.TokenService;
import com.silvestre_lanchonete.auth_service.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User existingUser;
    private final UUID USER_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        existingUser = new User();
        existingUser.setId(USER_ID);
        existingUser.setName("João Silva");
        existingUser.setEmail("joao@email.com");
        existingUser.setPassword("$2a$10$hashedPassword");
        existingUser.setRole(Role.Usuario);
    }

    @Nested
    @DisplayName("login()")
    class Login {

        @Test
        @DisplayName("deve retornar tokens quando credenciais são válidas")
        void deveRetornarTokensComCredenciaisValidas() {
            var request = new LoginRequestDTO("joao@email.com", "senha123");
            when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(existingUser));
            when(passwordEncoder.matches("senha123", existingUser.getPassword())).thenReturn(true);
            when(tokenService.generateToken(existingUser)).thenReturn("access-token-xyz");
            when(tokenService.generateRefreshToken(existingUser)).thenReturn("refresh-token-xyz");
            ResponseDTO response = authService.login(request);
            assertThat(response.name()).isEqualTo("João Silva");
            assertThat(response.token()).isEqualTo("access-token-xyz");
            assertThat(response.refreshToken()).isEqualTo("refresh-token-xyz");
            verify(tokenService).generateToken(existingUser);
            verify(tokenService).generateRefreshToken(existingUser);
        }

        @Test
        @DisplayName("deve lançar UserNotFoundException quando e-mail não existe")
        void deveLancarExcecaoQuandoEmailNaoExiste() {
            var request = new LoginRequestDTO("naoexiste@email.com", "senha123");
            when(userRepository.findByEmail("naoexiste@email.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessage("E-mail ou senha incorretos.");

            verify(tokenService, never()).generateToken(any());
        }

        @Test
        @DisplayName("deve lançar UserNotFoundException quando senha está errada")
        void deveLancarExcecaoQuandoSenhaEstaErrada() {
            var request = new LoginRequestDTO("joao@email.com", "senhaErrada");
            when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(existingUser));
            when(passwordEncoder.matches("senhaErrada", existingUser.getPassword())).thenReturn(false);

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessage("E-mail ou senha incorretos.");

            verify(tokenService, never()).generateToken(any());
        }
    }

    @Nested
    @DisplayName("register()")
    class Register {

        @Test
        @DisplayName("deve registrar novo usuário e retornar tokens")
        void deveRegistrarNovoUsuario() {
            var request = new RegisterRequestDTO("Maria Souza", "maria@email.com", "senha456");
            when(userRepository.findByEmail("maria@email.com")).thenReturn(Optional.empty());
            when(passwordEncoder.encode("senha456")).thenReturn("$2a$10$encodedPassword");
            when(tokenService.generateToken(any(User.class))).thenReturn("access-token-novo");
            when(tokenService.generateRefreshToken(any(User.class))).thenReturn("refresh-token-novo");

            ResponseDTO response = authService.register(request);

            assertThat(response.name()).isEqualTo("Maria Souza");
            assertThat(response.token()).isEqualTo("access-token-novo");
            assertThat(response.refreshToken()).isEqualTo("refresh-token-novo");
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();

            assertThat(savedUser.getName()).isEqualTo("Maria Souza");
            assertThat(savedUser.getEmail()).isEqualTo("maria@email.com");
            assertThat(savedUser.getPassword()).isEqualTo("$2a$10$encodedPassword");
            assertThat(savedUser.getRole()).isEqualTo(Role.Usuario);
        }

        @Test
        @DisplayName("deve lançar UserAlreadyExistsException quando e-mail já está em uso")
        void deveLancarExcecaoQuandoEmailJaExiste() {
            var request = new RegisterRequestDTO("João Silva", "joao@email.com", "senha123");
            when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(existingUser));

            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(UserAlreadyExistsException.class)
                    .hasMessageContaining("joao@email.com");
            verify(userRepository, never()).save(any());
            verify(passwordEncoder, never()).encode(anyString());
        }

        @Test
        @DisplayName("deve sempre criar usuário com role Usuario, nunca Administrador")
        void deveAtribuirRoleUsuarioPorPadrao() {
            var request = new RegisterRequestDTO("Carlos Admin", "carlos@email.com", "senha789");
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(passwordEncoder.encode(anyString())).thenReturn("hash");
            when(tokenService.generateToken(any())).thenReturn("t");
            when(tokenService.generateRefreshToken(any())).thenReturn("rt");
            authService.register(request);
            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().getRole()).isNotEqualTo(Role.Administrador);
        }
    }

    @Nested
    @DisplayName("updateToken()")
    class UpdateToken {

        @Test
        @DisplayName("deve retornar novos tokens quando refresh token é válido")
        void deveRetornarNovosTokensComRefreshValido() {
            var data = new DataRefreshTokenDTO("refresh-token-valido");
            when(tokenService.validateToken("refresh-token-valido")).thenReturn(USER_ID.toString());
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
            when(tokenService.generateToken(existingUser)).thenReturn("novo-access-token");
            when(tokenService.generateRefreshToken(existingUser)).thenReturn("novo-refresh-token");

            ResponseDTO response = authService.updateToken(data);

            assertThat(response.name()).isEqualTo("João Silva");
            assertThat(response.token()).isEqualTo("novo-access-token");
            assertThat(response.refreshToken()).isEqualTo("novo-refresh-token");
        }

        @Test
        @DisplayName("deve lançar InvalidTokenException quando refresh token é inválido")
        void deveLancarExcecaoComRefreshTokenInvalido() {
            var data = new DataRefreshTokenDTO("token-invalido");
            when(tokenService.validateToken("token-invalido")).thenReturn(null);
            assertThatThrownBy(() -> authService.updateToken(data))
                    .isInstanceOf(InvalidTokenException.class)
                    .hasMessage("Refresh Token inválido ou expirado.");
        }

        @Test
        @DisplayName("deve lançar InvalidTokenException quando refresh token está vazio")
        void deveLancarExcecaoComRefreshTokenVazio() {
            var data = new DataRefreshTokenDTO("token-vazio");
            when(tokenService.validateToken("token-vazio")).thenReturn("");
            assertThatThrownBy(() -> authService.updateToken(data))
                    .isInstanceOf(InvalidTokenException.class);
        }

        @Test
        @DisplayName("deve lançar UserNotFoundException quando usuário do token não existe mais")
        void deveLancarExcecaoQuandoUsuarioNaoExiste() {
            var data = new DataRefreshTokenDTO("refresh-orfao");
            when(tokenService.validateToken("refresh-orfao")).thenReturn(USER_ID.toString());
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> authService.updateToken(data))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessage("Usuário não encontrado para este token.");
        }
    }
}