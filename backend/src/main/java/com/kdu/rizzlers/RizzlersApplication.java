package com.kdu.rizzlers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.amazonaws.xray.spring.aop.XRayEnabled;

@SpringBootApplication
@EnableScheduling
@XRayEnabled
public class RizzlersApplication {

    public static void main(String[] args) {
        // Set system property to ensure health checks work for AWS load balancer
        System.setProperty("server.tomcat.accesslog.enabled", "true");
        SpringApplication.run(RizzlersApplication.class, args);
    }
    
    /**
     * Register an additional dispatcher servlet at root context to handle health checks
     * This ensures health checks work even with a custom context path
     */
    @Bean
    public ServletRegistrationBean<DispatcherServlet> rootDispatcherServlet(DispatcherServlet dispatcherServlet) {
        ServletRegistrationBean<DispatcherServlet> servletRegistrationBean = 
            new ServletRegistrationBean<>(dispatcherServlet, "/", "/ping", "/health", "/healthcheck");
        servletRegistrationBean.setName("rootDispatcher");
        servletRegistrationBean.setLoadOnStartup(1);
        return servletRegistrationBean;
    }
} 