package com.silvestre_lanchonete.api.service;

import com.auth0.jwt.JWT;
import com.silvestre_lanchonete.api.DTO.RegisterRequestDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

@Service
public class LoginGoogleService {

    @Value("${GOOGLE_CLIENT_ID}")
    private String clientId;

    @Value("${GOOGLE_CLIENT_SECRET}")
    private String clientSecret;

    private final String redirectUri = "http://localhost:8080/auth/login/google/authorized";
    private final String redirectUriRegister = "http://localhost:8080/auth/register/google/authorized";

    private final RestClient restClient;

    public LoginGoogleService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    public String generateUrl() {
        return "https://accounts.google.com/o/oauth2/v2/auth" +
                "?client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&scope=https://www.googleapis.com/auth/userinfo.email" +
                "&response_type=code";
    }

    public String generateUrlRegister() {
        return "https://accounts.google.com/o/oauth2/v2/auth" +
                "?client_id=" + clientId +
                "&redirect_uri=" + redirectUriRegister +
                "&scope=https://www.googleapis.com/auth/userinfo.email%20https://www.googleapis.com/auth/userinfo.profile" +
                "&response_type=code";
    }

    private String getToken(String code, String uri) {
        var response = restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "code", code,
                        "client_id", clientId,
                        "client_secret", clientSecret,
                        "redirect_uri", uri,
                        "grant_type", "authorization_code"
                ))
                .retrieve()
                .body(Map.class);

        return response.get("id_token").toString();
    }

    public String getEmail(String code) {
        var token = getToken(code, redirectUri);
        var decodedJWT = JWT.decode(token);
        return decodedJWT.getClaim("email").asString();
    }

    public RegisterRequestDTO getDataOAuth(String code) {
        var token = getToken(code, redirectUriRegister);
        var decodedJWT = JWT.decode(token);

        var email = decodedJWT.getClaim("email").asString();
        var name = decodedJWT.getClaim("name").asString();
        if (name == null || name.isBlank()) {
            name = email.split("@")[0];
        }

        var senha = UUID.randomUUID().toString();

        return new RegisterRequestDTO(name, email, senha);
    }
}