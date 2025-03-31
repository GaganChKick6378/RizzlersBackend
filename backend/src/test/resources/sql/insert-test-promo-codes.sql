-- Insert test promo codes
INSERT INTO promo_codes (promo_code_id, title, description, promo_code, price_factor, start_date, end_date, is_available, created_at, updated_at)
VALUES
(1, 'Summer Special', '10% off for summer bookings', 'SUMMER10', 0.9, CURRENT_DATE - 10, CURRENT_DATE + 20, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Holiday Deal', '15% off for holiday bookings', 'HOLIDAY15', 0.85, CURRENT_DATE - 5, CURRENT_DATE + 15, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Expired Offer', '10% off - expired offer', 'EXPIRED10', 0.9, CURRENT_DATE - 30, CURRENT_DATE - 10, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'Inactive Offer', '10% off - inactive offer', 'INACTIVE10', 0.9, CURRENT_DATE - 5, CURRENT_DATE + 5, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'Future Offer', '20% off - upcoming offer', 'FUTURE20', 0.8, CURRENT_DATE + 10, CURRENT_DATE + 30, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP); 