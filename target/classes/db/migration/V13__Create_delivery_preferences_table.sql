-- Create delivery_preferences table for customer delivery preferences
CREATE TABLE IF NOT EXISTS delivery_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fulfillment_id UUID NOT NULL REFERENCES fulfillments(id) ON DELETE CASCADE,
    delivery_id UUID REFERENCES deliveries(id) ON DELETE CASCADE,
    tenant_id UUID NOT NULL,
    customer_user_id UUID NOT NULL,
    
    -- Scheduling
    scheduled_delivery_date DATE,
    scheduled_delivery_time_start TIME,
    scheduled_delivery_time_end TIME,
    delivery_time_window VARCHAR(100), -- "MORNING", "AFTERNOON", "EVENING", "9AM-12PM", etc.
    
    -- Delivery instructions
    delivery_instructions TEXT,
    special_handling_notes TEXT,
    leave_at_door BOOLEAN DEFAULT FALSE,
    hand_to_customer BOOLEAN DEFAULT TRUE,
    require_signature BOOLEAN DEFAULT FALSE,
    
    -- Contact preferences
    preferred_contact_method VARCHAR(50), -- PHONE, SMS, EMAIL, APP
    preferred_contact_time VARCHAR(100),
    do_not_disturb BOOLEAN DEFAULT FALSE,
    
    -- Location preferences
    preferred_delivery_location VARCHAR(200), -- "Front door", "Back gate", etc.
    gate_code VARCHAR(50),
    building_name VARCHAR(200),
    floor_number VARCHAR(20),
    apartment_number VARCHAR(50),
    
    -- Status
    is_active BOOLEAN DEFAULT TRUE,
    applied_at TIMESTAMP,
    
    -- Metadata
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    CONSTRAINT fk_preference_fulfillment FOREIGN KEY (fulfillment_id) 
        REFERENCES fulfillments(id) ON DELETE CASCADE,
    CONSTRAINT fk_preference_delivery FOREIGN KEY (delivery_id) 
        REFERENCES deliveries(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_preference_fulfillment_id ON delivery_preferences(fulfillment_id);
CREATE INDEX IF NOT EXISTS idx_preference_delivery_id ON delivery_preferences(delivery_id);
CREATE INDEX IF NOT EXISTS idx_preference_customer ON delivery_preferences(customer_user_id);
CREATE INDEX IF NOT EXISTS idx_preference_tenant ON delivery_preferences(tenant_id);
CREATE INDEX IF NOT EXISTS idx_preference_scheduled_date ON delivery_preferences(scheduled_delivery_date);

