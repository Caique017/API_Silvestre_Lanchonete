package com.silvestre_lanchonete.api.domain.orderProduct;

import com.silvestre_lanchonete.api.domain.order.Order;
import com.silvestre_lanchonete.api.domain.product.Product;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "OrderProduct")
@Data
public class OrderProduct {

    @Id
    @GeneratedValue
    private UUID id;

    private Integer amount;
    private Double price;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}
