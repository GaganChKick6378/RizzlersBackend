-- Add version column to booking_locks table for optimistic locking
ALTER TABLE booking_locks ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0 NOT NULL;

-- Add comment to explain the purpose of the version column
COMMENT ON COLUMN booking_locks.version IS 'Used for optimistic locking to handle concurrent booking requests'; 