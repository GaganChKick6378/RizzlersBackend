CREATE TABLE IF NOT EXISTS public.users
(
    id uuid NOT NULL DEFAULT gen_random_uuid(),
    tenant_id integer NOT NULL,
    email character varying(255) COLLATE pg_catalog."default",
    phone character varying(20) COLLATE pg_catalog."default",
    first_name character varying(100) COLLATE pg_catalog."default",
    last_name character varying(100) COLLATE pg_catalog."default",
    email_verified boolean NOT NULL DEFAULT false,
    preferences_config jsonb NOT NULL DEFAULT '{"currency": "USD", "language": "english"}'::jsonb,
    created_at timestamp with time zone NOT NULL DEFAULT now(),
    updated_at timestamp with time zone NOT NULL DEFAULT now(),
    deleted_at timestamp with time zone,
    guest_id integer,
    CONSTRAINT users_pkey PRIMARY KEY (id)
);

-- Create index on email for faster lookups
CREATE INDEX IF NOT EXISTS idx_users_email ON public.users(email); 