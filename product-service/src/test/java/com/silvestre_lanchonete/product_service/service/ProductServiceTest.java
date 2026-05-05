package com.silvestre_lanchonete.product_service.service;

import com.silvestre_lanchonete.product_service.domain.Product;
import com.silvestre_lanchonete.product_service.dto.ProductRequestDTO;
import com.silvestre_lanchonete.product_service.infra.exceptions.ProductNotFoundException;
import com.silvestre_lanchonete.product_service.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private ProductService productService;

    private UUID productId;
    private Product existingProduct;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();

        existingProduct = new Product();
        existingProduct.setId(productId);
        existingProduct.setName("X-Burguer");
        existingProduct.setDescription("Hamburguer artesanal");
        existingProduct.setPrice(new BigDecimal("25.90"));
        existingProduct.setCategory("Lanches");
        existingProduct.setAvailable(true);
        ReflectionTestUtils.setField(productService, "bucketName", "silvestre-bucket");
    }

    @Nested
    @DisplayName("createProduct()")
    class CreateProduct {

        @Test
        @DisplayName("deve criar produto sem imagem corretamente")
        void deveCriarProdutoSemImagem() {
            var request = new ProductRequestDTO(
                    "X-Burguer", "Hamburguer artesanal", 25.90, "Lanches", true, null
            );
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
                Product p = inv.getArgument(0);
                p.setId(productId);
                return p;
            });

            Product result = productService.createProduct(request);

            assertThat(result.getName()).isEqualTo("X-Burguer");
            assertThat(result.getDescription()).isEqualTo("Hamburguer artesanal");
            assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("25.90"));
            assertThat(result.getCategory()).isEqualTo("Lanches");
            assertThat(result.getAvailable()).isTrue();
            assertThat(result.getImageUrl()).isNull();
            verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        }

        @Test
        @DisplayName("deve criar produto com imagem e fazer upload no S3")
        void deveCriarProdutoComImagemEFazerUpload() {
            var imageFile = new MockMultipartFile(
                    "image", "burguer.jpg", "image/jpeg", "fake-image-bytes".getBytes()
            );
            var request = new ProductRequestDTO(
                    "X-Bacon", "Com bacon crocante", 32.00, "Lanches", true, imageFile
            );

            when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Product result = productService.createProduct(request);

            assertThat(result.getImageUrl()).isNotNull();
            assertThat(result.getImageUrl()).startsWith("https://silvestre-bucket.s3.amazonaws.com/images/");
            assertThat(result.getImageUrl()).endsWith("-burguer.jpg");

            verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        }

        @Test
        @DisplayName("deve converter price Double para BigDecimal corretamente")
        void deveConverterPrecoParaBigDecimal() {
            var request = new ProductRequestDTO("Açaí", "Açaí 500ml", 18.50, "Bebidas", true, null);
            when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Product result = productService.createProduct(request);

            assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("18.50"));
            assertThat(result.getPrice()).isInstanceOf(BigDecimal.class);
        }
    }

    @Nested
    @DisplayName("getProductById()")
    class GetProductById {

        @Test
        @DisplayName("deve retornar produto quando ID existe")
        void deveRetornarProdutoQuandoIdExiste() {
            when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));

            Product result = productService.getProductById(productId);

            assertThat(result.getId()).isEqualTo(productId);
            assertThat(result.getName()).isEqualTo("X-Burguer");
        }

        @Test
        @DisplayName("deve lançar ProductNotFoundException quando ID não existe")
        void deveLancarExcecaoQuandoIdNaoExiste() {
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.getProductById(productId))
                    .isInstanceOf(ProductNotFoundException.class)
                    .hasMessageContaining(productId.toString());
        }
    }

    @Nested
    @DisplayName("updateProduct()")
    class UpdateProduct {

        @Test
        @DisplayName("deve atualizar campos do produto sem nova imagem")
        void deveAtualizarCamposDosProdutoSemImagem() {
            var request = new ProductRequestDTO(
                    "X-Burguer Premium", "Nova descrição", 29.90, "Lanches Especiais", false, null
            );
            when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
            when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Product result = productService.updateProduct(productId, request);

            assertThat(result.getName()).isEqualTo("X-Burguer Premium");
            assertThat(result.getDescription()).isEqualTo("Nova descrição");
            assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("29.90"));
            assertThat(result.getCategory()).isEqualTo("Lanches Especiais");
            assertThat(result.getAvailable()).isFalse();

            verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        }

        @Test
        @DisplayName("deve atualizar imagem no S3 quando nova imagem é enviada")
        void deveAtualizarImagemNoS3QuandoNovaImagemEnviada() {
            var novaImagem = new MockMultipartFile(
                    "image", "nova-foto.jpg", "image/jpeg", "new-bytes".getBytes()
            );
            var request = new ProductRequestDTO(
                    "X-Burguer", "Descrição", 25.90, "Lanches", true, novaImagem
            );
            when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
            when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Product result = productService.updateProduct(productId, request);

            assertThat(result.getImageUrl()).isNotNull();
            verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        }

        @Test
        @DisplayName("deve lançar ProductNotFoundException ao tentar atualizar produto inexistente")
        void deveLancarExcecaoAoAtualizarProdutoInexistente() {
            var request = new ProductRequestDTO("Nome", "Desc", 10.0, "Cat", true, null);
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.updateProduct(productId, request))
                    .isInstanceOf(ProductNotFoundException.class);

            verify(productRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteProduct()")
    class DeleteProduct {

        @Test
        @DisplayName("deve deletar produto existente")
        void deveDeletarProdutoExistente() {
            when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));

            productService.deleteProduct(productId);

            verify(productRepository).delete(existingProduct);
        }

        @Test
        @DisplayName("deve lançar ProductNotFoundException ao tentar deletar produto inexistente")
        void deveLancarExcecaoAoDeletarProdutoInexistente() {
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.deleteProduct(productId))
                    .isInstanceOf(ProductNotFoundException.class);

            verify(productRepository, never()).delete((Product) any());
        }
    }

    @Nested
    @DisplayName("listProducts()")
    class ListProducts {

        @Test
        @DisplayName("deve retornar todos os produtos")
        void deveRetornarTodosOsProdutos() {
            var p1 = new Product(); p1.setName("X-Burguer");
            var p2 = new Product(); p2.setName("Coca-Cola");
            when(productRepository.findAll()).thenReturn(List.of(p1, p2));

            List<Product> result = productService.listProducts();

            assertThat(result).hasSize(2);
            assertThat(result).extracting(Product::getName)
                    .containsExactlyInAnyOrder("X-Burguer", "Coca-Cola");
        }

        @Test
        @DisplayName("deve retornar lista vazia quando não há produtos cadastrados")
        void deveRetornarListaVaziaQuandoSemProdutos() {
            when(productRepository.findAll()).thenReturn(List.of());

            List<Product> result = productService.listProducts();

            assertThat(result).isEmpty();
        }
    }
}