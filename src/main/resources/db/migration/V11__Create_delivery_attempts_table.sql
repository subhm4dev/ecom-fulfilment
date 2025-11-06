-- Create delivery_attempts table for tracking delivery attempts
CREATE TABLE IF NOT EXISTS delivery_attempts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    delivery_id UUID NOT NULL REFERENCES deliveries(id) ON DELETE CASCADE,
    fulfillment_id UUID REFERENCES fulfillments(id) ON DELETE CASCADE,
    tenant_id UUID NOT NULL,
    
    -- Attempt details
    attempt_number INTEGER NOT NULL,
    attempted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    attempted_by_user_id UUID, -- Driver/agent user ID
    attempt_status VARCHAR(50) NOT NULL,
    -- SUCCESSFUL, FAILED, CANCELLED
    
    -- Failure details
    failure_reason VARCHAR(200),
    failure_code VARCHAR(50),
    -- CUSTOMER_NOT_AVAILABLE, WRONG_ADDRESS, DAMAGED_GOODS, REFUSED, etc.
    
    -- Location
    attempt_latitude DECIMAL(10, 8),
    attempt_longitude DECIMAL(11, 8),
    attempt_location_description VARCHAR(500),
    
    -- Photos/Evidence
    photo_urls TEXT[], -- Array of photo URLs
    notes TEXT,
    
    -- Next attempt
    next_attempt_at TIMESTAMP,
    next_attempt_scheduled BOOLEAN DEFAULT FALSE,
    
    -- Metadata
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    CONSTRAINT fk_delivery_attempt_delivery FOREIGN KEY (delivery_id) 
        REFERENCES deliveries(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_delivery_attempt_delivery_id ON delivery_attempts(delivery_id);
CREATE INDEX IF NOT EXISTS idx_delivery_attempt_fulfillment_id ON delivery_attempts(fulfillment_id);
CREATE INDEX IF NOT EXISTS idx_delivery_attempt_status ON delivery_attempts(attempt_status);
CREATE INDEX IF NOT EXISTS idx_delivery_attempt_tenant ON delivery_attempts(tenant_id);
CREATE INDEX IF NOT EXISTS idx_delivery_attempt_next_attempt ON delivery_attempts(next_attempt_at);

