-- ============================================================
-- V5: Revise test data display names and align POL-2026-001 values
-- ============================================================

-- 1. Translate all attribute display names to Thai
UPDATE attribute_master SET display_name = 'ความยินยอมในการเก็บรวบรวม ใช้ หรือเปิดเผยข้อมูลส่วนบุคคล (PDPA)' WHERE code = 'PDPA_CONSENT';
UPDATE attribute_master SET display_name = 'วันที่ให้ความยินยอม PDPA' WHERE code = 'PDPA_CONSENT_DATE';
UPDATE attribute_master SET display_name = 'ความยินยอมเพื่อวัตถุประสงค์ทางการตลาด' WHERE code = 'MARKETING_CONSENT';
UPDATE attribute_master SET display_name = 'วันที่ให้ความยินยอมทางการตลาด' WHERE code = 'MARKETING_CONSENT_DATE';

UPDATE attribute_master SET display_name = 'ผู้เอาประกันภัยมีอายุตั้งแต่ 60 ปีขึ้นไป ณ วันที่ทำสัญญา' WHERE code = 'VC_OLDER_THAN_60';
UPDATE attribute_master SET display_name = 'ระดับความรู้ความเข้าใจเกี่ยวกับการประกันภัย' WHERE code = 'VC_INSURANCE_KNOWLEDGE';
UPDATE attribute_master SET display_name = 'ผู้เอาประกันภัยมีความบกพร่องทางร่างกายหรือการเคลื่อนไหว' WHERE code = 'VC_PHYSICAL_DISABILITY';
UPDATE attribute_master SET display_name = 'ผู้เอาประกันภัยมีความบกพร่องทางสติปัญญาหรือจิตฟั่นเฟือน' WHERE code = 'VC_MENTAL_ILLNESS';

UPDATE attribute_master SET display_name = 'ระดับชั้นความเสี่ยงที่สอดคล้องกับเกณฑ์คำนวณคะแนน' WHERE code = 'RPQ_RISK_LEVEL';
UPDATE attribute_master SET display_name = 'คะแนนการทำแบบประเมินรวม (0 - 40 คะแนน)' WHERE code = 'RPQ_SCORE';
UPDATE attribute_master SET display_name = 'ช่วงอายุผู้ร่วมตอบแบบวัดระดับประเมินความเสี่ยง' WHERE code = 'RPQ_AGE_RANGE';
UPDATE attribute_master SET display_name = 'สัดส่วนค่าใช้จ่ายในการลงทุนเมื่อเทียบกับรายได้' WHERE code = 'RPQ_PC_EXPENSE';
UPDATE attribute_master SET display_name = 'สัดส่วนหนี้สินต่อทรัพย์สิน' WHERE code = 'RPQ_DEBT_EQUITY';
UPDATE attribute_master SET display_name = 'ประสบการณ์ในการลงทุนในหลักทรัพย์หรือทรัพย์สินทางการเงิน' WHERE code = 'RPQ_INVEST_EXPERIENCE';
UPDATE attribute_master SET display_name = 'อัตราผลตอบแทนจากการลงทุนที่คาดหวังต่อปี' WHERE code = 'RPQ_EXPECT_RETURN_YR';
UPDATE attribute_master SET display_name = 'วัตถุประสงค์หลักในการลงทุน' WHERE code = 'RPQ_INVEST_OBJECTIVE';
UPDATE attribute_master SET display_name = 'รูปแบบหรือแนวทางการลงทุนที่ยอมรับได้' WHERE code = 'RPQ_INVEST_STYLE';
UPDATE attribute_master SET display_name = 'ผลกระทบหากผลการลงทุนไม่เป็นไปตามคาดหวังหรือเกิดผลขาดทุนสูง' WHERE code = 'RPQ_HIGH_RISK_IMPACT';
UPDATE attribute_master SET display_name = 'ระดับความกังวลต่อโอกาสที่จะเกิดการขาดทุนจากเงินลงทุน' WHERE code = 'RPQ_PC_LOSS_CONCERN';
UPDATE attribute_master SET display_name = 'การดำเนินการเมื่อมูลค่าเงินลงทุนลดลงอย่างรวดเร็ว' WHERE code = 'RPQ_LOSS_ACTION';
UPDATE attribute_master SET display_name = 'ผู้เอาประกันยอมรับความเสี่ยงเรื่องอัตราแลกเปลี่ยนเงินในกองต่างประเทศ' WHERE code = 'RPQ_EXCHANGE_RATE';
UPDATE attribute_master SET display_name = 'การประเมินความเสี่ยง RPQ เสร็จสิ้น' WHERE code = 'RPQ_COMPLETED';

-- 2. Update regex validation pattern for RPQ_RISK_LEVEL to support 'High Risk (C4)'
UPDATE attribute_master 
SET regex_pattern = '^(Low|Medium|High|Very High|High Risk \(C4\))$',
    regex_error_msg = 'Must be Low, Medium, High, Very High, or High Risk (C4)'
WHERE code = 'RPQ_RISK_LEVEL';
