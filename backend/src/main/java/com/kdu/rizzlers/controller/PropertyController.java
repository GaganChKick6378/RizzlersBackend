package com.kdu.rizzlers.controller;

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
        Property property = propertyGraphQLService.getPropertyByName(propertyName).block();
        if (property == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(property);
    }
}