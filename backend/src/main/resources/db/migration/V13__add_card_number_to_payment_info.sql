-- Replace card_name column with card_number column in payment_info table
-- First, drop the existing card_name column
ALTER TABLE payment_info DROP COLUMN IF EXISTS card_name;

-- Then, add the new card_number column
ALTER TABLE payment_info ADD COLUMN IF NOT EXISTS card_number VARCHAR(255); 