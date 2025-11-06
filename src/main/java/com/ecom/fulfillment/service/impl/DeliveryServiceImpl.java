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
import com.ecom.fulfillment.entity.DeliveryProvider;
import com.ecom.fulfillment.provider.DeliveryProviderService;
import com.ecom.fulfillment.provider.dto.CreateShipmentRequest;
import com.ecom.fulfillment.provider.dto.CreateShipmentResponse;
import com.ecom.fulfillment.repository.DeliveryProviderRepository;
import com.ecom.fulfillment.repository.DeliveryRepository;
import com.ecom.fulfillment.repository.FulfillmentRepository;
import com.ecom.fulfillment.repository.TrackingHistoryRepository;
import com.ecom.fulfillment.service.DeliveryService;
import com.ecom.fulfillment.service.ProviderSelectionService;
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
    private final DeliveryProviderRepository providerRepository;
    private final ProviderSelectionService providerSelectionService;
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
        
        // Verify driver (only for own fleet deliveries)
        if (delivery.getDeliveryType() == Delivery.DeliveryType.OWN_FLEET) {
            if (delivery.getDriverId() == null || !delivery.getDriverId().equals(driverId)) {
                throw new BusinessException(
                    ErrorCode.ACCESS_DENIED,
                    "Delivery is not assigned to this driver"
                );
            }
        } else {
            // For third-party providers, tracking is handled via webhooks/sync
            log.warn("Attempting to track third-party delivery manually: deliveryId={}", deliveryId);
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
        
        return new com.ecom.fulfillment.model.response.TrackingResponse(
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
        
        // Verify driver (only for own fleet)
        if (delivery.getDeliveryType() == Delivery.DeliveryType.OWN_FLEET) {
            if (delivery.getDriverId() == null || !delivery.getDriverId().equals(driverId)) {
                throw new BusinessException(
                    ErrorCode.ACCESS_DENIED,
                    "Delivery is not assigned to this driver"
                );
            }
        } else {
            throw new BusinessException(
                ErrorCode.INVALID_OPERATION,
                "Cannot complete third-party delivery manually. Use provider sync."
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
    
    @Override
    @Transactional
    public DeliveryResponse createDeliveryWithProvider(
        UUID fulfillmentId, 
        UUID tenantId, 
        String providerCode,
        boolean isIntercity
    ) {
        log.info("Creating delivery with provider: fulfillmentId={}, providerCode={}, isIntercity={}", 
            fulfillmentId, providerCode, isIntercity);
        
        Fulfillment fulfillment = fulfillmentRepository.findById(fulfillmentId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                "Fulfillment not found: " + fulfillmentId
            ));
        
        if (!fulfillment.getTenantId().equals(tenantId)) {
            throw new BusinessException(
                ErrorCode.ACCESS_DENIED,
                "Fulfillment belongs to different tenant"
            );
        }
        
        // Select provider
        DeliveryProvider provider = providerSelectionService.selectProvider(
            tenantId, isIntercity, providerCode
        );
        
        DeliveryProviderService providerService = providerSelectionService.getProviderService(
            provider.getProviderCode().name()
        );
        
        // Create shipment request (TODO: Get address details from AddressBook service)
        CreateShipmentRequest shipmentRequest = new CreateShipmentRequest(
            fulfillment.getOrderId(),
            fulfillmentId,
            tenantId,
            null,  // TODO: Get pickup address
            null,  // TODO: Get delivery address
            null,  // TODO: Get package details
            null,  // TODO: Get COD amount
            isIntercity ? "INTERCITY" : "INTRACITY"
        );
        
        // Create shipment with provider
        CreateShipmentResponse shipmentResponse = providerService.createShipment(shipmentRequest);
        
        // Create delivery record
        Delivery delivery = Delivery.builder()
            .fulfillment(fulfillment)
            .deliveryType(provider.getProviderCode() == DeliveryProvider.ProviderCode.OWN_FLEET 
                ? Delivery.DeliveryType.OWN_FLEET 
                : Delivery.DeliveryType.THIRD_PARTY)
            .driverId(null)  // Will be set for OWN_FLEET
            .provider(provider)
            .providerTrackingId(shipmentResponse.providerTrackingId())
            .tenantId(tenantId)
            .status(Delivery.DeliveryStatus.ASSIGNED)
            .trackingNumber("TRK-" + fulfillmentId.toString().substring(0, 8).toUpperCase())
            .providerStatus(shipmentResponse.status())
            .createdAt(LocalDateTime.now())
            .build();
        
        Delivery savedDelivery = deliveryRepository.save(delivery);
        
        log.info("Delivery created with provider: deliveryId={}, providerTrackingId={}", 
            savedDelivery.getId(), shipmentResponse.providerTrackingId());
        
        return toResponse(savedDelivery);
    }
    
    @Override
    @Transactional
    public DeliveryResponse syncTrackingFromProvider(UUID deliveryId, UUID tenantId) {
        log.info("Syncing tracking from provider: deliveryId={}", deliveryId);
        
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
        
        if (delivery.getDeliveryType() == Delivery.DeliveryType.OWN_FLEET) {
            log.debug("Own fleet delivery, tracking handled internally");
            return toResponse(delivery);
        }
        
        if (delivery.getProviderTrackingId() == null) {
            throw new BusinessException(
                ErrorCode.INVALID_OPERATION,
                "No provider tracking ID found"
            );
        }
        
        // Get provider service
        DeliveryProviderService providerService = providerSelectionService.getProviderService(
            delivery.getProvider().getProviderCode().name()
        );
        
        // Get tracking from provider
        com.ecom.fulfillment.provider.dto.TrackingResponse providerTracking = providerService.getTracking(
            delivery.getProviderTrackingId()
        );
        
        // Update delivery with provider status
        delivery.setProviderStatus(providerTracking.status());
        delivery.setCurrentLocation(providerTracking.currentLocation());
        delivery.setLatitude(providerTracking.latitude());
        delivery.setLongitude(providerTracking.longitude());
        
        // Map provider status to our status
        if (providerTracking.status() != null) {
            try {
                delivery.setStatus(Delivery.DeliveryStatus.valueOf(providerTracking.status()));
            } catch (IllegalArgumentException e) {
                log.warn("Unknown provider status: {}", providerTracking.status());
            }
        }
        
        delivery.setUpdatedAt(LocalDateTime.now());
        
        // Create tracking history entry
        if (providerTracking.trackingEvents() != null && !providerTracking.trackingEvents().isEmpty()) {
            var latestEvent = providerTracking.trackingEvents().get(0);
            TrackingHistory trackingHistory = TrackingHistory.builder()
                .delivery(delivery)
                .latitude(providerTracking.latitude())
                .longitude(providerTracking.longitude())
                .locationDescription(latestEvent.location())
                .status(latestEvent.status())
                .updatedBy(null)  // Provider update
                .createdAt(latestEvent.eventTime() != null ? latestEvent.eventTime() : LocalDateTime.now())
                .build();
            trackingHistoryRepository.save(trackingHistory);
        }
        
        Delivery savedDelivery = deliveryRepository.save(delivery);
        
        log.info("Tracking synced from provider: deliveryId={}, status={}", 
            savedDelivery.getId(), providerTracking.status());
        
        return toResponse(savedDelivery);
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

