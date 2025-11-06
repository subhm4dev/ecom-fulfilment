package com.ecom.fulfillment.service.impl;

import com.ecom.error.exception.BusinessException;
import com.ecom.error.model.ErrorCode;
import com.ecom.fulfillment.entity.Delivery;
import com.ecom.fulfillment.entity.Fulfillment;
import com.ecom.fulfillment.entity.TrackingHistory;
import com.ecom.fulfillment.event.DeliveryCompletedEvent;
import com.ecom.fulfillment.event.DeliveryStatusUpdatedEvent;
import com.ecom.fulfillment.model.request.TrackDeliveryRequest;
import com.ecom.fulfillment.model.response.DeliveryResponse;
import com.ecom.fulfillment.model.response.TrackingHistoryResponse;
import com.ecom.fulfillment.model.response.TrackingResponse;
import com.ecom.fulfillment.repository.DeliveryRepository;
import com.ecom.fulfillment.repository.FulfillmentRepository;
import com.ecom.fulfillment.repository.TrackingHistoryRepository;
import com.ecom.fulfillment.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Delivery Service Implementation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryServiceImpl implements DeliveryService {
    
    private final DeliveryRepository deliveryRepository;
    private final FulfillmentRepository fulfillmentRepository;
    private final TrackingHistoryRepository trackingHistoryRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    private static final String DELIVERY_STATUS_UPDATED_TOPIC = "delivery-status-updated";
    private static final String DELIVERY_COMPLETED_TOPIC = "delivery-completed";
    
    @Override
    @Transactional
    public DeliveryResponse trackDelivery(UUID deliveryId, UUID driverId, UUID tenantId, TrackDeliveryRequest request) {
        log.info("Tracking delivery: deliveryId={}, driverId={}", deliveryId, driverId);
        
        Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                "Delivery not found: " + deliveryId
            ));
        
        // Verify tenant
        if (!delivery.getTenantId().equals(tenantId)) {
            throw new BusinessException(
                ErrorCode.ACCESS_DENIED,
                "Delivery belongs to different tenant"
            );
        }
        
        // Verify driver
        if (!delivery.getDriverId().equals(driverId)) {
            throw new BusinessException(
                ErrorCode.ACCESS_DENIED,
                "Delivery is not assigned to this driver"
            );
        }
        
        // Update delivery location
        delivery.setCurrentLocation(request.locationDescription());
        delivery.setLatitude(request.latitude());
        delivery.setLongitude(request.longitude());
        if (request.status() != null) {
            delivery.setStatus(Delivery.DeliveryStatus.valueOf(request.status()));
        }
        delivery.setUpdatedAt(LocalDateTime.now());
        
        // Create tracking history entry
        TrackingHistory trackingHistory = TrackingHistory.builder()
            .delivery(delivery)
            .latitude(request.latitude())
            .longitude(request.longitude())
            .locationDescription(request.locationDescription())
            .status(request.status())
            .updatedBy(driverId)
            .createdAt(LocalDateTime.now())
            .build();
        
        trackingHistoryRepository.save(trackingHistory);
        delivery.getTrackingHistory().add(trackingHistory);
        
        Delivery savedDelivery = deliveryRepository.save(delivery);
        
        log.info("Delivery location updated: deliveryId={}", deliveryId);
        
        // Publish DeliveryStatusUpdated event
        try {
            DeliveryStatusUpdatedEvent event = DeliveryStatusUpdatedEvent.of(
                savedDelivery.getId(),
                savedDelivery.getFulfillmentId(),
                savedDelivery.getDriverId(),
                savedDelivery.getStatus(),
                savedDelivery.getLatitude(),
                savedDelivery.getLongitude(),
                savedDelivery.getUpdatedAt()
            );
            kafkaTemplate.send(DELIVERY_STATUS_UPDATED_TOPIC, savedDelivery.getId().toString(), event);
        } catch (Exception e) {
            log.error("Failed to publish DeliveryStatusUpdated event: deliveryId={}", savedDelivery.getId(), e);
        }
        
        return toResponse(savedDelivery);
    }
    
    @Override
    @Transactional(readOnly = true)
    public TrackingResponse getTracking(String trackingNumber, UUID tenantId) {
        log.debug("Getting tracking info: trackingNumber={}", trackingNumber);
        
        Delivery delivery = deliveryRepository.findByTrackingNumberAndTenantId(trackingNumber, tenantId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                "Tracking not found for number: " + trackingNumber
            ));
        
        Fulfillment fulfillment = fulfillmentRepository.findById(delivery.getFulfillmentId())
            .orElseThrow(() -> new BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                "Fulfillment not found: " + delivery.getFulfillmentId()
            ));
        
        List<TrackingHistoryResponse> history = trackingHistoryRepository
            .findByDeliveryIdOrderByCreatedAtDesc(delivery.getId())
            .stream()
            .map(th -> new TrackingHistoryResponse(
                th.getId(),
                th.getDeliveryId(),
                th.getLatitude(),
                th.getLongitude(),
                th.getLocationDescription(),
                th.getStatus(),
                th.getUpdatedBy(),
                th.getCreatedAt()
            ))
            .collect(Collectors.toList());
        
        return new TrackingResponse(
            delivery.getTrackingNumber(),
            fulfillment.getOrderId(),
            delivery.getStatus(),
            delivery.getCurrentLocation(),
            delivery.getLatitude(),
            delivery.getLongitude(),
            fulfillment.getEstimatedDelivery(),
            history,
            delivery.getUpdatedAt()
        );
    }
    
    @Override
    @Transactional(readOnly = true)
    public DeliveryResponse getDeliveryById(UUID deliveryId, UUID tenantId, List<String> userRoles) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                "Delivery not found: " + deliveryId
            ));
        
        if (!delivery.getTenantId().equals(tenantId)) {
            throw new BusinessException(
                ErrorCode.ACCESS_DENIED,
                "Delivery belongs to different tenant"
            );
        }
        
        return toResponse(delivery);
    }
    
    @Override
    @Transactional
    public DeliveryResponse completeDelivery(UUID deliveryId, UUID driverId, UUID tenantId) {
        log.info("Completing delivery: deliveryId={}, driverId={}", deliveryId, driverId);
        
        Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                "Delivery not found: " + deliveryId
            ));
        
        if (!delivery.getTenantId().equals(tenantId)) {
            throw new BusinessException(
                ErrorCode.ACCESS_DENIED,
                "Delivery belongs to different tenant"
            );
        }
        
        if (!delivery.getDriverId().equals(driverId)) {
            throw new BusinessException(
                ErrorCode.ACCESS_DENIED,
                "Delivery is not assigned to this driver"
            );
        }
        
        delivery.setStatus(Delivery.DeliveryStatus.DELIVERED);
        delivery.setUpdatedAt(LocalDateTime.now());
        
        Delivery savedDelivery = deliveryRepository.save(delivery);
        
        // Update fulfillment status
        Fulfillment fulfillment = fulfillmentRepository.findById(delivery.getFulfillmentId())
            .orElseThrow(() -> new BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                "Fulfillment not found: " + delivery.getFulfillmentId()
            ));
        
        fulfillment.setStatus(Fulfillment.FulfillmentStatus.DELIVERED);
        fulfillment.setActualDelivery(LocalDateTime.now());
        fulfillment.setUpdatedAt(LocalDateTime.now());
        fulfillmentRepository.save(fulfillment);
        
        log.info("Delivery completed: deliveryId={}", deliveryId);
        
        // Publish DeliveryCompleted event
        try {
            DeliveryCompletedEvent event = DeliveryCompletedEvent.of(
                savedDelivery.getId(),
                savedDelivery.getFulfillmentId(),
                savedDelivery.getDriverId(),
                fulfillment.getOrderId(),
                savedDelivery.getUpdatedAt()
            );
            kafkaTemplate.send(DELIVERY_COMPLETED_TOPIC, savedDelivery.getId().toString(), event);
        } catch (Exception e) {
            log.error("Failed to publish DeliveryCompleted event: deliveryId={}", savedDelivery.getId(), e);
        }
        
        return toResponse(savedDelivery);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<DeliveryResponse> getDeliveriesByDriver(UUID driverId, UUID tenantId) {
        return deliveryRepository.findByDriverIdAndTenantIdAndStatus(driverId, tenantId, null)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
    
    private DeliveryResponse toResponse(Delivery delivery) {
        return new DeliveryResponse(
            delivery.getId(),
            delivery.getFulfillmentId(),
            delivery.getDriverId(),
            delivery.getTenantId(),
            delivery.getCurrentLocation(),
            delivery.getLatitude(),
            delivery.getLongitude(),
            delivery.getStatus(),
            delivery.getTrackingNumber(),
            delivery.getTrackingHistory().stream()
                .map(th -> new TrackingHistoryResponse(
                    th.getId(),
                    th.getDeliveryId(),
                    th.getLatitude(),
                    th.getLongitude(),
                    th.getLocationDescription(),
                    th.getStatus(),
                    th.getUpdatedBy(),
                    th.getCreatedAt()
                ))
                .collect(Collectors.toList()),
            delivery.getCreatedAt(),
            delivery.getUpdatedAt()
        );
    }
}

