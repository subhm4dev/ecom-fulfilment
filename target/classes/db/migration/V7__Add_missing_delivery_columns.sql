-- Add missing columns to deliveries table if they don't exist
-- This migration fixes the schema mismatch where columns were added to the migration file
-- after the migration was already applied to the database

DO $$
BEGIN
    -- Add provider_id column if missing
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'deliveries' 
        AND column_name = 'provider_id'
    ) THEN
        ALTER TABLE deliveries 
        ADD COLUMN provider_id UUID;
        
        -- Add foreign key constraint if delivery_providers table exists
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'delivery_providers') THEN
            -- Check if constraint already exists
            IF NOT EXISTS (
                SELECT 1 
                FROM information_schema.table_constraints 
                WHERE table_name = 'deliveries' 
                AND constraint_name = 'fk_delivery_provider'
            ) THEN
                ALTER TABLE deliveries 
                ADD CONSTRAINT fk_delivery_provider 
                FOREIGN KEY (provider_id) REFERENCES delivery_providers(id) ON DELETE SET NULL;
            END IF;
        END IF;
        
        COMMENT ON COLUMN deliveries.provider_id IS 'NULL for own fleet';
    END IF;
    
    -- Add provider_tracking_id column if missing
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'deliveries' 
        AND column_name = 'provider_tracking_id'
    ) THEN
        ALTER TABLE deliveries 
        ADD COLUMN provider_tracking_id VARCHAR(200);
        
        COMMENT ON COLUMN deliveries.provider_tracking_id IS 'Provider''s tracking ID';
    END IF;
    
    -- Add provider_status column if missing
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'deliveries' 
        AND column_name = 'provider_status'
    ) THEN
        ALTER TABLE deliveries 
        ADD COLUMN provider_status VARCHAR(100);
        
        COMMENT ON COLUMN deliveries.provider_status IS 'Provider''s status (e.g., "In Transit", "Out for Delivery")';
    END IF;
    
    -- Create index on provider_id if it doesn't exist
    IF NOT EXISTS (
        SELECT 1 
        FROM pg_indexes 
        WHERE tablename = 'deliveries' 
        AND indexname = 'idx_delivery_provider_id'
    ) THEN
        CREATE INDEX IF NOT EXISTS idx_delivery_provider_id ON deliveries(provider_id);
    END IF;
    
    -- Create index on provider_tracking_id if it doesn't exist
    IF NOT EXISTS (
        SELECT 1 
        FROM pg_indexes 
        WHERE tablename = 'deliveries' 
        AND indexname = 'idx_delivery_provider_tracking_id'
    ) THEN
        CREATE INDEX IF NOT EXISTS idx_delivery_provider_tracking_id ON deliveries(provider_tracking_id);
    END IF;
END $$;


