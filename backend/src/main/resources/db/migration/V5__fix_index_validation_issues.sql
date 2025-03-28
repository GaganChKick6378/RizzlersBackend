-- First drop any existing indexes that might be causing conflicts
DROP INDEX IF EXISTS idx_reviews_room_type_id;
DROP INDEX IF EXISTS idx_room_type_amenities_room_type_id;
DROP INDEX IF EXISTS idx_room_type_amenities_amenity_id;

-- Then recreate them with IF NOT EXISTS to ensure idempotency
CREATE INDEX IF NOT EXISTS idx_reviews_room_type_id ON reviews(room_type_id);
CREATE INDEX IF NOT EXISTS idx_room_type_amenities_room_type_id ON room_type_amenities(room_type_id);
CREATE INDEX IF NOT EXISTS idx_room_type_amenities_amenity_id ON room_type_amenities(amenity_id);

-- Ensure tables exist (in case they were created through schema.sql but not migrations)
CREATE TABLE IF NOT EXISTS amenities (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS reviews (
    id SERIAL PRIMARY KEY,
    room_type_id INTEGER NOT NULL,
    rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    description TEXT NOT NULL,
    images TEXT[] NOT NULL DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    review_count INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS room_type_amenities (
    id SERIAL PRIMARY KEY,
    room_type_id INTEGER NOT NULL,
    amenity_id INTEGER NOT NULL,
    FOREIGN KEY (amenity_id) REFERENCES amenities(id),
    UNIQUE (room_type_id, amenity_id)
); 