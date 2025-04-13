-- First, drop the existing exclusion constraint
ALTER TABLE booking_locks
DROP CONSTRAINT IF EXISTS no_overlapping_bookings;

-- Re-create the constraint with exclusive upper bound '[)' instead of inclusive '[]'
ALTER TABLE booking_locks
ADD CONSTRAINT no_overlapping_bookings 
EXCLUDE USING gist (
    daterange(start_date, end_date, '[)'::text) WITH &&,
    room_id WITH =
) 
WHERE (status::text = ANY (ARRAY['PENDING'::character varying, 'CONFIRMED'::character varying]::text[]));

-- Add a comment on the updated constraint
COMMENT ON CONSTRAINT no_overlapping_bookings ON booking_locks
    IS 'Prevents overlapping date ranges for the same room when status is PENDING or CONFIRMED. Uses exclusive end_date.'; 