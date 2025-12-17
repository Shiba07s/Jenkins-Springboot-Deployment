package com.ecommerce.productservice.service;


import com.ecommerce.productservice.dto.TagDTO;

import com.ecommerce.productservice.entity.Tag; 
import com.ecommerce.productservice.exception.ResourceNotFoundException;
import com.ecommerce.productservice.repository.TagRepository;
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
public class TagServiceImpl implements TagService {

    private static final Logger logger = LoggerFactory.getLogger(TagServiceImpl.class);

    private final TagRepository tagRepository;
    private final ModelMapper modelMapper;
//    private static final String TAG_TOPIC = "tag-event";
//    private final KafkaTemplate<String, TagEvent> kafkaTemplate;

    @Override
    public TagDTO createNewTag(TagDTO tagDTO) {
        Tag tag = modelMapper.map(tagDTO, Tag.class);
        logger.info("Creating new tag: name={}, color={}", tag.getName(), tag.getColor());

        Tag savedTag = tagRepository.save(tag);
        logger.info("Tag created successfully with ID: {}", savedTag.getId());

        // Create and publish event
//        TagEvent event = modelMapper.map(savedTag, TagEvent.class);
//        event.setEventType(TagEvent.EventType.CREATED);
//
//        // Send to Kafka
//        kafkaTemplate.send(TAG_TOPIC, String.valueOf(savedTag.getId()), event);
//        logger.info("Tag created event published to Kafka");
        return modelMapper.map(savedTag, TagDTO.class);
    }

    @Override
    public TagDTO getTagById(long tagId) {
        logger.info("Fetching tag with ID: {}", tagId);
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> {
                    logger.error("Tag not found with ID: {}", tagId);
                    return new ResourceNotFoundException("Tag is not present with this id : " + tagId);
                });
        return modelMapper.map(tag, TagDTO.class);
    }

    @Override
    public List<TagDTO> getAllTagDetails() {
        logger.info("Fetching all tag details");
        List<Tag> all = tagRepository.findAll();
        return all.stream().map(tags -> modelMapper.map(tags, TagDTO.class)).collect(Collectors.toList());
    }

    @Override
    public TagDTO updateTagDetails(long tagId, TagDTO tagDTO) {
        logger.info("Updating tag with ID: {}", tagId);
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> {
                    logger.error("Tag not found for update with ID: {}", tagId);
                    return new ResourceNotFoundException("Tag is not present with this id : " + tagId);
                });

        tag.setName(tagDTO.getName());
        tag.setColor(tagDTO.getColor());
        tag.setUpdatedAt(LocalDateTime.now());

        Tag updateTag = tagRepository.save(tag);
        logger.info("Tag updated successfully with ID: {}", updateTag.getId());

        return modelMapper.map(updateTag, TagDTO.class);
    }

    @Override
    public void deleteTagDetails(long tagId) {
        logger.info("Deleting tag with ID: {}", tagId);
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> {
                    logger.error("Tag not found for deletion with ID: {}", tagId);
                    return new ResourceNotFoundException("Tag is not present with this id : " + tagId);
                });

        tagRepository.delete(tag);
        logger.info("Tag deleted successfully with ID: {}", tagId);
    }
}
