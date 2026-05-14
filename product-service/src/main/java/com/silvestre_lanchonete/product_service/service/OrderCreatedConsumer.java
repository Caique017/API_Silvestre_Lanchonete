package com.silvestre_lanchonete.product_service.service;

import com.silvestre_lanchonete.product_service.dto.OrderCreatedEventDTO;
import com.silvestre_lanchonete.product_service.dto.OrderItemEventDTO;
import com.silvestre_lanchonete.product_service.domain.Product;
import com.silvestre_lanchonete.product_service.infra.exceptions.ProductNotFoundException;
import com.silvestre_lanchonete.product_service.repository.ProductRepository;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderCreatedConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderCreatedConsumer.class);

    private final ProductRepository productRepository;

    public OrderCreatedConsumer(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @SqsListener("${aws.sqs.queue.order-created}")
    @Transactional
    public void onOrderCreated(OrderCreatedEventDTO event) {
        log.info("[SQS] Evento recebido — pedido {} do usuário {}",
                event.orderId(), event.userEmail());

        for (OrderItemEventDTO item : event.items()) {
            processItem(event.orderId().toString(), item);
        }

        log.info("[SQS] Pedido {} processado com sucesso. Total: R$ {}",
                event.orderId(), event.total());
    }

    private void processItem(String orderId, OrderItemEventDTO item) {
        try {
            Product product = productRepository.findById(item.productId())
                    .orElseThrow(() -> new ProductNotFoundException(
                            "Produto " + item.productId() + " do pedido " + orderId + " não encontrado."
                    ));

            log.info("[SQS] Produto '{}' — quantidade solicitada: {}",
                    product.getName(), item.amount());

           if (Boolean.FALSE.equals(product.getAvailable())) {
                log.warn("[SQS] Atenção: produto '{}' está marcado como indisponível " +
                        "mas foi incluído no pedido {}", product.getName(), orderId);
            }

        } catch (ProductNotFoundException e) {

            log.error("[SQS] {}", e.getMessage());
        }
    }
}