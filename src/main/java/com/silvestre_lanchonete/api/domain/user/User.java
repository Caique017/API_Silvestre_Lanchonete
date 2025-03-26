package com.silvestre_lanchonete.api.domain.user;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
public class User implements UserDetails {

    @Id
    @GeneratedValue
    private UUID id;

    private String name;
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if(this.role == Role.Administrador) return List.of(new SimpleGrantedAuthority("Administrador"), new SimpleGrantedAuthority("Usuario"));
        else return List.of(new SimpleGrantedAuthority("Usuario"));
    }

    @Override
    public String getUsername() {
        return email;
    }

    public enum Role {
        Usuario, Administrador
    }
}
