-- Create proof_of_delivery table for POD photos and signatures
CREATE TABLE IF NOT EXISTS proof_of_delivery (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    delivery_id UUID NOT NULL REFERENCES deliveries(id) ON DELETE CASCADE,
    fulfillment_id UUID REFERENCES fulfillments(id) ON DELETE CASCADE,
    tenant_id UUID NOT NULL,
    
    -- Delivery confirmation reference
    delivery_confirmation_id UUID REFERENCES delivery_confirmations(id) ON DELETE SET NULL,
    
    -- POD type
    pod_type VARCHAR(50) NOT NULL DEFAULT 'PHOTO',
    -- PHOTO, SIGNATURE, OTP, VIDEO, COMBINATION
    
    -- Photo POD
    photo_urls TEXT[], -- Array of photo URLs
    photo_taken_at TIMESTAMP,
    photo_taken_by_user_id UUID, -- Driver/agent user ID
    
    -- Signature POD
    signature_url VARCHAR(500), -- URL to signature image
    signature_data TEXT, -- Base64 signature data
    signature_taken_at TIMESTAMP,
    signature_taken_by_user_id UUID,
    recipient_name VARCHAR(200), -- Name of person who signed
    
    -- OTP POD
    otp_verified BOOLEAN DEFAULT FALSE,
    otp_code VARCHAR(10),
    otp_verified_at TIMESTAMP,
    otp_phone_number VARCHAR(20),
    
    -- Video POD (optional)
    video_url VARCHAR(500),
    video_taken_at TIMESTAMP,
    
    -- Location
    pod_latitude DECIMAL(10, 8),
    pod_longitude DECIMAL(11, 8),
    pod_location_description VARCHAR(500),
    
    -- Recipient details
    received_by_name VARCHAR(200),
    received_by_phone VARCHAR(20),
    received_by_relation VARCHAR(50), -- SELF, FAMILY_MEMBER, NEIGHBOR, SECURITY, etc.
    is_alternate_recipient BOOLEAN DEFAULT FALSE,
    alternate_recipient_id UUID REFERENCES alternate_recipients(id) ON DELETE SET NULL,
    
    -- Status
    pod_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    -- PENDING, COMPLETED, REJECTED, MANUAL_REVIEW
    
    -- Metadata
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    verified_at TIMESTAMP,
    verified_by_user_id UUID, -- Admin who verified (if manual review)
    
    CONSTRAINT fk_pod_delivery FOREIGN KEY (delivery_id) 
        REFERENCES deliveries(id) ON DELETE CASCADE,
    CONSTRAINT fk_pod_confirmation FOREIGN KEY (delivery_confirmation_id) 
        REFERENCES delivery_confirmations(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_pod_delivery_id ON proof_of_delivery(delivery_id);
CREATE INDEX IF NOT EXISTS idx_pod_fulfillment_id ON proof_of_delivery(fulfillment_id);
CREATE INDEX IF NOT EXISTS idx_pod_confirmation_id ON proof_of_delivery(delivery_confirmation_id);
CREATE INDEX IF NOT EXISTS idx_pod_status ON proof_of_delivery(pod_status);
CREATE INDEX IF NOT EXISTS idx_pod_tenant ON proof_of_delivery(tenant_id);

