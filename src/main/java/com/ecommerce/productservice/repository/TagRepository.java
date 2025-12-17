package com.ecommerce.productservice.repository;

 import com.ecommerce.productservice.entity.Tag;
 import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag,Long> {
}
