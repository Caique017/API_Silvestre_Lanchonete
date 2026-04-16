package com.silvestre_lanchonete.product_service.controller;

import com.silvestre_lanchonete.product_service.dto.ProductRequestDTO;
import com.silvestre_lanchonete.product_service.domain.Product;
import com.silvestre_lanchonete.product_service.dto.ProductResponseDTO;
import com.silvestre_lanchonete.product_service.repository.ProductRepository;
import com.silvestre_lanchonete.product_service.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    private final ProductRepository productRepository;

    public ProductController(ProductService productService, ProductRepository productRepository) {
        this.productService = productService;
        this.productRepository = productRepository;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Product> createProduct(@RequestParam("name") String name,
                                                 @RequestParam(value = "description", required = false) String description,
                                                 @RequestParam("price") Double price,
                                                 @RequestParam("category") String category,
                                                 @RequestParam("available") Boolean available,
                                                 @RequestParam(value = "image", required = false) MultipartFile image) {
        ProductRequestDTO productRequestDTO = new ProductRequestDTO(name, description, price, category, available, image);
        Product newProduct = this.productService.createProduct(productRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newProduct);
    }
    @PutMapping(path = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<Product> updateProduct(
            @PathVariable UUID id,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("price") Double price,
            @RequestParam("category") String category,
            @RequestParam("available") Boolean available,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        ProductRequestDTO productRequestDTO = new ProductRequestDTO(name, description, price, category, available, image);

        Product updatedProduct = productService.updateProduct(id, productRequestDTO);

        return ResponseEntity.status(HttpStatus.OK).body(updatedProduct);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable UUID id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        // Converta a Entidade para o novo DTO
        ProductResponseDTO responseDTO = new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                BigDecimal.valueOf(product.getPrice())
        );

        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<Product>> listProducts() {
        return ResponseEntity.ok(productService.listProducts());
    }
}
