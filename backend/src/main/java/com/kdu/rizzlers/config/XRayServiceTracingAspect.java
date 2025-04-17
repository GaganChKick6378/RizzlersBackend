package com.kdu.rizzlers.config;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.entities.Subsegment;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Aspect to trace service operations with AWS X-Ray
 */
@Aspect
@Component
public class XRayServiceTracingAspect {

    /**
     * Pointcut for service methods
     */
    @Pointcut("execution(* com.kdu.rizzlers.service.*.*(..))")
    public void serviceMethodPointcut() {}
    
    /**
     * Pointcut for service implementation methods
     */
    @Pointcut("execution(* com.kdu.rizzlers.service.impl.*.*(..))")
    public void serviceImplMethodPointcut() {}

    /**
     * Create X-Ray subsegment for service operations
     */
    @Around("serviceMethodPointcut() || serviceImplMethodPointcut()")
    public Object traceServiceOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        
        // Skip simple getters and setters
        if (isSimpleGetterOrSetter(methodName)) {
            return joinPoint.proceed();
        }
        
        Subsegment subsegment = AWSXRay.beginSubsegment("Service: " + className + "." + methodName);
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("class", className);
            metadata.put("method", methodName);
            subsegment.putMetadata("service", metadata);
            
            return joinPoint.proceed();
        } catch (Exception e) {
            subsegment.addException(e);
            throw e;
        } finally {
            AWSXRay.endSubsegment();
        }
    }
    
    /**
     * Checks if a method is a simple getter or setter
     */
    private boolean isSimpleGetterOrSetter(String methodName) {
        return (methodName.startsWith("get") && methodName.length() > 3) || 
               (methodName.startsWith("set") && methodName.length() > 3) || 
               (methodName.startsWith("is") && methodName.length() > 2);
    }
} 