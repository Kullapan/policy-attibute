-- ============================================================
-- V2: PAMS Test Data — Sample attributes and policy mappings
-- ============================================================

-- ----------------------------------------------------------
-- Attribute Master: 22 attribute definitions from policy_att.md + RPQ_COMPLETED
-- ----------------------------------------------------------
INSERT INTO attribute_master (code, display_name, data_type, status, is_required, regex_pattern, regex_error_msg, created_by) VALUES
('PDPA_CONSENT',            'PDPA Consent',                             'STRING',   'ACTIVE', FALSE, '^(Yes|No)$',                              'Must be Yes or No',                                    'SYSTEM'),
('PDPA_CONSENT_DATE',       'PDPA Consent Date',                        'DATE',     'ACTIVE', FALSE, '^\d{4}-\d{2}-\d{2}$',                     'Must be in YYYY-MM-DD format',                         'SYSTEM'),
('MARKETING_CONSENT',       'Marketing Consent',                        'STRING',   'ACTIVE', FALSE, '^(Yes|No)$',                              'Must be Yes or No',                                    'SYSTEM'),
('MARKETING_CONSENT_DATE',  'Marketing Consent Date',                   'DATE',     'ACTIVE', FALSE, '^\d{4}-\d{2}-\d{2}$',                     'Must be in YYYY-MM-DD format',                         'SYSTEM'),
('VC_OLDER_THAN_60',        'Vulnerable Customer: Older Than 60',       'BOOLEAN',  'ACTIVE', FALSE, '^(true|false)$',                          'Must be true or false',                                'SYSTEM'),
('VC_INSURANCE_KNOWLEDGE',  'Vulnerable Customer: Insurance Knowledge',  'STRING',   'ACTIVE', FALSE, '^(High|Medium|Low|None)$',                  'Must be High, Medium, Low, or None',                   'SYSTEM'),
('VC_PHYSICAL_DISABILITY',  'Vulnerable Customer: Physical Disability',  'BOOLEAN',  'ACTIVE', FALSE, '^(true|false)$',                          'Must be true or false',                                'SYSTEM'),
('VC_MENTAL_ILLNESS',       'Vulnerable Customer: Mental Illness',      'BOOLEAN',  'ACTIVE', FALSE, '^(true|false)$',                          'Must be true or false',                                'SYSTEM'),
('RPQ_RISK_LEVEL',          'RPQ Risk Level',                           'STRING',   'ACTIVE', FALSE, '^(Low|Medium|High|Very High)$',           'Must be Low, Medium, High, or Very High',              'SYSTEM'),
('RPQ_SCORE',               'RPQ Score',                                'NUMBER',   'ACTIVE', FALSE, '^\d+$',                                   'Must be a positive integer',                           'SYSTEM'),
('RPQ_AGE_RANGE',           'RPQ Age Range',                            'STRING',   'ACTIVE', FALSE, '^(\d{1,2}\-\d{1,2}|\d{1,2}\+)$',          'Must be a range (e.g. 18-25) or a boundary (e.g. 60+)', 'SYSTEM'),
('RPQ_PC_EXPENSE',          'RPQ PC Expense',                           'NUMBER',   'ACTIVE', FALSE, '^\d+(\.\d{1,2})?$',                       'Must be a valid decimal number',                       'SYSTEM'),
('RPQ_DEBT_EQUITY',         'RPQ Debt to Equity',                       'STRING',   'ACTIVE', FALSE, '^[A-Za-z0-9\s\-]+$',                      'Must contain only alphanumeric, space, or hyphen',     'SYSTEM'),
('RPQ_INVEST_EXPERIENCE',   'RPQ Investment Experience',                'STRING',   'ACTIVE', FALSE, '^(None|Less than 1 year|1-3 years|3-5 years|More than 5 years)$', 'Must be a valid investment experience range', 'SYSTEM'),
('RPQ_EXPECT_RETURN_YR',    'RPQ Expected Return Per Year',             'NUMBER',   'ACTIVE', FALSE, '^\-?\d+(\.\d{1,2})?$',                    'Must be a valid decimal percentage',                   'SYSTEM'),
('RPQ_INVEST_OBJECTIVE',    'RPQ Investment Objective',                 'STRING',   'ACTIVE', FALSE, '^(Capital Preservation|Income|Growth|Speculation)$', 'Must be Capital Preservation, Income, Growth, or Speculation', 'SYSTEM'),
('RPQ_INVEST_STYLE',        'RPQ Investment Style',                     'STRING',   'ACTIVE', FALSE, '^(Conservative|Moderate|Aggressive)$',   'Must be Conservative, Moderate, or Aggressive',        'SYSTEM'),
('RPQ_HIGH_RISK_IMPACT',    'RPQ High Risk Impact',                     'STRING',   'ACTIVE', FALSE, '^(Acceptable|Unacceptable|Neutral)$',     'Must be Acceptable, Unacceptable, or Neutral',         'SYSTEM'),
('RPQ_PC_LOSS_CONCERN',     'RPQ Percent Loss Concern',                 'STRING',   'ACTIVE', FALSE, '^(High|Medium|Low|None)$',                  'Must be High, Medium, Low, or None',                   'SYSTEM'),
('RPQ_LOSS_ACTION',         'RPQ Loss Action',                          'STRING',   'ACTIVE', FALSE, '^(Sell All|Hold|Buy More)$',              'Must be Sell All, Hold, or Buy More',                  'SYSTEM'),
('RPQ_EXCHANGE_RATE',       'RPQ Exchange Rate',                        'NUMBER',   'ACTIVE', FALSE, '^\d+(\.\d{1,6})?$',                       'Must be a valid decimal number',                       'SYSTEM'),
('RPQ_COMPLETED',           'RPQ Completed',                            'STRING',   'ACTIVE', FALSE, '^(Yes|No|Pending|Valid)$',                'Must be Yes, No, Pending, or Valid',                    'SYSTEM');
