package com.kdu.rizzlers.config;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.AWSXRayRecorderBuilder;
import com.amazonaws.xray.jakarta.servlet.AWSXRayServletFilter;
import com.amazonaws.xray.plugins.EC2Plugin;
import com.amazonaws.xray.plugins.ECSPlugin;
import com.amazonaws.xray.strategy.sampling.LocalizedSamplingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.servlet.Filter;
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
     * Register X-Ray servlet filter to trace HTTP requests
     * The segment name should match your application name in X-Ray console
     */
    @Bean
    public Filter xrayFilter() {
        return new AWSXRayServletFilter("rizzlers-backend");
    }
} 