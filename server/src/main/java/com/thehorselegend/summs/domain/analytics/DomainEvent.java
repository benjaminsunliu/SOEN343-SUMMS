package com.thehorselegend.summs.domain.analytics;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base class for all domain events.
 * Provides common functionality for event tracking and timing.
 * 
 * NOTE: Currently, no other class implements this. 
 * This was created in anticipation of Phase 4, if we need *more* analytics
 * We can created custom ones (something like RentalCreatedEvent, as an example).
 */
public abstract class DomainEvent {

    private final String eventId;
    private final LocalDateTime timestamp;

    protected DomainEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
    }

    public String getEventId() {
        return eventId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
