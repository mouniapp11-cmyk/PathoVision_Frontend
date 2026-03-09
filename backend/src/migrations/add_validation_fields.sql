-- Add validation and report fields to Cases table
ALTER TABLE Cases ADD COLUMN IF NOT EXISTS validation_status VARCHAR(20) DEFAULT 'pending';
-- pending, validated, signed_off

ALTER TABLE Cases ADD COLUMN IF NOT EXISTS pathologist_report TEXT;
ALTER TABLE Cases ADD COLUMN IF NOT EXISTS validated_at TIMESTAMP;
ALTER TABLE Cases ADD COLUMN IF NOT EXISTS validated_by_id INTEGER REFERENCES Users(id);
ALTER TABLE Cases ADD COLUMN IF NOT EXISTS signed_off_at TIMESTAMP;

-- Add index for faster filtering
CREATE INDEX IF NOT EXISTS idx_cases_validation_status ON Cases(validation_status);
