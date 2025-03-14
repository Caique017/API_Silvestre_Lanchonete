package com.silvestre_lanchonete.api.controller;

import com.silvestre_lanchonete.api.DTO.ProductRequestDTO;
import com.silvestre_lanchonete.api.model.product.Product;
import com.silvestre_lanchonete.api.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping(path = "/create", consumes = "multipart/form-data")
    @PreAuthorize("hasAuthority('Administrador')")
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
    @PreAuthorize("hasAuthority('Administrador')")
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

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('Administrador')")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('Administrador', 'Usuario')")
    public ResponseEntity<List<Product>> listProducts() {
        return ResponseEntity.ok(productService.listProducts());
    }
}
