package com.silvestre_lanchonete.product_service.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Data
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue
    private UUID id;

    private String name;
    private String description;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;
    private String category;
    private String imageUrl;
    private Boolean available = true;
}
