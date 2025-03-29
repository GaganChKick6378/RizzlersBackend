package com.kdu.rizzlers.service.impl;

import com.kdu.rizzlers.service.UrlShortenerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Implementation of UrlShortenerService using TinyURL's API.
 * TinyURL offers a simple API that can be used without registration for basic use cases.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TinyUrlShortenerService implements UrlShortenerService {

    private final WebClient.Builder webClientBuilder;
    
    // TinyURL base URL for API
    private static final String TINYURL_API_URL = "https://tinyurl.com/api-create.php";

    @Override
    public String shortenUrl(String longUrl) {
        log.info("Shortening URL: {}", longUrl);
        
        try {
            String url = UriComponentsBuilder.fromHttpUrl(TINYURL_API_URL)
                    .queryParam("url", longUrl)
                    .build()
                    .toUriString();
                    
            String shortUrl = webClientBuilder.build()
                    .get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
                    
            log.info("URL shortened successfully: {} -> {}", longUrl, shortUrl);
            return shortUrl;
        } catch (Exception e) {
            log.error("Error shortening URL: {}", e.getMessage(), e);
            // Return the original URL in case of failure
            return longUrl;
        }
    }

    @Override
    public String getLongUrl(String shortUrl) {
        log.warn("TinyURL API does not support retrieving the original URL from a shortened URL");
        return null;
    }
} 