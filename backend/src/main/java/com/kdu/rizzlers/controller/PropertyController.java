package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.cache.CacheService;
import com.kdu.rizzlers.entity.Property;
import com.kdu.rizzlers.service.PropertyGraphQLService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/properties/graphql")
@Tag(name = "Properties", description = "APIs for property data retrieval via GraphQL")
public class PropertyController {

    private final PropertyGraphQLService propertyGraphQLService;
    @Autowired
    private CacheService cacheService;

    @Autowired
    public PropertyController(PropertyGraphQLService propertyGraphQLService) {
        this.propertyGraphQLService = propertyGraphQLService;
    }

    @GetMapping("/name/{propertyName}")
    @Operation(summary = "Get property by name", description = "Retrieves property information by its name via GraphQL")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Property found",
                   content = @Content(schema = @Schema(implementation = Property.class))),
        @ApiResponse(responseCode = "404", description = "Property not found")
    })
    public ResponseEntity<Property> getPropertyByName(
            @Parameter(description = "Name of the property", required = true)
            @PathVariable String propertyName) {
        String cacheKey = "property:graphql:name:" + propertyName;
        Property cached = cacheService.get(cacheKey, Property.class);
        if (cached != null) {
            return ResponseEntity.ok(cached);
        }
        Property property = propertyGraphQLService.getPropertyByName(propertyName).block();
        if (property == null) {
            return ResponseEntity.notFound().build();
        }
        cacheService.set(cacheKey, property, 10, java.util.concurrent.TimeUnit.MINUTES);
        return ResponseEntity.ok(property);
    }
}