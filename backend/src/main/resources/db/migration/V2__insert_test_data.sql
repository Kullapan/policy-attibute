-- ============================================================
-- V2: PAMS Test Data — Sample attributes and policy mappings
-- ============================================================

-- ----------------------------------------------------------
-- Attribute Master: 12 sample attribute definitions
-- ----------------------------------------------------------
INSERT INTO attribute_master (code, display_name, data_type, status, is_required, regex_pattern, regex_error_msg, created_by) VALUES
('MAX_COVERAGE',        'Maximum Coverage',         'NUMBER',   'ACTIVE', TRUE,  '^\d+(\.\d{1,2})?$',           'Must be a valid number with up to 2 decimal places',   'SYSTEM'),
('DEDUCTIBLE_AMOUNT',   'Deductible Amount',        'NUMBER',   'ACTIVE', TRUE,  '^\d+(\.\d{1,2})?$',           'Must be a valid number with up to 2 decimal places',   'SYSTEM'),
('POLICY_START_DATE',   'Policy Start Date',        'DATE',     'ACTIVE', TRUE,  '^\d{4}-\d{2}-\d{2}$',         'Must be in YYYY-MM-DD format',                         'SYSTEM'),
('POLICY_END_DATE',     'Policy End Date',          'DATE',     'ACTIVE', TRUE,  '^\d{4}-\d{2}-\d{2}$',         'Must be in YYYY-MM-DD format',                         'SYSTEM'),
('IS_RENEWABLE',        'Is Renewable',             'BOOLEAN',  'ACTIVE', FALSE, '^(true|false)$',              'Must be true or false',                                'SYSTEM'),
('BENEFICIARY_NAME',    'Beneficiary Name',         'STRING',   'ACTIVE', TRUE,  '^[A-Za-z\s\-\.]{2,100}$',     'Only letters, spaces, hyphens, and dots (2-100 chars)','SYSTEM'),
('PREMIUM_FREQUENCY',   'Premium Frequency',        'STRING',   'ACTIVE', TRUE,  '^(MONTHLY|QUARTERLY|ANNUAL)$','Must be MONTHLY, QUARTERLY, or ANNUAL',                'SYSTEM'),
('COVERAGE_REGION',     'Coverage Region',          'STRING',   'ACTIVE', FALSE, '^[A-Z]{2,3}$',                'Must be a 2-3 letter uppercase region code',           'SYSTEM'),
('CLAIM_LIMIT',         'Annual Claim Limit',       'NUMBER',   'ACTIVE', FALSE, '^\d+(\.\d{1,2})?$',           'Must be a valid number with up to 2 decimal places',   'SYSTEM'),
('HAS_COPAY',           'Has Co-Payment',           'BOOLEAN',  'ACTIVE', FALSE, '^(true|false)$',              'Must be true or false',                                'SYSTEM'),
('RISK_CATEGORY',       'Risk Category',            'STRING',   'ACTIVE', TRUE,  '^(LOW|MEDIUM|HIGH|CRITICAL)$','Must be LOW, MEDIUM, HIGH, or CRITICAL',               'SYSTEM'),
('LEGACY_CODE',         'Legacy System Code',       'STRING',   'ARCHIVED', FALSE, NULL,                        NULL,                                                   'SYSTEM');

-- ----------------------------------------------------------
-- Policy Attribute Values: 3 sample policies with mappings
-- ----------------------------------------------------------

-- Policy: POL-2026-001 (Health Insurance - Individual)
INSERT INTO policy_attribute_values (policy_no, attribute_code, attribute_value, created_by) VALUES
('POL-2026-001', 'MAX_COVERAGE',        '500000.00',    'SYSTEM'),
('POL-2026-001', 'DEDUCTIBLE_AMOUNT',   '2500.00',      'SYSTEM'),
('POL-2026-001', 'POLICY_START_DATE',   '2026-01-15',   'SYSTEM'),
('POL-2026-001', 'POLICY_END_DATE',     '2027-01-14',   'SYSTEM'),
('POL-2026-001', 'IS_RENEWABLE',        'true',         'SYSTEM'),
('POL-2026-001', 'BENEFICIARY_NAME',    'John A. Smith','SYSTEM'),
('POL-2026-001', 'PREMIUM_FREQUENCY',   'MONTHLY',      'SYSTEM'),
('POL-2026-001', 'COVERAGE_REGION',     'US',           'SYSTEM'),
('POL-2026-001', 'CLAIM_LIMIT',         '100000.00',    'SYSTEM'),
('POL-2026-001', 'HAS_COPAY',           'true',         'SYSTEM'),
('POL-2026-001', 'RISK_CATEGORY',       'LOW',          'SYSTEM');

-- Policy: POL-2026-002 (Auto Insurance - Comprehensive)
INSERT INTO policy_attribute_values (policy_no, attribute_code, attribute_value, created_by) VALUES
('POL-2026-002', 'MAX_COVERAGE',        '150000.00',    'SYSTEM'),
('POL-2026-002', 'DEDUCTIBLE_AMOUNT',   '1000.00',      'SYSTEM'),
('POL-2026-002', 'POLICY_START_DATE',   '2026-03-01',   'SYSTEM'),
('POL-2026-002', 'POLICY_END_DATE',     '2027-02-28',   'SYSTEM'),
('POL-2026-002', 'IS_RENEWABLE',        'false',        'SYSTEM'),
('POL-2026-002', 'BENEFICIARY_NAME',    'Jane Doe',     'SYSTEM'),
('POL-2026-002', 'PREMIUM_FREQUENCY',   'QUARTERLY',    'SYSTEM'),
('POL-2026-002', 'RISK_CATEGORY',       'MEDIUM',       'SYSTEM');

-- Policy: POL-2026-003 (Life Insurance - Term)
INSERT INTO policy_attribute_values (policy_no, attribute_code, attribute_value, created_by) VALUES
('POL-2026-003', 'MAX_COVERAGE',        '1000000.00',   'SYSTEM'),
('POL-2026-003', 'DEDUCTIBLE_AMOUNT',   '0.00',         'SYSTEM'),
('POL-2026-003', 'POLICY_START_DATE',   '2026-06-01',   'SYSTEM'),
('POL-2026-003', 'POLICY_END_DATE',     '2046-05-31',   'SYSTEM'),
('POL-2026-003', 'IS_RENEWABLE',        'true',         'SYSTEM'),
('POL-2026-003', 'BENEFICIARY_NAME',    'Robert K. Lee','SYSTEM'),
('POL-2026-003', 'PREMIUM_FREQUENCY',   'ANNUAL',       'SYSTEM'),
('POL-2026-003', 'COVERAGE_REGION',     'SEA',          'SYSTEM'),
('POL-2026-003', 'RISK_CATEGORY',       'HIGH',         'SYSTEM');
