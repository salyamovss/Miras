--liquibase formatted sql

--changeset miras:001-create-initial-schema
CREATE TABLE admin_user (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(160) NOT NULL,
    last_name VARCHAR(160),
    role VARCHAR(32) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_admin_user_email UNIQUE (email),
    CONSTRAINT ck_admin_user_role CHECK (role IN ('ADMIN', 'SUPER_ADMIN'))
);

CREATE TABLE material (
    id UUID PRIMARY KEY,
    material_type VARCHAR(64),
    primary_language_code VARCHAR(2),
    country VARCHAR(120),
    year_or_period VARCHAR(120),
    categories_and_keywords TEXT,
    creation_place TEXT,
    access_condition VARCHAR(32),
    created_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_material_created_by FOREIGN KEY (created_by) REFERENCES admin_user (id),
    CONSTRAINT ck_material_type CHECK (material_type IS NULL OR material_type IN ('BOOK_RESEARCH', 'AUDIO', 'VIDEO', 'PHOTOGRAPHY')),
    CONSTRAINT ck_material_language CHECK (primary_language_code IS NULL OR primary_language_code IN ('ky', 'ru', 'en')),
    CONSTRAINT ck_material_access_condition CHECK (access_condition IS NULL OR access_condition IN ('OPEN_ACCESS', 'READ_ONLY'))
);

CREATE TABLE publication_request (
    id UUID PRIMARY KEY,
    material_id UUID NOT NULL,
    status VARCHAR(32) NOT NULL,
    submitted_by UUID NOT NULL,
    submitted_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_by UUID,
    reviewed_at TIMESTAMPTZ,
    review_note TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_publication_request_material FOREIGN KEY (material_id)
        REFERENCES material (id) ON DELETE CASCADE,
    CONSTRAINT fk_publication_request_submitted_by FOREIGN KEY (submitted_by)
        REFERENCES admin_user (id),
    CONSTRAINT fk_publication_request_reviewed_by FOREIGN KEY (reviewed_by)
        REFERENCES admin_user (id),
    CONSTRAINT ck_publication_request_status CHECK (status IN ('PENDING_REVIEW', 'APPROVED', 'REJECTED'))
);

CREATE INDEX ix_publication_request_status_submitted_at
    ON publication_request (status, submitted_at DESC);
CREATE INDEX ix_publication_request_material_created_at
    ON publication_request (material_id, created_at DESC);

CREATE TABLE material_translation (
    material_id UUID NOT NULL,
    language_code VARCHAR(2) NOT NULL,
    title VARCHAR(500),
    short_description TEXT,
    PRIMARY KEY (material_id, language_code),
    CONSTRAINT fk_material_translation_material FOREIGN KEY (material_id)
        REFERENCES material (id) ON DELETE CASCADE,
    CONSTRAINT ck_material_translation_language CHECK (language_code IN ('ky', 'ru', 'en'))
);

CREATE TABLE author (
    id UUID PRIMARY KEY,
    display_name VARCHAR(300) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE material_author (
    material_id UUID NOT NULL,
    author_id UUID NOT NULL,
    display_order INTEGER NOT NULL,
    PRIMARY KEY (material_id, author_id),
    CONSTRAINT uq_material_author_order UNIQUE (material_id, display_order),
    CONSTRAINT fk_material_author_material FOREIGN KEY (material_id)
        REFERENCES material (id) ON DELETE CASCADE,
    CONSTRAINT fk_material_author_author FOREIGN KEY (author_id)
        REFERENCES author (id)
);

CREATE TABLE material_file (
    id UUID PRIMARY KEY,
    material_id UUID NOT NULL,
    language_code VARCHAR(2) NOT NULL,
    original_filename VARCHAR(500) NOT NULL,
    content_type VARCHAR(160) NOT NULL,
    size_bytes BIGINT NOT NULL,
    storage_key VARCHAR(1024) NOT NULL,
    source_and_rights_holder TEXT,
    uploaded_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_material_file_material FOREIGN KEY (material_id)
        REFERENCES material (id) ON DELETE CASCADE,
    CONSTRAINT fk_material_file_uploaded_by FOREIGN KEY (uploaded_by)
        REFERENCES admin_user (id),
    CONSTRAINT ck_material_file_language CHECK (language_code IN ('ky', 'ru', 'en')),
    CONSTRAINT ck_material_file_size CHECK (size_bytes >= 0)
);

CREATE INDEX ix_material_file_material ON material_file (material_id);
--rollback DROP TABLE material_file;
--rollback DROP TABLE publication_request;
--rollback DROP TABLE material_author;
--rollback DROP TABLE author;
--rollback DROP TABLE material_translation;
--rollback DROP TABLE material;
--rollback DROP TABLE admin_user;
