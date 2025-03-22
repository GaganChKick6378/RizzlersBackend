-- Insert test data for property promotions
-- Active promotions with different date ranges
INSERT INTO property_promotion_schedule (property_id, promotion_id, price_factor, start_date, end_date, is_active, created_at, updated_at) 
VALUES 
(1, 101, 0.80, CURRENT_DATE - 10, CURRENT_DATE + 10, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 102, 0.85, CURRENT_DATE - 20, CURRENT_DATE - 5, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 103, 0.75, CURRENT_DATE + 5, CURRENT_DATE + 20, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 201, 0.90, CURRENT_DATE - 15, CURRENT_DATE + 15, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Inactive promotion
INSERT INTO property_promotion_schedule (property_id, promotion_id, price_factor, start_date, end_date, is_active, created_at, updated_at) 
VALUES 
(1, 104, 0.70, CURRENT_DATE - 5, CURRENT_DATE + 5, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP); 