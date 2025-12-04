ALTER TABLE certificates ADD COLUMN download_token VARCHAR(64) UNIQUE;

CREATE INDEX idx_certificates_download_token ON certificates(download_token);
