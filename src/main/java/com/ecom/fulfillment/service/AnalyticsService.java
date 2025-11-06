package com.ecom.fulfillment.service;

import com.ecom.fulfillment.model.response.DashboardMetricsResponse;
import com.ecom.fulfillment.model.response.DriverPerformanceResponse;
import com.ecom.fulfillment.model.response.ProviderPerformanceResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service for analytics and dashboard metrics
 */
public interface AnalyticsService {
    
    /**
     * Get admin dashboard metrics
     */
    DashboardMetricsResponse getDashboardMetrics(UUID tenantId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Get driver performance metrics
     */
    List<DriverPerformanceResponse> getDriverPerformance(UUID tenantId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Get provider performance metrics
     */
    List<ProviderPerformanceResponse> getProviderPerformance(UUID tenantId, LocalDate startDate, LocalDate endDate);
}

