package com.ecommerce.productservice.service;


import com.ecommerce.productservice.dto.TagDTO;

import java.util.List;

public interface TagService {

    TagDTO createNewTag(TagDTO tagDTO);

    TagDTO getTagById(long tagId);

    List<TagDTO>getAllTagDetails();

    TagDTO updateTagDetails(long tagId,TagDTO tagDTO);

    void deleteTagDetails(long tagId);

//    List<TagDTO> getTagsByProductId(Long productId);
}
