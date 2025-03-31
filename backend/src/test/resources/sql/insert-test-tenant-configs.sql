-- Insert test tenant configurations
INSERT INTO tenant_configuration (id, tenant_id, page, field, value, is_active, created_at, updated_at)
VALUES
(1, 100, 'homepage', 'logo', '{"url": "https://example.com/logo.png"}', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 100, 'homepage', 'theme', '{"primary": "#1a73e8", "secondary": "#f5f5f5"}', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 100, 'search', 'filters', '{"categories": ["hotel", "resort", "villa"]}', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 100, 'booking', 'payment', '{"methods": ["credit", "debit", "paypal"]}', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 100, 'profile', 'preferences', '{"notifications": true, "language": "en"}', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 200, 'homepage', 'logo', '{"url": "https://example.org/logo.png"}', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 200, 'search', 'filters', '{"categories": ["apartment", "condo"]}', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 300, 'homepage', 'theme', '{"primary": "#ff5722", "secondary": "#e0e0e0"}', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP); 