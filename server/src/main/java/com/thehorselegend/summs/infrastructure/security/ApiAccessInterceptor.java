package com.thehorselegend.summs.infrastructure.security;

import com.thehorselegend.summs.domain.analytics.ApiAccessEvent;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * HTTP interceptor for tracking API access.
 * Intercepts all requests and publishes ApiAccessEvent for analytics tracking.
 * Integrates with Spring's HandlerInterceptor to capture pre- and post-request information.
 */
@Component
public class ApiAccessInterceptor implements HandlerInterceptor {

    private final ApplicationEventPublisher eventPublisher;
    private static final String REQUEST_START_TIME = "requestStartTime";

    public ApiAccessInterceptor(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    // Mark the request start time before processing.
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        request.setAttribute(REQUEST_START_TIME, System.currentTimeMillis());
        return true;
    }

    // After the request is fully processed, publish an analytics event.
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                               Object handler, Exception ex) throws Exception {
        long startTime = (long) request.getAttribute(REQUEST_START_TIME);
        long processingTimeMs = System.currentTimeMillis() - startTime;

        String endpoint = request.getRequestURI();
        String method = request.getMethod();
        int httpStatus = response.getStatus();

        // Publish event for analytics processing
        ApiAccessEvent event = new ApiAccessEvent(
                this,
                endpoint,
                method,
                httpStatus,
                processingTimeMs
        );
        eventPublisher.publishEvent(event);
    }
}
