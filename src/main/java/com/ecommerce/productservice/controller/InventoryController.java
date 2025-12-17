package com.ecommerce.productservice.controller;


import com.ecommerce.productservice.dto.InventoryDTO;
import com.ecommerce.productservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventories")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    // Create inventory
    @PostMapping("/create")
    public ResponseEntity<InventoryDTO> createInventory(@RequestBody InventoryDTO inventoryDTO) {
        InventoryDTO created = inventoryService.createInventory(inventoryDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Get inventory by ID
    @GetMapping("/{inventoryId}")
    public ResponseEntity<InventoryDTO> getInventoryById(@PathVariable Long inventoryId) {
        InventoryDTO inventory = inventoryService.inventoryDetailsById(inventoryId);
        return ResponseEntity.ok(inventory);
    }

    // Get all inventory records
    @GetMapping
    public ResponseEntity<List<InventoryDTO>> getAllInventories() {
        List<InventoryDTO> inventories = inventoryService.getAllInventoryDetails();
        return ResponseEntity.ok(inventories);
    }

    // Get inventory by product ID
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<InventoryDTO>> getInventoriesByProductId(@PathVariable Long productId) {
        List<InventoryDTO> inventories = inventoryService.getInventoryDetailsByProductId(productId);
        return ResponseEntity.ok(inventories);
    }

    // Update inventory by ID
    @PutMapping("/update/{inventoryId}")
    public ResponseEntity<InventoryDTO> updateInventory(@PathVariable Long inventoryId,
                                                        @RequestBody InventoryDTO inventoryDTO) {
        InventoryDTO updated = inventoryService.updateInventoryDetails(inventoryId, inventoryDTO);
        return ResponseEntity.ok(updated);
    }

    // Delete inventory by ID
    @DeleteMapping("/delete/{inventoryId}")
    public ResponseEntity<?> deleteInventory(@PathVariable Long inventoryId) {
        inventoryService.deleteInventoryDetails(inventoryId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Inventory Details Succesfully deleted !!!");// 204 No Content
    }

    // Get low-stock items (quantity < reorder level)
    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryDTO>> getLowStockItems() {
        List<InventoryDTO> lowStockItems = inventoryService.getLowStockItems();
        return ResponseEntity.ok(lowStockItems);
    }

    // Get total quantity of inventory for a product
    @GetMapping("/product/{productId}/total")
    public ResponseEntity<Integer> getTotalQuantity(@PathVariable Long productId) {
        Integer total = inventoryService.getTotalQuantityByProductId(productId);
        return ResponseEntity.ok(total);
    }

    // Get available quantity (total - reserved) for a product
    @GetMapping("/product/{productId}/available")
    public ResponseEntity<Integer> getAvailableQuantity(@PathVariable Long productId) {
        Integer available = inventoryService.getAvailableQuantityByProductId(productId);
        return ResponseEntity.ok(available);
    }

    // Reserve quantity of inventory
    @PostMapping("/product/{productId}/reserve")
    public ResponseEntity<String> reserveInventory(@PathVariable Long productId,
                                                   @RequestParam("quantity") Integer quantity) {
        inventoryService.reserveInventory(productId, quantity);
        return ResponseEntity.ok("Inventory reserved successfully.");
    }

    // Release reserved inventory
    @PostMapping("/product/{productId}/release")
    public ResponseEntity<String> releaseReservedInventory(@PathVariable Long productId,
                                                           @RequestParam("quantity") Integer quantity) {
        inventoryService.releaseReservedInventory(productId, quantity);
        return ResponseEntity.ok("Reserved inventory released successfully.");
    }
}
