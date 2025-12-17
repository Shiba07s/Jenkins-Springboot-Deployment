package com.ecommerce.productservice.controller;

import com.ecommerce.productservice.dto.ProductImageDTO;
import com.ecommerce.productservice.service.ProductImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/product-images")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductImageService productImageService;

    @PostMapping
    public ResponseEntity<ProductImageDTO> uploadImage(@RequestBody ProductImageDTO productImageDTO) {
        ProductImageDTO saved = productImageService.uploadProductImage(productImageDTO);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{productImageId}")
    public ResponseEntity<ProductImageDTO> getImageById(@PathVariable long productImageId) {
        return ResponseEntity.ok(productImageService.fetchProductImageById(productImageId));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductImageDTO>> getAllImages(@PathVariable long productId) {
        return ResponseEntity.ok(productImageService.listAllProductImages(productId));
    }

    @PutMapping("/update/{productImageId}")
    public ResponseEntity<ProductImageDTO> updateImage(@PathVariable long productImageId, @RequestBody ProductImageDTO productImageDTO) {
        return ResponseEntity.ok(productImageService.updateProductImageInfo(productImageId, productImageDTO));
    }

    @DeleteMapping("/delete/{productImageId}")
    public ResponseEntity<String> deleteImage(@PathVariable long productImageId) {
        productImageService.deleteProductImageById(productImageId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Product image deleted successfully");
    }

    @GetMapping("/product/{productId}/primary")
    public ResponseEntity<ProductImageDTO>getPrimaryImageByProductId(@PathVariable Long productId){
        ProductImageDTO primaryImageByProductId = productImageService.getPrimaryImageByProductId(productId);
        return new ResponseEntity<ProductImageDTO>(primaryImageByProductId,HttpStatus.OK);
    }

    @PatchMapping("/{imageId}/set-primary")
    public ResponseEntity<Void> setPrimaryImage(@PathVariable Long imageId) {
        productImageService.setPrimaryImage(imageId);
        return ResponseEntity.ok().build();
    }


}
