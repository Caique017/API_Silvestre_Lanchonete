package com.silvestre_lanchonete.order_service.infra.config;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthTokenInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {

        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getDetails() instanceof WebAuthenticationDetails details) {
            String token = ((WebAuthenticationDetails) authentication.getDetails()).toString();
            request.getHeaders().set("Authorization", "Bearer " + token);
        }

        if (authentication != null && authentication.getCredentials() instanceof String token) {
            request.getHeaders().set("Authorization", "Bearer " + token);
        }

        return execution.execute(request, body);
    }
}