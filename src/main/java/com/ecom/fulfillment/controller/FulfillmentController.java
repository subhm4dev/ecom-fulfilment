package com.ecom.fulfillment.controller;

import com.ecom.fulfillment.model.request.AssignDriverRequest;
import com.ecom.fulfillment.model.request.CreateFulfillmentRequest;
import com.ecom.fulfillment.model.request.UpdateFulfillmentStatusRequest;
import com.ecom.fulfillment.model.response.FulfillmentResponse;
import com.ecom.fulfillment.security.JwtAuthenticationToken;
import com.ecom.fulfillment.service.FulfillmentService;
import com.ecom.response.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Fulfillment Controller
 */
@RestController
@RequestMapping("/api/v1/fulfillment")
@Tag(name = "Fulfillment", description = "Fulfillment management endpoints")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class FulfillmentController {
    
    private final FulfillmentService fulfillmentService;
    
    @PostMapping
    @Operation(summary = "Create fulfillment", description = "Creates a new fulfillment for an order")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<ApiResponse<FulfillmentResponse>> createFulfillment(
            @Valid @RequestBody CreateFulfillmentRequest request,
            Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        FulfillmentResponse response = fulfillmentService.createFulfillment(tenantId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "Fulfillment created successfully"));
    }
    
    @GetMapping("/{fulfillmentId}")
    @Operation(summary = "Get fulfillment by ID", description = "Retrieves fulfillment details")
    public ResponseEntity<ApiResponse<FulfillmentResponse>> getFulfillment(
            @PathVariable UUID fulfillmentId,
            Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        List<String> roles = getRolesFromAuthentication(authentication);
        FulfillmentResponse response = fulfillmentService.getFulfillmentById(fulfillmentId, tenantId, roles);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get fulfillment by order ID", description = "Retrieves fulfillment for an order")
    public ResponseEntity<ApiResponse<FulfillmentResponse>> getFulfillmentByOrderId(
            @PathVariable UUID orderId,
            Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        List<String> roles = getRolesFromAuthentication(authentication);
        FulfillmentResponse response = fulfillmentService.getFulfillmentByOrderId(orderId, tenantId, roles);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PutMapping("/{fulfillmentId}/assign")
    @Operation(summary = "Assign driver", description = "Assigns a driver to fulfillment")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<ApiResponse<FulfillmentResponse>> assignDriver(
            @PathVariable UUID fulfillmentId,
            @Valid @RequestBody AssignDriverRequest request,
            Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        List<String> roles = getRolesFromAuthentication(authentication);
        FulfillmentResponse response = fulfillmentService.assignDriver(fulfillmentId, tenantId, roles, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Driver assigned successfully"));
    }
    
    @PutMapping("/{fulfillmentId}/status")
    @Operation(summary = "Update fulfillment status", description = "Updates fulfillment status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('DRIVER')")
    public ResponseEntity<ApiResponse<FulfillmentResponse>> updateStatus(
            @PathVariable UUID fulfillmentId,
            @Valid @RequestBody UpdateFulfillmentStatusRequest request,
            Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        List<String> roles = getRolesFromAuthentication(authentication);
        FulfillmentResponse response = fulfillmentService.updateStatus(fulfillmentId, tenantId, roles, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Status updated successfully"));
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

