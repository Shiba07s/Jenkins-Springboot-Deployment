package com.ecommerce.productservice.service;

import com.ecommerce.productservice.dto.ProductImageDTO;

import java.util.List;

public interface ProductImageService {

    ProductImageDTO uploadProductImage(ProductImageDTO productImageDTO);

    ProductImageDTO fetchProductImageById(long productImageId);

    List<ProductImageDTO> listAllProductImages(long productImageId);

    ProductImageDTO updateProductImageInfo(long productImageId, ProductImageDTO productImageDTO);

    void deleteProductImageById(long productId);

    ProductImageDTO getPrimaryImageByProductId(Long productId);

    void setPrimaryImage(Long imageId);
}
