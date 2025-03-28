-- Add promo_code and is_visible columns to property_promotion_schedule
ALTER TABLE property_promotion_schedule 
ADD COLUMN IF NOT EXISTS promo_code VARCHAR(50) UNIQUE DEFAULT NULL,
ADD COLUMN IF NOT EXISTS is_visible BOOLEAN NOT NULL DEFAULT TRUE;

-- Create an index on the promo_code column for better query performance
CREATE INDEX IF NOT EXISTS idx_promotion_schedule_promo_code ON property_promotion_schedule(promo_code);

-- Update existing records with default promo codes (will only apply if promo_code is NULL)
UPDATE property_promotion_schedule
SET promo_code = 'PROMO' || id
WHERE promo_code IS NULL;

-- Sample promo codes for demonstration purposes
-- Note: These will fail if there are no matching property_id or promotion_id values
-- Insert only if the same property_id and promotion_id combination doesn't already exist
DO $$
BEGIN
    -- Only insert if there are no existing records
    IF (SELECT COUNT(*) FROM property_promotion_schedule) = 0 THEN
        INSERT INTO property_promotion_schedule 
        (property_id, promotion_id, price_factor, start_date, end_date, is_active, title, description, promo_code, is_visible)
        VALUES
        (1, 1, 0.80, '2023-06-01', '2023-08-31', TRUE, 'Summer Sale 2023', 'Get 20% off on all bookings for summer 2023', 'SUMMER20', TRUE),
        (2, 2, 0.85, '2023-01-01', '2023-12-31', TRUE, 'New User Discount', 'First-time users get 15% off their first booking', 'NEWUSER15', TRUE),
        (3, 3, 0.90, '2023-01-01', '2023-12-31', TRUE, 'Weekend Special', 'Book on weekends and get 10% off', 'WEEKEND10', TRUE),
        (4, 4, 0.75, '2023-11-15', '2024-01-15', TRUE, 'Holiday Season', 'Special 25% discount for holiday season bookings', 'HOLIDAY25', TRUE),
        (5, 5, 0.70, '2023-07-15', '2023-07-17', FALSE, 'Flash Sale', '30% off for next 48 hours', 'FLASH30', FALSE),
        (6, 6, 0.80, '2023-09-01', '2023-09-30', TRUE, 'Anniversary Special', 'Celebrating our anniversary with 20% off all bookings', 'ANNIV20', TRUE),
        (7, 7, 0.85, '2023-03-01', '2023-04-15', FALSE, 'Spring Break', 'Spring break special offer with 15% off', 'SPRING15', FALSE),
        (8, 8, 0.75, '2023-01-01', '2023-12-31', TRUE, 'Extended Stay', 'Book for 5+ nights and get 25% off', 'EXTENDED25', TRUE),
        (9, 9, 0.80, '2023-01-01', '2023-12-31', TRUE, 'Corporate Discount', 'Special 20% discount for corporate bookings', 'CORP20', TRUE),
        (10, 10, 0.85, '2023-01-01', '2023-12-31', TRUE, 'Early Bird Offer', 'Book 30 days in advance and get 15% off', 'EARLY15', TRUE);
    END IF;
END $$; 