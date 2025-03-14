package com.kdu.rizzlers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = "com.kdu.rizzlers.entity")
public class RizzlersApplication {

    public static void main(String[] args) {
        SpringApplication.run(RizzlersApplication.class, args);
    }
} 