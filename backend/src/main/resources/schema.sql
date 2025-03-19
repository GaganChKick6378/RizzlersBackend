-- Drop existing tables if they exist
DROP TABLE IF EXISTS students;

-- Tenant-specific configuration table
CREATE TABLE IF NOT EXISTS tenant_configuration (
    id SERIAL PRIMARY KEY,
    tenant_id INTEGER NOT NULL,
    page VARCHAR(50) NOT NULL, -- 'landing', 'results', 'details', 'checkout'
    field VARCHAR(100) NOT NULL, -- 'header_logo', 'page_title', etc.
    value JSONB NOT NULL, -- Flexible JSON structure to store configuration values
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Property assignment table
CREATE TABLE IF NOT EXISTS tenant_property_assignment (
    id SERIAL PRIMARY KEY,
    tenant_id INTEGER NOT NULL,
    property_id INTEGER NOT NULL,
    is_assigned BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (tenant_id, property_id)
);

-- Promotion schedule table
CREATE TABLE IF NOT EXISTS property_promotion_schedule (
    id SERIAL PRIMARY KEY,
    property_id INTEGER NOT NULL,
    promotion_id INTEGER NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Room images table
CREATE TABLE IF NOT EXISTS room_type_images (
    id SERIAL PRIMARY KEY,
    tenant_id INTEGER NOT NULL,
    room_type_id INTEGER NOT NULL,
    property_id INTEGER NOT NULL,
    image_urls VARCHAR(512)[] NOT NULL,  -- Array of image URLs
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- Composite unique constraint to ensure no duplicates
    UNIQUE (tenant_id, property_id, room_type_id, display_order)
);

-- Guest type definitions for dropdown
CREATE TABLE IF NOT EXISTS guest_type_definition (
    id SERIAL PRIMARY KEY,
    tenant_id INTEGER NOT NULL,
    guest_type VARCHAR(50) NOT NULL, -- 'adult', 'teen', 'kid'
    min_age INTEGER NOT NULL,
    max_age INTEGER NOT NULL,
    description VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_tenant_configuration_tenant_id ON tenant_configuration(tenant_id);
CREATE INDEX IF NOT EXISTS idx_tenant_configuration_page ON tenant_configuration(page);
CREATE INDEX IF NOT EXISTS idx_tenant_configuration_is_active ON tenant_configuration(is_active);

CREATE INDEX IF NOT EXISTS idx_tenant_property_tenant_id ON tenant_property_assignment(tenant_id);
CREATE INDEX IF NOT EXISTS idx_tenant_property_property_id ON tenant_property_assignment(property_id);
CREATE INDEX IF NOT EXISTS idx_tenant_property_is_assigned ON tenant_property_assignment(is_assigned);

CREATE INDEX IF NOT EXISTS idx_promotion_schedule_property_id ON property_promotion_schedule(property_id);
CREATE INDEX IF NOT EXISTS idx_promotion_schedule_promotion_id ON property_promotion_schedule(promotion_id);
CREATE INDEX IF NOT EXISTS idx_promotion_schedule_dates ON property_promotion_schedule(start_date, end_date);

CREATE INDEX IF NOT EXISTS idx_room_type_images_tenant_id ON room_type_images(tenant_id);
CREATE INDEX IF NOT EXISTS idx_room_type_images_room_type_id ON room_type_images(room_type_id);
CREATE INDEX IF NOT EXISTS idx_room_type_images_property_id ON room_type_images(property_id);

CREATE INDEX IF NOT EXISTS idx_guest_type_tenant_id ON guest_type_definition(tenant_id);
CREATE INDEX IF NOT EXISTS idx_guest_type_is_active ON guest_type_definition(is_active); 