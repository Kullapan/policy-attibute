import type {
  AttributeMaster,
  AttributeMasterForm,
  AttributeStatus,
  BulkUploadResult,
  PolicyAttributeValue,
  PolicyMaster,
} from '../types';

const API_BASE = 'http://localhost:8080/api/v1';

async function handleResponse<T>(res: Response): Promise<T> {
  if (!res.ok) {
    const error = await res.json().catch(() => ({ message: res.statusText }));
    throw new Error(error.message || `Request failed: ${res.status}`);
  }
  return res.json();
}

// ── Attribute Master (Dictionary) ────────────────────────

export async function fetchAttributes(
  search?: string,
  status?: AttributeStatus
): Promise<AttributeMaster[]> {
  const params = new URLSearchParams();
  if (search) params.set('search', search);
  if (status) params.set('status', status);
  const qs = params.toString();
  const res = await fetch(`${API_BASE}/attributes${qs ? '?' + qs : ''}`);
  return handleResponse(res);
}

export async function fetchAttributeByCode(code: string): Promise<AttributeMaster> {
  const res = await fetch(`${API_BASE}/attributes/${code}`);
  return handleResponse(res);
}

export async function createAttribute(dto: AttributeMasterForm): Promise<AttributeMaster> {
  const res = await fetch(`${API_BASE}/attributes`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(dto),
  });
  return handleResponse(res);
}

export async function updateAttribute(
  code: string,
  dto: AttributeMasterForm
): Promise<AttributeMaster> {
  const res = await fetch(`${API_BASE}/attributes/${code}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(dto),
  });
  return handleResponse(res);
}

export async function deleteAttribute(code: string): Promise<void> {
  const res = await fetch(`${API_BASE}/attributes/${code}`, { method: 'DELETE' });
  if (!res.ok) {
    const error = await res.json().catch(() => ({ message: res.statusText }));
    throw new Error(error.message || `Delete failed: ${res.status}`);
  }
}

// ── Policy Master & Attributes ─────────────────────────────

export async function fetchAllPolicies(): Promise<PolicyMaster[]> {
  const res = await fetch(`${API_BASE}/policies`);
  return handleResponse(res);
}

export async function fetchPolicyAttributes(
  policyNo: string
): Promise<PolicyAttributeValue[]> {
  const res = await fetch(`${API_BASE}/policies/${policyNo}/attributes`);
  return handleResponse(res);
}

export async function updatePolicyAttribute(
  policyNo: string,
  attributeCode: string,
  attributeValue: string
): Promise<PolicyAttributeValue> {
  const res = await fetch(
    `${API_BASE}/policies/${policyNo}/attributes/${attributeCode}`,
    {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ attributeValue }),
    }
  );
  return handleResponse(res);
}

// ── Bulk Upload ──────────────────────────────────────────

export async function uploadCsv(file: File): Promise<BulkUploadResult> {
  const formData = new FormData();
  formData.append('file', file);
  const res = await fetch(`${API_BASE}/bulk-upload`, {
    method: 'POST',
    body: formData,
  });
  return handleResponse(res);
}
