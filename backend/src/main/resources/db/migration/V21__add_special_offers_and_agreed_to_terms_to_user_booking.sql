-- Add special_offers and agreed_to_terms columns to user_booking table
-- with default value of true for existing rows
ALTER TABLE public.user_booking
ADD COLUMN IF NOT EXISTS special_offers BOOLEAN NOT NULL DEFAULT TRUE,
ADD COLUMN IF NOT EXISTS agreed_to_terms BOOLEAN NOT NULL DEFAULT TRUE;

-- Add comment to explain the columns
COMMENT ON COLUMN public.user_booking.special_offers IS 'Whether the user has opted in to receive special offers';
COMMENT ON COLUMN public.user_booking.agreed_to_terms IS 'Whether the user has agreed to the terms and conditions'; 