package com.kdu.rizzlers.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "room_type_images")
@Slf4j
public class RoomTypeImage extends BaseEntity {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;

    @Column(name = "room_type_id", nullable = false)
    private Integer roomTypeId;

    @Column(name = "property_id", nullable = false)
    private Integer propertyId;

    @Column(name = "image_urls", nullable = false)
    private String imageUrlsJson;

    @Transient
    private String[] imageUrls;

    @Column(name = "display_order")
    private Integer displayOrder = 0;
    
    @PostLoad
    private void onLoad() {
        try {
            if (imageUrlsJson != null && !imageUrlsJson.isEmpty()) {
                // Handle PostgreSQL array format like {"url1","url2"}
                if (imageUrlsJson.startsWith("{") && imageUrlsJson.endsWith("}")) {
                    String content = imageUrlsJson.substring(1, imageUrlsJson.length() - 1);
                    List<String> urls = new ArrayList<>();
                    
                    // Split by comma but respect quotes
                    boolean inQuotes = false;
                    StringBuilder currentUrl = new StringBuilder();
                    
                    for (char c : content.toCharArray()) {
                        if (c == '"') {
                            inQuotes = !inQuotes;
                            // Don't include the quotes in the URLs
                            continue;
                        }
                        
                        if (c == ',' && !inQuotes) {
                            urls.add(currentUrl.toString());
                            currentUrl = new StringBuilder();
                            continue;
                        }
                        
                        currentUrl.append(c);
                    }
                    
                    // Add the last URL if there's content
                    if (currentUrl.length() > 0) {
                        urls.add(currentUrl.toString());
                    }
                    
                    this.imageUrls = urls.toArray(new String[0]);
                } else {
                    // Try to parse as JSON array
                    try {
                        this.imageUrls = OBJECT_MAPPER.readValue(imageUrlsJson, String[].class);
                    } catch (Exception ex) {
                        log.error("Failed to parse image URLs JSON: {}", ex.getMessage());
                        this.imageUrls = new String[0];
                    }
                }
            } else {
                this.imageUrls = new String[0];
            }
        } catch (Exception e) {
            log.error("Error processing image URLs: {}", e.getMessage());
            this.imageUrls = new String[0];
        }
    }

    @PrePersist
    @PreUpdate
    private void beforeSave() {
        try {
            if (imageUrls != null && imageUrls.length > 0) {
                this.imageUrlsJson = OBJECT_MAPPER.writeValueAsString(imageUrls);
            } else if (imageUrlsJson == null || imageUrlsJson.isEmpty()) {
                this.imageUrlsJson = "[]";
            }
        } catch (JsonProcessingException e) {
            log.error("Error serializing image URLs: {}", e.getMessage());
            this.imageUrlsJson = "[]";
        }
    }
} 