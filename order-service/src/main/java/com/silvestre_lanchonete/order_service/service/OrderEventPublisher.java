package com.silvestre_lanchonete.order_service.service;

import com.silvestre_lanchonete.order_service.domain.Order;
import com.silvestre_lanchonete.order_service.dto.OrderCreatedEventDTO;
import com.silvestre_lanchonete.order_service.dto.OrderItemEventDTO;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OrderEventPublisher {

    private final SqsTemplate sqsTemplate;

    @Value("${aws.sqs.queue.order-created}")
    private String queueName;

    public OrderEventPublisher(SqsTemplate sqsTemplate) {
        this.sqsTemplate = sqsTemplate;
    }

    public void publishOrderCreatedEvent(Order order) {

        var event = new OrderCreatedEventDTO(
                order.getId(),
                order.getUserEmail(),
                order.getTotal(),
                order.getOrderProducts().stream()
                        .map(it -> new OrderItemEventDTO(it.getProductId(), it.getAmount()))
                        .toList()
        );

        // 2. Despachamos para a AWS
        sqsTemplate.send(queueName, event);

        System.out.println("🚀 [SQS] Pedido " + order.getId() + " publicado com sucesso!");
    }
}