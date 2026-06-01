-- ============================================================
-- V2: Consolidated PAMS Seed Data
-- ============================================================

-- ----------------------------------------------------------
-- Seed Initial Attribute Groups
-- ----------------------------------------------------------
INSERT INTO attribute_group (code, display_name_en, display_name_th, display_order, created_by) VALUES
('CONSENT', 'Consent',                   'ความยินยอม (Consent)',          1, 'SYSTEM'),
('VC',      'Vulnerable Customer',       'กลุ่มเปราะบาง (VC)',             2, 'SYSTEM'),
('RPQ',     'Risk Profile Questionnaire', 'การประเมินความเสี่ยง (RPQ)',    3, 'SYSTEM');

-- ----------------------------------------------------------
-- Seed Attribute Definitions
-- ----------------------------------------------------------
INSERT INTO attribute_master (code, display_name, data_type, status, is_required, regex_pattern, regex_error_msg, group_code, created_by) VALUES
('PDPA_CONSENT',            'ความยินยอมในการเก็บรวบรวม ใช้ หรือเปิดเผยข้อมูลส่วนบุคคล (PDPA)', 'STRING',   'ACTIVE', FALSE, '^(Yes|No)$',                              'Must be Yes or No',                                    'CONSENT', 'SYSTEM'),
('PDPA_CONSENT_DATE',       'วันที่ให้ความยินยอม PDPA',                                    'DATE',     'ACTIVE', FALSE, '^\d{4}-\d{2}-\d{2}$',                     'Must be in YYYY-MM-DD format',                         'CONSENT', 'SYSTEM'),
('MARKETING_CONSENT',       'ความยินยอมเพื่อวัตถุประสงค์ทางการตลาด',                           'STRING',   'ACTIVE', FALSE, '^(Yes|No)$',                              'Must be Yes or No',                                    'CONSENT', 'SYSTEM'),
('MARKETING_CONSENT_DATE',  'วันที่ให้ความยินยอมทางการตลาด',                                'DATE',     'ACTIVE', FALSE, '^\d{4}-\d{2}-\d{2}$',                     'Must be in YYYY-MM-DD format',                         'CONSENT', 'SYSTEM'),

('VC_OLDER_THAN_60',        'ผู้เอาประกันภัยมีอายุตั้งแต่ 60 ปีขึ้นไป ณ วันที่ทำสัญญา',                'BOOLEAN',  'ACTIVE', FALSE, '^(true|false)$',                          'Must be true or false',                                'VC',      'SYSTEM'),
('VC_INSURANCE_KNOWLEDGE',  'ระดับความรู้ความเข้าใจเกี่ยวกับการประกันภัย',                          'STRING',   'ACTIVE', FALSE, '^(High|Medium|Low|None)$',                  'Must be High, Medium, Low, or None',                   'VC',      'SYSTEM'),
('VC_PHYSICAL_DISABILITY',  'ผู้เอาประกันภัยมีความบกพร่องทางร่างกายหรือการเคลื่อนไหว',                  'BOOLEAN',  'ACTIVE', FALSE, '^(true|false)$',                          'Must be true or false',                                'VC',      'SYSTEM'),
('VC_MENTAL_ILLNESS',       'ผู้เอาประกันภัยมีความบกพร่องทางสติปัญญาหรือจิตฟั่นเฟือน',                  'BOOLEAN',  'ACTIVE', FALSE, '^(true|false)$',                          'Must be true or false',                                'VC',      'SYSTEM'),

('RPQ_RISK_LEVEL',          'ระดับชั้นความเสี่ยงที่สอดคล้องกับเกณฑ์คำนวณคะแนน',                       'STRING',   'ACTIVE', FALSE, '^(Low|Medium|High|Very High|High Risk \(C4\))$', 'Must be Low, Medium, High, Very High, or High Risk (C4)', 'RPQ',     'SYSTEM'),
('RPQ_SCORE',               'คะแนนการทำแบบประเมินรวม (0 - 40 คะแนน)',                           'NUMBER',   'ACTIVE', FALSE, '^\d+$',                                   'Must be a positive integer',                           'RPQ',     'SYSTEM'),
('RPQ_AGE_RANGE',           'ช่วงอายุผู้ร่วมตอบแบบวัดระดับประเมินความเสี่ยง',                         'STRING',   'ACTIVE', FALSE, '^(\d{1,2}\-\d{1,2}|\d{1,2}\+)$',          'Must be a range (e.g. 18-25) or a boundary (e.g. 60+)', 'RPQ',     'SYSTEM'),
('RPQ_PC_EXPENSE',          'สัดส่วนค่าใช้จ่ายในการลงทุนเมื่อเทียบกับรายได้',                          'NUMBER',   'ACTIVE', FALSE, '^\d+(\.\d{1,2})?$',                       'Must be a valid decimal number',                       'RPQ',     'SYSTEM'),
('RPQ_DEBT_EQUITY',         'สัดส่วนหนี้สินต่อทรัพย์สิน',                                       'STRING',   'ACTIVE', FALSE, '^[A-Za-z0-9\s\-]+$',                      'Must contain only alphanumeric, space, or hyphen',     'RPQ',     'SYSTEM'),
('RPQ_INVEST_EXPERIENCE',   'ประสบการณ์ในการลงทุนในหลักทรัพย์หรือทรัพย์สินทางการเงิน',                 'STRING',   'ACTIVE', FALSE, '^(None|Less than 1 year|1-3 years|3-5 years|More than 5 years)$', 'Must be a valid investment experience range', 'RPQ', 'SYSTEM'),
('RPQ_EXPECT_RETURN_YR',    'อัตราผลตอบแทนจากการลงทุนที่คาดหวังต่อปี',                            'NUMBER',   'ACTIVE', FALSE, '^\-?\d+(\.\d{1,2})?$',                    'Must be a valid decimal percentage',                   'RPQ',     'SYSTEM'),
('RPQ_INVEST_OBJECTIVE',    'วัตถุประสงค์หลักในการลงทุน',                                      'STRING',   'ACTIVE', FALSE, '^(Capital Preservation|Income|Growth|Speculation)$', 'Must be Capital Preservation, Income, Growth, or Speculation', 'RPQ', 'SYSTEM'),
('RPQ_INVEST_STYLE',        'รูปแบบหรือแนวทางการลงทุนที่ยอมรับได้',                            'STRING',   'ACTIVE', FALSE, '^(Conservative|Moderate|Aggressive)$',   'Must be Conservative, Moderate, or Aggressive',        'RPQ',     'SYSTEM'),
('RPQ_HIGH_RISK_IMPACT',    'ผลกระทบหากผลการลงทุนไม่เป็นไปตามคาดหวังหรือเกิดผลขาดทุนสูง',              'STRING',   'ACTIVE', FALSE, '^(Acceptable|Unacceptable|Neutral)$',     'Must be Acceptable, Unacceptable, or Neutral',         'RPQ',     'SYSTEM'),
('RPQ_PC_LOSS_CONCERN',     'ระดับความกังวลต่อโอกาสที่จะเกิดการขาดทุนจากเงินลงทุน',                   'STRING',   'ACTIVE', FALSE, '^(High|Medium|Low|None)$',                  'Must be High, Medium, Low, or None',                   'RPQ',     'SYSTEM'),
('RPQ_LOSS_ACTION',         'การดำเนินการเมื่อมูลค่าเงินลงทุนลดลงอย่างรวดเร็ว',                          'STRING',   'ACTIVE', FALSE, '^(Sell All|Hold|Buy More)$',              'Must be Sell All, Hold, or Buy More',                  'RPQ',     'SYSTEM'),
('RPQ_EXCHANGE_RATE',       'ผู้เอาประกันยอมรับความเสี่ยงเรื่องอัตราแลกเปลี่ยนเงินในกองต่างประเทศ',        'NUMBER',   'ACTIVE', FALSE, '^\d+(\.\d{1,6})?$',                       'Must be a valid decimal number',                       'RPQ',     'SYSTEM'),
('RPQ_COMPLETED',           'การประเมินความเสี่ยง RPQ เสร็จสิ้น',                                'STRING',   'ACTIVE', FALSE, '^(Yes|No|Pending|Valid)$',                'Must be Yes, No, Pending, or Valid',                    'RPQ',     'SYSTEM');

-- ----------------------------------------------------------
-- Seed Sample Policies
-- ----------------------------------------------------------
INSERT INTO policy_master (policy_no, status, created_by) VALUES
('POL-2026-001', 'ACTIVE', 'SYSTEM'),
('POL-2026-002', 'ACTIVE', 'SYSTEM'),
('POL-2026-003', 'ACTIVE', 'SYSTEM'),
('501-545623',   'ACTIVE', 'SYSTEM'),
('502-123456',   'ACTIVE', 'SYSTEM'),
('503-987654',   'ACTIVE', 'SYSTEM');

-- ----------------------------------------------------------
-- Seed Policy Attribute Mapping Values
-- ----------------------------------------------------------
-- POL-2026-001 (คุณธนพงษ์ มั่งมี)
INSERT INTO policy_attribute_values (policy_no, attribute_code, attribute_value, created_by) VALUES
('POL-2026-001', 'PDPA_CONSENT',            'Yes',     'SYSTEM'),
('POL-2026-001', 'RPQ_COMPLETED',           'Valid',   'SYSTEM'),
('POL-2026-001', 'MARKETING_CONSENT',       'Yes',     'SYSTEM'),
('POL-2026-001', 'VC_OLDER_THAN_60',        'true',    'SYSTEM'),
('POL-2026-001', 'VC_INSURANCE_KNOWLEDGE',  'High',    'SYSTEM'),
('POL-2026-001', 'RPQ_SCORE',               '32',      'SYSTEM'),
('POL-2026-001', 'RPQ_RISK_LEVEL',          'High',    'SYSTEM');

-- POL-2026-002 (Jane Doe)
INSERT INTO policy_attribute_values (policy_no, attribute_code, attribute_value, created_by) VALUES
('POL-2026-002', 'PDPA_CONSENT',            'No',      'SYSTEM'),
('POL-2026-002', 'RPQ_COMPLETED',           'Pending', 'SYSTEM'),
('POL-2026-002', 'MARKETING_CONSENT',       'No',      'SYSTEM'),
('POL-2026-002', 'VC_OLDER_THAN_60',        'false',   'SYSTEM'),
('POL-2026-002', 'VC_INSURANCE_KNOWLEDGE',  'Medium',  'SYSTEM'),
('POL-2026-002', 'RPQ_SCORE',               '15',      'SYSTEM'),
('POL-2026-002', 'RPQ_RISK_LEVEL',          'Medium',  'SYSTEM');

-- POL-2026-003 (Robert K. Lee)
INSERT INTO policy_attribute_values (policy_no, attribute_code, attribute_value, created_by) VALUES
('POL-2026-003', 'PDPA_CONSENT',            'Yes',     'SYSTEM'),
('POL-2026-003', 'RPQ_COMPLETED',           'Valid',   'SYSTEM'),
('POL-2026-003', 'MARKETING_CONSENT',       'No',      'SYSTEM'),
('POL-2026-003', 'VC_OLDER_THAN_60',        'false',   'SYSTEM'),
('POL-2026-003', 'VC_INSURANCE_KNOWLEDGE',  'Low',     'SYSTEM'),
('POL-2026-003', 'RPQ_SCORE',               '22',      'SYSTEM'),
('POL-2026-003', 'RPQ_RISK_LEVEL',          'Medium',  'SYSTEM');

-- 501-545623 (Somchai Dee-ing)
INSERT INTO policy_attribute_values (policy_no, attribute_code, attribute_value, created_by) VALUES
('501-545623', 'PDPA_CONSENT',      'Yes',     'SYSTEM'),
('501-545623', 'RPQ_COMPLETED',     'Pending', 'SYSTEM'),
('501-545623', 'MARKETING_CONSENT', 'Yes',     'SYSTEM');

-- 502-123456 (Mana Permpoon)
INSERT INTO policy_attribute_values (policy_no, attribute_code, attribute_value, created_by) VALUES
('502-123456', 'PDPA_CONSENT',      'No',    'SYSTEM'),
('502-123456', 'RPQ_COMPLETED',     'Valid', 'SYSTEM'),
('502-123456', 'MARKETING_CONSENT', 'No',    'SYSTEM');

-- 503-987654 (Wassana Siri)
INSERT INTO policy_attribute_values (policy_no, attribute_code, attribute_value, created_by) VALUES
('503-987654', 'PDPA_CONSENT',      'Yes',   'SYSTEM'),
('503-987654', 'RPQ_COMPLETED',     'Valid', 'SYSTEM'),
('503-987654', 'MARKETING_CONSENT', 'Yes',   'SYSTEM');
