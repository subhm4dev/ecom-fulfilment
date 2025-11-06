package com.ecom.fulfillment.provider.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO from delivery provider after creating shipment
 */
public record CreateShipmentResponse(
    @JsonProperty("provider_tracking_id")
    String providerTrackingId,
    
    @JsonProperty("tracking_url")
    String trackingUrl,
    
    @JsonProperty("awb_number")
    String awbNumber,  // Airway Bill Number (for courier services)
    
    String status,
    
    @JsonProperty("estimated_delivery")
    LocalDateTime estimatedDelivery,
    
    @JsonProperty("shipping_cost")
    BigDecimal shippingCost,
    
    @JsonProperty("provider_response")
    Object providerResponse  // Raw response from provider API
) {}

