-- ============================================================
-- V6: Introduce attribute_group and link it to attribute_master
-- ============================================================

-- ----------------------------------------------------------
-- Table: attribute_group
-- ----------------------------------------------------------
CREATE TABLE attribute_group (
    code            VARCHAR(50)     NOT NULL,
    display_name_en VARCHAR(100)    NOT NULL,
    display_name_th VARCHAR(100)    NOT NULL,
    display_order   INT             NOT NULL DEFAULT 0,
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    version         BIGINT          NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(100)    NOT NULL DEFAULT 'SYSTEM',

    CONSTRAINT pk_attribute_group PRIMARY KEY (code),
    CONSTRAINT chk_group_status CHECK (status IN ('ACTIVE', 'ARCHIVED'))
);

COMMENT ON TABLE  attribute_group IS 'Defines group categories for policy attributes';
COMMENT ON COLUMN attribute_group.code IS 'Unique code identifier for the group, e.g. CONSENT';

-- ----------------------------------------------------------
-- Seed Initial Attribute Groups
-- ----------------------------------------------------------
INSERT INTO attribute_group (code, display_name_en, display_name_th, display_order, created_by) VALUES
('CONSENT', 'Consent',                   'ความยินยอม (Consent)',          1, 'SYSTEM'),
('VC',      'Vulnerable Customer',       'กลุ่มเปราะบาง (VC)',             2, 'SYSTEM'),
('RPQ',     'Risk Profile Questionnaire', 'การประเมินความเสี่ยง (RPQ)',    3, 'SYSTEM');

-- ----------------------------------------------------------
-- Add group_code to attribute_master
-- ----------------------------------------------------------
ALTER TABLE attribute_master ADD COLUMN group_code VARCHAR(50);

ALTER TABLE attribute_master ADD CONSTRAINT fk_attr_master_group_code
    FOREIGN KEY (group_code) REFERENCES attribute_group (code)
    ON UPDATE CASCADE ON DELETE SET NULL;

COMMENT ON COLUMN attribute_master.group_code IS 'Links the attribute definition to an attribute_group';

-- ----------------------------------------------------------
-- Migrate existing attribute definitions to their groups
-- ----------------------------------------------------------
UPDATE attribute_master
SET group_code = 'CONSENT'
WHERE code IN ('PDPA_CONSENT', 'PDPA_CONSENT_DATE', 'MARKETING_CONSENT', 'MARKETING_CONSENT_DATE');

UPDATE attribute_master
SET group_code = 'VC'
WHERE code IN ('VC_OLDER_THAN_60', 'VC_INSURANCE_KNOWLEDGE', 'VC_PHYSICAL_DISABILITY', 'VC_MENTAL_ILLNESS');

UPDATE attribute_master
SET group_code = 'RPQ'
WHERE code LIKE 'RPQ_%';
