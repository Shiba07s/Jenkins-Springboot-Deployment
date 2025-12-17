package com.ecommerce.productservice.service;


import com.ecommerce.productservice.dto.ProductDTO;

import java.util.List;

public interface ProductService {

    ProductDTO createNewProduct(ProductDTO productDTO);

    ProductDTO getProductDetailsById(long productId);

    List<ProductDTO>getAllProductDetails();

    ProductDTO updateProductDetails(long productId,ProductDTO productDTO);

    void deleteProducts(long productId);
}
