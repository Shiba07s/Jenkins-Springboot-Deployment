package com.ecommerce.productservice.service;


import com.ecommerce.productservice.dto.InventoryDTO;
import com.ecommerce.productservice.entity.Inventory;
import com.ecommerce.productservice.entity.Product;
import com.ecommerce.productservice.exception.ResourceNotFoundException;
import com.ecommerce.productservice.repository.InventoryRepository;
import com.ecommerce.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
  //  private static final String INVENTORY_TOPIC = "inventory-event";
 //   private final KafkaTemplate<String, InventoryEvent> kafkaTemplate;
    private static final Logger logger = LoggerFactory.getLogger(InventoryServiceImpl.class);


    @Override
    public InventoryDTO createInventory(InventoryDTO inventoryDTO) {
        Product product = productRepository.findById(inventoryDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product Not Found"));
        Inventory inventory = modelMapper.map(inventoryDTO, Inventory.class);
        inventory.setProduct(product);

        Inventory save = inventoryRepository.save(inventory);

 /*       // Create and publish event
        InventoryEvent event = modelMapper.map(save, InventoryEvent.class);
        event.setEventType(InventoryEvent.EventType.CREATED);

        // Send to Kafka
        kafkaTemplate.send(INVENTORY_TOPIC, String.valueOf(save.getId()), event);
        logger.info("Inventory created event published to Kafka");*/
        return modelMapper.map(save, InventoryDTO.class);
    }

    @Override
    public InventoryDTO inventoryDetailsById(Long inventoryId) {
        Inventory inventory = inventoryRepository.findById(inventoryId).orElseThrow(() -> new ResourceNotFoundException("Inventory details is not found with this id : " + inventoryId));
        return modelMapper.map(inventory, InventoryDTO.class);
    }

    @Override
    public List<InventoryDTO> getAllInventoryDetails() {
        List<Inventory> inventoryList = inventoryRepository.findAll();
        return inventoryList.stream()
                .map(inventory -> modelMapper.map(inventory, InventoryDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryDTO> getInventoryDetailsByProductId(Long productId) {
        List<Inventory> inventoryListByProductId = inventoryRepository.findByProductId(productId);
        return inventoryListByProductId.stream()
                .map(inventory -> modelMapper.map(inventory, InventoryDTO.class))
                .collect(Collectors.toList());
    }

    //check: propductname is not update
    @Override
    public InventoryDTO updateInventoryDetails(Long inventoryId, InventoryDTO inventoryDTO) {
        Product product = productRepository.findById(inventoryDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product Not Found"));
//        Inventory inventory = modelMapper.map(inventoryDTO, Inventory.class);
        Inventory inventory = inventoryRepository.findById(inventoryId).orElseThrow(() -> new ResourceNotFoundException("Inventory details is not found with this id : " + inventoryId));

        inventory.setQuantity(inventoryDTO.getQuantity());
        inventory.setReservedQuantity(inventoryDTO.getReservedQuantity());
        inventory.setWarehouseLocation(inventoryDTO.getWarehouseLocation());
        inventory.setReorderLevel(inventoryDTO.getReorderLevel());
        inventory.setUpdatedAt(LocalDateTime.now());
        inventory.setProduct(product);

        Inventory update = inventoryRepository.save(inventory);

        return modelMapper.map(update, InventoryDTO.class);
    }

    @Override
    public void deleteInventoryDetails(Long inventoryId) {
        Inventory inventory = inventoryRepository.findById(inventoryId).orElseThrow(() -> new ResourceNotFoundException("Inventory details is not found with this id : " + inventoryId));
        inventoryRepository.delete(inventory);

    }

    /*
     * getLowStockItems using query
     * */
    @Override
    public List<InventoryDTO> getLowStockItems() {
        List<Inventory> lowStockItems = inventoryRepository.findLowStockItems();
        return lowStockItems.stream()
                .map(lowStockItem -> modelMapper.map(lowStockItem, InventoryDTO.class))
                .collect(Collectors.toList());
    }

    /*
     * getLowStockItems using backend logic
     * */
//    @Override
//    public List<InventoryDTO> getLowStockItems() {
//        List<Inventory> lowStockItems = inventoryRepository.findAll().stream()
//                .filter(item -> {
//                    Integer quantity = item.getQuantity();
//                    Integer reorderLevel = item.getReorderLevel();
//
//                    if (quantity == null || reorderLevel == null) {
//                        logger.warn("Skipping inventory ID {} due to null quantity or reorder level", item.getId());
//                        return false;
//                    }
//
//                    return quantity <= reorderLevel;
//                })
//
//                .toList();
//        return lowStockItems.stream()
//                 .map(lowStockItem-> modelMapper.map(lowStockItem, InventoryDTO.class))
//                .collect(Collectors.toList());
//    }


    /**
     * Retrieves the total quantity of all inventory records for a given product.
     * <p>
     * ✅ Optimized Approach:
     * This method delegates the summation to a custom repository query executed directly in the database,
     * which is more efficient especially for large datasets, as it avoids fetching all records into memory.
     *
     * @param productId the ID of the product
     * @return the total quantity of inventory for the given product
     */
    @Override
    public Integer getTotalQuantityByProductId(Long productId) {
        return inventoryRepository.getTotalQuantityByProductId(productId);
    }


    /**
     * Retrieves the total quantity of all inventory records for a given product.
     * <p>
     * ⚠ Less Optimized Approach:
     * This method fetches all inventory records for the product into memory
     * and then uses Java Streams to calculate the sum. While functionally correct,
     * it is less efficient for large datasets due to increased memory usage and network overhead.
     *
     * @param productId the ID of the product
     * @return the total quantity of inventory for the given product
     */
//    @Override
//    public Integer getTotalQuantityByProductId(Long productId) {
//        return inventoryRepository.findByProductId(productId)
//                .stream()
//                .mapToInt(Inventory::getQuantity)
//                .sum();
//    }

    /*
    * if you want handle null check in query side then COALESCE is better choice
    @Query("SELECT COALESCE(SUM(i.quantity), 0) FROM Inventory i WHERE i.product.id = :productId")
    Integer getTotalQuantityByProductId(@Param("productId") Long productId);

    @Query("SELECT COALESCE(SUM(i.reservedQuantity), 0) FROM Inventory i WHERE i.product.id = :productId")
    Integer getTotalReservedQuantityByProductId(@Param("productId") Long productId);
    * */
    @Override
    public Integer getAvailableQuantityByProductId(Long productId) {

        Integer totalQuantity = inventoryRepository.getTotalQuantityByProductId(productId);
         Integer reservedQuantity = inventoryRepository.getTotalReservedQuantityByProductId(productId);
         return (totalQuantity != null ? totalQuantity : 0) - (reservedQuantity != null ? reservedQuantity : 0);
    }

    /**
     * Reserve inventory for an order across multiple locations
     */
    @Override
    public void reserveInventory(Long productId, Integer quantity) {
        if (quantity<=0){
            throw new IllegalArgumentException("Quantity must be positive");
        }
        List<Inventory> productIdOrderByQuantityDesc = inventoryRepository.findByProductIdOrderByQuantityDesc(productId);
        if (productIdOrderByQuantityDesc.isEmpty()){
            throw new ResourceNotFoundException("product Not found");
        }
        int remainingToReserve=quantity;
        for(Inventory inventory : productIdOrderByQuantityDesc){
            if (remainingToReserve<=0) break;

            int availableQuantity=inventory.getQuantity()-inventory.getReservedQuantity();
            if (availableQuantity>0){
                int toReserve = Math.min(remainingToReserve, availableQuantity);
                inventory.setReservedQuantity(inventory.getReservedQuantity()+toReserve);
                remainingToReserve-=toReserve;
                inventoryRepository.save(inventory);
            }
            if (remainingToReserve > 0) {
                throw new IllegalArgumentException("Insufficient inventory to reserve");
            }
        }

    }

    /**
     * Release reserved inventory back to available stock
     */

    @Override
    public void releaseReservedInventory(Long productId, Integer quantity) {
        List<Inventory> inventories = inventoryRepository.findByProductId(productId);

        int remainingToRelease = quantity;
        for (Inventory inventory : inventories) {
            if (remainingToRelease <= 0) break;

            if (inventory.getReservedQuantity() > 0) {
                int toRelease = Math.min(remainingToRelease, inventory.getReservedQuantity());
                inventory.setReservedQuantity(inventory.getReservedQuantity() - toRelease);
                remainingToRelease -= toRelease;
                inventoryRepository.save(inventory);
            }
        }
    }
}
