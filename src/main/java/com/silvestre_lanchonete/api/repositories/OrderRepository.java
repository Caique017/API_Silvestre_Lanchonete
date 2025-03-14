package com.silvestre_lanchonete.api.repositories;

import com.silvestre_lanchonete.api.model.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
}
