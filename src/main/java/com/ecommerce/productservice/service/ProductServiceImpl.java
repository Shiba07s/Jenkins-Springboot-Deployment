package com.ecommerce.productservice.service;

import com.ecommerce.productservice.dto.ProductDTO;
import com.ecommerce.productservice.dto.TagDTO;
import com.ecommerce.productservice.entity.Inventory;
import com.ecommerce.productservice.entity.Product;
import com.ecommerce.productservice.entity.ProductImage;
import com.ecommerce.productservice.entity.Tag;
import com.ecommerce.productservice.repository.ProductRepository;
import com.ecommerce.productservice.repository.TagRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final ProductRepository productRepository;
      private final ModelMapper modelMapper;
    private final TagRepository tagRepository;

    @Override
    public ProductDTO createNewProduct(ProductDTO productDTO) {
        Product product = modelMapper.map(productDTO, Product.class);
//        product.setCategory(categoryRepository.findById(productDTO.getCategoryId())
//                .orElseThrow(() -> new EntityNotFoundException("Category not found")));
//        product.setSeller(sellerRepository.findById(productDTO.getSellerId())
//                .orElseThrow(() -> new EntityNotFoundException("Seller not found")));
        // ✅ Convert and assign images
        List<ProductImage> images = productDTO.getImages().stream().map(dto -> {
            ProductImage image = new ProductImage();
            image.setImageUrl(dto.getImageUrl());
            image.setAltText(dto.getAltText());
            image.setSortOrder(dto.getSortOrder());
            image.setPrimary(dto.isPrimary());
            image.setCreatedAt(LocalDateTime.now());
            image.setUpdatedAt(LocalDateTime.now());

            // 🔑 Set the parent product reference
            image.setProduct(product);
            return image;
        }).collect(Collectors.toList());
        product.setImages(images);

        // ✅ Convert and assign inventory items
        List<Inventory> inventoryList = productDTO.getInventory().stream().map(dto -> {
            Inventory inventory = new Inventory();
            inventory.setQuantity(dto.getQuantity());
            inventory.setReservedQuantity(dto.getReservedQuantity() != null ? dto.getReservedQuantity() : 0);
            inventory.setReorderLevel(dto.getReorderLevel() != null ? dto.getReorderLevel() : 10);
            inventory.setWarehouseLocation(dto.getWarehouseLocation());
            inventory.setCreatedAt(LocalDateTime.now());
            inventory.setUpdatedAt(LocalDateTime.now());

            // 🔑 Link to product
            inventory.setProduct(product);
            return inventory;
        }).collect(Collectors.toList());
        product.setInventory(inventoryList);

        Set<Tag> tags = new HashSet<>();
        for (TagDTO tagDTO : productDTO.getTags()) {
            Tag tag = tagRepository.findById(tagDTO.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Tag not found with id: " + tagDTO.getId()));
            tags.add(tag);
        }
        product.getTags().clear(); // Clear first
        product.getTags().addAll(tags);
      //  product.setTags(tags);

        Product saved = productRepository.save(product);
        logger.info("Patient created successfully: {}", saved);
        ProductDTO savedDto = modelMapper.map(saved, ProductDTO.class);

        // Create and publish event

        // Send to Kafka
         logger.info("Product created event published to Kafka");
        return savedDto;
    }

    @Override
    public ProductDTO getProductDetailsById(long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        return modelMapper.map(product, ProductDTO.class);
    }

    @Override
    public List<ProductDTO> getAllProductDetails() {
        return productRepository.findAll()
                .stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .collect(Collectors.toList());
    }


    //do not update the created time
    @Override
    public ProductDTO updateProductDetails(long productId, ProductDTO productDTO) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        modelMapper.map(productDTO, existingProduct);

//        if (productDTO.getCategoryId() != null) {
//            existingProduct.setCategory(categoryRepository.findById(productDTO.getCategoryId())
//                    .orElseThrow(() -> new EntityNotFoundException("Category not found")));
//        }
//
//        if (productDTO.getSellerId() != null) {
//            existingProduct.setSeller(sellerRepository.findById(productDTO.getSellerId())
//                    .orElseThrow(() -> new EntityNotFoundException("Seller not found")));
//        }

        Product updated = productRepository.save(existingProduct);
        return modelMapper.map(updated, ProductDTO.class);
    }

    @Override
    public void deleteProducts(long productId) {
        if (!productRepository.existsById(productId)) {
            throw new EntityNotFoundException("Product not found");
        }
        productRepository.deleteById(productId);
    }
}
