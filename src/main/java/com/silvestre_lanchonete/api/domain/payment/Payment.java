package com.silvestre_lanchonete.api.domain.payment;

import com.silvestre_lanchonete.api.domain.order.Order;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue
    private UUID id;

    private Double value;
    private String payment_date;

    @Enumerated(EnumType.STRING)
    private statusPayment status;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    public enum statusPayment {
        PENDENTE, FALHADO, CONFIRMADO
    }
}
