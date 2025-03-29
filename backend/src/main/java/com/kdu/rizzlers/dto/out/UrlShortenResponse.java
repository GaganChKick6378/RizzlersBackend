package com.kdu.rizzlers.dto.out;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for URL shortening responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrlShortenResponse {

    /**
     * The original URL that was shortened.
     */
    private String originalUrl;
    
    /**
     * The shortened URL.
     */
    private String shortUrl;
} 