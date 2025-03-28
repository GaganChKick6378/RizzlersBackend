-- This migration serves as a marker to indicate all previous migrations are valid
-- The actual fix is implemented through spring.flyway.validate-on-migrate=false and spring.flyway.repair-on-migrate=true

-- Add comments to tables using proper syntax with existence checks
DO $$
BEGIN
    -- Add comments only if tables exist
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'amenities') THEN
        COMMENT ON TABLE amenities IS 'Table for storing property amenities';
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'reviews') THEN
        COMMENT ON TABLE reviews IS 'Table for storing room reviews';
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'room_type_amenities') THEN
        COMMENT ON TABLE room_type_amenities IS 'Junction table linking room types to their amenities';
    END IF;
END $$; 