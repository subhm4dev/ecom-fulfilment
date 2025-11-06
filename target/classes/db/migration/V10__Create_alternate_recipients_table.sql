-- Create alternate_recipients table for allowing alternate users to receive orders
CREATE TABLE IF NOT EXISTS alternate_recipients (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    delivery_id UUID NOT NULL REFERENCES deliveries(id) ON DELETE CASCADE,
    delivery_confirmation_id UUID REFERENCES delivery_confirmations(id) ON DELETE CASCADE,
    tenant_id UUID NOT NULL,
    customer_user_id UUID NOT NULL, -- Original customer who shared the link
    
    -- Alternate recipient details
    alternate_user_id UUID, -- User ID if they have account
    alternate_phone_number VARCHAR(20), -- Phone number (can be used without account)
    alternate_name VARCHAR(200), -- Name of alternate recipient
    alternate_email VARCHAR(200), -- Email (optional)
    
    -- Sharing details
    share_token VARCHAR(100) UNIQUE NOT NULL, -- Unique token for sharing link
    share_link VARCHAR(500), -- Full shareable link
    shared_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    shared_by_user_id UUID NOT NULL, -- User who shared (customer or admin)
    shared_via VARCHAR(50) DEFAULT 'SMS', -- SMS, EMAIL, WHATSAPP, LINK
    
    -- Status
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING', 
    -- PENDING, ACTIVE, CONFIRMED, EXPIRED, REVOKED
    confirmed_at TIMESTAMP,
    confirmed_by_user_id UUID, -- Which alternate user confirmed
    
    -- Confirmation details (when alternate user confirms)
    confirmed_latitude DECIMAL(10, 8),
    confirmed_longitude DECIMAL(11, 8),
    confirmed_location_accuracy DECIMAL(5, 2),
    proximity_verified BOOLEAN DEFAULT FALSE,
    distance_to_agent DECIMAL(8, 2), -- Distance to agent when confirmed
    
    -- Expiry
    expires_at TIMESTAMP, -- Link expiry (default 24 hours)
    revoked_at TIMESTAMP,
    revoked_by_user_id UUID,
    revoke_reason VARCHAR(200),
    
    -- Metadata
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    CONSTRAINT fk_alternate_recipient_delivery FOREIGN KEY (delivery_id) 
        REFERENCES deliveries(id) ON DELETE CASCADE,
    CONSTRAINT fk_alternate_recipient_confirmation FOREIGN KEY (delivery_confirmation_id) 
        REFERENCES delivery_confirmations(id) ON DELETE SET NULL
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_alternate_recipient_delivery_id ON alternate_recipients(delivery_id);
CREATE INDEX IF NOT EXISTS idx_alternate_recipient_confirmation_id ON alternate_recipients(delivery_confirmation_id);
CREATE INDEX IF NOT EXISTS idx_alternate_recipient_token ON alternate_recipients(share_token);
CREATE INDEX IF NOT EXISTS idx_alternate_recipient_phone ON alternate_recipients(alternate_phone_number);
CREATE INDEX IF NOT EXISTS idx_alternate_recipient_user ON alternate_recipients(alternate_user_id);
CREATE INDEX IF NOT EXISTS idx_alternate_recipient_status ON alternate_recipients(status);
CREATE INDEX IF NOT EXISTS idx_alternate_recipient_customer ON alternate_recipients(customer_user_id);
CREATE INDEX IF NOT EXISTS idx_alternate_recipient_expires ON alternate_recipients(expires_at);

-- Add alternate recipient fields to delivery_confirmations table
ALTER TABLE delivery_confirmations
ADD COLUMN IF NOT EXISTS alternate_recipient_id UUID REFERENCES alternate_recipients(id) ON DELETE SET NULL,
ADD COLUMN IF NOT EXISTS confirmed_by_alternate BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS alternate_recipient_name VARCHAR(200),
ADD COLUMN IF NOT EXISTS alternate_recipient_phone VARCHAR(20);

CREATE INDEX IF NOT EXISTS idx_delivery_confirmation_alternate ON delivery_confirmations(alternate_recipient_id);

