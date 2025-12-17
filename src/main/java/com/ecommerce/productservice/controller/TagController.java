package com.ecommerce.productservice.controller;


import com.ecommerce.productservice.dto.TagDTO;
import com.ecommerce.productservice.service.TagService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
public class TagController {

    private static final Logger logger = LoggerFactory.getLogger(TagController.class);
    private final TagService tagService;

    // Create a new tag
    @PostMapping("/create")
    public ResponseEntity<TagDTO> create(@RequestBody TagDTO tagDTO) {
        logger.info("Creating tag with details: {}", tagDTO);
        TagDTO newTag = tagService.createNewTag(tagDTO);
        return new ResponseEntity<>(newTag, HttpStatus.CREATED);
    }

    // Get all tags
    @GetMapping
    public ResponseEntity<List<TagDTO>> getAllTags() {
        logger.info("Fetching all tags");
        List<TagDTO> tags = tagService.getAllTagDetails();
        return new ResponseEntity<>(tags, HttpStatus.OK);
    }

    // Get a tag by ID
    @GetMapping("/{tagId}")
    public ResponseEntity<TagDTO> getTagById(@PathVariable("tagId") Long tagId) {
        logger.info("Fetching tag by ID: {}", tagId);
        TagDTO tag = tagService.getTagById(tagId);
        return new ResponseEntity<>(tag, HttpStatus.OK);
    }

    // Update a tag
    @PutMapping("/{tagId}")
    public ResponseEntity<TagDTO> updateTag(@PathVariable("tagId") Long tagId, @RequestBody TagDTO tagDTO) {
        logger.info("Updating tag ID: {} with details: {}", tagId, tagDTO);
        TagDTO updatedTag = tagService.updateTagDetails(tagId, tagDTO);
        return new ResponseEntity<>(updatedTag, HttpStatus.OK);
    }

    // Delete a tag
    @DeleteMapping("/{tagId}")
    public ResponseEntity<String> deleteTag(@PathVariable("tagId") Long tagId) {
        logger.info("Deleting tag with ID: {}", tagId);
        tagService.deleteTagDetails(tagId);
        return new ResponseEntity<>("Tag deleted successfully", HttpStatus.NO_CONTENT);
    }
}
