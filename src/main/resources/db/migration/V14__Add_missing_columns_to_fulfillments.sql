-- Add missing columns to fulfillments table
DO $$
BEGIN
    -- Priority
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'fulfillments' AND column_name = 'priority'
    ) THEN
        ALTER TABLE fulfillments
        ADD COLUMN priority VARCHAR(50) DEFAULT 'NORMAL';
        -- URGENT, HIGH, NORMAL, LOW
        
        COMMENT ON COLUMN fulfillments.priority IS 'Delivery priority level';
    END IF;
    
    -- Exception reason
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'fulfillments' AND column_name = 'exception_reason'
    ) THEN
        ALTER TABLE fulfillments
        ADD COLUMN exception_reason VARCHAR(200);
        
        COMMENT ON COLUMN fulfillments.exception_reason IS 'Reason for exception status';
    END IF;
    
    -- Delivery instructions
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'fulfillments' AND column_name = 'delivery_instructions'
    ) THEN
        ALTER TABLE fulfillments
        ADD COLUMN delivery_instructions TEXT;
        
        COMMENT ON COLUMN fulfillments.delivery_instructions IS 'Special delivery instructions';
    END IF;
    
    -- Scheduled delivery date
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'fulfillments' AND column_name = 'scheduled_delivery_date'
    ) THEN
        ALTER TABLE fulfillments
        ADD COLUMN scheduled_delivery_date TIMESTAMP;
        
        COMMENT ON COLUMN fulfillments.scheduled_delivery_date IS 'Scheduled delivery date/time';
    END IF;
    
    -- Delivery time window
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'fulfillments' AND column_name = 'delivery_time_window'
    ) THEN
        ALTER TABLE fulfillments
        ADD COLUMN delivery_time_window VARCHAR(100);
        
        COMMENT ON COLUMN fulfillments.delivery_time_window IS 'Preferred delivery time window';
    END IF;
END $$;

