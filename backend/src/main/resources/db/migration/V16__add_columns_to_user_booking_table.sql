-- Add room_type_id and average_nightly_price columns to user_booking table
ALTER TABLE public.user_booking
ADD COLUMN IF NOT EXISTS room_type_id INTEGER,
ADD COLUMN IF NOT EXISTS average_nightly_price DECIMAL(10, 2);

-- Create index for faster lookups by room_type_id
CREATE INDEX IF NOT EXISTS idx_user_booking_room_type_id
    ON public.user_booking USING btree (room_type_id)
    TABLESPACE pg_default; 