package com.silvestre_lanchonete.api.domain.order;

import com.silvestre_lanchonete.api.domain.orderProduct.OrderProduct;
import com.silvestre_lanchonete.api.domain.user.User;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "Order")
public class Order {

    @Id
    @GeneratedValue
    private UUID id;

    private OrderStatus status;
    private Double total;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private PaymentMethod paymentMethod;

    @JoinColumn(name = "user_id")
    @ManyToOne
    private User user;

    public enum OrderStatus {
        PENDENTE, EM_ANDAMENTO, CONCLUIDO, CANCELADO
    }

    public enum PaymentMethod {
        CARTAO, PIX, DINHEIRO, WHATSAPP
    }

    @OneToMany(mappedBy = "order")
    private List<OrderProduct> OrderProducts;
}
