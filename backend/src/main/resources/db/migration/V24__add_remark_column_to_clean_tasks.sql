-- Add remark column to clean_tasks table
ALTER TABLE clean_tasks
ADD COLUMN remark VARCHAR(100);

-- Add comment to explain the column purpose
COMMENT ON COLUMN clean_tasks.remark IS 'Stores the detailed service-level task type (IMMEDIATE_CHECKOUT_CLEANING, DELAYED_CHECKOUT_CLEANING, EARLY_DAILY_CLEANUP, DAILY_CLEANUP)'; 