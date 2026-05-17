import { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import TopNav from '../components/TopNav';
import Toast from '../components/Toast';
import PolicyDetailPanel from '../components/PolicyDetailPanel';
import DynamicInput from '../components/DynamicInput';
import type { PolicyMaster, PolicyAttributeValue, AttributeMaster } from '../types';
import { fetchAllPolicies, fetchPolicyAttributes, updatePolicyAttribute, fetchAttributes } from '../api/client';

export default function PolicyMappingPage() {
  const location = useLocation();
  const [searchInput, setSearchInput] = useState(() => {
    const params = new URLSearchParams(location.search);
    return params.get('search') ?? '';
  });
  const [selectedPolicy, setSelectedPolicy] = useState<PolicyMaster | null>(null);
  
  const [attributes, setAttributes] = useState<PolicyAttributeValue[]>([]);
  const [values, setValues] = useState<Record<string, string>>({});
  
  const [loading, setLoading] = useState(false);
  const [savingField, setSavingField] = useState<string | null>(null);
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null);

  // Modal State
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [masterDictionary, setMasterDictionary] = useState<AttributeMaster[]>([]);
  const [newSelectedCode, setNewSelectedCode] = useState('');
  const [newInitialValue, setNewInitialValue] = useState('');
  
  const handleSearch = async () => {
    if (!searchInput.trim()) return;
    setLoading(true);
    try {
      // Temporarily fetching all policies to find the metadata. 
      // In a real system we would have a GET /policies/{policyNo} endpoint.
      const allPolicies = await fetchAllPolicies();
      const policy = allPolicies.find(p => p.policyNo === searchInput.trim());
      
      if (!policy) {
        throw new Error('Policy not found');
      }
      setSelectedPolicy(policy);

      const data = await fetchPolicyAttributes(policy.policyNo);
      setAttributes(data);

      const vals: Record<string, string> = {};
      data.forEach((attr) => {
        vals[attr.attributeCode] = attr.attributeValue || '';
      });
      setValues(vals);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to search policy';
      setToast({ message: msg, type: 'error' });
      setSelectedPolicy(null);
      setAttributes([]);
    } finally {
      setLoading(false);
    }
  };

  // Auto-search when navigated from Policy Dashboard with ?search= param
  useEffect(() => {
    const params = new URLSearchParams(location.search);
    if (params.get('search')) {
      handleSearch();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleSaveField = async (attributeCode: string) => {
    if (!selectedPolicy) return;
    setSavingField(attributeCode);
    try {
      await updatePolicyAttribute(selectedPolicy.policyNo, attributeCode, values[attributeCode]);
      setToast({ message: `${attributeCode} saved successfully`, type: 'success' });
      
      setAttributes(prev => prev.map(a => 
        a.attributeCode === attributeCode ? { ...a, attributeValue: values[attributeCode] } : a
      ));
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Save failed';
      setToast({ message: msg, type: 'error' });
    } finally {
      setSavingField(null);
    }
  };

  const handleOpenModal = async () => {
    try {
      const allMaster = await fetchAttributes();
      setMasterDictionary(allMaster);
      setIsModalOpen(true);
      setNewSelectedCode('');
      setNewInitialValue('');
    } catch(err) {
      setToast({ message: 'Failed to load dictionary', type: 'error' });
    }
  };

  const handleAddNewAttribute = async () => {
    if (!selectedPolicy || !newSelectedCode) return;
    
    try {
      // Call update which serves as an upsert/insert
      await updatePolicyAttribute(selectedPolicy.policyNo, newSelectedCode, newInitialValue);
      setToast({ message: `Attribute ${newSelectedCode} added`, type: 'success' });
      setIsModalOpen(false);
      
      // Refresh the list to reflect new data types and info
      const data = await fetchPolicyAttributes(selectedPolicy.policyNo);
      setAttributes(data);
      const vals: Record<string, string> = {};
      data.forEach((attr) => {
        vals[attr.attributeCode] = attr.attributeValue || '';
      });
      setValues(vals);
      
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to add attribute';
      setToast({ message: msg, type: 'error' });
    }
  };

  // Filter out already mapped attributes and ARCHIVED attributes for the modal dropdown
  const unmappedAttributes = masterDictionary.filter(
    md => md.status !== 'ARCHIVED' && !attributes.some(a => a.attributeCode === md.code)
  );

  const selectedAttrDef = unmappedAttributes.find(a => a.code === newSelectedCode);

  return (
    <>
      <TopNav title="Policy Management Dashboard" />

      <div className="flex-1 overflow-y-auto px-16 py-8">
        {/* ── Search Bar ── */}
        <div className="flex items-center gap-3 mb-8 max-w-lg mx-auto">
            <input
              type="text"
              placeholder="Enter Policy Number (e.g. POL-2026-001)"
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
              className="flex-1 px-4 py-2.5 rounded-md bg-white text-sm font-sans text-on_surface
                border border-[rgba(198,197,213,0.3)] outline-none transition-all duration-200
                focus:border-2 focus:border-tertiary-container focus:shadow-ambient shadow-sm"
            />
            <button
              onClick={handleSearch}
              disabled={loading}
              className="px-6 py-2.5 rounded-md text-sm font-semibold text-white
                bg-gradient-to-br from-primary-custom to-primary-container border-none
                hover:shadow-[0_4px_12px_rgba(20,28,41,0.12)] transition-all duration-200
                disabled:opacity-50"
            >
              {loading ? 'Searching…' : 'Search'}
            </button>
        </div>

        {/* ── Stacked View ── */}
        {selectedPolicy ? (
          <PolicyDetailPanel
            policy={selectedPolicy}
            attributes={attributes}
            values={values}
            onValueChange={(code, val) => setValues(prev => ({ ...prev, [code]: val }))}
            onSaveField={handleSaveField}
            onAddAttributeClick={handleOpenModal}
            savingField={savingField}
            loading={loading}
          />
        ) : (
          !loading && (
            <div className="flex flex-col items-center justify-center py-24 text-center mt-8">
              <div className="text-5xl mb-4 opacity-30">🔍</div>
              <h3 className="font-display text-lg font-semibold text-on_surface mb-2">
                Search for a Policy
              </h3>
              <p className="text-sm text-outline max-w-sm">
                Enter a policy number above to view and edit its metadata and detailed attributes.
                Try <button onClick={() => { setSearchInput('POL-2026-001'); }} className="text-primary-container font-semibold hover:underline">POL-2026-001</button>
              </p>
            </div>
          )
        )}
      </div>

      {/* Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-[#141c29]/40 backdrop-blur-sm">
          <div className="bg-surface-container-lowest rounded-xl shadow-2xl p-8 max-w-lg w-full border border-[rgba(198,197,213,0.2)]">
            <h2 className="font-display text-xl font-semibold text-on_surface mb-6">Add New Attribute</h2>
            
            <div className="space-y-6">
              <div className="grid grid-cols-[33%_67%] items-start gap-x-4 gap-y-2">
                <div className="pt-2.5">
                  <label className="text-xs font-bold text-[#767685] uppercase tracking-wider block">Attribute</label>
                </div>
                <div>
                  <select 
                    value={newSelectedCode}
                    onChange={e => setNewSelectedCode(e.target.value)}
                    className="w-full px-4 py-2.5 rounded-md border border-[rgba(198,197,213,0.3)] text-sm bg-white outline-none focus:border-tertiary-container focus:shadow-ambient"
                  >
                    <option value="" disabled>-- Select an unmapped attribute --</option>
                    {unmappedAttributes.map(attr => (
                      <option key={attr.code} value={attr.code}>
                        {attr.displayName || attr.code}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <div>
                {selectedAttrDef ? (
                  <DynamicInput
                    label="Initial Value"
                    value={newInitialValue}
                    onChange={setNewInitialValue}
                    dataType={selectedAttrDef.dataType || 'STRING'}
                    required={selectedAttrDef.isRequired}
                    regex={selectedAttrDef.regexPattern}
                    regexErrorMsg={selectedAttrDef.regexErrorMsg}
                  />
                ) : (
                  <div className="grid grid-cols-[33%_67%] items-start gap-x-4 gap-y-2">
                    <div className="pt-2.5">
                      <label className="text-xs font-bold text-[#767685] uppercase tracking-wider block">Initial Value</label>
                    </div>
                    <div>
                      <input 
                        type="text" 
                        value=""
                        disabled
                        placeholder="Select an attribute first"
                        className="w-full px-4 py-2.5 rounded-md border border-[rgba(198,197,213,0.3)] text-sm bg-surface-dim outline-none opacity-50 cursor-not-allowed"
                      />
                    </div>
                  </div>
                )}
              </div>
            </div>

            <div className="flex gap-3 justify-end mt-8">
              <button onClick={() => setIsModalOpen(false)} className="px-4 py-2 text-sm font-semibold text-on_surface-variant hover:text-on_surface">Cancel</button>
              <button 
                onClick={handleAddNewAttribute} 
                className="px-4 py-2 rounded-md bg-gradient-to-br from-primary-custom to-primary-container text-white text-sm font-semibold hover:shadow-md disabled:opacity-50"
                disabled={!newSelectedCode}
              >
                Add to Policy
              </button>
            </div>
          </div>
        </div>
      )}

      {toast && (
        <Toast message={toast.message} type={toast.type} onClose={() => setToast(null)} />
      )}
    </>
  );
}
