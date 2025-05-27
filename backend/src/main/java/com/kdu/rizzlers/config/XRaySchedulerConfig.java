package com.kdu.rizzlers.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.entities.Segment;

/**
 * This aspect handles AWS X-Ray tracing for @Scheduled methods.
 * It creates a new segment for each scheduled task execution.
 */
@Aspect
@Configuration
public class XRaySchedulerConfig {
    
    @Value("${aws.xray.enabled:true}")
    private boolean xrayEnabled;

    /**
     * Creates a new X-Ray segment for any method annotated with @Scheduled
     */
    @Around("@annotation(scheduled)")
    public Object traceScheduledTask(ProceedingJoinPoint pjp, Scheduled scheduled) throws Throwable {
        // Skip tracing if X-Ray is disabled
        if (!xrayEnabled) {
            return pjp.proceed();
        }
        
        // Create a name based on the class and method
        String name = pjp.getSignature().getDeclaringType().getSimpleName() + "." + pjp.getSignature().getName();
        
        // Create a new segment for this scheduled task
        Segment segment = AWSXRay.beginSegment("Scheduled: " + name);
        
        try {
            // Execute the scheduled method
            return pjp.proceed();
        } catch (Exception e) {
            // Record any exceptions
            segment.addException(e);
            throw e;
        } finally {
            // Always end the segment
            AWSXRay.endSegment();
        }
    }
} 