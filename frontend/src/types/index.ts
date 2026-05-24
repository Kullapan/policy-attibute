// ── Domain Types ─────────────────────────────────────────

export type DataType = 'STRING' | 'NUMBER' | 'DATE' | 'BOOLEAN';
export type AttributeStatus = 'ACTIVE' | 'ARCHIVED';

export interface AttributeGroup {
  code: string;
  displayNameEn: string;
  displayNameTh: string;
  displayOrder: number;
  status: AttributeStatus;
  version?: number;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
}

export interface AttributeGroupForm {
  code: string;
  displayNameEn: string;
  displayNameTh: string;
  displayOrder: number;
  status?: AttributeStatus;
  version?: number;
}

export interface AttributeMaster {
  code: string;
  displayName: string;
  dataType: DataType;
  status: AttributeStatus;
  isRequired: boolean;
  regexPattern: string | null;
  regexErrorMsg: string | null;
  groupCode: string | null;
  version: number;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
}

export interface AttributeMasterForm {
  code: string;
  displayName: string;
  dataType: DataType;
  isRequired: boolean;
  regexPattern: string;
  regexErrorMsg: string;
  groupCode: string;
  version?: number;
}

export interface PolicyAttributeValue {
  policyNo: string;
  attributeCode: string;
  attributeValue: string;
  displayName: string;
  dataType: DataType;
  isRequired: boolean;
  regexPattern: string | null;
  regexErrorMsg: string | null;
  groupCode: string | null;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
}

export interface BulkUploadResult {
  totalRows: number;
  successCount: number;
  errorCount: number;
  errors: RowError[];
  successRows?: SuccessRow[]; // populated on frontend from original file
}

export interface RowError {
  rowNumber: number;
  policyNo: string;
  attributeCode: string;
  errorMessage: string;
}

export interface SuccessRow {
  rowNumber: number;
  policyNo: string;
  attributeCode: string;
  attributeValue: string;
}

// ── Policy Master ──────────────────────────────────────────

export interface PolicyMaster {
  policyNo: string;
  status: string;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
}

// ── API Error ────────────────────────────────────────────

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
}
