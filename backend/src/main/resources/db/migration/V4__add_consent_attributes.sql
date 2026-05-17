-- ============================================================
-- V4: Add consent attributes and sample policy consent data
-- ============================================================

-- ----------------------------------------------------------
-- New attribute definitions (consent-related, no CUSTOMER_NAME)
-- ----------------------------------------------------------
INSERT INTO attribute_master (code, display_name, data_type, status, is_required, regex_pattern, regex_error_msg, created_by)
VALUES
('PDPA_CONSENT',      'PDPA Consent',      'STRING', 'ACTIVE', FALSE, '^(Yes|No)$',                   'Must be Yes or No',                      'SYSTEM'),
('RPQ_COMPLETED',     'RPQ Completed',     'STRING', 'ACTIVE', FALSE, '^(Yes|No|Pending|Valid)$',      'Must be Yes, No, Pending, or Valid',      'SYSTEM'),
('MARKETING_CONSENT', 'Marketing Consent', 'STRING', 'ACTIVE', FALSE, '^(Yes|No)$',                   'Must be Yes or No',                      'SYSTEM')
ON CONFLICT (code) DO NOTHING;

-- ----------------------------------------------------------
-- Seed sample policies (consent-focused)
-- ----------------------------------------------------------
INSERT INTO policy_master (policy_no, status, created_by)
VALUES
('501-545623', 'ACTIVE', 'SYSTEM'),
('502-123456', 'ACTIVE', 'SYSTEM'),
('503-987654', 'ACTIVE', 'SYSTEM')
ON CONFLICT (policy_no) DO NOTHING;

-- ----------------------------------------------------------
-- Seed consent attribute values per policy
-- ----------------------------------------------------------

-- 501-545623 (Somchai Dee-ing)
INSERT INTO policy_attribute_values (policy_no, attribute_code, attribute_value, created_by)
VALUES
('501-545623', 'PDPA_CONSENT',      'Yes',     'SYSTEM'),
('501-545623', 'RPQ_COMPLETED',     'Pending', 'SYSTEM'),
('501-545623', 'MARKETING_CONSENT', 'Yes',     'SYSTEM')
ON CONFLICT (policy_no, attribute_code) DO NOTHING;

-- 502-123456 (Mana Permpoon)
INSERT INTO policy_attribute_values (policy_no, attribute_code, attribute_value, created_by)
VALUES
('502-123456', 'PDPA_CONSENT',      'No',    'SYSTEM'),
('502-123456', 'RPQ_COMPLETED',     'Valid', 'SYSTEM'),
('502-123456', 'MARKETING_CONSENT', 'No',    'SYSTEM')
ON CONFLICT (policy_no, attribute_code) DO NOTHING;

-- 503-987654 (Wassana Siri)
INSERT INTO policy_attribute_values (policy_no, attribute_code, attribute_value, created_by)
VALUES
('503-987654', 'PDPA_CONSENT',      'Yes',   'SYSTEM'),
('503-987654', 'RPQ_COMPLETED',     'Valid', 'SYSTEM'),
('503-987654', 'MARKETING_CONSENT', 'Yes',   'SYSTEM')
ON CONFLICT (policy_no, attribute_code) DO NOTHING;
