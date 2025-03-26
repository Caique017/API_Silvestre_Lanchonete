package com.silvestre_lanchonete.api.service;

import com.silvestre_lanchonete.api.DTO.OrderRequestDTO;
import com.silvestre_lanchonete.api.infra.websocket.OrderStatusWebSocketHandler;
import com.silvestre_lanchonete.api.domain.order.Order;
import com.silvestre_lanchonete.api.domain.orderProduct.OrderProduct;
import com.silvestre_lanchonete.api.domain.product.Product;
import com.silvestre_lanchonete.api.domain.user.User;
import com.silvestre_lanchonete.api.repositories.OrderProductRepository;
import com.silvestre_lanchonete.api.repositories.OrderRepository;
import com.silvestre_lanchonete.api.repositories.ProductRepository;
import com.silvestre_lanchonete.api.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderProductRepository orderProductRepository;

    @Autowired
    private OrderStatusWebSocketHandler webSocketHandler;

    @Autowired
    private OrderRepository orderRepository;

    @Transactional
    public Order createOrder(OrderRequestDTO orderRequest) {
        User user = userRepository.findById(orderRequest.userId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        double total = 0.0;
        List<OrderProduct> orderProducts = new ArrayList<>();

        for (OrderRequestDTO.OrderProductRequest productRequest : orderRequest.products()) {

            Product product = productRepository.findById(productRequest.productId())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

            OrderProduct orderProduct = new OrderProduct();
            orderProduct.setProduct(product);
            orderProduct.setAmount(productRequest.quantity());
            orderProduct.setPrice(product.getPrice() * productRequest.quantity());

            orderProducts.add(orderProduct);
            total += orderProduct.getPrice();
        }

        Order order = new Order();
        order.setUser(user);
        order.setTotal(total);
        order.setStatus(Order.OrderStatus.PENDENTE);

        order = orderRepository.save(order);

        for (OrderProduct op : orderProducts) {
            op.setOrder(order);
        }
        orderProductRepository.saveAll(orderProducts);

        return order;
    }

    @Transactional
    public Order updateOrderStatus(UUID id, Order.OrderStatus newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        order.setStatus(newStatus);
        order = orderRepository.save(order);

        notifyUser(order, newStatus);

        return order;
    }

    public List<Order> getAllOrders(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        return orderRepository.findByUser(user);
    }

    private void notifyUser(Order order, Order.OrderStatus status) {
        String userEmail = order.getUser().getEmail();
        String message = String.format("Olá, %s! Seu pedido na Silvestre Lanchonete foi atualizado para: %s. ", order.getUser().getName() ,status);

        if (userEmail != null && !userEmail.isEmpty()) {
            webSocketHandler.sendNotification(userEmail, message);
        }
    }
}
