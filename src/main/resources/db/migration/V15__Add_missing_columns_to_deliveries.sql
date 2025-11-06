-- Add missing columns to deliveries table
DO $$
BEGIN
    -- Attempt count
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'deliveries' AND column_name = 'attempt_count'
    ) THEN
        ALTER TABLE deliveries
        ADD COLUMN attempt_count INTEGER DEFAULT 0;
        
        COMMENT ON COLUMN deliveries.attempt_count IS 'Number of delivery attempts';
    END IF;
    
    -- Last attempt
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'deliveries' AND column_name = 'last_attempt_at'
    ) THEN
        ALTER TABLE deliveries
        ADD COLUMN last_attempt_at TIMESTAMP;
        
        COMMENT ON COLUMN deliveries.last_attempt_at IS 'Timestamp of last delivery attempt';
    END IF;
    
    -- Next attempt
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'deliveries' AND column_name = 'next_attempt_at'
    ) THEN
        ALTER TABLE deliveries
        ADD COLUMN next_attempt_at TIMESTAMP;
        
        COMMENT ON COLUMN deliveries.next_attempt_at IS 'Scheduled time for next attempt';
    END IF;
    
    -- Failure reason
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'deliveries' AND column_name = 'failure_reason'
    ) THEN
        ALTER TABLE deliveries
        ADD COLUMN failure_reason VARCHAR(200);
        
        COMMENT ON COLUMN deliveries.failure_reason IS 'Reason for delivery failure';
    END IF;
    
    -- COD amount
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'deliveries' AND column_name = 'cod_amount'
    ) THEN
        ALTER TABLE deliveries
        ADD COLUMN cod_amount DECIMAL(10, 2);
        
        COMMENT ON COLUMN deliveries.cod_amount IS 'Cash on delivery amount';
    END IF;
    
    -- COD collected
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'deliveries' AND column_name = 'cod_collected'
    ) THEN
        ALTER TABLE deliveries
        ADD COLUMN cod_collected BOOLEAN DEFAULT FALSE;
        
        COMMENT ON COLUMN deliveries.cod_collected IS 'Whether COD was collected';
    END IF;
    
    -- Estimated arrival
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'deliveries' AND column_name = 'estimated_arrival'
    ) THEN
        ALTER TABLE deliveries
        ADD COLUMN estimated_arrival TIMESTAMP;
        
        COMMENT ON COLUMN deliveries.estimated_arrival IS 'Estimated time of arrival';
    END IF;
END $$;

