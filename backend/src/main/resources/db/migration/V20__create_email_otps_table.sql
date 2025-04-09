-- Create email OTPs table for storing verification codes
CREATE TABLE IF NOT EXISTS public.email_otps
(
    id uuid NOT NULL DEFAULT gen_random_uuid(),
    email character varying(255) NOT NULL,
    otp character varying(6) NOT NULL,
    expiry_time timestamp with time zone NOT NULL,
    verified boolean NOT NULL DEFAULT false,
    attempt_count integer NOT NULL DEFAULT 0,
    last_attempt_time timestamp with time zone,
    session_expiry_time timestamp with time zone,
    created_at timestamp with time zone NOT NULL DEFAULT now(),
    updated_at timestamp with time zone NOT NULL DEFAULT now(),
    CONSTRAINT email_otps_pkey PRIMARY KEY (id)
);

-- Create index on email for faster lookups
CREATE INDEX IF NOT EXISTS idx_email_otps_email ON public.email_otps(email);

-- Create index on OTP for faster lookups
CREATE INDEX IF NOT EXISTS idx_email_otps_otp ON public.email_otps(otp);

-- Create index on expiry_time for cleanup jobs
CREATE INDEX IF NOT EXISTS idx_email_otps_expiry_time ON public.email_otps(expiry_time); 