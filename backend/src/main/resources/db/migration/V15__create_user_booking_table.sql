-- Create user_booking table to store booking financial details
CREATE TABLE IF NOT EXISTS public.user_booking (
    id BIGSERIAL PRIMARY KEY,
    booking_id INTEGER NOT NULL,
    property_id INTEGER,
    user_id UUID,
    guest_id INTEGER,
    nightly_rate DECIMAL(10, 2),
    subtotal DECIMAL(10, 2),
    taxes_and_fees DECIMAL(10, 2),
    total_for_stay DECIMAL(10, 2),
    promotion_id INTEGER,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

-- Create index for faster lookups by booking_id
CREATE INDEX IF NOT EXISTS idx_user_booking_booking_id
    ON public.user_booking USING btree (booking_id)
    TABLESPACE pg_default;

-- Create index for faster lookups by guest_id
CREATE INDEX IF NOT EXISTS idx_user_booking_guest_id
    ON public.user_booking USING btree (guest_id)
    TABLESPACE pg_default; 