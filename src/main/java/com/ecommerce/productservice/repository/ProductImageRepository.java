package com.ecommerce.productservice.repository;

 import com.ecommerce.productservice.entity.ProductImage;
 import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage,Long> {
    List<ProductImage> findByProductId(long id);
    List<ProductImage> findByProductIdOrderBySortOrder(Long productId);

    ProductImage findByProductIdAndPrimaryTrue(Long productId);
}
