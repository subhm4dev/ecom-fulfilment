-- Create pod_photos table for @ElementCollection
CREATE TABLE IF NOT EXISTS pod_photos (
    pod_id UUID NOT NULL REFERENCES proof_of_delivery(id) ON DELETE CASCADE,
    photo_url VARCHAR(500) NOT NULL,
    PRIMARY KEY (pod_id, photo_url)
);

CREATE INDEX IF NOT EXISTS idx_pod_photos_pod_id ON pod_photos(pod_id);

