package com.thehorselegend.summs.infrastructure.config;

import com.thehorselegend.summs.infrastructure.security.ApiAccessInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration for registering interceptors.
 * Registers the ApiAccessInterceptor to track all API requests for analytics.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final ApiAccessInterceptor apiAccessInterceptor;

    public WebMvcConfig(ApiAccessInterceptor apiAccessInterceptor) {
        this.apiAccessInterceptor = apiAccessInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiAccessInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/api/auth/**", "/api/admin/analytics/**");  // Don't track auth or analytics endpoints
    }
}
