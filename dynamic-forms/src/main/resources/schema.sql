CREATE TABLE IF NOT EXISTS users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    login_id VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS category (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    parent_id BIGINT,
    name VARCHAR(100) NOT NULL,
    depth INT NOT NULL,
    sort_order INT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_category_parent FOREIGN KEY (parent_id) REFERENCES category (id)
);

CREATE TABLE IF NOT EXISTS template (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    category_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    content_html CLOB NOT NULL,
    -- User-facing "temp save" of the filled-in form. Seeded to content_html
    -- on create so a fresh template starts with an identical draft. While
    -- draft_saved_at is still null (nobody has saved a draft yet), admin
    -- edits keep re-seeding this column too; once a real draft exists it's
    -- left alone so editing the template design can't silently wipe
    -- someone's in-progress work. See TemplateMapper.xml `update`.
    draft_content_html CLOB,
    draft_saved_at TIMESTAMP,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_by BIGINT,
    updated_at TIMESTAMP,
    CONSTRAINT fk_template_category FOREIGN KEY (category_id) REFERENCES category (id),
    CONSTRAINT fk_template_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT fk_template_updated_by FOREIGN KEY (updated_by) REFERENCES users (id)
);

ALTER TABLE template ADD COLUMN IF NOT EXISTS draft_content_html CLOB;
ALTER TABLE template ADD COLUMN IF NOT EXISTS draft_saved_at TIMESTAMP;
