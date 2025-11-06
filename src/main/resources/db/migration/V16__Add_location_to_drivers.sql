-- Add location and earnings fields to drivers table
DO $$
BEGIN
    -- Current location
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'drivers' AND column_name = 'current_latitude'
    ) THEN
        ALTER TABLE drivers
        ADD COLUMN current_latitude DECIMAL(10, 8);
        
        COMMENT ON COLUMN drivers.current_latitude IS 'Current driver latitude';
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'drivers' AND column_name = 'current_longitude'
    ) THEN
        ALTER TABLE drivers
        ADD COLUMN current_longitude DECIMAL(11, 8);
        
        COMMENT ON COLUMN drivers.current_longitude IS 'Current driver longitude';
    END IF;
    
    -- Last location update
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'drivers' AND column_name = 'last_location_update'
    ) THEN
        ALTER TABLE drivers
        ADD COLUMN last_location_update TIMESTAMP;
        
        COMMENT ON COLUMN drivers.last_location_update IS 'Last location update timestamp';
    END IF;
    
    -- Earnings
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'drivers' AND column_name = 'earnings'
    ) THEN
        ALTER TABLE drivers
        ADD COLUMN earnings DECIMAL(10, 2) DEFAULT 0.00;
        
        COMMENT ON COLUMN drivers.earnings IS 'Total earnings for driver';
    END IF;
END $$;

