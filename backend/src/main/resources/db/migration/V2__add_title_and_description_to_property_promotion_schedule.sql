-- Add title and description columns to property_promotion_schedule
ALTER TABLE property_promotion_schedule 
ADD COLUMN IF NOT EXISTS title VARCHAR(255) NOT NULL DEFAULT 'Default Promotion Title',
ADD COLUMN IF NOT EXISTS description TEXT NOT NULL DEFAULT 'Default promotion description for existing records';

-- Create an index on the new title column for better performance
CREATE INDEX IF NOT EXISTS idx_promotion_schedule_title ON property_promotion_schedule(title); 