import { useState, useEffect, useCallback } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import TopNav from '../components/TopNav';
import Toast from '../components/Toast';
import PolicyDetailPanel from '../components/PolicyDetailPanel';
import DynamicInput from '../components/DynamicInput';
import { fetchAllPolicies, fetchPolicyAttributes, updatePolicyAttribute, fetchAttributes, fetchAttributeGroups } from '../api/client';
import type { PolicyMaster, PolicyAttributeValue, AttributeMaster, AttributeGroup } from '../types';

// ── Static customer info (not an attribute, displayed from UI layer only) ────
export const CUSTOMER_INFO: Record<string, { name: string; email: string }> = {
  'POL-2026-001': { name: 'คุณธนพงษ์ มั่งมี',    email: 'john.s@example.com'   },
  'POL-2026-002': { name: 'Jane Doe',         email: 'jane.d@example.com'   },
  'POL-2026-003': { name: 'Robert K. Lee',    email: 'robert.l@example.com' },
  '501-545623':   { name: 'Somchai Dee-ing',  email: 'somchai.d@example.com'},
  '502-123456':   { name: 'Mana Permpoon',    email: 'mana.p@example.com'   },
  '503-987654':   { name: 'Wassana Siri',     email: 'wassana.s@example.com'},
};

// ── Policy row data shape ─────────────────────────────────────────────────────
interface PolicyRow {
  policy: PolicyMaster;
}

// ── Main Page ─────────────────────────────────────────────────────────────────
export default function PolicyListPage() {
  const navigate = useNavigate();
  const location = useLocation();

  // Policy List states
  const [rows, setRows] = useState<PolicyRow[]>([]);
  const [loadingList, setLoadingList] = useState(true);
  const [search, setSearch] = useState('');
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null);

  // Policy Details states
  const [selectedPolicyNo, setSelectedPolicyNo] = useState<string | null>(null);
  const [attributes, setAttributes] = useState<PolicyAttributeValue[]>([]);
  const [values, setValues] = useState<Record<string, string>>({});
  const [loadingDetails, setLoadingDetails] = useState(false);
  const [savingField, setSavingField] = useState<string | null>(null);

  // Add Attribute Modal states
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [masterDictionary, setMasterDictionary] = useState<AttributeMaster[]>([]);
  const [newSelectedCode, setNewSelectedCode] = useState('');
  const [newInitialValue, setNewInitialValue] = useState('');
  const [groups, setGroups] = useState<AttributeGroup[]>([]);
  const [selectedGroupFilter, setSelectedGroupFilter] = useState<string>('');

  // Fetch active groups on mount
  useEffect(() => {
    async function loadGroups() {
      try {
        const data = await fetchAttributeGroups();
        setGroups(data);
      } catch {
        setToast({ message: 'Failed to load attribute groups', type: 'error' });
      }
    }
    loadGroups();
  }, []);

  // 1. Fetch Policy List
  const loadList = useCallback(async () => {
    setLoadingList(true);
    try {
      const policies = await fetchAllPolicies();
      setRows(policies.map(p => ({ policy: p })));
    } catch {
      setToast({ message: 'Failed to load policies', type: 'error' });
    } finally {
      setLoadingList(false);
    }
  }, []);

  useEffect(() => {
    loadList();
  }, [loadList]);

  // 2. Fetch Policy Details (Attributes)
  const selectPolicy = async (policyNo: string) => {
    setSelectedPolicyNo(policyNo);
    setLoadingDetails(true);
    try {
      const data = await fetchPolicyAttributes(policyNo);
      setAttributes(data);

      const vals: Record<string, string> = {};
      data.forEach((attr) => {
        vals[attr.attributeCode] = attr.attributeValue || '';
      });
      setValues(vals);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to load policy attributes';
      setToast({ message: msg, type: 'error' });
      setSelectedPolicyNo(null);
    } finally {
      setLoadingDetails(false);
    }
  };

  // 3. Auto-load when navigation contains ?search= query parameter
  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const searchParam = params.get('search');
    if (searchParam) {
      selectPolicy(searchParam);
    } else {
      setSelectedPolicyNo(null);
    }
  }, [location.search]);

  // 4. Navigation interactions
  const handleSelectPolicy = (policyNo: string) => {
    navigate(`?search=${encodeURIComponent(policyNo)}`);
  };

  const handleBackToSearch = () => {
    navigate('/policies');
  };

  // 5. Update/Save individual attribute field
  const handleSaveField = async (attributeCode: string) => {
    if (!selectedPolicyNo) return;
    setSavingField(attributeCode);
    try {
      await updatePolicyAttribute(selectedPolicyNo, attributeCode, values[attributeCode]);
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

  // 6. Open Modal to Add New Attribute definition to policy
  const handleOpenModal = async () => {
    try {
      const allMaster = await fetchAttributes();
      setMasterDictionary(allMaster);
      setIsModalOpen(true);
      setNewSelectedCode('');
      setNewInitialValue('');
      setSelectedGroupFilter('');
    } catch(err) {
      setToast({ message: 'Failed to load dictionary', type: 'error' });
    }
  };

  const handleAddNewAttribute = async () => {
    if (!selectedPolicyNo || !newSelectedCode) return;
    
    try {
      await updatePolicyAttribute(selectedPolicyNo, newSelectedCode, newInitialValue);
      setToast({ message: `Attribute ${newSelectedCode} added`, type: 'success' });
      setIsModalOpen(false);
      
      // Refresh detailed attributes list
      const data = await fetchPolicyAttributes(selectedPolicyNo);
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

  const filtered = rows.filter(r =>
    r.policy.policyNo.toLowerCase().includes(search.toLowerCase()) ||
    (CUSTOMER_INFO[r.policy.policyNo]?.name ?? '').toLowerCase().includes(search.toLowerCase())
  );

  // Modal Dropdown list attributes filter
  const unmappedAttributes = masterDictionary.filter(
    md => md.status !== 'ARCHIVED' && !attributes.some(a => a.attributeCode === md.code)
  );

  const filteredUnmappedAttributes = unmappedAttributes.filter(attr => {
    if (selectedGroupFilter === '') return true;
    if (selectedGroupFilter === '_UNASSIGNED_') return !attr.groupCode;
    return attr.groupCode === selectedGroupFilter;
  });

  const selectedAttrDef = unmappedAttributes.find(a => a.code === newSelectedCode);

  // If a policy is currently selected, display the PolicyDetailPanel mapping view
  if (selectedPolicyNo) {
    const selectedPolicy = rows.find(r => r.policy.policyNo === selectedPolicyNo)?.policy || {
      policyNo: selectedPolicyNo,
      status: 'ACTIVE',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      createdBy: 'SYSTEM'
    };

    return (
      <>
        <TopNav title="Policy Management Dashboard" />
        <div className="flex-1 overflow-y-auto px-16 py-8">
          <PolicyDetailPanel
            policy={selectedPolicy}
            attributes={attributes}
            values={values}
            onValueChange={(code, val) => setValues(prev => ({ ...prev, [code]: val }))}
            onSaveField={handleSaveField}
            onAddAttributeClick={handleOpenModal}
            savingField={savingField}
            loading={loadingDetails}
            onBack={handleBackToSearch}
            groups={groups}
          />
        </div>

        {/* Add Attribute Modal */}
        {isModalOpen && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-[#141c29]/40 backdrop-blur-sm">
            <div className="bg-surface-container-lowest rounded-xl shadow-2xl p-8 max-w-lg w-full border border-[rgba(198,197,213,0.2)]">
              <h2 className="font-display text-xl font-semibold text-on_surface mb-6">Add New Attribute</h2>
              
              <div className="space-y-6">
                {/* Filter by Group */}
                <div className="grid grid-cols-[33%_67%] items-start gap-x-4 gap-y-2">
                  <div className="pt-2.5">
                    <label className="text-xs font-bold text-[#767685] uppercase tracking-wider block">Group</label>
                  </div>
                  <div>
                    <select 
                      value={selectedGroupFilter}
                      onChange={e => {
                        setSelectedGroupFilter(e.target.value);
                        setNewSelectedCode('');
                        setNewInitialValue('');
                      }}
                      className="w-full px-4 py-2.5 rounded-md border border-[rgba(198,197,213,0.3)] text-sm bg-white outline-none focus:border-tertiary-container focus:shadow-ambient cursor-pointer"
                    >
                      <option value="">All Groups</option>
                      {groups.map(g => (
                        <option key={g.code} value={g.code}>
                          {g.displayNameTh || g.displayNameEn} ({g.code})
                        </option>
                      ))}
                      <option value="_UNASSIGNED_">-- Unassigned --</option>
                    </select>
                  </div>
                </div>

                <div className="grid grid-cols-[33%_67%] items-start gap-x-4 gap-y-2">
                  <div className="pt-2.5">
                    <label className="text-xs font-bold text-[#767685] uppercase tracking-wider block">Attribute</label>
                  </div>
                  <div>
                    <select 
                      value={newSelectedCode}
                      onChange={e => setNewSelectedCode(e.target.value)}
                      className="w-full px-4 py-2.5 rounded-md border border-[rgba(198,197,213,0.3)] text-sm bg-white outline-none focus:border-tertiary-container focus:shadow-ambient cursor-pointer"
                      disabled={filteredUnmappedAttributes.length === 0}
                    >
                      {filteredUnmappedAttributes.length === 0 ? (
                        <option value="" disabled>-- No unmapped attributes --</option>
                      ) : (
                        <>
                          <option value="" disabled>-- Select an unmapped attribute --</option>
                          {filteredUnmappedAttributes.map(attr => (
                            <option key={attr.code} value={attr.code}>
                              {attr.displayName || attr.code}
                            </option>
                          ))}
                        </>
                      )}
                    </select>
                  </div>
                </div>

                {filteredUnmappedAttributes.length === 0 && (
                  <div className="px-4 py-3 rounded-md bg-[#e7eeff] text-xs font-semibold text-[#000051] flex items-center gap-2.5 transition-all duration-200">
                    <span className="text-sm">✓</span>
                    <span>
                      {selectedGroupFilter === '' 
                        ? 'All available dictionary attributes are already mapped to this policy.' 
                        : 'All attributes in this group are already mapped to this policy.'}
                    </span>
                  </div>
                )}

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
                <button onClick={() => setIsModalOpen(false)} className="px-4 py-2 text-sm font-semibold text-on_surface-variant hover:text-on_surface cursor-pointer">Cancel</button>
                <button 
                  onClick={handleAddNewAttribute} 
                  className="px-4 py-2 rounded-md bg-gradient-to-br from-primary-custom to-primary-container text-white text-sm font-semibold hover:shadow-md disabled:opacity-50 cursor-pointer"
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

  // Otherwise, display the searchable Policy List view
  return (
    <>
      <TopNav title="Policy Management Dashboard" />

      <div className="flex-1 overflow-y-auto px-16 py-8">
        {/* ── Header ── */}
        <div className="flex items-center justify-between mb-6">
          <div>
            <h2 className="font-display text-2xl font-semibold text-[#000051]">Policy Overview</h2>
            <p className="text-[0.8125rem] text-[#767685] mt-0.5">
              {filtered.length} {filtered.length === 1 ? 'policy' : 'policies'} found
            </p>
          </div>

          {/* Search */}
          <div className="relative w-72">
            <span className="absolute left-3 top-1/2 -translate-y-1/2 text-[#767685] text-sm">🔍</span>
            <input
              type="text"
              placeholder="Search policy no. or customer…"
              value={search}
              onChange={e => setSearch(e.target.value)}
              className="w-full pl-9 pr-4 py-2 rounded-md bg-white text-sm text-[#141c29] outline-none transition-all duration-200
                border border-[rgba(198,197,213,0.30)] focus:border-2 focus:border-[#002a40] focus:shadow-[0_0_0_3px_rgba(0,42,64,0.08)]"
            />
          </div>
        </div>

        {/* ── Table ── */}
        <div className="rounded-lg overflow-hidden" style={{ boxShadow: '0 4px 20px rgba(20, 28, 41, 0.05)' }}>
          {loadingList ? (
            <div className="bg-white flex items-center justify-center py-32">
              <div className="flex flex-col items-center gap-3">
                <div className="w-8 h-8 border-3 border-[#e0e0ff] border-t-[#00008f] rounded-full animate-spin" />
                <p className="text-sm text-[#767685]">Loading policies…</p>
              </div>
            </div>
          ) : (
            <table className="w-full border-collapse">
              {/* Header */}
              <thead>
                <tr style={{ backgroundColor: '#dbe3f5' }}>
                  {[
                    { label: 'Policy No',          width: 'w-1/2'  },
                    { label: 'Customer Name',       width: 'w-1/2'  },
                  ].map(col => (
                    <th
                      key={col.label}
                      className={`${col.width} px-6 py-3 text-left text-[0.6875rem] font-bold text-[#000051] uppercase tracking-[0.05em]`}
                    >
                      {col.label}
                    </th>
                  ))}
                </tr>
              </thead>

              {/* Body */}
              <tbody>
                {filtered.length === 0 ? (
                  <tr>
                    <td colSpan={2} className="px-6 py-16 text-center text-sm text-[#767685]" style={{ backgroundColor: '#f9f9ff' }}>
                      No policies match your search.
                    </td>
                  </tr>
                ) : (
                  filtered.map((row, idx) => {
                    const customer = CUSTOMER_INFO[row.policy.policyNo];
                    const isEven   = idx % 2 === 1;
                    const rowBg    = isEven ? '#f0f3ff' : '#f9f9ff';

                    return (
                      <tr
                        key={row.policy.policyNo}
                        style={{ backgroundColor: rowBg }}
                        className="transition-colors duration-100 hover:!bg-[#e0e8fb] group"
                      >
                        {/* Policy No */}
                        <td className="px-6 py-4 align-middle">
                          <button
                            onClick={() => handleSelectPolicy(row.policy.policyNo)}
                            className="text-[#00008f] font-semibold text-sm hover:underline underline-offset-2 leading-snug text-left cursor-pointer"
                          >
                            {row.policy.policyNo}
                          </button>
                        </td>

                        {/* Customer Name */}
                        <td className="px-6 py-4 align-middle">
                          {customer ? (
                            <div>
                              <div className="text-sm font-medium text-[#141c29]">{customer.name}</div>
                              <div className="text-[0.6875rem] text-[#767685] mt-0.5">{customer.email}</div>
                            </div>
                          ) : (
                            <span className="text-sm text-[#767685]">—</span>
                          )}
                        </td>
                      </tr>
                    );
                  })
                )}
              </tbody>
            </table>
          )}
        </div>

        {/* ── Footer summary ── */}
        {!loadingList && filtered.length > 0 && (
          <div
            className="mt-4 px-6 py-3 rounded-md text-[0.6875rem] text-[#767685] flex items-center gap-4"
            style={{ backgroundColor: '#d2daec' }}
          >
            <span>Total: <strong className="text-[#141c29]">{filtered.length}</strong> policies</span>
            <span className="text-[#c6c5d5]">|</span>
            <span>
              Active:{' '}
              <strong className="text-[#141c29]">
                {filtered.filter(r => r.policy.status === 'ACTIVE').length}
              </strong>
            </span>
          </div>
        )}
      </div>

      {toast && (
        <Toast message={toast.message} type={toast.type} onClose={() => setToast(null)} />
      )}
    </>
  );
}
