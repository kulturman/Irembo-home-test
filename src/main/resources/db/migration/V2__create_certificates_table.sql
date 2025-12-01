CREATE TABLE certificates (
    id UUID PRIMARY KEY,
    template_id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    variables TEXT,
    status VARCHAR(50) NOT NULL,
    file_path VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_certificates_template FOREIGN KEY (template_id)
        REFERENCES templates(id) ON DELETE CASCADE
);

CREATE INDEX idx_certificates_tenant_id ON certificates(tenant_id);
CREATE INDEX idx_certificates_template_id ON certificates(template_id);
CREATE INDEX idx_certificates_status ON certificates(status);
