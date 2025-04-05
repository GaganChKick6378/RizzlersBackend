-- Create billing_info table
CREATE TABLE IF NOT EXISTS billing_info (
    id SERIAL PRIMARY KEY,
    booking_id INTEGER NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    mailing_address1 VARCHAR(255),
    mailing_address2 VARCHAR(255),
    country VARCHAR(255),
    city VARCHAR(255),
    state VARCHAR(255),
    zip VARCHAR(255),
    phone VARCHAR(255),
    email VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Add index for faster lookups by booking_id
CREATE INDEX IF NOT EXISTS idx_billing_booking_id ON billing_info(booking_id);

-- Create payment_info table
CREATE TABLE IF NOT EXISTS payment_info (
    id SERIAL PRIMARY KEY,
    booking_id INTEGER NOT NULL,
    card_number VARCHAR(255),
    exp_month VARCHAR(255),
    exp_year VARCHAR(255),
    special_offers BOOLEAN,
    agreed_to_terms BOOLEAN,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Add index for faster lookups by booking_id
CREATE INDEX IF NOT EXISTS idx_payment_booking_id ON payment_info(booking_id); 