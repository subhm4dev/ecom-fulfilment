-- Add alternate recipient support to age_verifications table
-- Whoever receives the order (customer OR alternate) must be age verified
DO $$
BEGIN
    -- Add alternate recipient fields
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'age_verifications' AND column_name = 'alternate_recipient_id'
    ) THEN
        ALTER TABLE age_verifications
        ADD COLUMN alternate_recipient_id UUID REFERENCES alternate_recipients(id) ON DELETE SET NULL,
        ADD COLUMN verified_by_alternate BOOLEAN DEFAULT FALSE,
        ADD COLUMN verified_user_id UUID, -- The actual user who was verified (customer OR alternate)
        ADD COLUMN verified_user_phone VARCHAR(20), -- Phone of verified user
        ADD COLUMN verified_user_name VARCHAR(200); -- Name of verified user
        
        COMMENT ON COLUMN age_verifications.alternate_recipient_id IS 'Alternate recipient ID if verification done by alternate';
        COMMENT ON COLUMN age_verifications.verified_by_alternate IS 'True if verified by alternate recipient instead of customer';
        COMMENT ON COLUMN age_verifications.verified_user_id IS 'Actual user ID who was verified (customer or alternate)';
        COMMENT ON COLUMN age_verifications.verified_user_phone IS 'Phone of the user who was verified';
        COMMENT ON COLUMN age_verifications.verified_user_name IS 'Name of the user who was verified';
        
        CREATE INDEX IF NOT EXISTS idx_age_verification_alternate ON age_verifications(alternate_recipient_id);
        CREATE INDEX IF NOT EXISTS idx_age_verification_verified_user ON age_verifications(verified_user_id);
    END IF;
END $$;

