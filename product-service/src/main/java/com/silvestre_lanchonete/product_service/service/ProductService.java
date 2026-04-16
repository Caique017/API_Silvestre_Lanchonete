package com.silvestre_lanchonete.product_service.service;

import com.silvestre_lanchonete.product_service.dto.ProductRequestDTO;
import com.silvestre_lanchonete.product_service.domain.Product;
import com.silvestre_lanchonete.product_service.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    @Value("${aws-bucket-name}")
    private String bucketName;

    @Autowired
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product createProduct(ProductRequestDTO data) {

        String imageUrl = null;

        if (data.image() != null) {
            imageUrl = this.uploadImg(data.image());
        }

        Product newProduct = new Product();
        newProduct.setName(data.name());
        newProduct.setDescription(data.description());
        newProduct.setPrice(data.price());
        newProduct.setCategory(data.category());
        newProduct.setAvailable(data.available());
        newProduct.setImageUrl(imageUrl);

        return productRepository.save(newProduct);
    }

    public Product updateProduct(UUID id, ProductRequestDTO data) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        existingProduct.setName(data.name());
        existingProduct.setDescription(data.description());
        existingProduct.setPrice(data.price());
        existingProduct.setCategory(data.category());
        existingProduct.setAvailable(data.available());

        if (data.image() != null && !data.image().isEmpty()) {
            String newImageUrl = this.uploadImg(data.image());
            existingProduct.setImageUrl(newImageUrl);
        }

        return productRepository.save(existingProduct);
    }

    public void deleteProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        productRepository.delete(product);
    }

    public List<Product> listProducts() {
        return productRepository.findAll();
    }

    private String uploadImg(MultipartFile multipartFile) {
        try {
            S3Client s3 = S3Client.builder().region(Region.US_EAST_1).build();

            String folder = "images/";
            String fileName = UUID.randomUUID() + "-" + multipartFile.getOriginalFilename();

            String fileKey = folder + fileName;

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .contentType(multipartFile.getContentType())
                    .build();

            s3.putObject(putObjectRequest, RequestBody.fromBytes(multipartFile.getBytes()));
            return String.format("https://%s.s3.amazonaws.com/%s", bucketName, fileKey);

        } catch (Exception e) {
            System.out.println("Erro ao subir o arquivo no Amazon S3: " + e.getMessage());
            return null;
        }
    }
}
