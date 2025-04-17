package com.kdu.rizzlers.config;

import jakarta.servlet.Filter;
import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.AWSXRayRecorderBuilder;
import com.amazonaws.xray.jakarta.servlet.AWSXRayServletFilter;
import com.amazonaws.xray.plugins.ECSPlugin;
import com.amazonaws.xray.plugins.EC2Plugin;
import com.amazonaws.xray.strategy.sampling.LocalizedSamplingStrategy;
import com.amazonaws.xray.strategy.LogErrorContextMissingStrategy;
import com.amazonaws.xray.entities.Segment;
import com.amazonaws.xray.entities.Subsegment;
import com.amazonaws.xray.sql.postgres.TracingInterceptor;

@Configuration
public class XRayConfig {

    static {
        AWSXRayRecorderBuilder builder = AWSXRayRecorderBuilder.standard()
                .withPlugin(new ECSPlugin())
                .withPlugin(new EC2Plugin())
                .withContextMissingStrategy(new LogErrorContextMissingStrategy())
                .withSamplingStrategy(new LocalizedSamplingStrategy(
                        XRayConfig.class.getClassLoader().getResource("sampling-rules.json")));
        
        AWSXRay.setGlobalRecorder(builder.build());
    }

    @Bean
    public Filter awsXRayServletFilter() {
        return new AWSXRayServletFilter("Rizzlers-Backend");
    }
    
    @Bean
    public TracingInterceptor postgresqlInterceptor() {
        return new TracingInterceptor();
    }
} 