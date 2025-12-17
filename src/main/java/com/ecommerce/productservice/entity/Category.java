package com.ecommerce.productservice.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    private String imageUrl;

    private boolean active = true;

    private Integer sortOrder = 0;

    // Self-referencing: Parent category
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnoreProperties("subcategories")
    private Category parent;

    // Self-referencing: Child categories (subcategories)
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("parent")
    @OrderBy("sortOrder ASC")
    private List<Category> subcategories = new ArrayList<>();

    // One-to-Many: Category has many products
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("category")
    private List<Product> products = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Helper methods
    public boolean isRootCategory() {
        return parent == null;
    }

    public int getLevel() {
        if (parent == null) return 0;
        return parent.getLevel() + 1;
    }

    public String getFullPath() {
        if (parent == null) return name;
        return parent.getFullPath() + " > " + name;
    }

    public List<Category> getAllDescendants() {
        List<Category> descendants = new ArrayList<>();
        for (Category child : subcategories) {
            descendants.add(child);
            descendants.addAll(child.getAllDescendants());
        }
        return descendants;
    }

    public Category getRootCategory() {
        if (parent == null) return this;
        return parent.getRootCategory();
    }
}






 