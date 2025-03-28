package com.kdu.rizzlers.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Generic pagination response wrapper for API endpoints
 * @param <T> The type of content being paginated
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
        int totalElements = content.size();
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        
        // Calculate the sliced content for the requested page
        int start = Math.min(pageNumber * pageSize, totalElements);
        int end = Math.min((pageNumber + 1) * pageSize, totalElements);
        List<T> pageContent = content.subList(start, end);
        
        return PageResponse.<T>builder()
                .content(pageContent)
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .last(pageNumber >= totalPages - 1)
                .build();
    }
} 