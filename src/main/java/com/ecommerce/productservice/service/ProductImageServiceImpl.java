package com.ecommerce.productservice.service;

import com.ecommerce.productservice.dto.ProductImageDTO;

import com.ecommerce.productservice.entity.Product;
import com.ecommerce.productservice.entity.ProductImage; 
import com.ecommerce.productservice.exception.ResourceNotFoundException;
import com.ecommerce.productservice.repository.ProductImageRepository;
import com.ecommerce.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Service
public class ProductImageServiceImpl implements ProductImageService{
    private static final Logger logger = LoggerFactory.getLogger(ProductImageServiceImpl.class);
    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
 //   private static final String PRODUCTIMAGE_TOPIC = "productImage-event";
 //   private final KafkaTemplate<String, ProductImageEvent> kafkaTemplate;

    @Override
    public ProductImageDTO uploadProductImage(ProductImageDTO productImageDTO) {
        Product productDetails = productRepository.findById(productImageDTO.getProductId()).orElseThrow(() -> new ResourceNotFoundException("Product is not found "));
        ProductImage map = modelMapper.map(productImageDTO, ProductImage.class);
        map.setProduct(productDetails);
        // 🟡 If marked as primary, unset primary for other images of this product
        if (map.isPrimary()) {
            List<ProductImage> existingImages = productImageRepository.findByProductId(productDetails.getId());
            for (ProductImage existing : existingImages) {
                if (existing.isPrimary()) {
                    existing.setPrimary(false);
                    productImageRepository.save(existing);
                }
            }
        }
        ProductImage save = productImageRepository.save(map);

 /*       // Create and publish event
        ProductImageEvent event = modelMapper.map(save, ProductImageEvent.class);
        event.setEventType(ProductImageEvent.EventType.CREATED);

        // Send to Kafka
        kafkaTemplate.send(PRODUCTIMAGE_TOPIC, String.valueOf(save.getId()), event);
        logger.info("ProductImage created event published to Kafka");*/
        return modelMapper.map(save,ProductImageDTO.class);
    }

    @Override
    public ProductImageDTO fetchProductImageById(long productImageId) {
        ProductImage productImage = productImageRepository.findById(productImageId)
                .orElseThrow(() -> new ResourceNotFoundException("product image is not found with this id : " + productImageId));
        return modelMapper.map(productImage,ProductImageDTO.class);
    }

    @Override
    public List<ProductImageDTO> listAllProductImages(long productId) {
        List<ProductImage> all = productImageRepository.findByProductIdOrderBySortOrder(productId);
        return all.stream()
                .map(images->modelMapper.map(images,ProductImageDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public ProductImageDTO updateProductImageInfo(long productImageId, ProductImageDTO productImageDTO) {
        Product productDetails = productRepository.findById(productImageDTO.getProductId()).orElseThrow(() -> new ResourceNotFoundException("Product is not found "));

        ProductImage productImage = productImageRepository.findById(productImageId).orElseThrow(() -> new ResourceNotFoundException("product image is not found with this id : " + productImageId));

        productImage.setImageUrl(productImageDTO.getImageUrl());
        productImage.setAltText(productImageDTO.getAltText());
        productImage.setSortOrder(productImageDTO.getSortOrder());
        productImage.setProduct(productDetails);
        productImage.setUpdatedAt(LocalDateTime.now());
        return null;
    }

    @Override
    public void deleteProductImageById(long productImageId) {

        if (!productImageRepository.existsById(productImageId)){
            throw new ResourceNotFoundException("Image is not found");
         }
        productImageRepository.deleteById(productImageId);
    }

    /*
    *
    *get the primary image using stream api(for small datasets is good)
    *
    */

//    @Override
//    public ProductImageDTO getPrimaryImageByProductId(Long productId) {
//        Optional<ProductImage> byId = productImageRepository.findById(productId);
//
//        Boolean image = byId.stream()
//                .map(ProductImage::isPrimary).findFirst()
//                .orElseThrow(() -> new ResourceNotFoundException("Primary image not found for product ID: " + productId));
//
//        return modelMapper.map(image,ProductImageDTO.class);
//    }

    /*
     *
     *get the primary image using query(for large datasets is good)
     *
     */

    @Override
    public ProductImageDTO getPrimaryImageByProductId(Long productId) {
        ProductImage byProductIdAndPrimaryTrue = productImageRepository.findByProductIdAndPrimaryTrue(productId);

        if (byProductIdAndPrimaryTrue!=null){
            throw new ResourceNotFoundException("Primary image is not found");
        }
        return modelMapper.map(byProductIdAndPrimaryTrue,ProductImageDTO.class);
    }

    public void setPrimaryImage(Long imageId) {

        ProductImage productImage = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Product image not found"));

        Long productId = productImage.getProduct().getId();

        // Unset other primary images for this product
       List<ProductImage> existingPrimaryImage = productImageRepository.findByProductId(productId);
        for(ProductImage images: existingPrimaryImage){
            if (images.isPrimary()) {
                images.setPrimary(false);
                productImageRepository.save(images);
             }
        }

        // Set the selected image as primary (if not already)
        if (!productImage.isPrimary()){
            productImage.setPrimary(true);
            productImageRepository.save(productImage);
        }

    }

//    @Override
//    public void setPrimaryImage(Long imageId) {
//        ProductImage productImage = productImageRepository.findById(imageId)
//                .orElseThrow(() -> new ResourceNotFoundException("Product image not found with ID: " + imageId));
//
//        Long productId = productImage.getProduct().getId();
//
//        // Unset other primary images for the same product
//        List<ProductImage> images = productImageRepository.findByProductId(productId);
//        for (ProductImage image : images) {
//            if (image.isPrimary()) {
//                image.setPrimary(false);
//                productImageRepository.save(image);
//            }
//        }
//
//        // Set the selected image as primary (if not already)
//        if (!productImage.isPrimary()) {
//            productImage.setPrimary(true);
//            productImageRepository.save(productImage);
//        }
//    }

}
