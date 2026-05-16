package com.silvestre_lanchonete.product_service.service;

import com.silvestre_lanchonete.product_service.dto.ProductRequestDTO;
import com.silvestre_lanchonete.product_service.dto.ProductResponseDTO;
import com.silvestre_lanchonete.product_service.domain.Product;
import com.silvestre_lanchonete.product_service.infra.exceptions.ProductNotFoundException;
import com.silvestre_lanchonete.product_service.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    @Value("${aws-bucket-name}")
    private String bucketName;

    private final ProductRepository productRepository;
    private final S3Client s3Client;

    public ProductService(ProductRepository productRepository, S3Client s3Client) {
        this.productRepository = productRepository;
        this.s3Client = s3Client;
    }

    public Product createProduct(ProductRequestDTO data) {
        String imageUrl = null;
        if (data.image() != null && !data.image().isEmpty()) {
            imageUrl = this.uploadImg(data.image());
        }

        Product newProduct = new Product();
        newProduct.setName(data.name());
        newProduct.setDescription(data.description());
        newProduct.setPrice(BigDecimal.valueOf(data.price()));
        newProduct.setCategory(data.category());
        newProduct.setAvailable(data.available());
        newProduct.setImageUrl(imageUrl);

        return productRepository.save(newProduct);
    }

    public Product updateProduct(UUID id, ProductRequestDTO data) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Produto com id " + id + " não encontrado."));

        existing.setName(data.name());
        existing.setDescription(data.description());
        existing.setPrice(BigDecimal.valueOf(data.price()));
        existing.setCategory(data.category());
        existing.setAvailable(data.available());

        if (data.image() != null && !data.image().isEmpty()) {
            existing.setImageUrl(this.uploadImg(data.image()));
        }

        return productRepository.save(existing);
    }

    public Product getProductById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Produto com id " + id + " não encontrado."));
    }

    public void deleteProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Produto com id " + id + " não encontrado."));
        productRepository.delete(product);
    }

    public Page<ProductResponseDTO> listProducts(
            String category,
            Boolean available,
            String name,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable) {

        Specification<Product> spec = (root, query, cb) -> cb.conjunction();

        if (category != null && !category.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("category")), category.toLowerCase()));
        }

        if (available != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("available"), available));
        }

        if (name != null && !name.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
        }

        if (minPrice != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("price"), minPrice));
        }

        if (maxPrice != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("price"), maxPrice));
        }

        return productRepository.findAll(spec, pageable)
                .map(p -> new ProductResponseDTO(p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getAvailable()));
    }

    public List<Product> listProducts() {
        return productRepository.findAll();
    }

    private String uploadImg(MultipartFile multipartFile) {
        try {
            String fileName = UUID.randomUUID() + "-" + multipartFile.getOriginalFilename();
            String fileKey = "images/" + fileName;

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .contentType(multipartFile.getContentType())
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(multipartFile.getBytes()));

            return String.format("https://%s.s3.amazonaws.com/%s", bucketName, fileKey);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao fazer upload da imagem: " + e.getMessage(), e);
        }
    }
}