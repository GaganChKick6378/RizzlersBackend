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
 * Aspect to trace GraphQL operations with AWS X-Ray
 */
@Aspect
@Component
public class XRayGraphQLTracingAspect {

    /**
     * Pointcut for GraphQL controllers
     */
    @Pointcut("@within(org.springframework.graphql.data.method.annotation.QueryMapping) || " +
              "@within(org.springframework.graphql.data.method.annotation.MutationMapping) || " +
              "@within(org.springframework.graphql.data.method.annotation.SubscriptionMapping)")
    public void graphqlControllerPointcut() {}

    /**
     * Pointcut for GraphQL controller methods
     */
    @Pointcut("@annotation(org.springframework.graphql.data.method.annotation.QueryMapping) || " +
              "@annotation(org.springframework.graphql.data.method.annotation.MutationMapping) || " +
              "@annotation(org.springframework.graphql.data.method.annotation.SubscriptionMapping)")
    public void graphqlMethodPointcut() {}

    /**
     * Create X-Ray subsegment for GraphQL operations
     */
    @Around("graphqlControllerPointcut() || graphqlMethodPointcut()")
    public Object traceGraphQLOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        String operationName = joinPoint.getSignature().getDeclaringType().getSimpleName() + "." + 
                              joinPoint.getSignature().getName();
        
        Subsegment subsegment = AWSXRay.beginSubsegment("GraphQL: " + operationName);
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("operation", operationName);
            subsegment.putMetadata("graphql", metadata);
            
            return joinPoint.proceed();
        } catch (Exception e) {
            subsegment.addException(e);
            throw e;
        } finally {
            AWSXRay.endSubsegment();
        }
    }
} 