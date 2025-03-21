-- Delete existing data to avoid duplicate keys during re-initialization
DELETE FROM tenant_configuration;
DELETE FROM tenant_property_assignment;
DELETE FROM property_promotion_schedule;
DELETE FROM room_type_images;
DELETE FROM guest_type_definition;

-- Reset sequences
ALTER SEQUENCE tenant_configuration_id_seq RESTART WITH 1;
ALTER SEQUENCE tenant_property_assignment_id_seq RESTART WITH 1;
ALTER SEQUENCE property_promotion_schedule_id_seq RESTART WITH 1;
ALTER SEQUENCE room_type_images_id_seq RESTART WITH 1;
ALTER SEQUENCE guest_type_definition_id_seq RESTART WITH 1;

-- Sample tenant configuration data
INSERT INTO tenant_configuration (tenant_id, page, field, value, is_active)
VALUES
    -- Landing page configurations for tenant 1
    (1, 'landing', 'header_logo', '{"url": "https://example.com/logos/tenant1-logo.png", "alt": "Resort Logo"}', TRUE),
    (1, 'landing', 'page_title', '{"text": "Book Your Dream Vacation"}', TRUE),
    (1, 'landing', 'banner_image', '{"url": "https://example.com/banners/beach-resort.jpg", "alt": "Beach Resort"}', TRUE),
    (1, 'landing', 'length_of_stay', '{"min": 1, "max": 30, "default": 3}', TRUE),
    (1, 'landing', 'guest_options', '{"show": true, "use_guest_type_definitions": true}', TRUE),
    (1, 'landing', 'room_options', '{"show": true, "max_rooms": 3}', TRUE),
    (1, 'landing', 'accessibility_options', '{"show": true, "options": ["wheelchair", "hearing", "visual"]}', TRUE),
    (1, 'landing', 'number_of_rooms', '{"value": 3, "min": 1, "max": 5}', TRUE),
    
    -- Results page configurations for tenant 1
    (1, 'results', 'filters', '{"show": true, "position": "left"}', TRUE),
    (1, 'results', 'filter_options', '{"price": true, "amenities": true, "room_type": true, "accessibility": true}', TRUE),
    (1, 'results', 'sort_options', '{"price_low_high": true, "price_high_low": true, "rating": true}', TRUE),
    
    -- Room details configurations for tenant 1
    (1, 'details', 'show_images', '{"show": true, "max_images": 10}', TRUE),
    (1, 'details', 'show_description', '{"show": true}', TRUE),
    (1, 'details', 'show_amenities', '{"show": true}', TRUE),
    
    -- Checkout page configurations for tenant 1
    (1, 'checkout', 'traveler_info', '{"fields": ["name", "email", "phone", "address"]}', TRUE),
    (1, 'checkout', 'billing_info', '{"fields": ["card_number", "expiry", "cvv", "billing_address"]}', TRUE),
    (1, 'checkout', 'taxes_surcharges', '{"show": true, "tax_rate": 0.12, "resort_fee": 25}', TRUE),
    (1, 'checkout', 'due_at_resort', '{"percentage": 20}', TRUE),
    
    -- Landing page configurations for tenant 2
    (2, 'landing', 'header_logo', '{"url": "https://example.com/logos/tenant2-logo.png", "alt": "Hotel Logo"}', TRUE),
    (2, 'landing', 'page_title', '{"text": "Find Your Perfect Stay"}', TRUE),
    (2, 'landing', 'banner_image', '{"url": "https://example.com/banners/luxury-hotel.jpg", "alt": "Luxury Hotel"}', TRUE);

-- Sample property assignment data
INSERT INTO tenant_property_assignment (tenant_id, property_id, is_assigned, is_active)
VALUES
    (1, 1, FALSE, TRUE),
    (1, 2, FALSE, TRUE),
    (1, 3, FALSE, TRUE),
    (1, 4, FALSE, TRUE),
    (1, 5, FALSE, TRUE),
    (1, 6, FALSE, TRUE),
    (1, 7, FALSE, TRUE),
    (1, 8, FALSE, TRUE),
    (1, 9, FALSE, TRUE),
    (1, 10, TRUE, TRUE),
    (1, 11, FALSE, TRUE),
    (1, 12, FALSE, TRUE),
    (1, 13, FALSE, TRUE),
    (1, 14, FALSE, TRUE),
    (1, 15, FALSE, TRUE),
    (1, 16, FALSE, TRUE),
    (1, 17, FALSE, TRUE),
    (1, 18, FALSE, TRUE),
    (1, 19, FALSE, TRUE),
    (1, 20, FALSE, TRUE),
    (1, 21, FALSE, TRUE),
    (1, 22, FALSE, TRUE),
    (1, 23, FALSE, TRUE),
    (1, 24, FALSE, TRUE);

-- Sample promotion schedule data
INSERT INTO property_promotion_schedule (property_id, promotion_id, start_date, end_date, is_active)
VALUES
    (1, 1, '2023-12-01', '2023-12-31', TRUE),
    (1, 2, '2024-01-01', '2024-01-31', TRUE),
    (2, 3, '2023-12-15', '2024-01-15', TRUE),
    (5, 4, '2023-12-01', '2024-02-29', TRUE),
    (6, 5, '2024-01-01', '2024-03-31', TRUE);

-- Sample room type images data
INSERT INTO room_type_images (tenant_id, room_type_id, property_id, image_urls, display_order, is_active)
VALUES
    (1, 1, 1, ARRAY['https://example.com/images/room1-1.jpg', 'https://example.com/images/room1-2.jpg', 'https://example.com/images/room1-3.jpg'], 1, TRUE),
    (1, 2, 1, ARRAY['https://example.com/images/room2-1.jpg', 'https://example.com/images/room2-2.jpg'], 1, TRUE),
    (1, 3, 2, ARRAY['https://example.com/images/room3-1.jpg', 'https://example.com/images/room3-2.jpg', 'https://example.com/images/room3-3.jpg'], 1, TRUE),
    (2, 4, 5, ARRAY['https://example.com/images/room4-1.jpg', 'https://example.com/images/room4-2.jpg'], 1, TRUE),
    (2, 5, 6, ARRAY['https://example.com/images/room5-1.jpg', 'https://example.com/images/room5-2.jpg', 'https://example.com/images/room5-3.jpg'], 1, TRUE);

-- Sample guest type definitions
INSERT INTO guest_type_definition (tenant_id, guest_type, min_age, max_age, description, is_active, max_count)
VALUES
    (1, 'adult', 18, 999, 'Adults (18+)', TRUE, 4),
    (1, 'teen', 13, 17, 'Teens (13-17)', TRUE, 3),
    (1, 'kid', 0, 12, 'Kids (0-12)', TRUE, 2),
    (2, 'adult', 18, 999, 'Adults (18+)', TRUE, 4),
    (2, 'child', 0, 17, 'Children (0-17)', TRUE, 2); 