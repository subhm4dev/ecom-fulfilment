package com.ecom.fulfillment.provider.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Tracking response from delivery provider
 */
public record TrackingResponse(
    @JsonProperty("provider_tracking_id")
    String providerTrackingId,
    
    String status,
    
    @JsonProperty("current_location")
    String currentLocation,
    
    BigDecimal latitude,
    
    BigDecimal longitude,
    
    @JsonProperty("estimated_delivery")
    LocalDateTime estimatedDelivery,
    
    @JsonProperty("tracking_events")
    List<TrackingEvent> trackingEvents,
    
    @JsonProperty("provider_response")
    Object providerResponse  // Raw response from provider API
) {
    public record TrackingEvent(
        String status,
        String location,
        @JsonProperty("event_time")
        LocalDateTime eventTime,
        String description
    ) {}
}

