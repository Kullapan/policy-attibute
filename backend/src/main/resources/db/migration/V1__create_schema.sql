-- ============================================================
-- V1: PAMS Schema — attribute_master + policy_attribute_values
-- ============================================================

-- ----------------------------------------------------------
-- Table: attribute_master
-- ----------------------------------------------------------
CREATE TABLE attribute_master (
    code            VARCHAR(100)    NOT NULL,
    display_name    VARCHAR(255)    NOT NULL,
    data_type       VARCHAR(20)     NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    is_required     BOOLEAN         NOT NULL DEFAULT FALSE,
    regex_pattern   VARCHAR(255),
    regex_error_msg VARCHAR(255),
    version         BIGINT          NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(100)    NOT NULL DEFAULT 'SYSTEM',

    CONSTRAINT pk_attribute_master PRIMARY KEY (code),
    CONSTRAINT chk_attribute_code_upper CHECK (code = UPPER(code)),
    CONSTRAINT chk_data_type CHECK (data_type IN ('STRING', 'NUMBER', 'DATE', 'BOOLEAN')),
    CONSTRAINT chk_status CHECK (status IN ('ACTIVE', 'ARCHIVED'))
);

COMMENT ON TABLE  attribute_master IS 'Master dictionary of all policy attribute definitions';
COMMENT ON COLUMN attribute_master.code IS 'UPPER_SNAKE_CASE unique identifier, e.g. MAX_COVERAGE';
COMMENT ON COLUMN attribute_master.data_type IS 'Value type constraint: STRING | NUMBER | DATE | BOOLEAN';
COMMENT ON COLUMN attribute_master.status IS 'Soft-delete lifecycle: ACTIVE | ARCHIVED';
COMMENT ON COLUMN attribute_master.regex_pattern IS 'Optional regex pattern for value validation';
COMMENT ON COLUMN attribute_master.regex_error_msg IS 'User-facing error message when regex validation fails';

-- ----------------------------------------------------------
-- Table: policy_attribute_values
-- ----------------------------------------------------------
CREATE TABLE policy_attribute_values (
    policy_no       VARCHAR(50)     NOT NULL,
    attribute_code  VARCHAR(100)    NOT NULL,
    attribute_value TEXT,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(100)    NOT NULL DEFAULT 'SYSTEM',

    CONSTRAINT pk_policy_attribute_values PRIMARY KEY (policy_no, attribute_code),
    CONSTRAINT fk_policy_attr_code FOREIGN KEY (attribute_code)
        REFERENCES attribute_master (code)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE INDEX idx_policy_attr_policy_no ON policy_attribute_values (policy_no);

COMMENT ON TABLE  policy_attribute_values IS 'Maps attribute values to specific policies';
COMMENT ON COLUMN policy_attribute_values.policy_no IS 'Policy identifier from the upstream policy system';
COMMENT ON COLUMN policy_attribute_values.attribute_code IS 'FK reference to attribute_master.code';
