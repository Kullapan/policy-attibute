import { useCallback, useEffect, useState } from 'react';
import TopNav from '../components/TopNav';
import StatusChip from '../components/StatusChip';
import Modal from '../components/Modal';
import Toast from '../components/Toast';
import type { AttributeMaster, AttributeMasterForm, AttributeStatus, DataType } from '../types';
import {
  fetchAttributes,
  createAttribute,
  updateAttribute,
  deleteAttribute,
} from '../api/client';

const DATA_TYPES: DataType[] = ['STRING', 'NUMBER', 'DATE', 'BOOLEAN'];

const emptyForm: AttributeMasterForm = {
  code: '',
  displayName: '',
  dataType: 'STRING',
  isRequired: false,
  regexPattern: '',
  regexErrorMsg: '',
};

export default function DictionaryPage() {
  const [attributes, setAttributes] = useState<AttributeMaster[]>([]);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState<AttributeStatus | ''>('');
  const [loading, setLoading] = useState(false);

  // Modal state
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(false);
  const [form, setForm] = useState<AttributeMasterForm>(emptyForm);

  // Delete confirmation
  const [deleteCode, setDeleteCode] = useState<string | null>(null);

  // Toast
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null);

  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      const data = await fetchAttributes(
        search || undefined,
        (statusFilter as AttributeStatus) || undefined
      );
      setAttributes(data);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to load attributes';
      setToast({ message: msg, type: 'error' });
    } finally {
      setLoading(false);
    }
  }, [search, statusFilter]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  // ── Open modal for create ──
  const openCreate = () => {
    setForm(emptyForm);
    setEditing(false);
    setModalOpen(true);
  };

  // ── Open modal for edit ──
  const openEdit = (attr: AttributeMaster) => {
    setForm({
      code: attr.code,
      displayName: attr.displayName,
      dataType: attr.dataType,
      isRequired: attr.isRequired,
      regexPattern: attr.regexPattern || '',
      regexErrorMsg: attr.regexErrorMsg || '',
      version: attr.version,
    });
    setEditing(true);
    setModalOpen(true);
  };

  // ── Save (create or update) ──
  const handleSave = async () => {
    try {
      if (editing) {
        await updateAttribute(form.code, form);
        setToast({ message: `Attribute "${form.code}" updated successfully`, type: 'success' });
      } else {
        await createAttribute(form);
        setToast({ message: `Attribute "${form.code}" created successfully`, type: 'success' });
      }
      setModalOpen(false);
      loadData();
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Save failed';
      setToast({ message: msg, type: 'error' });
    }
  };

  // ── Soft delete ──
  const handleDelete = async () => {
    if (!deleteCode) return;
    try {
      await deleteAttribute(deleteCode);
      setToast({ message: `Attribute "${deleteCode}" archived`, type: 'success' });
      setDeleteCode(null);
      loadData();
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Delete failed';
      setToast({ message: msg, type: 'error' });
    }
  };

  return (
    <>
      <TopNav title="Attribute Dictionary" />

      <div className="flex-1 px-16 py-8">
        {/* ── Toolbar ── */}
        <div className="flex items-center gap-4 mb-6">
          <input
            type="text"
            placeholder="Search by name or code…"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="flex-1 max-w-md px-4 py-2.5 rounded-md bg-white text-sm font-sans text-[#141c29]
              border border-[rgba(198,197,213,0.2)] outline-none transition-all duration-200
              focus:border-2 focus:border-[#002a40] focus:shadow-[0_0_0_3px_rgba(0,42,64,0.08)]"
          />

          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value as AttributeStatus | '')}
            className="px-4 py-2.5 rounded-md bg-white text-sm font-sans text-[#141c29]
              border border-[rgba(198,197,213,0.2)] outline-none cursor-pointer"
          >
            <option value="">All Status</option>
            <option value="ACTIVE">Active</option>
            <option value="ARCHIVED">Archived</option>
          </select>

          <button
            onClick={openCreate}
            className="ml-auto px-6 py-2.5 rounded-md text-sm font-semibold text-white
              bg-gradient-to-br from-[#000051] to-[#00008f] border-none
              hover:shadow-[0_4px_12px_rgba(20,28,41,0.12)] transition-all duration-200"
          >
            + Add Attribute
          </button>
        </div>

        {/* ── Data Table ── */}
        <div className="bg-white rounded-lg shadow-ambient overflow-hidden">
          <table className="w-full border-collapse">
            <thead>
              <tr className="bg-[#dbe3f5]">
                {['Code', 'Display Name', 'Data Type', 'Required', 'Status', 'Regex', 'Actions'].map(
                  (col) => (
                    <th
                      key={col}
                      className="px-6 py-3 text-left text-xs font-bold text-[#000051]
                        uppercase tracking-wider font-sans sticky top-0 bg-[#dbe3f5]"
                    >
                      {col}
                    </th>
                  )
                )}
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={7} className="px-6 py-12 text-center text-sm text-[#767685]">
                    Loading…
                  </td>
                </tr>
              ) : attributes.length === 0 ? (
                <tr>
                  <td colSpan={7} className="px-6 py-12 text-center text-sm text-[#767685]">
                    No attributes found
                  </td>
                </tr>
              ) : (
                attributes.map((attr, i) => (
                  <tr
                    key={attr.code}
                    className={`transition-colors duration-150 hover:bg-[#e0e8fb]
                      ${i % 2 === 0 ? 'bg-[#f9f9ff]' : 'bg-[#f0f3ff]'}`}
                  >
                    <td className="px-6 py-3 text-sm font-medium text-[#141c29] font-mono">
                      {attr.code}
                    </td>
                    <td className="px-6 py-3 text-sm text-[#141c29]">{attr.displayName}</td>
                    <td className="px-6 py-3">
                      <span className="inline-flex px-2 py-0.5 rounded-sm bg-[#e7eeff] text-xs font-semibold text-[#000051]">
                        {attr.dataType}
                      </span>
                    </td>
                    <td className="px-6 py-3 text-sm text-[#454653]">
                      {attr.isRequired ? '✓ Yes' : '—'}
                    </td>
                    <td className="px-6 py-3">
                      <StatusChip status={attr.status} />
                    </td>
                    <td className="px-6 py-3 text-xs text-[#767685] font-mono max-w-[180px] truncate">
                      {attr.regexPattern || '—'}
                    </td>
                    <td className="px-6 py-3">
                      <div className="flex items-center gap-2">
                        <button
                          onClick={() => openEdit(attr)}
                          className="px-3 py-1.5 rounded-md text-xs font-medium text-[#000051]
                            hover:bg-[#e0e8fb] transition-colors"
                        >
                          Edit
                        </button>
                        {attr.status === 'ACTIVE' && (
                          <button
                            onClick={() => setDeleteCode(attr.code)}
                            className="px-3 py-1.5 rounded-md text-xs font-medium text-[#a63b00]
                              hover:bg-[#ffdbce] transition-colors"
                          >
                            Archive
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* ── Table footer ── */}
        <div className="mt-4 text-xs text-[#767685]">
          {attributes.length} attribute{attributes.length !== 1 ? 's' : ''} found
        </div>
      </div>

      {/* ── Create/Edit Modal ── */}
      <Modal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        title={editing ? `Edit Attribute: ${form.code}` : 'Add New Attribute'}
      >
        <div className="space-y-5">
          {/* Code */}
          <div className="grid grid-cols-[33%_67%] items-center gap-x-4">
            <label className="text-xs font-bold text-[#767685] uppercase tracking-wider">
              Attribute Code
            </label>
            <input
              type="text"
              value={form.code}
              onChange={(e) => setForm({ ...form, code: e.target.value.toUpperCase().replace(/[^A-Z0-9_]/g, '') })}
              disabled={editing}
              placeholder="e.g. MAX_COVERAGE"
              className="px-4 py-2.5 rounded-md bg-white text-sm font-sans text-[#141c29]
                border border-[rgba(198,197,213,0.2)] outline-none transition-all duration-200
                focus:border-2 focus:border-[#002a40] focus:shadow-[0_0_0_3px_rgba(0,42,64,0.08)]
                disabled:opacity-50 disabled:cursor-not-allowed font-mono"
            />
          </div>

          {/* Display Name */}
          <div className="grid grid-cols-[33%_67%] items-center gap-x-4">
            <label className="text-xs font-bold text-[#767685] uppercase tracking-wider">
              Display Name
            </label>
            <input
              type="text"
              value={form.displayName}
              onChange={(e) => setForm({ ...form, displayName: e.target.value })}
              placeholder="e.g. Maximum Coverage"
              className="px-4 py-2.5 rounded-md bg-white text-sm font-sans text-[#141c29]
                border border-[rgba(198,197,213,0.2)] outline-none transition-all duration-200
                focus:border-2 focus:border-[#002a40] focus:shadow-[0_0_0_3px_rgba(0,42,64,0.08)]"
            />
          </div>

          {/* Data Type */}
          <div className="grid grid-cols-[33%_67%] items-center gap-x-4">
            <label className="text-xs font-bold text-[#767685] uppercase tracking-wider">
              Data Type
            </label>
            <select
              value={form.dataType}
              onChange={(e) => setForm({ ...form, dataType: e.target.value as DataType })}
              className="px-4 py-2.5 rounded-md bg-white text-sm font-sans text-[#141c29]
                border border-[rgba(198,197,213,0.2)] outline-none cursor-pointer"
            >
              {DATA_TYPES.map((dt) => (
                <option key={dt} value={dt}>{dt}</option>
              ))}
            </select>
          </div>

          {/* Required toggle */}
          <div className="grid grid-cols-[33%_67%] items-center gap-x-4">
            <label className="text-xs font-bold text-[#767685] uppercase tracking-wider">
              Required
            </label>
            <button
              type="button"
              onClick={() => setForm({ ...form, isRequired: !form.isRequired })}
              className={`relative w-11 h-6 rounded-full transition-colors duration-200 ${
                form.isRequired ? 'bg-gradient-to-r from-[#000051] to-[#00008f]' : 'bg-[#c6c5d5]'
              }`}
            >
              <span
                className={`absolute top-0.5 left-0.5 w-5 h-5 rounded-full bg-white shadow-dropdown
                  transition-transform duration-200 ${form.isRequired ? 'translate-x-5' : 'translate-x-0'}`}
              />
            </button>
          </div>

          {/* Regex Pattern */}
          <div className="grid grid-cols-[33%_67%] items-center gap-x-4">
            <label className="text-xs font-bold text-[#767685] uppercase tracking-wider">
              Regex Pattern
            </label>
            <input
              type="text"
              value={form.regexPattern}
              onChange={(e) => setForm({ ...form, regexPattern: e.target.value })}
              placeholder="e.g. ^\d+(\.\d{1,2})?$"
              className="px-4 py-2.5 rounded-md bg-white text-sm font-mono text-[#141c29]
                border border-[rgba(198,197,213,0.2)] outline-none transition-all duration-200
                focus:border-2 focus:border-[#002a40] focus:shadow-[0_0_0_3px_rgba(0,42,64,0.08)]"
            />
          </div>

          {/* Regex Error Message */}
          <div className="grid grid-cols-[33%_67%] items-center gap-x-4">
            <label className="text-xs font-bold text-[#767685] uppercase tracking-wider">
              Regex Error Msg
            </label>
            <input
              type="text"
              value={form.regexErrorMsg}
              onChange={(e) => setForm({ ...form, regexErrorMsg: e.target.value })}
              placeholder="Custom error message for regex mismatch"
              className="px-4 py-2.5 rounded-md bg-white text-sm font-sans text-[#141c29]
                border border-[rgba(198,197,213,0.2)] outline-none transition-all duration-200
                focus:border-2 focus:border-[#002a40] focus:shadow-[0_0_0_3px_rgba(0,42,64,0.08)]"
            />
          </div>

          {/* Actions */}
          <div className="flex justify-end gap-3 pt-4">
            <button
              onClick={() => setModalOpen(false)}
              className="px-5 py-2.5 text-sm font-medium text-[#000051]
                hover:underline transition-all"
            >
              Cancel
            </button>
            <button
              onClick={handleSave}
              className="px-6 py-2.5 rounded-md text-sm font-semibold text-white
                bg-gradient-to-br from-[#000051] to-[#00008f] border-none
                hover:shadow-[0_4px_12px_rgba(20,28,41,0.12)] transition-all duration-200"
            >
              {editing ? 'Save Changes' : 'Create Attribute'}
            </button>
          </div>
        </div>
      </Modal>

      {/* ── Delete Confirmation Modal ── */}
      <Modal
        open={!!deleteCode}
        onClose={() => setDeleteCode(null)}
        title="Confirm Archive"
        width="max-w-sm"
      >
        <p className="text-sm text-[#454653] mb-6">
          Are you sure you want to archive <strong className="text-[#141c29]">{deleteCode}</strong>?
          This will set its status to ARCHIVED. This action can be reversed.
        </p>
        <div className="flex justify-end gap-3">
          <button
            onClick={() => setDeleteCode(null)}
            className="px-5 py-2.5 text-sm font-medium text-[#000051] hover:underline"
          >
            Cancel
          </button>
          <button
            onClick={handleDelete}
            className="px-6 py-2.5 rounded-md text-sm font-semibold text-white
              bg-[#a63b00] hover:bg-[#7f2b00] transition-colors"
          >
            Archive
          </button>
        </div>
      </Modal>

      {/* ── Toast ── */}
      {toast && (
        <Toast
          message={toast.message}
          type={toast.type}
          onClose={() => setToast(null)}
        />
      )}
    </>
  );
}
