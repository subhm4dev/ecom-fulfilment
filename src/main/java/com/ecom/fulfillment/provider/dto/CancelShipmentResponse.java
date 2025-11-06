package com.ecom.fulfillment.provider.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response from delivery provider after canceling shipment
 */
public record CancelShipmentResponse(
    @JsonProperty("provider_tracking_id")
    String providerTrackingId,
    
    boolean success,
    
    String message,
    
    @JsonProperty("refund_amount")
    java.math.BigDecimal refundAmount,
    
    @JsonProperty("provider_response")
    Object providerResponse  // Raw response from provider API
) {}

