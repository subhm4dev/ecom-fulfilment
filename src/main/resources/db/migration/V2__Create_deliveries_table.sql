-- Create deliveries table
CREATE TABLE IF NOT EXISTS deliveries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fulfillment_id UUID NOT NULL,
    delivery_type VARCHAR(50) NOT NULL DEFAULT 'OWN_FLEET',  -- OWN_FLEET, THIRD_PARTY
    driver_id UUID,  -- NULL for third-party providers
    provider_id UUID,  -- NULL for own fleet
    provider_tracking_id VARCHAR(200),  -- Provider's tracking ID
    tenant_id UUID NOT NULL,
    current_location VARCHAR(500),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    status VARCHAR(50) NOT NULL DEFAULT 'ASSIGNED',
    tracking_number VARCHAR(100) UNIQUE,
    provider_status VARCHAR(100),  -- Provider's status (e.g., "In Transit", "Out for Delivery")
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_delivery_fulfillment FOREIGN KEY (fulfillment_id) REFERENCES fulfillments(id) ON DELETE CASCADE,
    CONSTRAINT fk_delivery_provider FOREIGN KEY (provider_id) REFERENCES delivery_providers(id) ON DELETE SET NULL
);

-- Create indexes for deliveries table
CREATE INDEX IF NOT EXISTS idx_delivery_fulfillment_id ON deliveries(fulfillment_id);
CREATE INDEX IF NOT EXISTS idx_delivery_driver_id ON deliveries(driver_id);
CREATE INDEX IF NOT EXISTS idx_delivery_provider_id ON deliveries(provider_id);
CREATE INDEX IF NOT EXISTS idx_delivery_tenant_id ON deliveries(tenant_id);
CREATE INDEX IF NOT EXISTS idx_delivery_status ON deliveries(status);
CREATE INDEX IF NOT EXISTS idx_delivery_tracking_number ON deliveries(tracking_number);
CREATE INDEX IF NOT EXISTS idx_delivery_provider_tracking_id ON deliveries(provider_tracking_id);

