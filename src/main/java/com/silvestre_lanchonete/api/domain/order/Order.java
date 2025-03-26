package com.silvestre_lanchonete.api.domain.order;

import com.silvestre_lanchonete.api.domain.orderProduct.OrderProduct;
import com.silvestre_lanchonete.api.domain.user.User;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue
    private UUID id;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private Double total;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @JoinColumn(name = "user_id")
    @ManyToOne
    private User user;

    public enum OrderStatus {
        PENDENTE, EM_ANDAMENTO, CONCLUIDO, CANCELADO
    }

    @OneToMany(mappedBy = "order")
    private List<OrderProduct> orderProducts;

    public List<OrderProduct> getOrderProducts() {
        return orderProducts != null ? orderProducts : new ArrayList<>();
    }
}
