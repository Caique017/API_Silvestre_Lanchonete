package com.silvestre_lanchonete.order_service.repositories;

import com.silvestre_lanchonete.order_service.domain.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByUserEmail(String email);

    Page<Order> findByUserEmail(String email, Pageable pageable);

    Page<Order> findByUserEmailAndStatus(String email, Order.OrderStatus status, Pageable pageable);
}