package com.ecom.fulfillment.service;

import com.ecom.fulfillment.model.request.CreateProviderRequest;
import com.ecom.fulfillment.model.request.UpdateProviderRequest;
import com.ecom.fulfillment.model.response.ProviderResponse;

import java.util.List;
import java.util.UUID;

/**
 * Delivery Provider Management Service Interface
 * Manages delivery provider configurations (CRUD operations)
 */
public interface DeliveryProviderManagementService {
    
    ProviderResponse createProvider(UUID tenantId, CreateProviderRequest request);
    
    List<ProviderResponse> getAllProviders(UUID tenantId);
    
    ProviderResponse getProviderById(UUID id, UUID tenantId);
    
    ProviderResponse updateProvider(UUID id, UUID tenantId, UpdateProviderRequest request);
    
    void deleteProvider(UUID id, UUID tenantId);
}

