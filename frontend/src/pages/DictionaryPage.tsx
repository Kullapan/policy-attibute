import { useCallback, useEffect, useState } from 'react';
import TopNav from '../components/TopNav';
import StatusChip from '../components/StatusChip';
import Modal from '../components/Modal';
import Toast from '../components/Toast';
import type { AttributeMaster, AttributeMasterForm, AttributeStatus, DataType, AttributeGroup, AttributeGroupForm } from '../types';
import {
  fetchAttributes,
  createAttribute,
  updateAttribute,
  deleteAttribute,
  fetchAttributeGroups,
  createAttributeGroup,
  updateAttributeGroup,
  deleteAttributeGroup,
} from '../api/client';

const DATA_TYPES: DataType[] = ['STRING', 'NUMBER', 'DATE', 'BOOLEAN'];

const emptyForm: AttributeMasterForm = {
  code: '',
  displayName: '',
  dataType: 'STRING',
  isRequired: false,
  regexPattern: '',
  regexErrorMsg: '',
  groupCode: '',
};

const emptyGroupForm: AttributeGroupForm = {
  code: '',
  displayNameEn: '',
  displayNameTh: '',
  displayOrder: 0,
};

export default function DictionaryPage() {
  const [attributes, setAttributes] = useState<AttributeMaster[]>([]);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState<AttributeStatus | ''>('');
  const [loading, setLoading] = useState(false);
  const [groups, setGroups] = useState<AttributeGroup[]>([]);

  // Modal state
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(false);
  const [form, setForm] = useState<AttributeMasterForm>(emptyForm);

  // Delete confirmation
  const [deleteCode, setDeleteCode] = useState<string | null>(null);

  // Toast
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null);

  // Group filter state for main table
  const [groupFilter, setGroupFilter] = useState<string>('');

  // Group Modal states
  const [groupModalOpen, setGroupModalOpen] = useState(false);
  const [groupFormMode, setGroupFormMode] = useState<'list' | 'add' | 'edit'>('list');
  const [groupForm, setGroupForm] = useState<AttributeGroupForm>(emptyGroupForm);
  const [groupsList, setGroupsList] = useState<AttributeGroup[]>([]);

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

  const loadActiveGroups = useCallback(async () => {
    try {
      const data = await fetchAttributeGroups();
      setGroups(data);
    } catch {
      setToast({ message: 'Failed to load attribute groups', type: 'error' });
    }
  }, []);

  const loadGroupsList = useCallback(async () => {
    try {
      const data = await fetchAttributeGroups(true);
      setGroupsList(data);
    } catch {
      setToast({ message: 'Failed to load all attribute groups', type: 'error' });
    }
  }, []);

  useEffect(() => {
    loadActiveGroups();
  }, [loadActiveGroups]);

  const openGroupManager = () => {
    setGroupFormMode('list');
    setGroupForm(emptyGroupForm);
    loadGroupsList();
    setGroupModalOpen(true);
  };

  const handleSaveGroup = async () => {
    if (!groupForm.code.trim()) {
      setToast({ message: 'Group code is required', type: 'error' });
      return;
    }
    if (!groupForm.displayNameEn.trim() || !groupForm.displayNameTh.trim()) {
      setToast({ message: 'Display names (EN & TH) are required', type: 'error' });
      return;
    }
    try {
      if (groupFormMode === 'edit') {
        await updateAttributeGroup(groupForm.code, groupForm);
        setToast({ message: `Group "${groupForm.code}" updated successfully`, type: 'success' });
      } else {
        const codeClean = groupForm.code.toUpperCase().replace(/[^A-Z0-9_]/g, '');
        await createAttributeGroup({ ...groupForm, code: codeClean });
        setToast({ message: `Group "${codeClean}" created successfully`, type: 'success' });
      }
      setGroupFormMode('list');
      loadActiveGroups();
      loadGroupsList();
      loadData();
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to save group';
      setToast({ message: msg, type: 'error' });
    }
  };

  const handleArchiveGroup = async (code: string) => {
    try {
      await deleteAttributeGroup(code);
      setToast({ message: `Group "${code}" archived successfully`, type: 'success' });
      loadActiveGroups();
      loadGroupsList();
      loadData();
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to archive group';
      setToast({ message: msg, type: 'error' });
    }
  };

  const handleRestoreGroup = async (group: AttributeGroup) => {
    try {
      await updateAttributeGroup(group.code, {
        code: group.code,
        displayNameEn: group.displayNameEn,
        displayNameTh: group.displayNameTh,
        displayOrder: group.displayOrder,
        status: 'ACTIVE',
        version: group.version
      });
      setToast({ message: `Group "${group.code}" activated successfully`, type: 'success' });
      loadActiveGroups();
      loadGroupsList();
      loadData();
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to activate group';
      setToast({ message: msg, type: 'error' });
    }
  };

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
      groupCode: attr.groupCode || '',
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

  const filteredAttributes = attributes.filter(attr => {
    if (groupFilter === '') return true;
    if (groupFilter === '_UNASSIGNED_') return !attr.groupCode;
    return attr.groupCode === groupFilter;
  });

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

          <select
            value={groupFilter}
            onChange={(e) => setGroupFilter(e.target.value)}
            className="px-4 py-2.5 rounded-md bg-white text-sm font-sans text-[#141c29]
              border border-[rgba(198,197,213,0.2)] outline-none cursor-pointer"
          >
            <option value="">All Groups</option>
            {groups.map((g) => (
              <option key={g.code} value={g.code}>
                {g.displayNameTh || g.displayNameEn} ({g.code})
              </option>
            ))}
            <option value="_UNASSIGNED_">-- Unassigned --</option>
          </select>

          <button
            onClick={openGroupManager}
            className="ml-auto px-6 py-2.5 rounded-md text-sm font-semibold text-[#000051]
              bg-white border border-[#000051] hover:bg-[#f0f3ff] transition-all duration-200"
          >
            Manage Groups
          </button>

          <button
            onClick={openCreate}
            className="px-6 py-2.5 rounded-md text-sm font-semibold text-white
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
                {['Code', 'Display Name', 'Data Type', 'Required', 'Group', 'Status', 'Regex', 'Actions'].map(
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
                  <td colSpan={8} className="px-6 py-12 text-center text-sm text-[#767685]">
                    Loading…
                  </td>
                </tr>
              ) : filteredAttributes.length === 0 ? (
                <tr>
                  <td colSpan={8} className="px-6 py-12 text-center text-sm text-[#767685]">
                    No attributes found
                  </td>
                </tr>
              ) : (
                filteredAttributes.map((attr, i) => (
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
                    <td className="px-6 py-3 text-sm text-[#454653] font-medium">
                      {groups.find(g => g.code === attr.groupCode)?.displayNameTh || attr.groupCode || '—'}
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
          {filteredAttributes.length} attribute{filteredAttributes.length !== 1 ? 's' : ''} found
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
              placeholder="e.g. PDPA_CONSENT"
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
              placeholder="e.g. PDPA Consent"
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

          {/* Attribute Group */}
          <div className="grid grid-cols-[33%_67%] items-center gap-x-4">
            <label className="text-xs font-bold text-[#767685] uppercase tracking-wider">
              Attribute Group
            </label>
            <select
              value={form.groupCode}
              onChange={(e) => setForm({ ...form, groupCode: e.target.value })}
              className="px-4 py-2.5 rounded-md bg-white text-sm font-sans text-[#141c29]
                border border-[rgba(198,197,213,0.2)] outline-none cursor-pointer"
            >
              <option value="">-- No Group (Unassigned) --</option>
              {groups.map((g) => (
                <option key={g.code} value={g.code}>
                  {g.displayNameTh || g.displayNameEn} ({g.code})
                </option>
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

      {/* ── Manage Groups Modal ── */}
      <Modal
        open={groupModalOpen}
        onClose={() => setGroupModalOpen(false)}
        title={
          groupFormMode === 'list'
            ? 'Manage Attribute Groups'
            : groupFormMode === 'edit'
            ? `Edit Group: ${groupForm.code}`
            : 'Add New Group'
        }
        width={groupFormMode === 'list' ? 'max-w-3xl' : 'max-w-xl'}
      >
        {groupFormMode === 'list' ? (
          <div className="space-y-4">
            <div className="flex justify-between items-center">
              <span className="text-xs text-[#767685]">
                Configure categories used to organize policy attributes
              </span>
              <button
                onClick={() => {
                  setGroupForm(emptyGroupForm);
                  setGroupFormMode('add');
                }}
                className="px-4 py-2 rounded-md text-xs font-semibold text-white
                  bg-gradient-to-br from-[#000051] to-[#00008f] border-none
                  hover:shadow-[0_4px_12px_rgba(20,28,41,0.12)] transition-all duration-200"
              >
                + Add Group
              </button>
            </div>

            <div className="border border-[rgba(198,197,213,0.2)] rounded-md overflow-hidden bg-white">
              <table className="w-full border-collapse">
                <thead>
                  <tr className="bg-[#dbe3f5]">
                    {['Code', 'Display Name (EN)', 'Display Name (TH)', 'Order', 'Status', 'Actions'].map((col) => (
                      <th
                        key={col}
                        className="px-4 py-2 text-left text-xs font-bold text-[#000051] uppercase tracking-wider font-sans"
                      >
                        {col}
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {groupsList.length === 0 ? (
                    <tr>
                      <td colSpan={6} className="px-4 py-8 text-center text-xs text-[#767685]">
                        No groups found.
                      </td>
                    </tr>
                  ) : (
                    groupsList.map((g, i) => (
                      <tr
                        key={g.code}
                        className={`transition-colors duration-150 hover:bg-[#e0e8fb]
                          ${i % 2 === 0 ? 'bg-[#f9f9ff]' : 'bg-[#f0f3ff]'}`}
                      >
                        <td className="px-4 py-2 text-xs font-medium text-[#141c29] font-mono">
                          {g.code}
                        </td>
                        <td className="px-4 py-2 text-xs text-[#141c29]">{g.displayNameEn}</td>
                        <td className="px-4 py-2 text-xs text-[#141c29]">{g.displayNameTh}</td>
                        <td className="px-4 py-2 text-xs text-[#454653] font-mono">{g.displayOrder}</td>
                        <td className="px-4 py-2">
                          <StatusChip status={g.status} />
                        </td>
                        <td className="px-4 py-2">
                          <div className="flex items-center gap-2">
                            <button
                              onClick={() => {
                                setGroupForm({
                                  code: g.code,
                                  displayNameEn: g.displayNameEn,
                                  displayNameTh: g.displayNameTh,
                                  displayOrder: g.displayOrder,
                                  status: g.status,
                                  version: g.version
                                });
                                setGroupFormMode('edit');
                              }}
                              className="px-2 py-1 rounded text-xs font-medium text-[#000051] hover:bg-[#e0e8fb] transition-colors"
                            >
                              Edit
                            </button>
                            {g.status === 'ACTIVE' ? (
                              <button
                                onClick={() => handleArchiveGroup(g.code)}
                                className="px-2 py-1 rounded text-xs font-medium text-[#a63b00] hover:bg-[#ffdbce] transition-colors"
                              >
                                Archive
                              </button>
                            ) : (
                              <button
                                onClick={() => handleRestoreGroup(g)}
                                className="px-2 py-1 rounded text-xs font-medium text-[#008f51] hover:bg-[#e2ffef] transition-colors"
                              >
                                Activate
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

            <div className="flex justify-end pt-2">
              <button
                onClick={() => setGroupModalOpen(false)}
                className="px-5 py-2 rounded-md text-sm font-semibold text-white
                  bg-gradient-to-br from-[#000051] to-[#00008f] border-none
                  hover:shadow-[0_4px_12px_rgba(20,28,41,0.12)] transition-all duration-200"
              >
                Close
              </button>
            </div>
          </div>
        ) : (
          <div className="space-y-5">
            <div className="grid grid-cols-[33%_67%] items-center gap-x-4">
              <label className="text-xs font-bold text-[#767685] uppercase tracking-wider">
                Group Code
              </label>
              <input
                type="text"
                value={groupForm.code}
                onChange={(e) =>
                  setGroupForm({
                    ...groupForm,
                    code: e.target.value.toUpperCase().replace(/[^A-Z0-9_]/g, ''),
                  })
                }
                disabled={groupFormMode === 'edit'}
                placeholder="e.g. USER_PROFILE"
                className="px-4 py-2.5 rounded-md bg-white text-sm font-sans text-[#141c29]
                  border border-[rgba(198,197,213,0.2)] outline-none transition-all duration-200
                  focus:border-2 focus:border-[#002a40] focus:shadow-[0_0_0_3px_rgba(0,42,64,0.08)]
                  disabled:opacity-50 disabled:cursor-not-allowed font-mono"
              />
            </div>

            <div className="grid grid-cols-[33%_67%] items-center gap-x-4">
              <label className="text-xs font-bold text-[#767685] uppercase tracking-wider">
                Display Name (EN)
              </label>
              <input
                type="text"
                value={groupForm.displayNameEn}
                onChange={(e) => setGroupForm({ ...groupForm, displayNameEn: e.target.value })}
                placeholder="e.g. User Profile"
                className="px-4 py-2.5 rounded-md bg-white text-sm font-sans text-[#141c29]
                  border border-[rgba(198,197,213,0.2)] outline-none transition-all duration-200
                  focus:border-2 focus:border-[#002a40] focus:shadow-[0_0_0_3px_rgba(0,42,64,0.08)]"
              />
            </div>

            <div className="grid grid-cols-[33%_67%] items-center gap-x-4">
              <label className="text-xs font-bold text-[#767685] uppercase tracking-wider">
                Display Name (TH)
              </label>
              <input
                type="text"
                value={groupForm.displayNameTh}
                onChange={(e) => setGroupForm({ ...groupForm, displayNameTh: e.target.value })}
                placeholder="e.g. ข้อมูลผู้ใช้งาน"
                className="px-4 py-2.5 rounded-md bg-white text-sm font-sans text-[#141c29]
                  border border-[rgba(198,197,213,0.2)] outline-none transition-all duration-200
                  focus:border-2 focus:border-[#002a40] focus:shadow-[0_0_0_3px_rgba(0,42,64,0.08)]"
              />
            </div>

            <div className="grid grid-cols-[33%_67%] items-center gap-x-4">
              <label className="text-xs font-bold text-[#767685] uppercase tracking-wider">
                Display Order
              </label>
              <input
                type="number"
                value={groupForm.displayOrder === 0 ? '' : groupForm.displayOrder}
                onChange={(e) =>
                  setGroupForm({
                    ...groupForm,
                    displayOrder: parseInt(e.target.value, 10) || 0,
                  })
                }
                placeholder="e.g. 4"
                className="px-4 py-2.5 rounded-md bg-white text-sm font-sans text-[#141c29]
                  border border-[rgba(198,197,213,0.2)] outline-none transition-all duration-200
                  focus:border-2 focus:border-[#002a40] focus:shadow-[0_0_0_3px_rgba(0,42,64,0.08)] font-mono"
              />
            </div>

            <div className="flex justify-end gap-3 pt-4">
              <button
                onClick={() => setGroupFormMode('list')}
                className="px-5 py-2.5 text-sm font-medium text-[#000051] hover:underline transition-all"
              >
                Back to List
              </button>
              <button
                onClick={handleSaveGroup}
                className="px-6 py-2.5 rounded-md text-sm font-semibold text-white
                  bg-gradient-to-br from-[#000051] to-[#00008f] border-none
                  hover:shadow-[0_4px_12px_rgba(20,28,41,0.12)] transition-all duration-200"
              >
                {groupFormMode === 'edit' ? 'Save Changes' : 'Create Group'}
              </button>
            </div>
          </div>
        )}
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
