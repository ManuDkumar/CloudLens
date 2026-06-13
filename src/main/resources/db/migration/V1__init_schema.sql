CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);

CREATE TABLE file_metadata (
    internal_id UUID PRIMARY KEY,
    original_name VARCHAR(255) NOT NULL,
    storage_url VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    upload_timestamp TIMESTAMP NOT NULL,
    description VARCHAR(255),
    uploaded_by VARCHAR(255),
    extracted_metadata TEXT
);

CREATE INDEX idx_uploaded_by ON file_metadata (uploaded_by);
CREATE INDEX idx_original_name ON file_metadata (original_name);
CREATE INDEX idx_upload_timestamp ON file_metadata (upload_timestamp);
