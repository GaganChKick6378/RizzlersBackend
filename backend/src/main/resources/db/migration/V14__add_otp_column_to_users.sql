-- Add OTP column to users table for booking cancellation verification
ALTER TABLE IF EXISTS public.users
ADD COLUMN otp character varying(6),
ADD COLUMN otp_expiry timestamp with time zone;

-- Create index on OTP for faster lookups
CREATE INDEX IF NOT EXISTS idx_users_otp
    ON public.users USING btree
    (otp COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default; 