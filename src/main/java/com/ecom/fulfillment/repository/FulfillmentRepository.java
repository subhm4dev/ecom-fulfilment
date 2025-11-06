package com.ecom.fulfillment.repository;

import com.ecom.fulfillment.entity.Fulfillment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Fulfillment Repository
 */
@Repository
public interface FulfillmentRepository extends JpaRepository<Fulfillment, UUID> {
    
    Optional<Fulfillment> findByOrderIdAndTenantId(UUID orderId, UUID tenantId);
    
    List<Fulfillment> findByTenantId(UUID tenantId);
    
    @Query("SELECT f FROM Fulfillment f WHERE f.tenantId = :tenantId " +
           "AND (:status IS NULL OR f.status = :status)")
    List<Fulfillment> findByTenantIdAndStatus(
        @Param("tenantId") UUID tenantId,
        @Param("status") Fulfillment.FulfillmentStatus status
    );
    
    @Query("SELECT f FROM Fulfillment f WHERE f.assignedDriverId = :driverId " +
           "AND f.tenantId = :tenantId")
    List<Fulfillment> findByDriverIdAndTenantId(
        @Param("driverId") UUID driverId,
        @Param("tenantId") UUID tenantId
    );
}

