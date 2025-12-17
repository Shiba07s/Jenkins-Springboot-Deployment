package com.ecommerce.productservice.service;

import com.ecommerce.productservice.dto.CategoryDTO;

import com.ecommerce.productservice.dto.ProductDTO;
import com.ecommerce.productservice.entity.Category;
import com.ecommerce.productservice.entity.Product;
import com.ecommerce.productservice.exception.DuplicateResourceException;
import com.ecommerce.productservice.exception.ResourceNotFoundException;
import com.ecommerce.productservice.repository.CategoryRepository; 
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CategoryServiceImpl implements CategoryService {


    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;


    /**
     * 1- create new category
     */

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryCreateDTO) {
        // Validate parent category if provided
        Category parent = null;
        if (categoryCreateDTO.getParentId() != null) {
            parent = categoryRepository.findById(categoryCreateDTO.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with id: " + categoryCreateDTO.getParentId()));
        }

        // Check for duplicate names in the same context
        boolean nameExists;
        if (parent != null) {
            nameExists = categoryRepository.existsByNameIgnoreCaseAndParentId(
                    categoryCreateDTO.getName(), categoryCreateDTO.getParentId());
        } else {
            nameExists = categoryRepository.existsByNameIgnoreCaseAndParentIsNull(
                    categoryCreateDTO.getName());
        }

        if (nameExists) {
            String context = parent != null ? "under parent '" + parent.getName() + "'" : "at root level";
            throw new DuplicateResourceException("Category with name '" + categoryCreateDTO.getName() + "' already exists " + context);
        }

        // Manual mapping instead of ModelMapper to avoid conflicts
        Category category = new Category();
        category.setName(categoryCreateDTO.getName());
        category.setDescription(categoryCreateDTO.getDescription());
        category.setImageUrl(categoryCreateDTO.getImageUrl());
        category.setActive(categoryCreateDTO.isActive());
        category.setSortOrder(categoryCreateDTO.getSortOrder());
        category.setParent(parent);

        Category saved = categoryRepository.save(category);
        return convertToDTO(saved);
    }

    /**
     * 2- get all category details
     */

    @Override
    public List<CategoryDTO> getAllCategories() {
        List<Category> categories = categoryRepository.findAllByOrderBySortOrderAsc();
        return categories.stream()
                .map(this::convertToTreeDTO) // Changed to return nested structure
                .collect(Collectors.toList());
    }

    /**
     * 3- get Active Categories details
     */


    @Override
    public List<CategoryDTO> getActiveCategories() {
        List<Category> categories = categoryRepository.findByActiveTrueOrderBySortOrderAsc();
        return categories.stream()
                .map(this::convertToTreeDTO) // Changed to return nested structure
                .collect(Collectors.toList());
    }


    /**
     * 14- get Categories To Get All Subcategory Details
     */

    @Override
    public List<CategoryDTO> getCategoriesToGetAllSubcategoryDetails(Long categoryId) {
        List<Category> categories = categoryRepository.findByIdOrderBySortOrderAsc(categoryId);
        return categories.stream()
                .map(this::convertToTreeDTO) // Changed to return nested structure
                .collect(Collectors.toList());
    }

    /**
     * 4- get category deials by id
     */

    @Override
    public CategoryDTO getCategoryById(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
        return convertToDTO(category);
    }

    /**
     * 5
     */
    @Override
    public CategoryDTO getCategoryWithSubcategories(Long categoryId) {
        Category category = categoryRepository.findByIdWithSubcategories(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
        return convertToTreeDTO(category);
    }

    /**
     * 6
     */
    @Override
    public List<CategoryDTO> getRootCategories() {
        List<Category> rootCategories = categoryRepository.findByParentIsNullAndActiveTrueOrderBySortOrderAsc();
        return rootCategories.stream()
                .map(this::convertToDTO) // Simple DTO for root categories only
                .collect(Collectors.toList());
    }

    /**
     * 7
     */
    @Override
    public List<CategoryDTO> getRootCategoriesWithSubcategories() {
        List<Category> rootCategories = categoryRepository.findRootCategoriesWithSubcategories();
        return rootCategories.stream()
                .map(this::convertToTreeDTO)
                .collect(Collectors.toList());
    }

    /**
     * 8
     */
    @Override
    public List<CategoryDTO> getSubcategories(Long parentId) {
        // Changed to return nested subcategories
        List<Category> subcategories = categoryRepository.findByParentIdOrderBySortOrderAsc(parentId);
        return subcategories.stream()
                .map(this::convertToTreeDTO) // Changed from convertToDTO to convertToTreeDTO
                .collect(Collectors.toList());
    }



    /**
     * 9
     */
    @Override
    public List<CategoryDTO> searchCategories(String name) {
        List<Category> categories = categoryRepository.findByNameContainingIgnoreCase(name);
        return categories.stream()
                .map(this::convertToTreeDTO) // Changed to return nested structure
                .collect(Collectors.toList());
    }

    public List<CategoryDTO> searchSubcategories(Long parentId, String name) {
        List<Category> categories = categoryRepository.findByParentIdAndNameContainingIgnoreCaseOrderBySortOrderAsc(parentId, name);
        return categories.stream()
                .map(this::convertToTreeDTO) // Changed to return nested structure
                .collect(Collectors.toList());
    }

    /**
     * 10
     */
    @Override
    public CategoryDTO updateCategory(Long categoryId, CategoryDTO categoryDTO) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

        // Check for duplicate name if name is being updated
        if (categoryDTO.getName() != null &&
                !categoryDTO.getName().equalsIgnoreCase(category.getName())) {

            boolean nameExists;
            if (category.getParent() != null) {
                nameExists = categoryRepository.existsByNameIgnoreCaseAndParentIdAndIdNot(
                        categoryDTO.getName(), category.getParent().getId(), categoryId);
            } else {
                nameExists = categoryRepository.existsByNameIgnoreCaseAndParentIsNullAndIdNot(
                        categoryDTO.getName(), categoryId);
            }

            if (nameExists) {
                throw new DuplicateResourceException("Category with name '" + categoryDTO.getName() + "' already exists in this context");
            }
            category.setName(categoryDTO.getName());
        }

        // Update parent if provided (with validation)
        if (categoryDTO.getParentId() != null) {
            // Prevent circular references
            if (categoryDTO.getParentId().equals(categoryId)) {
                throw new IllegalArgumentException("Category cannot be its own parent");
            }

            // Check if new parent is not a descendant of current category
            if (isDescendant(categoryId, categoryDTO.getParentId())) {
                throw new IllegalArgumentException("Cannot set parent to a descendant category");
            }

            Category newParent = categoryRepository.findById(categoryDTO.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
            category.setParent(newParent);
        }

        // Update other fields
        if (categoryDTO.getDescription() != null) {
            category.setDescription(categoryDTO.getDescription());
        }
        if (categoryDTO.getImageUrl() != null) {
            category.setImageUrl(categoryDTO.getImageUrl());
        }
//        if (categoryDTO.get != null) {
//            category.setActive(categoryDTO.getActive());
//        }
        if (categoryDTO.getSortOrder() != null) {
            category.setSortOrder(categoryDTO.getSortOrder());
        }

        Category updatedCategory = categoryRepository.save(category);
        return convertToDTO(updatedCategory);
    }

    /**
     * 11
     */
    @Override
    public void deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

        // Check if category has subcategories
        if (categoryRepository.existsByParentId(categoryId)) {
            throw new RuntimeException("Cannot delete category with subcategories. Delete subcategories first or move them to another parent.");
        }

        // Check if category has products
        if (category.getProducts() != null && !category.getProducts().isEmpty()) {
            throw new RuntimeException("Cannot delete category with products. Move products to another category first.");
        }

        categoryRepository.delete(category);
    }

    /**
     * 12
     */
    @Override
    public CategoryDTO deactivateCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

        category.setActive(false);
        Category updatedCategory = categoryRepository.save(category);
        return convertToDTO(updatedCategory);
    }

    /**
     * 13
     */
    @Override
    public CategoryDTO activateCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

        category.setActive(true);
        Category updatedCategory = categoryRepository.save(category);
        return convertToDTO(updatedCategory);
    }

    // Helper method to convert Category to CategoryDTO (simple - no nested subcategories)
    private CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();

        // Basic fields
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setImageUrl(category.getImageUrl());
        dto.setActive(category.isActive());
        dto.setSortOrder(category.getSortOrder());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());

        // Parent information
        if (category.getParent() != null) {
            dto.setParentId(category.getParent().getId());
            dto.setParentName(category.getParent().getName());
        }

        // Hierarchy information
        dto.setLevel(category.getLevel());
        dto.setFullPath(category.getFullPath());
        dto.setRootCategory(category.isRootCategory());

        // Convert products
        List<ProductDTO> productDTOs = new ArrayList<>();
        if (category.getProducts() != null && !category.getProducts().isEmpty()) {
            productDTOs = category.getProducts().stream()
                    .filter(product -> product.isActive()) // Only include active products
                    .map(this::convertProductToDTO)
                    .collect(Collectors.toList());
        }
        dto.setProducts(productDTOs);

        // Initialize empty subcategories list to avoid null
        dto.setSubcategories(new ArrayList<>());

        return dto;
    }

    // Helper method to convert Category to CategoryDTO with nested subcategories (tree structure)
    private CategoryDTO convertToTreeDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();

        // Basic fields
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setImageUrl(category.getImageUrl());
        dto.setActive(category.isActive());
        dto.setSortOrder(category.getSortOrder());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());

        // Parent information
        if (category.getParent() != null) {
            dto.setParentId(category.getParent().getId());
            dto.setParentName(category.getParent().getName());
        }

        // Hierarchy information
        dto.setLevel(category.getLevel());
        dto.setFullPath(category.getFullPath());
        dto.setRootCategory(category.isRootCategory());

        // Convert products
        List<ProductDTO> productDTOs = new ArrayList<>();
        if (category.getProducts() != null && !category.getProducts().isEmpty()) {
            productDTOs = category.getProducts().stream()
                    .filter(product -> product.isActive()) // Only include active products
                    .map(this::convertProductToDTO)
                    .collect(Collectors.toList());
        }
        dto.setProducts(productDTOs);

        // Convert subcategories recursively
        List<CategoryDTO> subcategoryDTOs = new ArrayList<>();
        if (category.getSubcategories() != null && !category.getSubcategories().isEmpty()) {
            subcategoryDTOs = category.getSubcategories().stream()
                    .filter(subcat -> subcat.isActive()) // Only include active subcategories
                    .sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder())) // Sort by sortOrder
                    .map(this::convertToTreeDTO)
                    .collect(Collectors.toList());
        }
        dto.setSubcategories(subcategoryDTOs);

        return dto;
    }

    // Alternative convertToTreeDTO that includes all subcategories (active and inactive)
    private CategoryDTO convertToTreeDTOAll(Category category) {
        CategoryDTO dto = new CategoryDTO();

        // Basic fields
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setImageUrl(category.getImageUrl());
        dto.setActive(category.isActive());
        dto.setSortOrder(category.getSortOrder());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());

        // Parent information
        if (category.getParent() != null) {
            dto.setParentId(category.getParent().getId());
            dto.setParentName(category.getParent().getName());
        }

        // Hierarchy information
        dto.setLevel(category.getLevel());
        dto.setFullPath(category.getFullPath());
        dto.setRootCategory(category.isRootCategory());

        // Convert ALL subcategories recursively (including inactive ones)
        List<CategoryDTO> subcategoryDTOs = new ArrayList<>();
        if (category.getSubcategories() != null && !category.getSubcategories().isEmpty()) {
            subcategoryDTOs = category.getSubcategories().stream()
                    .sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder())) // Sort by sortOrder
                    .map(this::convertToTreeDTOAll)
                    .collect(Collectors.toList());
        }
        dto.setSubcategories(subcategoryDTOs);

        return dto;
    }

    // Helper method to check if a category is a descendant of another
    private boolean isDescendant(Long ancestorId, Long potentialDescendantId) {
        Category potentialDescendant = categoryRepository.findById(potentialDescendantId).orElse(null);
        if (potentialDescendant == null) return false;

        Category current = potentialDescendant.getParent();
        while (current != null) {
            if (current.getId().equals(ancestorId)) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    // Helper method to convert Product to ProductDTO
    private ProductDTO convertProductToDTO(Product product) {
        ProductDTO dto = new ProductDTO();

        // Basic fields
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setDiscountedPrice(product.getDiscountedPrice());
        dto.setSku(product.getSku());
//        dto.setStockQuantity(product.getStockQuantity());
        dto.setActive(product.isActive());
        dto.setFeatured(product.isFeatured());
        dto.setWeight(product.getWeight());
//        dto.setDimensions(product.getDimensions());
//        dto.setImageUrls(product.getImageUrls());
//        dto.setTags(product.getTags());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());

        // Category information (basic info to avoid circular reference)
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }

        // Add any other product fields you have

        return dto;
    }
    // Additional utility methods for different use cases

    // Get all categories as flat list (no nesting)
    public List<CategoryDTO> getAllCategoriesFlat() {
        List<Category> categories = categoryRepository.findAllByOrderBySortOrderAsc();
        return categories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get all active categories as flat list (no nesting)
    public List<CategoryDTO> getActiveCategoriesFlat() {
        List<Category> categories = categoryRepository.findByActiveTrueOrderBySortOrderAsc();
        return categories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get subcategories as flat list (no nesting)
    public List<CategoryDTO> getSubcategoriesFlat(Long parentId) {
        List<Category> subcategories = categoryRepository.findByParentIdAndActiveTrueOrderBySortOrderAsc(parentId);
        return subcategories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get all subcategories including inactive ones with nesting
    public List<CategoryDTO> getAllSubcategoriesWithChildren(Long parentId) {
        List<Category> subcategories = categoryRepository.findByParentIdOrderBySortOrderAsc(parentId);
        return subcategories.stream()
                .map(this::convertToTreeDTOAll)
                .collect(Collectors.toList());
    }
}
