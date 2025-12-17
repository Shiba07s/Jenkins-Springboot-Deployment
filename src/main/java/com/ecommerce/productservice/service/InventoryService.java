package com.ecommerce.productservice.service;


import com.ecommerce.productservice.dto.InventoryDTO;

import java.util.List;

public interface InventoryService {

    InventoryDTO createInventory(InventoryDTO inventoryDTO);

    InventoryDTO inventoryDetailsById(Long inventoryId);

    List<InventoryDTO>getAllInventoryDetails();

    List<InventoryDTO>getInventoryDetailsByProductId(Long productId);

    InventoryDTO updateInventoryDetails(Long inventoryId,InventoryDTO inventoryDTO);

    void deleteInventoryDetails(Long inventoryId);

    List<InventoryDTO> getLowStockItems();

    Integer getTotalQuantityByProductId(Long productId);

    Integer getAvailableQuantityByProductId(Long productId);

    void reserveInventory(Long productId, Integer quantity);

    void releaseReservedInventory(Long productId, Integer quantity);


}
