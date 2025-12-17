package com.ecommerce.productservice.controller;


import com.ecommerce.productservice.dto.CategoryDTO;
import com.ecommerce.productservice.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // Create new category
    @PostMapping("/create")
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody CategoryDTO categoryDTO) {
        CategoryDTO createdCategory = categoryService.createCategory(categoryDTO);
        return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
    }

    // Get all categories
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<CategoryDTO> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    // Get all active categories
    @GetMapping("/active")
    public ResponseEntity<List<CategoryDTO>> getActiveCategories() {
        List<CategoryDTO> categories = categoryService.getActiveCategories();
        return ResponseEntity.ok(categories);
    }

    // Get category by id
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long categoryId) {
        CategoryDTO category = categoryService.getCategoryById(categoryId);
        return ResponseEntity.ok(category);
    }

    @GetMapping("/category-subcategory/{categoryId}")
    public ResponseEntity<List<CategoryDTO>> getCategoriesToGetAllSubcategoryDetails(@PathVariable Long categoryId) {
        List<CategoryDTO> category = categoryService.getCategoriesToGetAllSubcategoryDetails(categoryId);
        return ResponseEntity.ok(category);
    }

    // Get category with subcategories
    @GetMapping("/{categoryId}/with-subcategories")
    public ResponseEntity<CategoryDTO> getCategoryWithSubcategories(@PathVariable Long categoryId) {
        CategoryDTO category = categoryService.getCategoryWithSubcategories(categoryId);
        return ResponseEntity.ok(category);
    }

    // Get root categories (categories without parent)
    @GetMapping("/root")
    public ResponseEntity<List<CategoryDTO>> getRootCategories() {
        List<CategoryDTO> rootCategories = categoryService.getRootCategories();
        return ResponseEntity.ok(rootCategories);
    }

    // Get root categories with their subcategories
    @GetMapping("/root/with-subcategories")
    public ResponseEntity<List<CategoryDTO>> getRootCategoriesWithSubcategories() {
        List<CategoryDTO> rootCategories = categoryService.getRootCategoriesWithSubcategories();
        return ResponseEntity.ok(rootCategories);
    }

    // Get subcategories by parent id
    @GetMapping("/{parentId}/subcategories")
    public ResponseEntity<List<CategoryDTO>> getSubcategories(@PathVariable Long parentId) {
        List<CategoryDTO> subcategories = categoryService.getSubcategories(parentId);
        return ResponseEntity.ok(subcategories);
    }

    // Search categories by name
    @GetMapping("/search")
    public ResponseEntity<List<CategoryDTO>> searchCategories(@RequestParam String name) {
        List<CategoryDTO> categories = categoryService.searchCategories(name);
        return ResponseEntity.ok(categories);
    }

    // Update category
    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Long categoryId,
                                                      @RequestBody CategoryDTO categoryDTO) {
        CategoryDTO updatedCategory = categoryService.updateCategory(categoryId, categoryDTO);
        return ResponseEntity.ok(updatedCategory);
    }

    // Delete category
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }

    // Deactivate category (soft delete)
    @PatchMapping("/{categoryId}/deactivate")
    public ResponseEntity<CategoryDTO> deactivateCategory(@PathVariable Long categoryId) {
        CategoryDTO deactivatedCategory = categoryService.deactivateCategory(categoryId);
        return ResponseEntity.ok(deactivatedCategory);
    }

    // Activate category
    @PatchMapping("/{categoryId}/activate")
    public ResponseEntity<CategoryDTO> activateCategory(@PathVariable Long categoryId) {
        CategoryDTO activatedCategory = categoryService.activateCategory(categoryId);
        return ResponseEntity.ok(activatedCategory);
    }

    // Get simple category list for dropdowns
//    @GetMapping("/simple")
//    public ResponseEntity<List<CategorySimpleDTO>> getSimpleCategories() {
//        List<CategorySimpleDTO> categories = categoryService.getSimpleCategories();
//        return ResponseEntity.ok(categories);
//    }
}
