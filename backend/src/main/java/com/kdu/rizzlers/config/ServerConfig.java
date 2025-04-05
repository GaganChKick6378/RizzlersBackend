package com.kdu.rizzlers.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

/**
 * Configuration class for server-specific information
 */
@Configuration
public class ServerConfig {

    @Value("${spring.application.name:rizzlers-backend}")
    private String applicationName;
    
    @Getter
    private String serverId;
    
    @PostConstruct
    public void init() {
        // Generate a unique server ID that includes hostname and a UUID
        String hostname;
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            hostname = "unknown-host";
        }
        
        // Use last 8 chars of UUID to keep ID short but unique
        String shortUuid = UUID.randomUUID().toString().substring(0, 8);
        
        this.serverId = String.format("%s-%s-%s", 
                applicationName, 
                hostname, 
                shortUuid);
    }
} 