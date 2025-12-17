package com.ecommerce.productservice.repository;

import com.ecommerce.productservice.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Existing methods
    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    List<Category> findAllByOrderBySortOrderAsc();

    List<Category> findByActiveTrueOrderBySortOrderAsc();

    List<Category> findByNameContainingIgnoreCase(String name);

    // New methods for self-referencing structure

    // Find root categories (no parent)
    List<Category> findByParentIsNullOrderBySortOrderAsc();

    List<Category> findByParentIsNullAndActiveTrueOrderBySortOrderAsc();

    // Find subcategories by parent
    List<Category> findByParentIdOrderBySortOrderAsc(Long parentId);

    List<Category> findByParentIdAndActiveTrueOrderBySortOrderAsc(Long parentId);

    // Check for duplicate names within same parent context
    boolean existsByNameIgnoreCaseAndParentId(String name, Long parentId);

    boolean existsByNameIgnoreCaseAndParentIsNull(String name);

    boolean existsByNameIgnoreCaseAndParentIdAndIdNot(String name, Long parentId, Long id);

    boolean existsByNameIgnoreCaseAndParentIsNullAndIdNot(String name, Long id);

    // Find category with all its subcategories (tree structure)
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.subcategories WHERE c.id = :id")
    Optional<Category> findByIdWithSubcategories(Long id);

    // Find root categories with their complete tree structure
    @Query("SELECT DISTINCT c FROM Category c " +
            "LEFT JOIN FETCH c.subcategories s1 " +
            "LEFT JOIN FETCH s1.subcategories s2 " +
            "LEFT JOIN FETCH s2.subcategories s3 " +
            "WHERE c.parent IS NULL AND c.active = true " +
            "ORDER BY c.sortOrder")
    List<Category> findRootCategoriesWithSubcategories();

    // Find all categories by level (depth in hierarchy)
    @Query("SELECT c FROM Category c WHERE " +
            "(c.parent IS NULL AND :level = 0) OR " +
            "(c.parent IS NOT NULL AND :level > 0)")
    List<Category> findByLevel(int level);


    @Query(value = "SELECT c.* FROM categories c WHERE c.parent_id = :categoryId", nativeQuery = true)
    List<Category> findAllDescendants(Long categoryId);

    // Check if category has subcategories
    boolean existsByParentId(Long parentId);

    // Count subcategories
    long countByParentId(Long parentId);

    // Find categories by specific parent and name pattern
    List<Category> findByParentIdAndNameContainingIgnoreCaseOrderBySortOrderAsc(Long parentId, String name);

    // Find leaf categories (no subcategories)
    @Query("SELECT c FROM Category c WHERE c.id NOT IN (SELECT DISTINCT p.id FROM Category p WHERE p.subcategories IS NOT EMPTY)")
    List<Category> findLeafCategories();


    List<Category> findByIdOrderBySortOrderAsc(Long categoryId);
}
