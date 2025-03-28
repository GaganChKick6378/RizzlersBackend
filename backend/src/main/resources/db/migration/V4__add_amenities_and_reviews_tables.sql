-- Create amenities table
CREATE TABLE IF NOT EXISTS amenities (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

-- Create reviews table
CREATE TABLE IF NOT EXISTS reviews (
    id SERIAL PRIMARY KEY,
    room_type_id INTEGER NOT NULL,
    rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    description TEXT NOT NULL,
    images TEXT[] NOT NULL DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    review_count INTEGER NOT NULL DEFAULT 0
);

-- Create room_type_amenities linking table
CREATE TABLE IF NOT EXISTS room_type_amenities (
    id SERIAL PRIMARY KEY,
    room_type_id INTEGER NOT NULL,
    amenity_id INTEGER NOT NULL,
    FOREIGN KEY (amenity_id) REFERENCES amenities(id),
    UNIQUE (room_type_id, amenity_id)
);

-- Create indexes for better performance (with safety checks)
DO $$ 
BEGIN
    -- Only create index if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_reviews_room_type_id') THEN
        CREATE INDEX idx_reviews_room_type_id ON reviews(room_type_id);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_room_type_amenities_room_type_id') THEN
        CREATE INDEX idx_room_type_amenities_room_type_id ON room_type_amenities(room_type_id);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_room_type_amenities_amenity_id') THEN
        CREATE INDEX idx_room_type_amenities_amenity_id ON room_type_amenities(amenity_id);
    END IF;
END $$; 