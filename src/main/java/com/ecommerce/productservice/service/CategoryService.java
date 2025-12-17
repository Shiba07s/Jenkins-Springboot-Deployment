package com.ecommerce.productservice.service;



import com.ecommerce.productservice.dto.CategoryDTO;
//import com.ecommerce.productservice.request.CategoryCreateDTO;
//import com.ecommerce.productservice.request.CategoryUpdateDTO;

import java.util.List;

public interface CategoryService {

    CategoryDTO createCategory(CategoryDTO categoryDTO);

    List<CategoryDTO> getAllCategories();

    List<CategoryDTO> getActiveCategories();

    CategoryDTO getCategoryById(Long categoryId);

    CategoryDTO getCategoryWithSubcategories(Long categoryId);

    List<CategoryDTO> getRootCategories();

    List<CategoryDTO> getRootCategoriesWithSubcategories();

    List<CategoryDTO> getSubcategories(Long parentId);

    List<CategoryDTO> searchCategories(String name);

    CategoryDTO updateCategory(Long categoryId, CategoryDTO categoryDTO);

    void deleteCategory(Long categoryId);

    CategoryDTO deactivateCategory(Long categoryId);

    CategoryDTO activateCategory(Long categoryId);

    List<CategoryDTO> getCategoriesToGetAllSubcategoryDetails(Long categoryId);

}