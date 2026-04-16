package com.silvestre_lanchonete.order_service.repositories;

import com.silvestre_lanchonete.order_service.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByUserEmail(String userEmail);

}