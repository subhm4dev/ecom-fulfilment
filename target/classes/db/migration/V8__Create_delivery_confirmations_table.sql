-- Create delivery_confirmations table for dual-confirmation delivery system
CREATE TABLE IF NOT EXISTS delivery_confirmations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    delivery_id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    
    -- Agent confirmation
    agent_confirmed BOOLEAN DEFAULT FALSE,
    agent_confirmed_at TIMESTAMP,
    agent_latitude DECIMAL(10, 8),
    agent_longitude DECIMAL(11, 8),
    agent_location_accuracy DECIMAL(5, 2), -- in meters
    agent_user_id UUID, -- driver/agent user ID
    
    -- Customer confirmation
    customer_confirmed BOOLEAN DEFAULT FALSE,
    customer_confirmed_at TIMESTAMP,
    customer_latitude DECIMAL(10, 8),
    customer_longitude DECIMAL(11, 8),
    customer_location_accuracy DECIMAL(5, 2), -- in meters
    customer_user_id UUID, -- customer user ID
    
    -- Proximity check (flexible location - parties can meet anywhere)
    proximity_verified BOOLEAN DEFAULT FALSE,
    distance_between_parties DECIMAL(8, 2), -- in meters (key check - parties must be close)
    distance_to_delivery_address DECIMAL(8, 2), -- in meters (for records)
    proximity_verified_at TIMESTAMP,
    
    -- Actual delivery location (where they actually met)
    actual_delivery_latitude DECIMAL(10, 8),
    actual_delivery_longitude DECIMAL(11, 8),
    actual_delivery_address TEXT, -- Optional description
    delivery_location_type VARCHAR(50) DEFAULT 'SCHEDULED_ADDRESS',
    -- SCHEDULED_ADDRESS, ALTERNATE_LOCATION, CUSTOMER_LOCATION
    
    -- Not available tracking
    agent_marked_unavailable BOOLEAN DEFAULT FALSE,
    agent_unavailable_at TIMESTAMP,
    agent_unavailable_reason VARCHAR(200),
    customer_marked_unavailable BOOLEAN DEFAULT FALSE,
    customer_unavailable_at TIMESTAMP,
    customer_unavailable_reason VARCHAR(200),
    
    -- Reschedule tracking
    reschedule_count INTEGER DEFAULT 0,
    last_reschedule_at TIMESTAMP,
    next_attempt_at TIMESTAMP,
    auto_return_initiated BOOLEAN DEFAULT FALSE,
    
    -- Age verification (for restricted items like alcohol)
    requires_age_verification BOOLEAN DEFAULT FALSE,
    minimum_age_required INTEGER, -- 18 or 21
    age_verified BOOLEAN DEFAULT FALSE,
    age_verification_method VARCHAR(50),
    -- PHOTO_VERIFICATION, AADHAAR_FACE_RD, ID_VERIFICATION, VIDEO_KYC
    age_verification_status VARCHAR(50),
    -- PENDING, VERIFIED, FAILED, REJECTED
    age_verification_at TIMESTAMP,
    
    -- Status
    confirmation_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    -- PENDING, AGENT_CONFIRMED, CUSTOMER_CONFIRMED, BOTH_CONFIRMED, 
    -- AGENT_UNAVAILABLE, CUSTOMER_UNAVAILABLE, BOTH_UNAVAILABLE, RETURNED, CONFLICT
    
    -- Metadata
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    CONSTRAINT fk_delivery_confirmation_delivery FOREIGN KEY (delivery_id) 
        REFERENCES deliveries(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_delivery_confirmation_delivery_id ON delivery_confirmations(delivery_id);
CREATE INDEX IF NOT EXISTS idx_delivery_confirmation_status ON delivery_confirmations(confirmation_status);
CREATE INDEX IF NOT EXISTS idx_delivery_confirmation_next_attempt ON delivery_confirmations(next_attempt_at);
CREATE INDEX IF NOT EXISTS idx_delivery_confirmation_tenant_id ON delivery_confirmations(tenant_id);

-- Add proximity fields to deliveries table
ALTER TABLE deliveries 
ADD COLUMN IF NOT EXISTS delivery_address_latitude DECIMAL(10, 8),
ADD COLUMN IF NOT EXISTS delivery_address_longitude DECIMAL(11, 8),
ADD COLUMN IF NOT EXISTS proximity_radius_meters INTEGER DEFAULT 50,
ADD COLUMN IF NOT EXISTS requires_dual_confirmation BOOLEAN DEFAULT TRUE,
ADD COLUMN IF NOT EXISTS confirmation_timeout_minutes INTEGER DEFAULT 5;

