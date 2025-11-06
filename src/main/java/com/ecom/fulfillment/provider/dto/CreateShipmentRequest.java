package com.ecom.fulfillment.provider.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Request DTO for creating a shipment with delivery provider
 */
public record CreateShipmentRequest(
    @JsonProperty("order_id")
    UUID orderId,
    
    @JsonProperty("fulfillment_id")
    UUID fulfillmentId,
    
    @JsonProperty("tenant_id")
    UUID tenantId,
    
    @JsonProperty("pickup_address")
    Address pickupAddress,
    
    @JsonProperty("delivery_address")
    Address deliveryAddress,
    
    @JsonProperty("package_details")
    PackageDetails packageDetails,
    
    @JsonProperty("cod_amount")
    BigDecimal codAmount,  // Cash on delivery amount (if applicable)
    
    @JsonProperty("delivery_type")
    String deliveryType  // INTERCITY, INTRACITY
) {
    public record Address(
        String name,
        String phone,
        String email,
        @JsonProperty("line1")
        String line1,
        @JsonProperty("line2")
        String line2,
        String city,
        String state,
        @JsonProperty("postal_code")
        String postalCode,
        String country,
        BigDecimal latitude,
        BigDecimal longitude
    ) {}
    
    public record PackageDetails(
        BigDecimal weight,  // in kg
        BigDecimal length,  // in cm
        BigDecimal width,   // in cm
        BigDecimal height,  // in cm
        @JsonProperty("item_count")
        Integer itemCount,
        String description
    ) {}
}

