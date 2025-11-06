package com.ecom.fulfillment.service;

import com.ecom.fulfillment.entity.Fulfillment;
import com.ecom.fulfillment.model.request.AssignDriverRequest;
import com.ecom.fulfillment.model.request.CreateFulfillmentRequest;
import com.ecom.fulfillment.model.request.UpdateFulfillmentStatusRequest;
import com.ecom.fulfillment.model.response.FulfillmentResponse;

import java.util.List;
import java.util.UUID;

/**
 * Fulfillment Service Interface
 */
public interface FulfillmentService {
    
    /**
     * Create fulfillment for an order
     */
    FulfillmentResponse createFulfillment(UUID tenantId, CreateFulfillmentRequest request);
    
    /**
     * Get fulfillment by ID
     */
    FulfillmentResponse getFulfillmentById(UUID fulfillmentId, UUID tenantId, List<String> userRoles);
    
    /**
     * Get fulfillment by order ID
     */
    FulfillmentResponse getFulfillmentByOrderId(UUID orderId, UUID tenantId, List<String> userRoles);
    
    /**
     * Assign driver to fulfillment
     */
    FulfillmentResponse assignDriver(UUID fulfillmentId, UUID tenantId, List<String> userRoles, AssignDriverRequest request);
    
    /**
     * Update fulfillment status
     */
    FulfillmentResponse updateStatus(UUID fulfillmentId, UUID tenantId, List<String> userRoles, UpdateFulfillmentStatusRequest request);
    
    /**
     * Auto-create fulfillment from OrderCreatedEvent
     */
    void createFulfillmentFromOrder(UUID orderId, UUID tenantId, UUID deliveryAddressId);
    
    /**
     * Check if user can access fulfillment
     */
    boolean canAccessFulfillment(UUID currentUserId, UUID fulfillmentUserId, List<String> userRoles);
}

