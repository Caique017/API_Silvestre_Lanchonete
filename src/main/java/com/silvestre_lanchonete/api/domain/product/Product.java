package com.silvestre_lanchonete.api.domain.product;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

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
    private Double price;
    private String category;
    private String imageUrl;
    private Boolean available = true;
}
