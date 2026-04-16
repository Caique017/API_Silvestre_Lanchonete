package com.silvestre_lanchonete.order_service.service;

import com.silvestre_lanchonete.order_service.domain.Order;
import com.silvestre_lanchonete.order_service.domain.OrderProduct;
import com.silvestre_lanchonete.order_service.dto.OrderRequestDTO;
import com.silvestre_lanchonete.order_service.dto.ProductResponseDTO;
import com.silvestre_lanchonete.order_service.repositories.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClientService productClient;
    private final OrderEventPublisher eventPublisher;

    public OrderService(OrderRepository orderRepository, ProductClientService productClient, OrderEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.productClient = productClient;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Order createOrder(OrderRequestDTO requestDTO, String userEmail, String token) {
        Order order = new Order();
        order.setUserEmail(userEmail);
        order.setStatus(Order.OrderStatus.PENDENTE);

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (var itemRequest : requestDTO.items()) {
            ProductResponseDTO productInfo = productClient.getProductById(itemRequest.productId(), token);

            OrderProduct orderProduct = new OrderProduct();
            orderProduct.setProductId(productInfo.id());
            orderProduct.setAmount(itemRequest.amount());
            orderProduct.setPrice(productInfo.price());
            orderProduct.setOrder(order);

            order.getOrderProducts().add(orderProduct);

            BigDecimal subtotal = productInfo.price().multiply(new BigDecimal(itemRequest.amount()));
            totalAmount = totalAmount.add(subtotal);
        }

        order.setTotal(totalAmount);
        Order savedOrder = orderRepository.save(order);

        eventPublisher.publishOrderCreatedEvent(savedOrder);

        return savedOrder;
    }

    @Transactional
    public Order updateOrderStatus(UUID id, Order.OrderStatus newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        order.setStatus(newStatus);
        order = orderRepository.save(order);

//         eventPublisher.publishOrderStatusUpdatedEvent(order);

        return order;
    }

    public List<Order> getAllOrders(String email) {
        return orderRepository.findByUserEmail(email);
    }
}