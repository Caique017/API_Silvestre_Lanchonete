package com.silvestre_lanchonete.api.service;

import com.silvestre_lanchonete.api.DTO.OrderRequestDTO;
import com.silvestre_lanchonete.api.infra.websocket.OrderStatusWebSocketHandler;
import com.silvestre_lanchonete.api.model.order.Order;
import com.silvestre_lanchonete.api.model.orderProduct.OrderProduct;
import com.silvestre_lanchonete.api.model.product.Product;
import com.silvestre_lanchonete.api.model.user.User;
import com.silvestre_lanchonete.api.repositories.OrderProductRepository;
import com.silvestre_lanchonete.api.repositories.OrderRepository;
import com.silvestre_lanchonete.api.repositories.ProductRepository;
import com.silvestre_lanchonete.api.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderProductRepository orderProductRepository;

    @Autowired
    private OrderStatusWebSocketHandler webSocketHandler;

    public Order createOrder(OrderRequestDTO orderRequest) {

        User user = userRepository.findById(orderRequest.userId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        List<OrderProduct> orderProducts = new ArrayList<>();
        double total = 0.0;

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
        order.setOrderProducts(orderProducts);
        order.setTotal(total);
        order.setStatus(Order.OrderStatus.PENDENTE);

        orderRepository.save(order);

        orderProducts.forEach(op -> op.setOrder(order));
        orderProductRepository.saveAll(orderProducts);

        return order;
    }

    public Order updateOrderStatus(UUID id, Order.OrderStatus newStatus) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        order.setStatus(newStatus);

        Order updatedOrder = orderRepository.save(order);

        String message = String.format("Pedido %s atualizado para %s (Total: R$ %.2f)", id, newStatus, order.getTotal());

        webSocketHandler.sendNotification(message);

        System.out.println("Notificação enviada: " + message);

        return updatedOrder;
    }


    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}
