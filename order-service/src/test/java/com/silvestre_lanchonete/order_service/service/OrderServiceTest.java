package com.silvestre_lanchonete.order_service.service;

import com.silvestre_lanchonete.order_service.domain.Order;
import com.silvestre_lanchonete.order_service.domain.OrderProduct;
import com.silvestre_lanchonete.order_service.dto.OrderItemRequestDTO;
import com.silvestre_lanchonete.order_service.dto.OrderRequestDTO;
import com.silvestre_lanchonete.order_service.dto.ProductResponseDTO;
import com.silvestre_lanchonete.order_service.repositories.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductClientService productClient;

    @Mock
    private OrderEventPublisher eventPublisher;

    @InjectMocks
    private OrderService orderService;

    private UUID productId;
    private UUID orderId;
    private ProductResponseDTO productInfo;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        orderId   = UUID.randomUUID();

        productInfo = new ProductResponseDTO(
                productId,
                "X-Burguer",
                new BigDecimal("25.90")
        );
    }

    @Nested
    @DisplayName("createOrder()")
    class CreateOrder {

        @Test
        @DisplayName("deve criar pedido com total correto para um item")
        void deveCriarPedidoComTotalCorreto() {
            var items = List.of(new OrderItemRequestDTO(productId, 2));
            var request = new OrderRequestDTO(items);

            when(productClient.getProductById(productId)).thenReturn(productInfo);
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                order.setId(orderId);
                return order;
            });
            Order result = orderService.createOrder(request, "cliente@email.com");
            assertThat(result.getUserEmail()).isEqualTo("cliente@email.com");
            assertThat(result.getStatus()).isEqualTo(Order.OrderStatus.PENDENTE);
            assertThat(result.getTotal()).isEqualByComparingTo(new BigDecimal("51.80"));
            assertThat(result.getOrderProducts()).hasSize(1);
        }

        @Test
        @DisplayName("deve calcular total corretamente para múltiplos itens diferentes")
        void deveCriarPedidoComMultiplosItens() {
            UUID productId2 = UUID.randomUUID();
            var product2 = new ProductResponseDTO(productId2, "Coca-Cola", new BigDecimal("8.00"));

            var items = List.of(
                    new OrderItemRequestDTO(productId, 1),
                    new OrderItemRequestDTO(productId2, 3)
            );
            var request = new OrderRequestDTO(items);

            when(productClient.getProductById(productId)).thenReturn(productInfo);
            when(productClient.getProductById(productId2)).thenReturn(product2);
            when(orderRepository.save(any())).thenAnswer(inv -> {
                Order o = inv.getArgument(0);
                o.setId(orderId);
                return o;
            });

            Order result = orderService.createOrder(request, "cliente@email.com");
            assertThat(result.getTotal()).isEqualByComparingTo(new BigDecimal("49.90"));
            assertThat(result.getOrderProducts()).hasSize(2);
        }

        @Test
        @DisplayName("deve salvar os dados corretos de cada item do pedido")
        void deveSalvarDadosCorretosDoItem() {
            var items = List.of(new OrderItemRequestDTO(productId, 3));
            var request = new OrderRequestDTO(items);

            when(productClient.getProductById(productId)).thenReturn(productInfo);
            when(orderRepository.save(any())).thenAnswer(inv -> {
                Order o = inv.getArgument(0);
                o.setId(orderId);
                return o;
            });

            Order result = orderService.createOrder(request, "cliente@email.com");

            OrderProduct item = result.getOrderProducts().get(0);
            assertThat(item.getProductId()).isEqualTo(productId);
            assertThat(item.getAmount()).isEqualTo(3);
            assertThat(item.getPrice()).isEqualByComparingTo(new BigDecimal("25.90"));
        }

        @Test
        @DisplayName("deve publicar evento SQS após salvar o pedido")
        void devePublicarEventoSQSAposSalvar() {
            var items = List.of(new OrderItemRequestDTO(productId, 1));
            var request = new OrderRequestDTO(items);

            when(productClient.getProductById(productId)).thenReturn(productInfo);
            when(orderRepository.save(any())).thenAnswer(inv -> {
                Order o = inv.getArgument(0);
                o.setId(orderId);
                return o;
            });

            orderService.createOrder(request, "cliente@email.com");
            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(eventPublisher, times(1)).publishOrderCreatedEvent(captor.capture());
            assertThat(captor.getValue().getId()).isEqualTo(orderId);
        }

        @Test
        @DisplayName("não deve publicar evento se o save do repositório lançar exceção")
        void naoDevePublicarEventoSeRepositorioFalhar() {
            var items = List.of(new OrderItemRequestDTO(productId, 1));
            var request = new OrderRequestDTO(items);

            when(productClient.getProductById(productId)).thenReturn(productInfo);
            when(orderRepository.save(any())).thenThrow(new RuntimeException("DB fora do ar"));

            assertThatThrownBy(() -> orderService.createOrder(request, "cliente@email.com"))
                    .isInstanceOf(RuntimeException.class);

            verify(eventPublisher, never()).publishOrderCreatedEvent(any());
        }
    }

    @Nested
    @DisplayName("updateOrderStatus()")
    class UpdateOrderStatus {

        @Test
        @DisplayName("deve atualizar status do pedido com sucesso")
        void deveAtualizarStatusComSucesso() {
            Order existing = new Order();
            existing.setId(orderId);
            existing.setStatus(Order.OrderStatus.PENDENTE);
            existing.setUserEmail("cliente@email.com");

            when(orderRepository.findById(orderId)).thenReturn(Optional.of(existing));
            when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Order result = orderService.updateOrderStatus(orderId, Order.OrderStatus.CONFIRMADO);

            assertThat(result.getStatus()).isEqualTo(Order.OrderStatus.CONFIRMADO);
            verify(orderRepository).save(existing);
        }

        @Test
        @DisplayName("deve lançar RuntimeException quando pedido não é encontrado")
        void deveLancarExcecaoQuandoPedidoNaoExiste() {
            when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.updateOrderStatus(orderId, Order.OrderStatus.CONFIRMADO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Pedido não encontrado");

            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve conseguir cancelar um pedido confirmado")
        void deveCancelarPedidoConfirmado() {
            Order existing = new Order();
            existing.setId(orderId);
            existing.setStatus(Order.OrderStatus.CONFIRMADO);

            when(orderRepository.findById(orderId)).thenReturn(Optional.of(existing));
            when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Order result = orderService.updateOrderStatus(orderId, Order.OrderStatus.CANCELADO);

            assertThat(result.getStatus()).isEqualTo(Order.OrderStatus.CANCELADO);
        }
    }

    @Nested
    @DisplayName("getAllOrders()")
    class GetAllOrders {

        @Test
        @DisplayName("deve retornar lista de pedidos do usuário")
        void deveRetornarPedidosDoUsuario() {
            Order order1 = new Order();
            order1.setId(UUID.randomUUID());
            order1.setUserEmail("cliente@email.com");

            Order order2 = new Order();
            order2.setId(UUID.randomUUID());
            order2.setUserEmail("cliente@email.com");

            when(orderRepository.findByUserEmail("cliente@email.com")).thenReturn(List.of(order1, order2));

            List<Order> result = orderService.getAllOrders("cliente@email.com");

            assertThat(result).hasSize(2);
            assertThat(result).extracting(Order::getUserEmail)
                    .containsOnly("cliente@email.com");
        }

        @Test
        @DisplayName("deve retornar lista vazia quando usuário não tem pedidos")
        void deveRetornarListaVaziaParaUsuarioSemPedidos() {
            when(orderRepository.findByUserEmail("novo@email.com")).thenReturn(List.of());

            List<Order> result = orderService.getAllOrders("novo@email.com");

            assertThat(result).isEmpty();
        }
    }
}
