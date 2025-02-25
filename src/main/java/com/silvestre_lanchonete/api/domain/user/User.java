package com.silvestre_lanchonete.api.domain.user;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "User")
@Data
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    private String name;
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private UserType userType;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum UserType {
        Client, Administrator
    }
}
