package com.silvestre_lanchonete.product_service.repository;

import com.silvestre_lanchonete.product_service.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
}
