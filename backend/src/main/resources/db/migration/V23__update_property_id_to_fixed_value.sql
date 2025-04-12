-- First, drop existing foreign key constraints that reference property_id
ALTER TABLE shifts DROP CONSTRAINT IF EXISTS fk_shifts_property_id;
ALTER TABLE staff DROP CONSTRAINT IF EXISTS fk_staff_property_id;
ALTER TABLE clean_tasks DROP CONSTRAINT IF EXISTS fk_clean_tasks_property_id;

-- Drop the current sequence
ALTER TABLE property_preferences ALTER COLUMN property_id DROP DEFAULT;
DROP SEQUENCE IF EXISTS property_preferences_property_id_seq CASCADE;

-- Update existing records to use property_id = 10
UPDATE property_preferences SET property_id = 10 WHERE property_id != 10;

-- Remove any conflicting records for property_id = 10 (if it exists)
DELETE FROM property_preferences WHERE property_id = 10 AND NOT (
  -- This keeps the row we want to make id=10 (helps if you have multiple properties)
  (timezone, check_out_time, check_in_time) = (SELECT timezone, check_out_time, check_in_time FROM property_preferences LIMIT 1)
);

-- Create a new fixed property with ID = 10 if none exists
INSERT INTO property_preferences (property_id, timezone, check_out_time, check_in_time)
SELECT 10, 'Asia/Kolkata', '11:00:00', '15:00:00'
WHERE NOT EXISTS (SELECT 1 FROM property_preferences WHERE property_id = 10);

-- Update foreign key constraints to reference property_id = 10
UPDATE shifts SET property_id = 10 WHERE property_id != 10;
UPDATE staff SET property_id = 10 WHERE property_id != 10;
UPDATE clean_tasks SET property_id = 10 WHERE property_id != 10;

-- Recreate the foreign key constraints
ALTER TABLE shifts ADD CONSTRAINT fk_shifts_property_id 
  FOREIGN KEY (property_id) REFERENCES property_preferences (property_id) ON DELETE CASCADE;
  
ALTER TABLE staff ADD CONSTRAINT fk_staff_property_id 
  FOREIGN KEY (property_id) REFERENCES property_preferences (property_id) ON DELETE CASCADE;
  
ALTER TABLE clean_tasks ADD CONSTRAINT fk_clean_tasks_property_id 
  FOREIGN KEY (property_id) REFERENCES property_preferences (property_id) ON DELETE CASCADE; 