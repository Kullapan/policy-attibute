import { useState, useMemo } from 'react';
import type { PolicyAttributeValue, PolicyMaster, AttributeGroup } from '../types';
import { CUSTOMER_INFO } from '../pages/PolicyListPage';

interface Props {
  policy: PolicyMaster | null;
  attributes: PolicyAttributeValue[];
  values: Record<string, string>;
  onValueChange: (code: string, value: string) => void;
  onSaveField: (code: string) => void;
  onAddAttributeClick: () => void;
  savingField: string | null;
  loading: boolean;
  onBack: () => void;
  groups: AttributeGroup[];
}

// ── Sub-component for individual attribute card ────────────────────────────────
interface AttributeCardProps {
  attr: PolicyAttributeValue;
  value: string;
  onValueChange: (val: string) => void;
  onSaveField: () => void;
  saving: boolean;
}

function AttributeCard({ attr, value, onValueChange, onSaveField, saving }: AttributeCardProps) {
  const [error, setError] = useState<string | null>(null);
  const [touched, setTouched] = useState(false);

  const validate = (val: string) => {
    if (attr.isRequired && !val.trim()) {
      return 'This field is required';
    }
    if (attr.regexPattern && val.trim()) {
      try {
        const pattern = new RegExp(attr.regexPattern);
        if (!pattern.test(val)) {
          return attr.regexErrorMsg || 'Value does not match expected pattern';
        }
      } catch {
        // Ignore compile errors
      }
    }
    return null;
  };

  const handleChange = (val: string) => {
    onValueChange(val);
    if (touched) {
      setError(validate(val));
    }
  };

  const handleBlur = () => {
    setTouched(true);
    setError(validate(value));
  };

  const isDirty = value !== (attr.attributeValue || '');
  const hasError = !!validate(value);

  // Check if regex represents a dropdown list (e.g. ^(Yes|No)$ or ^(High|Medium|Low)$)
  let dropdownOptions: string[] | null = null;
  if (attr.regexPattern) {
    const enumMatch = attr.regexPattern.match(/^\^\(([^)]+)\)\$$/);
    if (enumMatch && enumMatch[1]) {
      dropdownOptions = enumMatch[1].split('|').map(opt => opt.trim());
    }
  }

  const renderInput = () => {
    // ── BOOLEAN → Toggle ──
    if (attr.dataType === 'BOOLEAN') {
      const isOn = value === 'true';
      return (
        <div className="flex items-center gap-3 py-1 mt-1">
          <button
            type="button"
            onClick={() => handleChange(isOn ? 'false' : 'true')}
            className={`relative w-11 h-6 rounded-full transition-colors duration-200 ${
              isOn ? 'bg-gradient-to-r from-[#000051] to-[#00008f]' : 'bg-[#c6c5d5]'
            } cursor-pointer`}
          >
            <span
              className={`absolute top-0.5 left-0.5 w-5 h-5 rounded-full bg-white shadow-dropdown
                transition-transform duration-200 ${isOn ? 'translate-x-5' : 'translate-x-0'}`}
            />
          </button>
          <span className="text-xs text-outline font-medium font-sans">
            {isOn ? 'True' : 'False'}
          </span>
        </div>
      );
    }

    // ── DROPDOWN SELECT ──
    if (dropdownOptions) {
      return (
        <select
          value={value}
          onChange={(e) => handleChange(e.target.value)}
          onBlur={handleBlur}
          className={`w-full px-4 py-2.5 rounded-md text-sm font-sans text-[#141c29] bg-white transition-all duration-200 cursor-pointer border border-[rgba(198,197,213,0.3)] outline-none focus:border-2 focus:border-tertiary-container focus:shadow-ambient`}
        >
          <option value="">Select option...</option>
          {dropdownOptions.map((opt) => (
            <option key={opt} value={opt}>
              {opt}
            </option>
          ))}
        </select>
      );
    }

    // ── DATE → DatePicker ──
    if (attr.dataType === 'DATE') {
      return (
        <input
          type="date"
          value={value}
          onChange={(e) => handleChange(e.target.value)}
          onBlur={handleBlur}
          className="w-full px-4 py-2.5 rounded-md text-sm font-sans text-[#141c29] bg-white transition-all duration-200 border border-[rgba(198,197,213,0.3)] outline-none focus:border-2 focus:border-tertiary-container focus:shadow-ambient"
        />
      );
    }

    // ── STRING / NUMBER → Text Input ──
    return (
      <input
        type="text"
        value={value}
        onChange={(e) => handleChange(e.target.value)}
        onBlur={handleBlur}
        placeholder="Enter value"
        className="w-full px-4 py-2.5 rounded-md text-sm font-sans text-[#141c29] bg-white transition-all duration-200 border border-[rgba(198,197,213,0.3)] outline-none focus:border-2 focus:border-tertiary-container focus:shadow-ambient"
      />
    );
  };

  return (
    <div className="bg-white p-6 rounded-xl border border-[rgba(198,197,213,0.3)] shadow-sm hover:shadow-md transition-all flex flex-col justify-between min-h-[170px]">
      <div>
        <div className="text-sm font-semibold font-mono text-on_surface lowercase">
          {attr.attributeCode}
        </div>
        <div className="text-xs text-outline mt-1 mb-4 leading-relaxed min-h-[32px]">
          {attr.displayName || attr.attributeCode}
        </div>
      </div>
      <div>
        <div className="relative">
          {renderInput()}
        </div>

        {error && touched && (
          <p className="mt-1 text-xs text-[#ba1a1a] font-medium">{error}</p>
        )}

        {isDirty && !hasError && (
          <div className="flex justify-end mt-4">
            <button
              onClick={onSaveField}
              disabled={saving}
              className="px-4 py-1.5 rounded-md bg-secondary text-white font-semibold text-xs shadow-md hover:bg-secondary-container transition-all"
            >
              {saving ? 'Saving…' : 'Save Changes'}
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

// ── Main Component ──────────────────────────────────────────────────────────────
export default function PolicyDetailPanel({
  policy,
  attributes,
  values,
  onValueChange,
  onSaveField,
  onAddAttributeClick,
  savingField,
  loading,
  onBack,
  groups,
}: Props) {
  const [activeTab, setActiveTab] = useState<string>('');

  // Find if there are any attributes not mapped to any of the active groups
  const hasUnmappedAttributes = useMemo(() => {
    return attributes.some(attr => {
      const gc = attr.groupCode;
      return !gc || !groups.some(g => g.code === gc);
    });
  }, [attributes, groups]);

  // Combine active groups and dynamic fallback tab if needed
  const allTabs = useMemo(() => {
    const tabList = [...groups];
    if (hasUnmappedAttributes) {
      tabList.push({
        code: 'GENERAL_FALLBACK',
        displayNameEn: 'General',
        displayNameTh: 'ทั่วไป (General)',
        displayOrder: 999,
        status: 'ACTIVE'
      });
    }
    return tabList;
  }, [groups, hasUnmappedAttributes]);

  const currentTabCode = activeTab || (allTabs.length > 0 ? allTabs[0].code : '');

  // Filter and group attributes based on the active tab
  const filteredAttributes = useMemo(() => {
    return attributes.filter(attr => {
      const gc = attr.groupCode;
      if (currentTabCode === 'GENERAL_FALLBACK') {
        return !gc || !groups.some(g => g.code === gc);
      }
      return gc === currentTabCode;
    });
  }, [attributes, currentTabCode, groups]);

  if (!policy) {
    return (
      <div className="w-full flex-1 flex flex-col items-center justify-center p-12 text-center bg-surface">
        <div className="text-5xl mb-4 opacity-30">📋</div>
        <h3 className="font-display text-lg font-semibold text-on_surface mb-2">Policy Master Detail</h3>
        <p className="text-sm text-outline max-w-sm">Use the search bar above to load a policy and manage its attributes.</p>
      </div>
    );
  }

  return (
    <div className="w-full max-w-5xl mx-auto flex flex-col bg-surface shadow-ambient rounded-xl border border-[rgba(198,197,213,0.3)] my-8 overflow-hidden">
      {/* ── Detail Header ── */}
      <div className="px-10 py-8 border-b border-[rgba(198,197,213,0.3)] bg-surface-container-lowest">
        <button
          onClick={onBack}
          className="flex items-center gap-2 text-xs font-semibold text-outline hover:text-primary mb-6 cursor-pointer transition-colors"
        >
          <span>←</span> ย้อนกลับ (Back to Search)
        </button>
        
        <div className="flex justify-between items-center mb-6">
          <div className="flex items-center gap-16">
            <div>
              <span className="text-[0.6875rem] font-bold text-outline uppercase tracking-wider block mb-1">
                เลขที่กรมธรรม์
              </span>
              <span className="font-display text-xl font-bold text-primary">
                {policy.policyNo}
              </span>
            </div>
            <div>
              <span className="text-[0.6875rem] font-bold text-outline uppercase tracking-wider block mb-1">
                ผู้เอาประกัน
              </span>
              <span className="font-display text-xl font-bold text-on_surface">
                {CUSTOMER_INFO[policy.policyNo]?.name || '—'}
              </span>
            </div>
          </div>
          <div className="flex items-center gap-3">
             <button
               onClick={onAddAttributeClick}
               className="px-4 py-2 rounded-md bg-gradient-to-br from-primary-custom to-primary-container text-white font-semibold text-sm hover:shadow-[0_4px_12px_rgba(20,28,41,0.12)] transition-all cursor-pointer"
             >
               + Add New Attribute
             </button>
          </div>
        </div>
      </div>

      {/* ── Attributes Scroll Area ── */}
      <div className="flex-1 overflow-y-auto px-10 py-8">
        {/* ── Horizontal Tab Bar ── */}
        <div className="flex border-b border-[rgba(198,197,213,0.3)] mb-8 overflow-x-auto">
          {allTabs.map((tab) => {
            const isActive = currentTabCode === tab.code;
            return (
              <button
                key={tab.code}
                onClick={() => setActiveTab(tab.code)}
                className={`px-6 py-3 font-sans text-sm font-semibold transition-all relative cursor-pointer border-b-2 -mb-[2px] whitespace-nowrap ${
                  isActive 
                    ? 'text-primary border-primary font-bold' 
                    : 'text-outline border-transparent hover:text-on_surface'
                }`}
              >
                {tab.displayNameTh || tab.displayNameEn}
              </button>
            );
          })}
        </div>

        {loading ? (
          <div className="text-sm text-outline italic py-8">Loading attributes...</div>
        ) : filteredAttributes.length === 0 ? (
          <div className="text-sm text-outline py-8">No attributes configured in this category.</div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 max-w-4xl">
            {filteredAttributes.map((attr) => (
              <AttributeCard
                key={attr.attributeCode}
                attr={attr}
                value={values[attr.attributeCode] || ''}
                onValueChange={(val) => onValueChange(attr.attributeCode, val)}
                onSaveField={() => onSaveField(attr.attributeCode)}
                saving={savingField === attr.attributeCode}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
