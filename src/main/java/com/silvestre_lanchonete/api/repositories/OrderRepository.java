package com.silvestre_lanchonete.api.repositories;

import com.silvestre_lanchonete.api.domain.order.Order;
import com.silvestre_lanchonete.api.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order>findByUser(User user);
}
