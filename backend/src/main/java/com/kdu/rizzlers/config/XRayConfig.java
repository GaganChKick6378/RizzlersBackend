package com.kdu.rizzlers.config;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.AWSXRayRecorderBuilder;
import com.amazonaws.xray.plugins.EC2Plugin;
import com.amazonaws.xray.plugins.ECSPlugin;
import com.amazonaws.xray.strategy.sampling.LocalizedSamplingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.net.URL;

/**
 * AWS X-Ray configuration for distributed tracing
 */
@Configuration
public class XRayConfig {

    /**
     * Initialize AWS X-Ray recorder with ECS plugin
     */
    static {
        AWSXRayRecorderBuilder builder = AWSXRayRecorderBuilder.standard()
                .withPlugin(new ECSPlugin())
                .withPlugin(new EC2Plugin());
        
        URL samplingRules = XRayConfig.class.getResource("/sampling-rules.json");
        if (samplingRules != null) {
            builder.withSamplingStrategy(new LocalizedSamplingStrategy(samplingRules));
        }
        
        AWSXRay.setGlobalRecorder(builder.build());
    }

    /**
     * Custom X-Ray filter implementation for Jakarta compatibility
     */
    @Bean
    public Filter xrayFilter() {
        return (ServletRequest request, ServletResponse response, FilterChain chain) -> {
            HttpServletRequest httpRequest;
            if (request instanceof HttpServletRequest) {
                httpRequest = (HttpServletRequest) request;
            } else if (request instanceof HttpServletRequestWrapper) {
                httpRequest = (HttpServletRequest) ((HttpServletRequestWrapper) request).getRequest();
            } else {
                httpRequest = (HttpServletRequest) request;
            }
                
            String name = httpRequest.getRequestURI();
            AWSXRay.beginSegment("rizzlers-backend: " + name);
            try {
                chain.doFilter(request, response);
            } catch (Exception e) {
                AWSXRay.getCurrentSegment().addException(e);
                throw e;
            } finally {
                AWSXRay.endSegment();
            }
        };
    }
} 