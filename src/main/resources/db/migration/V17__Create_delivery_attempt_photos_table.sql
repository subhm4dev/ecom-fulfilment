-- Create delivery_attempt_photos table for @ElementCollection
CREATE TABLE IF NOT EXISTS delivery_attempt_photos (
    attempt_id UUID NOT NULL REFERENCES delivery_attempts(id) ON DELETE CASCADE,
    photo_url VARCHAR(500) NOT NULL,
    PRIMARY KEY (attempt_id, photo_url)
);

CREATE INDEX IF NOT EXISTS idx_attempt_photos_attempt_id ON delivery_attempt_photos(attempt_id);

