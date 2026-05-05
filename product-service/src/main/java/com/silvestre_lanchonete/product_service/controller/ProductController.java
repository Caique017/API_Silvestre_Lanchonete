package com.silvestre_lanchonete.product_service.controller;

import com.silvestre_lanchonete.product_service.dto.ProductRequestDTO;
import com.silvestre_lanchonete.product_service.domain.Product;
import com.silvestre_lanchonete.product_service.dto.ProductResponseDTO;
import com.silvestre_lanchonete.product_service.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/products")
@Tag(name = "Produtos", description = "Gerenciamento do cardápio da lanchonete")
@Validated
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping(consumes = "multipart/form-data")
    @Operation(summary = "Criar produto", description = "Cria um novo produto no cardápio. Requer role Administrador.")
    @ApiResponse(responseCode = "201", description = "Produto criado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "403", description = "Sem permissão de administrador")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Product> createProduct(
            @RequestParam("name")
            @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
            String name,

            @RequestParam(value = "description", required = false)
            @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
            String description,

            @RequestParam("price")
            @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
            Double price,

            @RequestParam("category") String category,
            @RequestParam("available") Boolean available,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        ProductRequestDTO dto = new ProductRequestDTO(name, description, price, category, available, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(dto));
    }

    @GetMapping
    @Operation(summary = "Listar produtos", description = "Lista produtos com paginação e filtros opcionais")
    @ApiResponse(responseCode = "200", description = "Lista de produtos")
    public ResponseEntity<Page<ProductResponseDTO>> listProducts(
            @Parameter(description = "Filtrar por categoria") @RequestParam(required = false) String category,
            @Parameter(description = "Filtrar por disponibilidade") @RequestParam(required = false) Boolean available,
            @Parameter(description = "Buscar por nome") @RequestParam(required = false) String name,
            @Parameter(description = "Preço mínimo") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Preço máximo") @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "Página (começa em 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Itens por página") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Ordenar por campo") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Direção: asc ou desc") @RequestParam(defaultValue = "asc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, Math.min(size, 50), sort);

        Page<ProductResponseDTO> products = productService.listProducts(
                category, available, name, minPrice, maxPrice, pageable
        );

        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar produto por ID")
    @ApiResponse(responseCode = "200", description = "Produto encontrado")
    @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable UUID id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice()
        ));
    }

    @PutMapping(path = "/{id}", consumes = "multipart/form-data")
    @Operation(summary = "Atualizar produto")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Product> updateProduct(
            @PathVariable UUID id,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("price") Double price,
            @RequestParam("category") String category,
            @RequestParam("available") Boolean available,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        ProductRequestDTO dto = new ProductRequestDTO(name, description, price, category, available, image);
        return ResponseEntity.ok(productService.updateProduct(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar produto")
    @ApiResponse(responseCode = "204", description = "Produto deletado")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}