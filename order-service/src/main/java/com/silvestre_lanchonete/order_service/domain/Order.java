package com.silvestre_lanchonete.order_service.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String userEmail;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private BigDecimal total;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum OrderStatus {
        PENDENTE, CONFIRMADO, CANCELADO
    }

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderProduct> orderProducts = new ArrayList<>();
}