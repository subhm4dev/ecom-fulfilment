-- Create drivers table
CREATE TABLE IF NOT EXISTS drivers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    name VARCHAR(200) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(255),
    vehicle_type VARCHAR(50),
    vehicle_number VARCHAR(50),
    status VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create indexes for drivers table
CREATE INDEX IF NOT EXISTS idx_driver_tenant_id ON drivers(tenant_id);
CREATE INDEX IF NOT EXISTS idx_driver_status ON drivers(status);
CREATE INDEX IF NOT EXISTS idx_driver_phone ON drivers(phone);

