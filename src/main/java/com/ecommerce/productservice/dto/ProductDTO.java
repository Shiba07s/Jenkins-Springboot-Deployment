package com.ecommerce.productservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private String sku;
    private BigDecimal price;
    private BigDecimal discountedPrice;
    private String brand;
    private String model;
    private Double weight;
    private String specifications;
    private boolean active;
    private boolean featured;
    private Long categoryId;
    private String categoryName;
    private Long sellerId;
    private String sellerName;
    private List<ProductImageDTO> images;
    private List<InventoryDTO> inventory;
    private Set<TagDTO> tags;
//    private List<ReviewDTO> reviews;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}