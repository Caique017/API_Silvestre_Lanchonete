package com.silvestre_lanchonete.api.repositories;

import com.silvestre_lanchonete.api.domain.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
}
