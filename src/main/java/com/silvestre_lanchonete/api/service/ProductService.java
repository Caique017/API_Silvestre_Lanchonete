package com.silvestre_lanchonete.api.service;

import com.amazonaws.services.s3.AmazonS3;
import com.silvestre_lanchonete.api.DTO.ProductRequestDTO;
import com.silvestre_lanchonete.api.model.product.Product;
import com.silvestre_lanchonete.api.repositories.ProductRepository;
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

    @Value("${aws-bucket-name}")
    private String bucketName;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AmazonS3 S3Client;

    public Product createProduct (ProductRequestDTO data) {

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

       productRepository.save(newProduct);

       return newProduct;
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
        String fileName = UUID.randomUUID() + "-" + multipartFile.getOriginalFilename();
        try {
            File file = this.convertMultipartToFile(multipartFile);
            S3Client.putObject(bucketName, fileName, file);
            file.delete();
            return S3Client.getUrl(bucketName, fileName).toString();
        } catch (Exception e) {
            System.out.println("Erro ao subir o arquivo");
            return null;
        }
    }

    private File convertMultipartToFile(MultipartFile multipartFile) throws IOException {
        File convFile = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(multipartFile.getBytes());
        fos.close();
        return convFile;
    }
}
