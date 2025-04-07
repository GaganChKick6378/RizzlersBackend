-- Create review_invitations table
CREATE TABLE IF NOT EXISTS review_invitations (
    id SERIAL PRIMARY KEY,
    booking_id INTEGER NOT NULL,
    guest_id INTEGER NOT NULL,
    guest_email VARCHAR(255) NOT NULL,
    token VARCHAR(64) NOT NULL UNIQUE,
    sent_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT unique_review_invitation_booking UNIQUE (booking_id)
);

CREATE INDEX idx_review_invitations_token ON review_invitations(token);
CREATE INDEX idx_review_invitations_booking ON review_invitations(booking_id);
CREATE INDEX idx_review_invitations_completed ON review_invitations(is_completed);

-- Create reviews_user table
CREATE TABLE IF NOT EXISTS reviews_user (
    id SERIAL PRIMARY KEY,
    invitation_id INTEGER NOT NULL,
    booking_id INTEGER NOT NULL,
    guest_id INTEGER NOT NULL,
    property_id INTEGER NOT NULL,
    room_type_id INTEGER NOT NULL,
    cleanliness_rating INTEGER NOT NULL CHECK (cleanliness_rating BETWEEN 1 AND 5),
    staff_service_rating INTEGER NOT NULL CHECK (staff_service_rating BETWEEN 1 AND 5),
    comfort_rating INTEGER NOT NULL CHECK (comfort_rating BETWEEN 1 AND 5),
    location_rating INTEGER NOT NULL CHECK (location_rating BETWEEN 1 AND 5),
    value_rating INTEGER NOT NULL CHECK (value_rating BETWEEN 1 AND 5),
    overall_rating INTEGER NOT NULL CHECK (overall_rating BETWEEN 1 AND 5),
    comment TEXT,
    images TEXT[],
    submitted_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    CONSTRAINT fk_invitation FOREIGN KEY (invitation_id) REFERENCES review_invitations(id),
    CONSTRAINT unique_review_per_invitation UNIQUE (invitation_id)
);

CREATE INDEX idx_reviews_user_booking_id ON reviews_user(booking_id);
CREATE INDEX idx_reviews_user_property_id ON reviews_user(property_id);
CREATE INDEX idx_reviews_user_room_type_id ON reviews_user(room_type_id); 