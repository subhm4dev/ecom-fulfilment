package com.ecom.fulfillment.service;

import com.ecom.fulfillment.model.request.UpdateDeliveryPreferenceRequest;
import com.ecom.fulfillment.model.response.DeliveryPreferenceResponse;

import java.util.UUID;

/**
 * Service for delivery preferences management
 */
public interface DeliveryPreferenceService {
    
    /**
     * Update delivery preferences
     */
    DeliveryPreferenceResponse updatePreferences(
        UUID fulfillmentId,
        UUID customerUserId,
        UUID tenantId,
        UpdateDeliveryPreferenceRequest request
    );
    
    /**
     * Get preferences for a fulfillment
     */
    DeliveryPreferenceResponse getPreferences(UUID fulfillmentId, UUID tenantId);
}

