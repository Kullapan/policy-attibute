import { useCallback, useState } from 'react';
import TopNav from '../components/TopNav';
import FileDropZone from '../components/FileDropZone';
import Toast from '../components/Toast';
import type { BulkUploadResult, RowError, SuccessRow } from '../types';
import { uploadCsv } from '../api/client';

type Step = 'upload' | 'processing' | 'review';

// ── CSV utilities ─────────────────────────────────────────

/**
 * Parse the raw CSV text from the uploaded file into row objects.
 * Expects header: policy_no, attribute_code, attribute_value
 */
function parseCsvRows(text: string): Array<{ policyNo: string; attributeCode: string; attributeValue: string }> {
  const lines = text.split(/\r?\n/).filter((l) => l.trim());
  // skip header row
  return lines.slice(1).map((line) => {
    const [policyNo = '', attributeCode = '', attributeValue = ''] = line.split(',').map((c) => c.trim());
    return { policyNo, attributeCode, attributeValue };
  });
}

/** Build a CSV string from an array of objects with a given header order. */
function buildCsv(headers: string[], rows: Record<string, string | number>[]): string {
  const headerLine = headers.join(',');
  const dataLines = rows.map((r) => headers.map((h) => `"${String(r[h] ?? '').replace(/"/g, '""')}"`).join(','));
  return [headerLine, ...dataLines].join('\n');
}

/** Trigger a browser file download. */
function triggerDownload(content: string, filename: string) {
  const blob = new Blob([content], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  a.click();
  URL.revokeObjectURL(url);
}

// ── Component ─────────────────────────────────────────────

export default function BulkUploadPage() {
  const [step, setStep] = useState<Step>('upload');
  const [file, setFile] = useState<File | null>(null);
  const [progress, setProgress] = useState(0);
  const [result, setResult] = useState<BulkUploadResult | null>(null);
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null);

  // ── File drop ──
  const handleFileDrop = (f: File) => {
    setFile(f);
    setResult(null);
    setStep('upload');
  };

  // ── Upload ──
  const handleUpload = useCallback(async () => {
    if (!file) return;
    setStep('processing');
    setProgress(0);

    const interval = setInterval(() => {
      setProgress((p) => Math.min(p + 15, 90));
    }, 200);

    try {
      // Read file text to derive success rows on the frontend
      const fileText = await file.text();
      const allParsedRows = parseCsvRows(fileText);

      const res = await uploadCsv(file);
      clearInterval(interval);
      setProgress(100);

      // Determine which row numbers had errors
      const errorRowNumbers = new Set(res.errors.map((e) => e.rowNumber));

      // Build successRows from rows not in the error set (1-indexed, row 1 = first data row after header)
      const successRows: SuccessRow[] = allParsedRows
        .map((row, idx) => ({ ...row, rowNumber: idx + 2 })) // +2: row 1 is header
        .filter((row) => !errorRowNumbers.has(row.rowNumber));

      setResult({ ...res, successRows });
      setStep('review');
      setToast({
        message: `Upload complete: ${res.successCount} success, ${res.errorCount} errors`,
        type: res.errorCount === 0 ? 'success' : 'error',
      });
    } catch (err: unknown) {
      clearInterval(interval);
      setProgress(0);
      setStep('upload');
      const msg = err instanceof Error ? err.message : 'Upload failed';
      setToast({ message: msg, type: 'error' });
    }
  }, [file]);

  // ── Downloads ──

  const downloadSuccessCsv = () => {
    if (!result?.successRows?.length) return;
    const rows = result.successRows.map((r) => ({
      row_number: r.rowNumber,
      policy_no: r.policyNo,
      attribute_code: r.attributeCode,
      attribute_value: r.attributeValue,
    }));
    const csv = buildCsv(['row_number', 'policy_no', 'attribute_code', 'attribute_value'], rows);
    triggerDownload(csv, `pams_success_${timestamp()}.csv`);
  };

  const downloadErrorCsv = () => {
    if (!result?.errors?.length) return;
    const rows = result.errors.map((e: RowError) => ({
      row_number: e.rowNumber,
      policy_no: e.policyNo,
      attribute_code: e.attributeCode,
      error_message: e.errorMessage,
    }));
    const csv = buildCsv(['row_number', 'policy_no', 'attribute_code', 'error_message'], rows);
    triggerDownload(csv, `pams_errors_${timestamp()}.csv`);
  };

  const downloadAllCsv = () => {
    if (!result) return;
    const successRows = (result.successRows ?? []).map((r) => ({
      row_number: r.rowNumber,
      policy_no: r.policyNo,
      attribute_code: r.attributeCode,
      attribute_value: r.attributeValue,
      status: 'SUCCESS',
      error_message: '',
    }));
    const errorRows = result.errors.map((e: RowError) => ({
      row_number: e.rowNumber,
      policy_no: e.policyNo,
      attribute_code: e.attributeCode,
      attribute_value: '',
      status: 'ERROR',
      error_message: e.errorMessage,
    }));
    const allRows = [...successRows, ...errorRows].sort((a, b) => Number(a.row_number) - Number(b.row_number));
    const csv = buildCsv(['row_number', 'policy_no', 'attribute_code', 'attribute_value', 'status', 'error_message'], allRows);
    triggerDownload(csv, `pams_result_${timestamp()}.csv`);
  };

  const downloadTemplate = () => {
    const csv = 'policy_no,attribute_code,attribute_value\nPOL-2026-001,MAX_COVERAGE,500000.00\nPOL-2026-001,IS_RENEWABLE,true';
    triggerDownload(csv, 'pams_upload_template.csv');
  };

  const handleReset = () => {
    setFile(null);
    setResult(null);
    setStep('upload');
    setProgress(0);
  };

  // ── Helpers ──

  const currentIndex = step === 'upload' ? 0 : step === 'processing' ? 1 : 2;

  return (
    <>
      <TopNav title="Bulk Upload Manager" />

      <div className="flex-1 px-16 py-8">

        {/* ── Step Indicator ── */}
        <div className="flex items-center gap-0 mb-10 max-w-lg mx-auto">
          {['Upload File', 'Processing', 'Review'].map((label, i) => {
            const isActive = i === currentIndex;
            const isDone = i < currentIndex;
            return (
              <div key={label} className="flex items-center flex-1">
                <div className="flex flex-col items-center flex-1">
                  <div
                    className={`w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold
                      transition-all duration-300
                      ${isDone
                        ? 'bg-gradient-to-br from-[#000051] to-[#00008f] text-white'
                        : isActive
                          ? 'bg-gradient-to-br from-[#000051] to-[#00008f] text-white ring-4 ring-[#e0e0ff]'
                          : 'bg-[#dbe3f5] text-[#767685]'
                      }`}
                  >
                    {isDone ? '✓' : i + 1}
                  </div>
                  <span className={`mt-2 text-xs font-medium ${isActive || isDone ? 'text-[#000051]' : 'text-[#767685]'}`}>
                    {label}
                  </span>
                </div>
                {i < 2 && (
                  <div className={`h-0.5 flex-1 mx-2 -mt-5 transition-colors duration-300 ${isDone ? 'bg-[#000051]' : 'bg-[#dbe3f5]'}`} />
                )}
              </div>
            );
          })}
        </div>

        {/* ── Step 1: Upload ── */}
        {step === 'upload' && (
          <div className="max-w-2xl mx-auto">
            <FileDropZone onFileDrop={handleFileDrop} />
            <div className="flex items-center justify-between mt-6">
              <button
                onClick={downloadTemplate}
                className="text-sm font-medium text-[#000051] underline underline-offset-2
                  decoration-transparent hover:decoration-[#000051] transition-all"
              >
                Download CSV Template
              </button>
              <button
                onClick={handleUpload}
                disabled={!file}
                className="px-6 py-2.5 rounded-md text-sm font-semibold text-white
                  bg-gradient-to-br from-[#000051] to-[#00008f] border-none
                  hover:shadow-[0_4px_12px_rgba(20,28,41,0.12)] transition-all duration-200
                  disabled:opacity-40 disabled:cursor-not-allowed"
              >
                Upload & Validate
              </button>
            </div>
          </div>
        )}

        {/* ── Step 2: Processing ── */}
        {step === 'processing' && (
          <div className="max-w-md mx-auto text-center">
            <div className="text-4xl mb-4 animate-bounce">⚙️</div>
            <h3 className="font-display text-lg font-semibold text-[#141c29] mb-4">Processing Upload…</h3>
            <div className="w-full h-2 rounded-full bg-[#dbe3f5] overflow-hidden">
              <div
                className="h-full rounded-full bg-gradient-to-r from-[#000051] to-[#00008f]
                  transition-all duration-300 ease-out"
                style={{ width: `${progress}%` }}
              />
            </div>
            <p className="text-xs text-[#767685] mt-2">{progress}% complete</p>
          </div>
        )}

        {/* ── Step 3: Review ── */}
        {step === 'review' && result && (
          <div className="max-w-3xl mx-auto">

            {/* Summary cards */}
            <div className="grid grid-cols-3 gap-4 mb-6">
              <div className="bg-white rounded-lg shadow-ambient p-5 text-center">
                <div className="font-display text-2xl font-bold text-[#141c29]">{result.totalRows}</div>
                <div className="text-xs text-[#767685] mt-1 uppercase tracking-wider font-bold">Total Rows</div>
              </div>
              <div className="bg-[#e0e0ff] rounded-lg p-5 text-center">
                <div className="font-display text-2xl font-bold text-[#00006e]">{result.successCount}</div>
                <div className="text-xs text-[#00006e] mt-1 uppercase tracking-wider font-bold">Successful</div>
              </div>
              <div className={`rounded-lg p-5 text-center ${result.errorCount > 0 ? 'bg-[#ffdad6]' : 'bg-[#e0e0ff]'}`}>
                <div className={`font-display text-2xl font-bold ${result.errorCount > 0 ? 'text-[#93000a]' : 'text-[#00006e]'}`}>
                  {result.errorCount}
                </div>
                <div className={`text-xs mt-1 uppercase tracking-wider font-bold ${result.errorCount > 0 ? 'text-[#93000a]' : 'text-[#00006e]'}`}>
                  Errors
                </div>
              </div>
            </div>

            {/* ── Download Buttons ── */}
            <div className="bg-white rounded-lg shadow-ambient p-5 mb-6">
              <h4 className="text-xs font-bold text-[#767685] uppercase tracking-wider mb-4">
                Download Results
              </h4>
              <div className="flex flex-wrap gap-3">

                {/* Download Success */}
                <button
                  onClick={downloadSuccessCsv}
                  disabled={!result.successRows?.length}
                  className="flex items-center gap-2 px-4 py-2.5 rounded-md text-sm font-semibold
                    bg-[#e0e0ff] text-[#00006e]
                    hover:bg-[#bfc2ff] transition-colors duration-200
                    disabled:opacity-40 disabled:cursor-not-allowed"
                >
                  <span>⬇</span>
                  Success Rows
                  {result.successRows?.length ? (
                    <span className="ml-1 px-1.5 py-0.5 rounded-full bg-[#00006e] text-white text-[0.625rem] font-bold">
                      {result.successRows.length}
                    </span>
                  ) : null}
                </button>

                {/* Download Errors */}
                <button
                  onClick={downloadErrorCsv}
                  disabled={!result.errors.length}
                  className="flex items-center gap-2 px-4 py-2.5 rounded-md text-sm font-semibold
                    bg-[#ffdad6] text-[#93000a]
                    hover:bg-[#ffb4ab] transition-colors duration-200
                    disabled:opacity-40 disabled:cursor-not-allowed"
                >
                  <span>⬇</span>
                  Error Rows
                  {result.errors.length ? (
                    <span className="ml-1 px-1.5 py-0.5 rounded-full bg-[#93000a] text-white text-[0.625rem] font-bold">
                      {result.errors.length}
                    </span>
                  ) : null}
                </button>

                {/* Download All */}
                <button
                  onClick={downloadAllCsv}
                  className="flex items-center gap-2 px-4 py-2.5 rounded-md text-sm font-semibold
                    bg-[#dbe3f5] text-[#000051]
                    hover:bg-[#c6cdde] transition-colors duration-200"
                >
                  <span>⬇</span>
                  Full Result
                  <span className="ml-1 px-1.5 py-0.5 rounded-full bg-[#000051] text-white text-[0.625rem] font-bold">
                    {result.totalRows}
                  </span>
                </button>

              </div>
              <p className="text-[0.6875rem] text-[#767685] mt-3">
                <strong>Success</strong>: rows that passed validation &nbsp;|&nbsp;
                <strong>Errors</strong>: rows that failed with reason &nbsp;|&nbsp;
                <strong>Full Result</strong>: all rows with status + error column
              </p>
            </div>

            {/* Error detail table */}
            {result.errors.length > 0 && (
              <div className="bg-white rounded-lg shadow-ambient overflow-hidden mb-6">
                <div className="px-6 py-4 bg-[#ffdad6] flex items-center justify-between">
                  <h4 className="text-sm font-bold text-[#93000a]">Error Details</h4>
                  <button
                    onClick={downloadErrorCsv}
                    className="text-xs font-semibold text-[#93000a] underline underline-offset-2
                      hover:no-underline transition-all"
                  >
                    ⬇ Download errors
                  </button>
                </div>
                <table className="w-full border-collapse">
                  <thead>
                    <tr className="bg-[#dbe3f5]">
                      {['Row', 'Policy No', 'Attribute Code', 'Error'].map((col) => (
                        <th
                          key={col}
                          className="px-6 py-3 text-left text-xs font-bold text-[#000051]
                            uppercase tracking-wider font-sans"
                        >
                          {col}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {result.errors.map((err, i) => (
                      <tr
                        key={i}
                        className={`${i % 2 === 0 ? 'bg-[#f9f9ff]' : 'bg-[#f0f3ff]'}
                          hover:bg-[#e0e8fb] transition-colors`}
                      >
                        <td className="px-6 py-3 text-sm text-[#141c29] font-mono">{err.rowNumber}</td>
                        <td className="px-6 py-3 text-sm text-[#141c29]">{err.policyNo || '—'}</td>
                        <td className="px-6 py-3 text-sm text-[#141c29] font-mono">{err.attributeCode || '—'}</td>
                        <td className="px-6 py-3 text-sm text-[#93000a]">
                          <span className="inline-flex items-center gap-1.5">
                            <span className="inline-flex px-2 py-0.5 rounded-full bg-[#ffdad6]
                              text-[0.6875rem] font-semibold text-[#93000a]">
                              Error
                            </span>
                            {err.errorMessage}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}

            {/* Actions */}
            <div className="flex justify-end">
              <button
                onClick={handleReset}
                className="px-6 py-2.5 rounded-md text-sm font-semibold text-white
                  bg-gradient-to-br from-[#000051] to-[#00008f] border-none
                  hover:shadow-[0_4px_12px_rgba(20,28,41,0.12)] transition-all duration-200"
              >
                Upload Another File
              </button>
            </div>
          </div>
        )}
      </div>

      {toast && (
        <Toast message={toast.message} type={toast.type} onClose={() => setToast(null)} />
      )}
    </>
  );
}

// Returns YYYYMMDD_HHmm for filenames
function timestamp() {
  const d = new Date();
  return `${d.getFullYear()}${String(d.getMonth() + 1).padStart(2, '0')}${String(d.getDate()).padStart(2, '0')}_${String(d.getHours()).padStart(2, '0')}${String(d.getMinutes()).padStart(2, '0')}`;
}
