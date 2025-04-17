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
 * Aspect to trace database operations with AWS X-Ray
 */
@Aspect
@Component
public class XRayDatabaseTracingAspect {

    /**
     * Pointcut for repository methods
     */
    @Pointcut("execution(* com.kdu.rizzlers.repository.*.*(..))")
    public void repositoryMethodPointcut() {}

    /**
     * Create X-Ray subsegment for database operations
     */
    @Around("repositoryMethodPointcut()")
    public Object traceDatabaseOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        String operationName = joinPoint.getSignature().getDeclaringType().getSimpleName() + "." + 
                              joinPoint.getSignature().getName();
        
        Subsegment subsegment = AWSXRay.beginSubsegment("Database: " + operationName);
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("operation", operationName);
            
            // Add method arguments as metadata (be careful with sensitive data)
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                for (int i = 0; i < args.length; i++) {
                    if (args[i] != null && isPrimitiveOrString(args[i].getClass())) {
                        metadata.put("arg" + i, args[i].toString());
                    }
                }
            }
            
            subsegment.putMetadata("database", metadata);
            
            return joinPoint.proceed();
        } catch (Exception e) {
            subsegment.addException(e);
            throw e;
        } finally {
            AWSXRay.endSubsegment();
        }
    }
    
    /**
     * Checks if a class is a primitive or String
     */
    private boolean isPrimitiveOrString(Class<?> clazz) {
        return clazz.isPrimitive() || 
               clazz.equals(String.class) || 
               clazz.equals(Integer.class) || 
               clazz.equals(Long.class) || 
               clazz.equals(Boolean.class) || 
               clazz.equals(Double.class) || 
               clazz.equals(Float.class);
    }
} 