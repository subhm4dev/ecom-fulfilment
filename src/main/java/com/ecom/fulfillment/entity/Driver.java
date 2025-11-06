package com.ecom.fulfillment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Driver Entity
 */
@Entity
@Table(name = "drivers", indexes = {
    @Index(name = "idx_driver_tenant_id", columnList = "tenant_id"),
    @Index(name = "idx_driver_status", columnList = "status"),
    @Index(name = "idx_driver_phone", columnList = "phone")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Driver {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    
    @Column(name = "name", nullable = false, length = 200)
    private String name;
    
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;
    
    @Column(name = "email", length = 255)
    private String email;
    
    @Column(name = "vehicle_type", length = 50)
    private String vehicleType;
    
    @Column(name = "vehicle_number", length = 50)
    private String vehicleNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private DriverStatus status = DriverStatus.AVAILABLE;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum DriverStatus {
        AVAILABLE,
        BUSY,
        OFFLINE,
        INACTIVE
    }
}

