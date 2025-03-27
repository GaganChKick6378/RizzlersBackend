package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.entity.Property;
import com.kdu.rizzlers.service.PropertyGraphQLService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/properties/graphql")
public class PropertyController {

    private final PropertyGraphQLService propertyGraphQLService;

    @Autowired
    public PropertyController(PropertyGraphQLService propertyGraphQLService) {
        this.propertyGraphQLService = propertyGraphQLService;
    }

    @GetMapping("/name/{propertyName}")
    public ResponseEntity<Property> getPropertyByName(@PathVariable String propertyName) {
        Property property = propertyGraphQLService.getPropertyByName(propertyName).block();
        if (property == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(property);
    }
}