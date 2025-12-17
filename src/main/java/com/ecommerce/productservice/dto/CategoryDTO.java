package com.ecommerce.productservice.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDTO {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private boolean active;
    private Integer sortOrder;

    // Parent information
    private Long parentId;
    private String parentName;

    // Nested subcategories
    private List<CategoryDTO> subcategories;

    // Products in this category
    private List<ProductDTO> products;

    // Hierarchy information
    private int level;
    private String fullPath;
    private boolean isRootCategory;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}















//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//public class CategoryDTO {
//    private Long id;
//    private String name;
//    private String description;
//    private String imageUrl;
//    private boolean active;
//    private Integer sortOrder;
//    private List<SubcategoryDTO> subcategories;
//    private List<ProductDTO> products;
//
//    private LocalDateTime createdAt;
//    private LocalDateTime updatedAt;
//}