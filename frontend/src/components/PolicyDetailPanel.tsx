import { useState, useMemo } from 'react';
import type { PolicyAttributeValue, PolicyMaster } from '../types';
import DynamicInput from './DynamicInput';

interface Props {
  policy: PolicyMaster | null;
  attributes: PolicyAttributeValue[];
  values: Record<string, string>;
  onValueChange: (code: string, value: string) => void;
  onSaveField: (code: string) => void;
  onAddAttributeClick: () => void;
  savingField: string | null;
  loading: boolean;
}

export default function PolicyDetailPanel({
  policy,
  attributes,
  values,
  onValueChange,
  onSaveField,
  onAddAttributeClick,
  savingField,
  loading,
}: Props) {
  const [filterStr, setFilterStr] = useState('');

  // Grouping logic based on Institutional Architect setup
  const groups = useMemo(() => {
    const categories: Record<string, PolicyAttributeValue[]> = {
      'Timeline & Status': [],
      'Financial Metadata': [],
      'General Info': [],
    };

    attributes.forEach(attr => {
      // Filter logic
      if (filterStr && !attr.attributeCode.toLowerCase().includes(filterStr.toLowerCase()) && 
          !(attr.displayName || '').toLowerCase().includes(filterStr.toLowerCase())) {
        return;
      }

      if (['POLICY_START_DATE', 'POLICY_END_DATE', 'IS_RENEWABLE'].includes(attr.attributeCode)) {
        categories['Timeline & Status'].push(attr);
      } else if (['MAX_COVERAGE', 'DEDUCTIBLE_AMOUNT', 'PREMIUM_FREQUENCY', 'CLAIM_LIMIT', 'HAS_COPAY'].includes(attr.attributeCode)) {
        categories['Financial Metadata'].push(attr);
      } else {
        categories['General Info'].push(attr);
      }
    });

    return categories;
  }, [attributes, filterStr]);

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
        <div className="flex justify-between items-start mb-6">
          <div>
            <h1 className="font-display text-3xl font-semibold text-on_surface tracking-tight mb-2">
              {policy.policyNo}
            </h1>
            <p className="text-sm text-outline font-sans">
              System ID: <span className="font-mono text-xs">{policy.policyNo}</span> • Created: {new Date(policy.createdAt).toLocaleDateString()}
            </p>
          </div>
          <div className="flex flex-col items-end gap-3">
             <span className={`text-xs px-3 py-1 rounded-full font-bold
                ${policy.status === 'ACTIVE' ? 'bg-primary-fixed text-primary-fixed_variant' : 'bg-surface-dim text-on_surface-variant'}`}>
                STATUS: {policy.status}
              </span>
             <button
               onClick={onAddAttributeClick}
               className="px-4 py-2 rounded-md bg-gradient-to-br from-primary-custom to-primary-container text-white font-semibold text-sm hover:shadow-[0_4px_12px_rgba(20,28,41,0.12)] transition-all"
             >
               + Add New Attribute
             </button>
          </div>
        </div>

        {/* Inline Attribute Search */}
        <div className="max-w-md">
          <input
            type="text"
            placeholder="Filter Attributes..."
            value={filterStr}
            onChange={(e) => setFilterStr(e.target.value)}
            className="w-full px-4 py-2 rounded-md bg-surface-container-low text-sm font-sans text-on_surface
              border border-transparent outline-none transition-all duration-200
              focus:bg-surface-container-lowest focus:border-outline-variant focus:shadow-ambient"
          />
        </div>
      </div>

      {/* ── Attributes Scroll Area ── */}
      <div className="flex-1 overflow-y-auto px-10 py-8">
        {loading ? (
          <div className="text-sm text-outline italic">Loading attributes...</div>
        ) : attributes.length === 0 ? (
          <div className="text-sm text-outline">No attributes configured.</div>
        ) : (
          <div className="space-y-10 max-w-4xl">
            {Object.entries(groups).map(([category, attrs]) => {
              if (attrs.length === 0) return null;
              
              return (
                <div key={category} className="bg-surface-container-lowest rounded-lg border border-[rgba(198,197,213,0.3)] shadow-ambient overflow-hidden">
                  <div className="bg-surface-container-low px-6 py-4 border-b border-[rgba(198,197,213,0.3)]">
                    <h3 className="font-display font-semibold text-on_surface">{category}</h3>
                  </div>
                  <div className="p-6 space-y-6">
                    {attrs.map((attr) => {
                      const currentValue = values[attr.attributeCode] || '';
                      const isDirty = currentValue !== (attr.attributeValue || '');
                      
                      return (
                        <div key={attr.attributeCode} className="group pb-4 border-b border-[rgba(198,197,213,0.15)] last:border-0 last:pb-0">
                          <DynamicInput
                            label={attr.displayName || attr.attributeCode}
                            subLabel={attr.attributeCode !== attr.displayName ? attr.attributeCode : undefined}
                            value={currentValue}
                            onChange={(val) => onValueChange(attr.attributeCode, val)}
                            dataType={attr.dataType || 'STRING'}
                            required={attr.isRequired}
                            regex={attr.regexPattern}
                            regexErrorMsg={attr.regexErrorMsg}
                          />
                          {/* Save trigger */}
                          <div className="grid grid-cols-[33%_67%] gap-x-4 mt-2">
                            <div />
                            <div className="flex items-center min-h-[32px]">
                              {isDirty && (
                                <button
                                  onClick={() => onSaveField(attr.attributeCode)}
                                  disabled={savingField === attr.attributeCode}
                                  className="px-4 py-1.5 rounded-md bg-secondary text-white font-semibold text-xs shadow-md hover:bg-secondary-container disabled:opacity-50 transition-all font-sans"
                                >
                                  {savingField === attr.attributeCode ? 'Saving…' : 'Save Changes'}
                                </button>
                              )}
                            </div>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
