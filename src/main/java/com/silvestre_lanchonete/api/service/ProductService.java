package com.silvestre_lanchonete.api.service;

import com.google.cloud.storage.*;
import com.silvestre_lanchonete.api.DTO.ProductRequestDTO;
import com.silvestre_lanchonete.api.domain.product.Product;
import com.silvestre_lanchonete.api.repositories.ProductRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class ProductService {

    @Value("${gcp-bucket-name}")
    private String bucketName;

    @Autowired
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
        System.out.println("ProductService instanciado");
    }

    @PostConstruct
    private void init() {
        String credentialsPath = System.getenv("PATH_CREDENTIALS");
        System.out.println("Valor de PATH-CREDENTIALS: " + credentialsPath);

        if (credentialsPath == null) {
            throw new RuntimeException("A variável de ambiente PATH-CREDENTIALS não está definida.");
        }

        System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", credentialsPath);
        System.out.println("Caminho das credenciais: " + credentialsPath);
        File file = new File(credentialsPath);
        System.out.println("Arquivo de credenciais existe: " + file.exists());
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
            Storage storage = StorageOptions.getDefaultInstance().getService();
            String fileName = UUID.randomUUID() + "-" + multipartFile.getOriginalFilename();

            BlobId blobId = BlobId.of(bucketName, fileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(multipartFile.getContentType())
                    .build();

            storage.create(blobInfo, multipartFile.getBytes());
            return String.format("https://storage.googleapis.com/%s/%s", bucketName, fileName);

        } catch (Exception e) {
            System.out.println("Erro ao subir o arquivo no Google Cloud Storage: " + e.getMessage());
            return null;
        }
    }
}
