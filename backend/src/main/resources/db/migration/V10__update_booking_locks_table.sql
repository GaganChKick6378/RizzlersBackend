-- Add new columns to booking_locks table
ALTER TABLE booking_locks ADD COLUMN IF NOT EXISTS booking_id INTEGER;
ALTER TABLE booking_locks ADD COLUMN IF NOT EXISTS status_updated_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE booking_locks ADD COLUMN IF NOT EXISTS last_error_message VARCHAR(500);
ALTER TABLE booking_locks ADD COLUMN IF NOT EXISTS server_id VARCHAR(100);

-- Add index on booking_id
CREATE INDEX IF NOT EXISTS idx_booking_locks_booking_id ON booking_locks(booking_id);

-- Update the cleanup function to delete instead of changing status
CREATE OR REPLACE FUNCTION cleanup_expired_locks() RETURNS INTEGER AS $$
DECLARE
    affected_count INTEGER;
BEGIN
    DELETE FROM booking_locks
    WHERE lock_expiry < NOW() AND status = 'PENDING';
    
    GET DIAGNOSTICS affected_count = ROW_COUNT;
    RETURN affected_count;
END;
$$ LANGUAGE plpgsql; 