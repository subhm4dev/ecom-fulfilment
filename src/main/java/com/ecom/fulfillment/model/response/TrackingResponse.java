package com.ecom.fulfillment.model.response;

import com.ecom.fulfillment.entity.Delivery;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for tracking information (public-facing)
 */
public record TrackingResponse(
    @JsonProperty("tracking_number")
    String trackingNumber,
    
    @JsonProperty("order_id")
    UUID orderId,
    
    Delivery.DeliveryStatus status,
    
    @JsonProperty("current_location")
    String currentLocation,
    
    BigDecimal latitude,
    
    BigDecimal longitude,
    
    @JsonProperty("estimated_delivery")
    LocalDateTime estimatedDelivery,
    
    @JsonProperty("tracking_history")
    List<TrackingHistoryResponse> trackingHistory,
    
    @JsonProperty("updated_at")
    LocalDateTime updatedAt
) {}

