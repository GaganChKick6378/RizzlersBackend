package com.kdu.rizzlers.dto.in;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for URL shortening requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrlShortenRequest {

    @NotBlank(message = "URL cannot be empty")
    @Pattern(regexp = "^(https?|ftp)://.*$", message = "Invalid URL format. Must start with http://, https://, or ftp://")
    private String url;
} 