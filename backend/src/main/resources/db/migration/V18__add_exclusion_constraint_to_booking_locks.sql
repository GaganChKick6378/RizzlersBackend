-- Create the btree_gist extension if not already available
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- Add comment explaining what this migration does
COMMENT ON EXTENSION btree_gist IS 'Support for indexing common datatypes in GiST (used for exclusion constraints on date ranges)';

-- First, make sure any existing overlapping locks with PENDING or CONFIRMED status are resolved
-- You might want to manually review these before running the migration
-- or modify their status/dates as needed
DO $$
DECLARE
    overlap_count INTEGER;
BEGIN
    WITH overlapping_locks AS (
        SELECT a.id, b.id AS overlapping_id, a.room_id, a.start_date, a.end_date, a.status
        FROM booking_locks a
        JOIN booking_locks b ON 
            a.id != b.id AND 
            a.room_id = b.room_id AND 
            a.start_date <= b.end_date AND 
            a.end_date >= b.start_date AND
            a.status IN ('PENDING', 'CONFIRMED') AND
            b.status IN ('PENDING', 'CONFIRMED')
    )
    SELECT COUNT(*) INTO overlap_count FROM overlapping_locks;
    
    RAISE NOTICE 'Found % overlapping bookings that need to be resolved before adding constraint', overlap_count;
    
    -- If overlaps exist, print a warning but continue
    -- In a production environment, you might want to handle this differently
    IF overlap_count > 0 THEN
        RAISE WARNING 'There are overlapping bookings in the database. Consider resolving them before adding the constraint.';
    END IF;
END
$$;

-- Add the exclusion constraint to prevent overlapping date ranges for the same room
-- Only applies to active locks (PENDING or CONFIRMED status)
ALTER TABLE booking_locks
ADD CONSTRAINT no_overlapping_bookings
EXCLUDE USING gist (
    room_id WITH =,
    daterange(start_date, end_date, '[]') WITH &&
) WHERE (status IN ('PENDING', 'CONFIRMED'));

-- Add comments to explain the constraint
COMMENT ON CONSTRAINT no_overlapping_bookings ON booking_locks IS 
'Prevents overlapping date ranges for the same room when status is PENDING or CONFIRMED'; 