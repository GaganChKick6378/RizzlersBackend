-- Check if the price_factor column exists
DO $$ 
BEGIN
    -- Add price_factor column if it does not exist
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'property_promotion_schedule' 
        AND column_name = 'price_factor'
    ) THEN
        -- Add the column
        ALTER TABLE property_promotion_schedule
        ADD COLUMN price_factor DECIMAL(5,2) NOT NULL DEFAULT 1.0;
    END IF;
END $$; 