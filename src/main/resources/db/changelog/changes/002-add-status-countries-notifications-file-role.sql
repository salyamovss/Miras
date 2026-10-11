--liquibase formatted sql

--changeset miras:002-add-status-countries-notifications-file-role

-- 1. Optimistic locking version columns
ALTER TABLE material ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE publication_request ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- 2. Material status
ALTER TABLE material ADD COLUMN status VARCHAR(32) NOT NULL DEFAULT 'DRAFT';
ALTER TABLE material ADD COLUMN published_at TIMESTAMPTZ;
ALTER TABLE material ADD CONSTRAINT ck_material_status
    CHECK (status IN ('DRAFT', 'PENDING_REVIEW', 'PUBLISHED', 'ARCHIVED'));

-- 3. Country tables
CREATE TABLE country (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(10) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_country_code UNIQUE (code)
);

CREATE TABLE country_translation (
    country_id UUID NOT NULL,
    language_code VARCHAR(2) NOT NULL,
    name VARCHAR(200) NOT NULL,
    PRIMARY KEY (country_id, language_code),
    CONSTRAINT fk_country_translation_country FOREIGN KEY (country_id)
        REFERENCES country (id) ON DELETE CASCADE,
    CONSTRAINT ck_country_translation_language CHECK (language_code IN ('ky', 'ru', 'en'))
);

INSERT INTO country (id, code, active)
VALUES ('a0000000-0000-0000-0000-000000000001', 'KG', TRUE);

INSERT INTO country_translation (country_id, language_code, name) VALUES
    ('a0000000-0000-0000-0000-000000000001', 'ky', 'Кыргызстан'),
    ('a0000000-0000-0000-0000-000000000001', 'ru', 'Кыргызстан'),
    ('a0000000-0000-0000-0000-000000000001', 'en', 'Kyrgyzstan');

-- 4. Replace material.country (text) with material.country_id (FK)
ALTER TABLE material ADD COLUMN country_id UUID;
ALTER TABLE material ADD CONSTRAINT fk_material_country
    FOREIGN KEY (country_id) REFERENCES country (id);
ALTER TABLE material DROP COLUMN country;

-- 5. Notification table
CREATE TABLE notification (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_id UUID NOT NULL,
    type VARCHAR(64) NOT NULL,
    material_id UUID,
    publication_request_id UUID,
    read_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_recipient FOREIGN KEY (recipient_id)
        REFERENCES admin_user (id),
    CONSTRAINT fk_notification_material FOREIGN KEY (material_id)
        REFERENCES material (id) ON DELETE SET NULL,
    CONSTRAINT fk_notification_publication_request FOREIGN KEY (publication_request_id)
        REFERENCES publication_request (id) ON DELETE SET NULL,
    CONSTRAINT ck_notification_type CHECK (type IN ('PUBLICATION_APPROVED', 'PUBLICATION_REJECTED'))
);

-- 6. File role on material_file
ALTER TABLE material_file ADD COLUMN file_role VARCHAR(16) NOT NULL DEFAULT 'MAIN';
ALTER TABLE material_file ADD CONSTRAINT ck_material_file_role
    CHECK (file_role IN ('MAIN', 'COVER'));
-- COVER files don't need a language; MAIN must have one
ALTER TABLE material_file ALTER COLUMN language_code DROP NOT NULL;
ALTER TABLE material_file ADD CONSTRAINT ck_material_file_main_has_language
    CHECK ((file_role = 'MAIN' AND language_code IS NOT NULL) OR file_role = 'COVER');

-- 7. Indexes for public catalog
CREATE INDEX ix_material_status_published_at ON material (status, published_at DESC);
CREATE INDEX ix_material_country_id ON material (country_id);
CREATE INDEX ix_material_type ON material (material_type);

-- 8. Partial unique index: one PENDING_REVIEW per material
CREATE UNIQUE INDEX uq_material_one_pending
    ON publication_request (material_id) WHERE status = 'PENDING_REVIEW';

-- 9. Partial unique indexes for files
CREATE UNIQUE INDEX uq_material_one_cover
    ON material_file (material_id) WHERE file_role = 'COVER';
CREATE UNIQUE INDEX uq_material_file_main_language
    ON material_file (material_id, language_code) WHERE file_role = 'MAIN';

-- 10. Notification index
CREATE INDEX ix_notification_recipient
    ON notification (recipient_id, read_at, created_at DESC);

--rollback ALTER TABLE material_file DROP CONSTRAINT ck_material_file_main_has_language;
--rollback ALTER TABLE material_file ALTER COLUMN language_code SET NOT NULL;
--rollback ALTER TABLE material_file DROP CONSTRAINT ck_material_file_role;
--rollback ALTER TABLE material_file DROP COLUMN file_role;
--rollback DROP TABLE notification;
--rollback ALTER TABLE material ADD COLUMN country VARCHAR(120);
--rollback ALTER TABLE material DROP CONSTRAINT fk_material_country;
--rollback ALTER TABLE material DROP COLUMN country_id;
--rollback DROP TABLE country_translation;
--rollback DROP TABLE country;
--rollback ALTER TABLE material DROP CONSTRAINT ck_material_status;
--rollback ALTER TABLE material DROP COLUMN published_at;
--rollback ALTER TABLE material DROP COLUMN status;
--rollback ALTER TABLE publication_request DROP COLUMN version;
--rollback ALTER TABLE material DROP COLUMN version;
