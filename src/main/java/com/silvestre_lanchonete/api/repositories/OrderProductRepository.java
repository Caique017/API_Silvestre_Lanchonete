package com.silvestre_lanchonete.api.repositories;

import com.silvestre_lanchonete.api.domain.orderProduct.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderProductRepository extends JpaRepository<OrderProduct, UUID> {
}
