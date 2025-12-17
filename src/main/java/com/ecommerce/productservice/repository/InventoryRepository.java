package com.ecommerce.productservice.repository;

 import com.ecommerce.productservice.entity.Inventory;
 import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory,Long> {

    List<Inventory> findByProductId(Long productId);


//    Optional<Inventory> findByProductIdAndWarehouseLocation(Long productId, String warehouseLocation);

    @Query("SELECT i FROM Inventory i WHERE i.quantity <= i.reorderLevel")
    List<Inventory> findLowStockItems();

    @Query("SELECT SUM(i.quantity) FROM Inventory i WHERE i.product.id = :productId")
    Integer getTotalQuantityByProductId(@Param("productId") Long productId);


    @Query("SELECT SUM(i.reservedQuantity) FROM Inventory i WHERE i.product.id = :productId")
    Integer getTotalReservedQuantityByProductId(@Param("productId") Long productId);

    List<Inventory> findByProductIdOrderByQuantityDesc(Long productId);

//    @Query("SELECT i FROM Inventory i WHERE i.warehouseLocation = :location")
//    List<Inventory> findByWarehouseLocation(@Param("location") String location);
//
//    void deleteByProductId(Long productId);
}
