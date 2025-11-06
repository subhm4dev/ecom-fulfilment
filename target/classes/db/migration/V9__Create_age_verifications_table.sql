-- Create age_verifications table for age-restricted items (alcohol, etc.)
CREATE TABLE IF NOT EXISTS age_verifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    delivery_confirmation_id UUID NOT NULL REFERENCES delivery_confirmations(id) ON DELETE CASCADE,
    delivery_id UUID NOT NULL REFERENCES deliveries(id) ON DELETE CASCADE,
    tenant_id UUID NOT NULL,
    customer_user_id UUID NOT NULL,
    
    -- Age requirement
    minimum_age_required INTEGER NOT NULL, -- 18 or 21
    customer_date_of_birth DATE, -- Extracted from verification
    
    -- Verification method
    verification_method VARCHAR(50) NOT NULL,
    -- PHOTO_VERIFICATION, AADHAAR_FACE_RD, ID_VERIFICATION, VIDEO_KYC
    
    -- Photo verification
    customer_photo_url VARCHAR(500),
    id_photo_url VARCHAR(500),
    id_type VARCHAR(50), -- AADHAAR, PAN, DRIVING_LICENSE, PASSPORT
    id_number VARCHAR(100),
    
    -- Aadhaar Face RD verification
    aadhaar_number VARCHAR(12),
    aadhaar_reference_id VARCHAR(100), -- UIDAI reference ID
    aadhaar_transaction_id VARCHAR(100), -- UIDAI transaction ID
    aadhaar_otp_reference_id VARCHAR(100), -- OTP reference ID
    biometric_match_score DECIMAL(5, 2), -- Face match confidence (0-100)
    aadhaar_verified BOOLEAN DEFAULT FALSE,
    aadhaar_demographic_data JSONB, -- Store DOB, name, etc. from UIDAI
    
    -- Verification result
    verification_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    -- PENDING, VERIFICATION_IN_PROGRESS, VERIFIED, FAILED, REJECTED, MANUAL_REVIEW
    age_verified BOOLEAN DEFAULT FALSE,
    verified_age INTEGER, -- Calculated age
    verification_confidence DECIMAL(5, 2), -- Overall confidence score (0-100)
    
    -- Metadata
    verified_by_system BOOLEAN DEFAULT TRUE,
    verified_by_admin UUID, -- Admin user ID if manual verification
    verification_notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    verified_at TIMESTAMP,
    
    CONSTRAINT fk_age_verification_confirmation FOREIGN KEY (delivery_confirmation_id) 
        REFERENCES delivery_confirmations(id) ON DELETE CASCADE,
    CONSTRAINT fk_age_verification_delivery FOREIGN KEY (delivery_id) 
        REFERENCES deliveries(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_age_verification_delivery_id ON age_verifications(delivery_id);
CREATE INDEX IF NOT EXISTS idx_age_verification_confirmation_id ON age_verifications(delivery_confirmation_id);
CREATE INDEX IF NOT EXISTS idx_age_verification_status ON age_verifications(verification_status);
CREATE INDEX IF NOT EXISTS idx_age_verification_customer ON age_verifications(customer_user_id);
CREATE INDEX IF NOT EXISTS idx_age_verification_tenant ON age_verifications(tenant_id);

