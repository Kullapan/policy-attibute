import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import TopNav from '../components/TopNav';
import Toast from '../components/Toast';
import { fetchAllPolicies, fetchPolicyAttributes } from '../api/client';
import type { PolicyMaster, PolicyAttributeValue } from '../types';

// ── Static customer info (not an attribute, displayed from UI layer only) ────
const CUSTOMER_INFO: Record<string, { name: string; email: string }> = {
  'POL-2026-001': { name: 'John A. Smith',    email: 'john.s@example.com'   },
  'POL-2026-002': { name: 'Jane Doe',         email: 'jane.d@example.com'   },
  'POL-2026-003': { name: 'Robert K. Lee',    email: 'robert.l@example.com' },
  '501-545623':   { name: 'Somchai Dee-ing',  email: 'somchai.d@example.com'},
  '502-123456':   { name: 'Mana Permpoon',    email: 'mana.p@example.com'   },
  '503-987654':   { name: 'Wassana Siri',     email: 'wassana.s@example.com'},
};

// ── Consent status colour config ──────────────────────────────────────────────
type ConsentChipType = 'yes' | 'no' | 'pending' | 'valid' | 'unknown';

function getChipType(value: string | undefined): ConsentChipType {
  if (!value) return 'unknown';
  const v = value.toLowerCase();
  if (v === 'yes')     return 'yes';
  if (v === 'no')      return 'no';
  if (v === 'pending') return 'pending';
  if (v === 'valid')   return 'valid';
  return 'unknown';
}

const CHIP_STYLES: Record<ConsentChipType, { bg: string; text: string; dot: string }> = {
  yes:     { bg: '#e8f5e9', text: '#1b5e20', dot: '#43a047' },  // soft green
  no:      { bg: '#fce4e4', text: '#7f1d1d', dot: '#e53935' },  // soft red
  pending: { bg: '#fff8e1', text: '#4d3600', dot: '#f59e0b' },  // soft amber
  valid:   { bg: '#ede8ff', text: '#1e0a55', dot: '#7c3aed' },  // soft purple
  unknown: { bg: '#f0f3ff', text: '#454653', dot: '#767685' },  // neutral
};

// ── Policy row data shape ─────────────────────────────────────────────────────
interface PolicyRow {
  policy:          PolicyMaster;
  pdpaConsent:     string;
  rpqCompleted:    string;
  marketingConsent:string;
}

// ── Consent Chip component ────────────────────────────────────────────────────
function ConsentChip({ value }: { value: string }) {
  const type = getChipType(value);
  const { bg, text, dot } = CHIP_STYLES[type];
  return (
    <span
      style={{ backgroundColor: bg, color: text }}
      className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-[0.6875rem] font-semibold tracking-wide whitespace-nowrap"
    >
      <span
        style={{ backgroundColor: dot }}
        className="w-1.5 h-1.5 rounded-full shrink-0"
      />
      {value || '—'}
    </span>
  );
}

// ── Action Menu component ─────────────────────────────────────────────────────
function ActionMenu({ policyNo, onNavigate }: { policyNo: string; onNavigate: (p: string) => void }) {
  const [open, setOpen] = useState(false);

  return (
    <div className="relative" onBlur={(e) => { if (!e.currentTarget.contains(e.relatedTarget)) setOpen(false); }}>
      <button
        onClick={() => setOpen(o => !o)}
        tabIndex={0}
        className="w-8 h-8 flex items-center justify-center rounded-md text-[#767685] hover:bg-[#e0e8fb] hover:text-[#000051] transition-colors duration-150 text-lg"
      >
        ⋮
      </button>

      {open && (
        <div
          className="absolute right-0 top-9 z-50 w-52 rounded-lg py-1 text-sm"
          style={{
            background: 'rgba(249, 249, 255, 0.95)',
            backdropFilter: 'blur(20px)',
            WebkitBackdropFilter: 'blur(20px)',
            boxShadow: '0 4px 16px rgba(20, 28, 41, 0.10)',
            border: '1px solid rgba(198,197,213,0.20)',
          }}
        >
          <button
            onClick={() => { setOpen(false); onNavigate(policyNo); }}
            className="w-full text-left px-4 py-2.5 text-[#141c29] hover:bg-[#e7eeff] transition-colors"
          >
            🔗 View Attribute Mapping
          </button>
          <button
            onClick={() => { navigator.clipboard.writeText(policyNo); setOpen(false); }}
            className="w-full text-left px-4 py-2.5 text-[#141c29] hover:bg-[#e7eeff] transition-colors"
          >
            📋 Copy Policy No
          </button>
        </div>
      )}
    </div>
  );
}

// ── Main Page ─────────────────────────────────────────────────────────────────
export default function PolicyListPage() {
  const navigate = useNavigate();

  const [rows,    setRows]    = useState<PolicyRow[]>([]);
  const [loading, setLoading] = useState(true);
  const [search,  setSearch]  = useState('');
  const [toast,   setToast]   = useState<{ message: string; type: 'success' | 'error' } | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const policies = await fetchAllPolicies();

      const settled = await Promise.allSettled(
        policies.map(async (p) => {
          let attrs: PolicyAttributeValue[] = [];
          try { attrs = await fetchPolicyAttributes(p.policyNo); } catch { /* not all policies have consent data */ }

          const find = (code: string) => attrs.find(a => a.attributeCode === code)?.attributeValue ?? '—';
          return {
            policy:          p,
            pdpaConsent:     find('PDPA_CONSENT'),
            rpqCompleted:    find('RPQ_COMPLETED'),
            marketingConsent:find('MARKETING_CONSENT'),
          } satisfies PolicyRow;
        })
      );

      setRows(
        settled
          .filter((r): r is PromiseFulfilledResult<PolicyRow> => r.status === 'fulfilled')
          .map(r => r.value)
      );
    } catch {
      setToast({ message: 'Failed to load policies', type: 'error' });
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  const handleNavigateMapping = (policyNo: string) => {
    navigate(`/policy-mapping?search=${encodeURIComponent(policyNo)}`);
  };

  const filtered = rows.filter(r =>
    r.policy.policyNo.toLowerCase().includes(search.toLowerCase()) ||
    (CUSTOMER_INFO[r.policy.policyNo]?.name ?? '').toLowerCase().includes(search.toLowerCase())
  );

  return (
    <>
      <TopNav title="Policy Consent Dashboard" />

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
          {loading ? (
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
                    { label: 'Policy No',          width: 'w-32'  },
                    { label: 'Customer Name',       width: 'w-56'  },
                    { label: 'PDPA Consent',        width: 'w-36'  },
                    { label: 'RPQ Completed',       width: 'w-36'  },
                    { label: 'Marketing Consent',   width: 'w-36'  },
                    { label: 'Actions',             width: 'w-20'  },
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
                    <td colSpan={6} className="px-6 py-16 text-center text-sm text-[#767685]" style={{ backgroundColor: '#f9f9ff' }}>
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
                            onClick={() => handleNavigateMapping(row.policy.policyNo)}
                            className="text-[#00008f] font-semibold text-sm hover:underline underline-offset-2 leading-snug text-left"
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

                        {/* PDPA Consent */}
                        <td className="px-6 py-4 align-middle">
                          <ConsentChip value={row.pdpaConsent} />
                        </td>

                        {/* RPQ Completed */}
                        <td className="px-6 py-4 align-middle">
                          <ConsentChip value={row.rpqCompleted} />
                        </td>

                        {/* Marketing Consent */}
                        <td className="px-6 py-4 align-middle">
                          <ConsentChip value={row.marketingConsent} />
                        </td>

                        {/* Actions */}
                        <td className="px-4 py-4 align-middle">
                          <ActionMenu policyNo={row.policy.policyNo} onNavigate={handleNavigateMapping} />
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
        {!loading && filtered.length > 0 && (
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
