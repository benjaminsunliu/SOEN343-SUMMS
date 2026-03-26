package com.thehorselegend.summs.domain.analytics;

import org.springframework.context.ApplicationEvent;

/**
 * Event published when an API endpoint is accessed.
 * Used to track gateway-level analytics (number of accesses to various services).
 */
public class ApiAccessEvent extends ApplicationEvent {

    private final String endpoint;
    private final String method;
    private final int httpStatus;
    private final long processingTimeMs;

    public ApiAccessEvent(Object source, String endpoint, String method, int httpStatus, long processingTimeMs) {
        super(source);
        this.endpoint = endpoint;
        this.method = method;
        this.httpStatus = httpStatus;
        this.processingTimeMs = processingTimeMs;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getMethod() {
        return method;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public long getProcessingTimeMs() {
        return processingTimeMs;
    }
}
