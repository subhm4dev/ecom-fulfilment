package com.ecom.fulfillment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Fulfillment Entity
 */
@Entity
@Table(name = "fulfillments", indexes = {
    @Index(name = "idx_fulfillment_order_id", columnList = "order_id"),
    @Index(name = "idx_fulfillment_tenant_id", columnList = "tenant_id"),
    @Index(name = "idx_fulfillment_status", columnList = "status"),
    @Index(name = "idx_fulfillment_driver_id", columnList = "assigned_driver_id"),
    @Index(name = "idx_fulfillment_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Fulfillment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "order_id", nullable = false)
    private UUID orderId;
    
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private FulfillmentStatus status = FulfillmentStatus.PENDING;
    
    @Column(name = "assigned_driver_id")
    private UUID assignedDriverId;
    
    @Column(name = "pickup_location", length = 500)
    private String pickupLocation;
    
    @Column(name = "delivery_address_id", nullable = false)
    private UUID deliveryAddressId;
    
    @Column(name = "estimated_delivery")
    private LocalDateTime estimatedDelivery;
    
    @Column(name = "actual_delivery")
    private LocalDateTime actualDelivery;
    
    @OneToMany(mappedBy = "fulfillment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Delivery> deliveries = new ArrayList<>();
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum FulfillmentStatus {
        PENDING,
        ASSIGNED,
        PICKED_UP,
        IN_TRANSIT,
        OUT_FOR_DELIVERY,
        DELIVERED,
        FAILED,
        CANCELLED
    }
}

