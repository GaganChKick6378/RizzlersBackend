package com.kdu.rizzlers.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.entities.Segment;

/**
 * Aspect to handle X-Ray tracing for methods annotated with @XRayTraced
 */
@Aspect
@Configuration
public class XRayInitializationConfig {

    @Value("${aws.xray.enabled:true}")
    private boolean xrayEnabled;
    
    @Value("${aws.xray.tracing-during-initialization:false}")
    private boolean tracingDuringInitialization;

    /**
     * Creates a new X-Ray segment for any method annotated with @XRayTraced
     */
    @Around("@annotation(xRayTraced)")
    public Object traceMethod(ProceedingJoinPoint pjp, XRayTraced xRayTraced) throws Throwable {
        // Skip tracing if X-Ray is disabled or tracing during initialization is disabled
        if (!xrayEnabled || !tracingDuringInitialization) {
            return pjp.proceed();
        }
        
        String name = xRayTraced.value();
        if (name.isEmpty()) {
            name = pjp.getSignature().getDeclaringType().getSimpleName() + "." + pjp.getSignature().getName();
        }
        
        // Create a new segment for this method
        Segment segment = AWSXRay.beginSegment("Initialization: " + name);
        
        try {
            // Execute the method
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