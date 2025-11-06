package com.ecom.fulfillment.controller;

import com.ecom.fulfillment.model.request.TrackDeliveryRequest;
import com.ecom.fulfillment.model.response.DeliveryResponse;
import com.ecom.fulfillment.model.response.TrackingResponse;
import com.ecom.fulfillment.security.JwtAuthenticationToken;
import com.ecom.fulfillment.service.DeliveryService;
import com.ecom.response.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Delivery Controller
 */
@RestController
@RequestMapping("/api/v1/delivery")
@Tag(name = "Delivery", description = "Delivery tracking endpoints")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class DeliveryController {
    
    private final DeliveryService deliveryService;
    
    @PostMapping("/{deliveryId}/track")
    @Operation(summary = "Track delivery location", description = "Updates delivery location (DRIVER only)")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<ApiResponse<DeliveryResponse>> trackDelivery(
            @PathVariable UUID deliveryId,
            @Valid @RequestBody TrackDeliveryRequest request,
            Authentication authentication) {
        
        UUID driverId = getUserIdFromAuthentication(authentication);
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        DeliveryResponse response = deliveryService.trackDelivery(deliveryId, driverId, tenantId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Location updated successfully"));
    }
    
    @GetMapping("/{deliveryId}/tracking")
    @Operation(summary = "Get tracking information", description = "Gets tracking information (public)")
    public ResponseEntity<ApiResponse<TrackingResponse>> getTracking(
            @PathVariable UUID deliveryId,
            @RequestParam String trackingNumber,
            Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        TrackingResponse response = deliveryService.getTracking(trackingNumber, tenantId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/{deliveryId}")
    @Operation(summary = "Get delivery by ID", description = "Retrieves delivery details")
    public ResponseEntity<ApiResponse<DeliveryResponse>> getDelivery(
            @PathVariable UUID deliveryId,
            Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        List<String> roles = getRolesFromAuthentication(authentication);
        DeliveryResponse response = deliveryService.getDeliveryById(deliveryId, tenantId, roles);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PutMapping("/{deliveryId}/complete")
    @Operation(summary = "Complete delivery", description = "Marks delivery as completed (DRIVER only)")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<ApiResponse<DeliveryResponse>> completeDelivery(
            @PathVariable UUID deliveryId,
            Authentication authentication) {
        
        UUID driverId = getUserIdFromAuthentication(authentication);
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        DeliveryResponse response = deliveryService.completeDelivery(deliveryId, driverId, tenantId);
        return ResponseEntity.ok(ApiResponse.success(response, "Delivery completed successfully"));
    }
    
    @GetMapping("/driver/{driverId}")
    @Operation(summary = "Get deliveries by driver", description = "Gets all deliveries for a driver")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<ApiResponse<List<DeliveryResponse>>> getDeliveriesByDriver(
            @PathVariable UUID driverId,
            Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        List<DeliveryResponse> response = deliveryService.getDeliveriesByDriver(driverId, tenantId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    private UUID getUserIdFromAuthentication(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            return UUID.fromString(jwtToken.getUserId());
        }
        throw new IllegalStateException("Invalid authentication token");
    }
    
    private UUID getTenantIdFromAuthentication(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            return UUID.fromString(jwtToken.getTenantId());
        }
        throw new IllegalStateException("Invalid authentication token");
    }
    
    private List<String> getRolesFromAuthentication(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            return jwtToken.getRoles();
        }
        return List.of();
    }
}

