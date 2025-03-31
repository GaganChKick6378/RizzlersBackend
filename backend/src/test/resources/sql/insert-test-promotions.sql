-- Insert test promotions
INSERT INTO property_promotion_schedule (id, property_id, promotion_id, title, description, promo_code, price_factor, start_date, end_date, is_active, is_visible, created_at, updated_at)
VALUES
(1, 1, 100, 'Summer Special', '10% off for summer bookings', 'SUMMER10', 0.9, CURRENT_DATE - 10, CURRENT_DATE + 20, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, 200, 'Early Bird', '15% off for early bookings', 'EARLY15', 0.85, CURRENT_DATE - 5, CURRENT_DATE + 15, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 2, 300, 'Weekend Special', '20% off for weekend stays', 'WEEKEND20', 0.8, CURRENT_DATE - 15, CURRENT_DATE + 5, true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 2, 400, 'First Time Booking', '25% off for first booking', 'FIRST25', 0.75, CURRENT_DATE - 20, CURRENT_DATE - 10, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 3, 500, 'Holiday Special', '30% off for holiday bookings', 'HOLIDAY30', 0.7, CURRENT_DATE + 10, CURRENT_DATE + 30, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 3, 600, 'Premium Offer', '10% off for premium rooms', 'PREMIUM10', 0.9, CURRENT_DATE - 5, CURRENT_DATE + 5, false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP); 