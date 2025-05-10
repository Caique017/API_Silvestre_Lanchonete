package com.silvestre_lanchonete.api.infra.security;

import com.silvestre_lanchonete.api.infra.cors.CorsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    private final SecurityFilter securityFilter;

    private final CorsConfig corsConfig;

    public SecurityConfig(CustomUserDetailsService userDetailsService, SecurityFilter securityFilter, CorsConfig corsConfig) {
        this.userDetailsService = userDetailsService;
        this.securityFilter = securityFilter;
        this.corsConfig = corsConfig;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/api-docs/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/auth/login",
                                        "/auth/register",
                                        "/auth/forgot-password",
                                        "/auth/reset-password",
                                        "/auth/validate-code",
                                        "/auth/update-token"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/auth/login/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/auth/register/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/products").permitAll()
                        .requestMatchers(HttpMethod.POST, "/products").hasAuthority("Administrador")
                        .requestMatchers("/products/**").hasAuthority("Administrador")
                        .requestMatchers(HttpMethod.POST, "/orders").hasAnyAuthority("Administrador", "Usuario")
                        .requestMatchers(HttpMethod.GET, "/orders").hasAnyAuthority("Administrador", "Usuario")
                        .requestMatchers(HttpMethod.PUT, "/orders/**").hasAuthority("Administrador")
                        .anyRequest().authenticated()
                )
                .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
