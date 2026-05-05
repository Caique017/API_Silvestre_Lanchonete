package com.silvestre_lanchonete.order_service.service;

import com.silvestre_lanchonete.order_service.domain.Order;
import com.silvestre_lanchonete.order_service.dto.OrderCreatedEventDTO;
import com.silvestre_lanchonete.order_service.dto.OrderItemEventDTO;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);

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

        try {
            sqsTemplate.send(queueName, event);
            log.info("[SQS] Pedido {} publicado na fila '{}' com {} item(ns) — total: R$ {}",
                    order.getId(), queueName, order.getOrderProducts().size(), order.getTotal());
        } catch (Exception e) {
            log.error("[SQS] Falha ao publicar pedido {} na fila '{}': {}",
                    order.getId(), queueName, e.getMessage(), e);
        }
    }
}