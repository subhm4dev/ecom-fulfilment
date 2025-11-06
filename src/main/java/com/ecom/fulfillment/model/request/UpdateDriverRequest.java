package com.ecom.fulfillment.model.request;

import com.ecom.fulfillment.entity.Driver;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request DTO for updating a driver
 */
public record UpdateDriverRequest(
    String name,
    
    String phone,
    
    String email,
    
    @JsonProperty("vehicle_type")
    String vehicleType,
    
    @JsonProperty("vehicle_number")
    String vehicleNumber,
    
    Driver.DriverStatus status
) {}

