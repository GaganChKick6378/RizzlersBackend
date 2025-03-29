package com.kdu.rizzlers.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic pagination response wrapper for API endpoints
 * @param <T> The type of content being paginated
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class PageResponse<T> {
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
    
    /**
     * Static factory method to create a PageResponse from a list of items
     * 
     * @param <T> The type of content
     * @param content The list of items
     * @param pageNumber The current page number (0-based)
     * @param pageSize The page size
     * @return A populated PageResponse
     */
    public static <T> PageResponse<T> of(List<T> content, int pageNumber, int pageSize) {
        log.info("Creating PageResponse with content size: {}, page: {}, size: {}", 
                content != null ? content.size() : 0, pageNumber, pageSize);
                
        // Ensure valid inputs
        if (pageSize <= 0) {
            log.warn("Invalid page size ({}), using default of 10", pageSize);
            pageSize = 10; // Default page size
        }
        
        if (pageNumber < 0) {
            log.warn("Invalid page number ({}), using default of 0", pageNumber);
            pageNumber = 0; // Default to first page
        }
        
        if (content == null) {
            log.warn("Content is null, returning empty response");
            return emptyPage(pageNumber, pageSize);
        }
        
        int totalElements = content.size();
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        
        // Calculate the sliced content for the requested page
        int start = Math.min(pageNumber * pageSize, totalElements);
        int end = Math.min((pageNumber + 1) * pageSize, totalElements);
        
        log.info("Pagination calculation: total={}, page={}, size={}, start={}, end={}", 
                totalElements, pageNumber, pageSize, start, end);
                
        // Create a new list with only the items for the current page to avoid potential shared list issues
        List<T> pageContent = start < end ? new ArrayList<>(content.subList(start, end)) : new ArrayList<>();
        
        log.info("Created page content with size: {}", pageContent.size());
        
        return PageResponse.<T>builder()
                .content(pageContent)
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .last(pageNumber >= totalPages - 1)
                .build();
    }
    
    /**
     * Creates an empty page response with the given page parameters
     */
    private static <T> PageResponse<T> emptyPage(int pageNumber, int pageSize) {
        return PageResponse.<T>builder()
                .content(new ArrayList<>())
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .totalElements(0)
                .totalPages(0)
                .last(true)
                .build();
    }
} 