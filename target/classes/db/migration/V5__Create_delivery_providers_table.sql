-- Create delivery_providers table
CREATE TABLE IF NOT EXISTS delivery_providers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    provider_code VARCHAR(50) NOT NULL,  -- BLUEDART, DELHIVERY, SHIPROCKET, DUNZO, RAPIDO, OWN_FLEET
    provider_name VARCHAR(200) NOT NULL,
    provider_type VARCHAR(50) NOT NULL,  -- INTERCITY, INTRACITY, OWN_FLEET
    is_active BOOLEAN NOT NULL DEFAULT true,
    api_key VARCHAR(500),
    api_secret VARCHAR(500),
    webhook_secret VARCHAR(500),
    base_url VARCHAR(500),
    config JSONB,  -- Provider-specific configuration
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE(tenant_id, provider_code)
);

-- Create indexes for delivery_providers table
CREATE INDEX IF NOT EXISTS idx_delivery_provider_tenant_id ON delivery_providers(tenant_id);
CREATE INDEX IF NOT EXISTS idx_delivery_provider_code ON delivery_providers(provider_code);
CREATE INDEX IF NOT EXISTS idx_delivery_provider_active ON delivery_providers(is_active);

