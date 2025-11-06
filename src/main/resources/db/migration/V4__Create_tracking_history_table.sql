-- Create tracking_history table
CREATE TABLE IF NOT EXISTS tracking_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    delivery_id UUID NOT NULL,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    location_description VARCHAR(500),
    status VARCHAR(50),
    updated_by UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tracking_history_delivery FOREIGN KEY (delivery_id) REFERENCES deliveries(id) ON DELETE CASCADE
);

-- Create indexes for tracking_history table
CREATE INDEX IF NOT EXISTS idx_tracking_history_delivery_id ON tracking_history(delivery_id);
CREATE INDEX IF NOT EXISTS idx_tracking_history_created_at ON tracking_history(created_at);

