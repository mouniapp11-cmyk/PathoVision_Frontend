-- Add MRN (Medical Record Number) field to Cases table
-- MRN is auto-generated and unique per case
ALTER TABLE Cases ADD COLUMN IF NOT EXISTS mrn VARCHAR(20) UNIQUE;

-- Add index for faster MRN lookups
CREATE INDEX IF NOT EXISTS idx_cases_mrn ON Cases(mrn);
