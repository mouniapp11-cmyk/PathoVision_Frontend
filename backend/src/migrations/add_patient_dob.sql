-- Add date of birth field to Users table for patient profiles
ALTER TABLE Users ADD COLUMN IF NOT EXISTS date_of_birth DATE;

-- Add index for faster filtering
CREATE INDEX IF NOT EXISTS idx_users_dob ON Users(date_of_birth);
