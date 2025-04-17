package com.kdu.rizzlers.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.entities.Subsegment;

@Aspect
@Component
public class XRayTracingAspect {
    
    @Value("${aws.xray.enabled:true}")
    private boolean xrayEnabled;
    
    @Value("${aws.xray.tracing-during-initialization:false}")
    private boolean tracingDuringInitialization;
    
    private static boolean isInitializationPhase = true;
    
    /**
     * Pointcut that matches all Spring components in service layer
     */
    @Pointcut("execution(* com.kdu.rizzlers.service.*.*(..))")
    public void serviceMethod() {}
    
    /**
     * Pointcut that matches all Spring Data repositories
     */
    @Pointcut("execution(* com.kdu.rizzlers.repository.*.*(..))")
    public void repositoryMethod() {}
    
    /**
     * Pointcut that matches all controllers
     */
    @Pointcut("execution(* com.kdu.rizzlers.controller.*.*(..))")
    public void controllerMethod() {}
    
    /**
     * Mark initialization as complete once Spring context is ready
     */
    @jakarta.annotation.PostConstruct
    public void init() {
        // Small delay to ensure all initialization is complete
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                isInitializationPhase = false;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    
    /**
     * Around advice that creates a new X-Ray subsegment for method execution
     */
    @Around("serviceMethod() || repositoryMethod() || controllerMethod()")
    public Object traceMethod(ProceedingJoinPoint pjp) throws Throwable {
        // Skip tracing if X-Ray is disabled or during initialization phase
        if (!xrayEnabled || (isInitializationPhase && !tracingDuringInitialization)) {
            return pjp.proceed();
        }
        
        String className = pjp.getSignature().getDeclaringType().getSimpleName();
        String methodName = pjp.getSignature().getName();
        
        // Try to create a subsegment, proceed normally if it fails (when no segment exists)
        try {
            Subsegment subsegment = AWSXRay.beginSubsegment(className + "." + methodName);
            try {
                return pjp.proceed();
            } catch (Exception e) {
                subsegment.addException(e);
                throw e;
            } finally {
                AWSXRay.endSubsegment();
            }
        } catch (Exception e) {
            // If there's no segment context, just proceed without tracing
            return pjp.proceed();
        }
    }
} 