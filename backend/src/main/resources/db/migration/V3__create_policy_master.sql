-- ============================================================
-- V3: Create policy_master and add foreign key
-- ============================================================

-- ----------------------------------------------------------
-- Table: policy_master
-- ----------------------------------------------------------
CREATE TABLE policy_master (
    policy_no       VARCHAR(50)     NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(100)    NOT NULL DEFAULT 'SYSTEM',

    CONSTRAINT pk_policy_master PRIMARY KEY (policy_no),
    CONSTRAINT chk_policy_status CHECK (status IN ('ACTIVE', 'ARCHIVED'))
);

COMMENT ON TABLE  policy_master IS 'Master table for policies';
COMMENT ON COLUMN policy_master.policy_no IS 'Policy unique identifier';

-- ----------------------------------------------------------
-- Seed existing policies so we can add the FK
-- ----------------------------------------------------------
INSERT INTO policy_master (policy_no, created_by)
SELECT DISTINCT policy_no, 'SYSTEM'
FROM policy_attribute_values
ON CONFLICT (policy_no) DO NOTHING;

-- ----------------------------------------------------------
-- Alter policy_attribute_values to add FK
-- ----------------------------------------------------------
ALTER TABLE policy_attribute_values
ADD CONSTRAINT fk_policy_no FOREIGN KEY (policy_no)
REFERENCES policy_master (policy_no)
ON UPDATE CASCADE ON DELETE RESTRICT;
