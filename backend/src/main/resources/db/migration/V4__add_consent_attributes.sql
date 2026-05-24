-- ============================================================
-- V4: Add consent attributes and sample policy consent data
-- ============================================================

-- ----------------------------------------------------------
-- Seed sample policies
-- ----------------------------------------------------------
INSERT INTO policy_master (policy_no, status, created_by)
VALUES
('POL-2026-001', 'ACTIVE', 'SYSTEM'),
('POL-2026-002', 'ACTIVE', 'SYSTEM'),
('POL-2026-003', 'ACTIVE', 'SYSTEM'),
('501-545623',   'ACTIVE', 'SYSTEM'),
('502-123456',   'ACTIVE', 'SYSTEM'),
('503-987654',   'ACTIVE', 'SYSTEM')
ON CONFLICT (policy_no) DO NOTHING;

-- ----------------------------------------------------------
-- Seed attribute values per policy
-- ----------------------------------------------------------

-- POL-2026-001 (คุณธนพงษ์ มั่งมี)
INSERT INTO policy_attribute_values (policy_no, attribute_code, attribute_value, created_by)
VALUES
('POL-2026-001', 'PDPA_CONSENT',            'Yes',     'SYSTEM'),
('POL-2026-001', 'RPQ_COMPLETED',           'Valid',   'SYSTEM'),
('POL-2026-001', 'MARKETING_CONSENT',       'Yes',     'SYSTEM'),
('POL-2026-001', 'VC_OLDER_THAN_60',        'true',    'SYSTEM'),
('POL-2026-001', 'VC_INSURANCE_KNOWLEDGE',  'High',    'SYSTEM'),
('POL-2026-001', 'RPQ_SCORE',               '32',      'SYSTEM'),
('POL-2026-001', 'RPQ_RISK_LEVEL',          'High',    'SYSTEM')
ON CONFLICT (policy_no, attribute_code) DO NOTHING;

-- POL-2026-002 (Jane Doe)
INSERT INTO policy_attribute_values (policy_no, attribute_code, attribute_value, created_by)
VALUES
('POL-2026-002', 'PDPA_CONSENT',            'No',      'SYSTEM'),
('POL-2026-002', 'RPQ_COMPLETED',           'Pending', 'SYSTEM'),
('POL-2026-002', 'MARKETING_CONSENT',       'No',      'SYSTEM'),
('POL-2026-002', 'VC_OLDER_THAN_60',        'false',   'SYSTEM'),
('POL-2026-002', 'VC_INSURANCE_KNOWLEDGE',  'Medium',  'SYSTEM'),
('POL-2026-002', 'RPQ_SCORE',               '15',      'SYSTEM'),
('POL-2026-002', 'RPQ_RISK_LEVEL',          'Medium',  'SYSTEM')
ON CONFLICT (policy_no, attribute_code) DO NOTHING;

-- POL-2026-003 (Robert K. Lee)
INSERT INTO policy_attribute_values (policy_no, attribute_code, attribute_value, created_by)
VALUES
('POL-2026-003', 'PDPA_CONSENT',            'Yes',     'SYSTEM'),
('POL-2026-003', 'RPQ_COMPLETED',           'Valid',   'SYSTEM'),
('POL-2026-003', 'MARKETING_CONSENT',       'No',      'SYSTEM'),
('POL-2026-003', 'VC_OLDER_THAN_60',        'false',   'SYSTEM'),
('POL-2026-003', 'VC_INSURANCE_KNOWLEDGE',  'Low',     'SYSTEM'),
('POL-2026-003', 'RPQ_SCORE',               '22',      'SYSTEM'),
('POL-2026-003', 'RPQ_RISK_LEVEL',          'Medium',  'SYSTEM')
ON CONFLICT (policy_no, attribute_code) DO NOTHING;

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
