package com.ecom.fulfillment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Delivery Entity
 */
@Entity
@Table(name = "deliveries", indexes = {
    @Index(name = "idx_delivery_fulfillment_id", columnList = "fulfillment_id"),
    @Index(name = "idx_delivery_driver_id", columnList = "driver_id"),
    @Index(name = "idx_delivery_tenant_id", columnList = "tenant_id"),
    @Index(name = "idx_delivery_status", columnList = "status"),
    @Index(name = "idx_delivery_tracking_number", columnList = "tracking_number")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Delivery {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fulfillment_id", nullable = false)
    private Fulfillment fulfillment;
    
    @Column(name = "fulfillment_id", insertable = false, updatable = false)
    private UUID fulfillmentId;
    
    @Column(name = "driver_id", nullable = false)
    private UUID driverId;
    
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    
    @Column(name = "current_location", length = 500)
    private String currentLocation;
    
    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;
    
    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private DeliveryStatus status = DeliveryStatus.ASSIGNED;
    
    @Column(name = "tracking_number", unique = true, length = 100)
    private String trackingNumber;
    
    @OneToMany(mappedBy = "delivery", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TrackingHistory> trackingHistory = new ArrayList<>();
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum DeliveryStatus {
        ASSIGNED,
        PICKED_UP,
        IN_TRANSIT,
        OUT_FOR_DELIVERY,
        DELIVERED,
        FAILED,
        RETURNED
    }
}

