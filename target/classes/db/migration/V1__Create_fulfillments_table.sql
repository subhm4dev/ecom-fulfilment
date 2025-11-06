-- Create fulfillments table
CREATE TABLE IF NOT EXISTS fulfillments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    assigned_driver_id UUID,
    pickup_location VARCHAR(500),
    delivery_address_id UUID NOT NULL,
    estimated_delivery TIMESTAMP,
    actual_delivery TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create indexes for fulfillments table
CREATE INDEX IF NOT EXISTS idx_fulfillment_order_id ON fulfillments(order_id);
CREATE INDEX IF NOT EXISTS idx_fulfillment_tenant_id ON fulfillments(tenant_id);
CREATE INDEX IF NOT EXISTS idx_fulfillment_status ON fulfillments(status);
CREATE INDEX IF NOT EXISTS idx_fulfillment_driver_id ON fulfillments(assigned_driver_id);
CREATE INDEX IF NOT EXISTS idx_fulfillment_created_at ON fulfillments(created_at);

