import { useState } from 'react';

interface DynamicInputProps {
  label: string;
  subLabel?: string;
  value: string;
  onChange: (val: string) => void;
  dataType: 'STRING' | 'NUMBER' | 'DATE' | 'BOOLEAN';
  required?: boolean;
  regex?: string | null;
  regexErrorMsg?: string | null;
  disabled?: boolean;
}

/**
 * Reusable DynamicInput component.
 * Renders the correct input type based on dataType and validates against regex on blur.
 */
export default function DynamicInput({
  label,
  subLabel,
  value,
  onChange,
  dataType,
  required = false,
  regex,
  regexErrorMsg,
  disabled = false,
}: DynamicInputProps) {
  const [error, setError] = useState<string | null>(null);
  const [touched, setTouched] = useState(false);

  const validate = (val: string) => {
    if (required && !val.trim()) {
      setError('This field is required');
      return;
    }
    if (regex && val.trim()) {
      try {
        const pattern = new RegExp(regex);
        if (!pattern.test(val)) {
          setError(regexErrorMsg || `Value does not match pattern: ${regex}`);
          return;
        }
      } catch {
        // Invalid regex on frontend — skip client validation
      }
    }
    setError(null);
  };

  const handleBlur = () => {
    setTouched(true);
    validate(value);
  };

  const handleChange = (val: string) => {
    onChange(val);
    if (touched) validate(val);
  };

  // ── BOOLEAN → Toggle ──
  if (dataType === 'BOOLEAN') {
    const isOn = value === 'true';
    return (
      <div className="grid grid-cols-[33%_67%] items-center gap-x-4 gap-y-2">
        <div className="pt-1">
          <label className="text-xs font-bold text-[#767685] uppercase tracking-wider block">
            {label}
            {required && <span className="text-[#ba1a1a] ml-0.5">*</span>}
          </label>
          {subLabel && <span className="text-[0.65rem] text-[#767685] font-mono mt-0.5 block">{subLabel}</span>}
        </div>
        <button
          type="button"
          disabled={disabled}
          onClick={() => handleChange(isOn ? 'false' : 'true')}
          className={`relative w-11 h-6 rounded-full transition-colors duration-200 ${
            isOn ? 'bg-gradient-to-r from-[#000051] to-[#00008f]' : 'bg-[#c6c5d5]'
          } ${disabled ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer'}`}
        >
          <span
            className={`absolute top-0.5 left-0.5 w-5 h-5 rounded-full bg-white shadow-dropdown
              transition-transform duration-200 ${isOn ? 'translate-x-5' : 'translate-x-0'}`}
          />
        </button>
      </div>
    );
  }

  // ── DATE → DatePicker ──
  if (dataType === 'DATE') {
    return (
      <div className="grid grid-cols-[33%_67%] items-start gap-x-4 gap-y-2">
        <div className="pt-2.5">
          <label className="text-xs font-bold text-[#767685] uppercase tracking-wider block">
            {label}
            {required && <span className="text-[#ba1a1a] ml-0.5">*</span>}
          </label>
          {subLabel && <span className="text-[0.65rem] text-[#767685] font-mono mt-0.5 block">{subLabel}</span>}
        </div>
        <div>
          <input
            type="date"
            value={value}
            onChange={(e) => handleChange(e.target.value)}
            onBlur={handleBlur}
            disabled={disabled}
            className={`w-full px-4 py-2.5 rounded-md text-sm font-sans text-[#141c29]
              bg-white transition-all duration-200
              ${error && touched
                ? 'border-2 border-[#ba1a1a] shadow-[0_0_0_3px_rgba(186,26,26,0.08)]'
                : 'border border-[rgba(198,197,213,0.2)] focus:border-2 focus:border-[#002a40] focus:shadow-[0_0_0_3px_rgba(0,42,64,0.08)]'
              }
              outline-none disabled:opacity-50 disabled:cursor-not-allowed`}
          />
          {error && touched && (
            <p className="mt-1 text-xs text-[#ba1a1a] font-medium">{error}</p>
          )}
        </div>
      </div>
    );
  }

  // Check if regex represents a dropdown list (e.g., ^(Yes|No)$ or ^(MONTHLY|QUARTERLY|ANNUAL)$)
  let dropdownOptions: string[] | null = null;
  if (regex) {
    const enumMatch = regex.match(/^\^\(([^)]+)\)\$$/);
    if (enumMatch && enumMatch[1]) {
      dropdownOptions = enumMatch[1].split('|').map(opt => opt.trim());
    }
  }

  if (dropdownOptions) {
    return (
      <div className="grid grid-cols-[33%_67%] items-start gap-x-4 gap-y-2">
        <div className="pt-2.5">
          <label className="text-xs font-bold text-[#767685] uppercase tracking-wider block">
            {label}
            {required && <span className="text-[#ba1a1a] ml-0.5">*</span>}
          </label>
          {subLabel && <span className="text-[0.65rem] text-[#767685] font-mono mt-0.5 block">{subLabel}</span>}
        </div>
        <div>
          <select
            value={value}
            onChange={(e) => handleChange(e.target.value)}
            onBlur={handleBlur}
            disabled={disabled}
            className={`w-full px-4 py-2.5 rounded-md text-sm font-sans text-[#141c29]
              bg-white transition-all duration-200 cursor-pointer appearance-none pr-10
              ${error && touched
                ? 'border-2 border-[#ba1a1a] shadow-[0_0_0_3px_rgba(186,26,26,0.08)]'
                : 'border border-[rgba(198,197,213,0.2)] focus:border-2 focus:border-[#002a40] focus:shadow-[0_0_0_3px_rgba(0,42,64,0.08)]'
              }
              outline-none disabled:opacity-50 disabled:cursor-not-allowed`}
            style={{
              backgroundImage: `url("data:image/svg+xml;charset=utf-8,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 20 20' fill='%23767685'%3E%3Cpath fill-rule='evenodd' d='M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z' clip-rule='evenodd'/%3E%3C/svg%3E")`,
              backgroundPosition: 'right 12px center',
              backgroundSize: '18px',
              backgroundRepeat: 'no-repeat',
            }}
          >
            <option value="">Select {label.toLowerCase()}...</option>
            {dropdownOptions.map((opt) => (
              <option key={opt} value={opt}>
                {opt}
              </option>
            ))}
          </select>
          {error && touched && (
            <p className="mt-1 text-xs text-[#ba1a1a] font-medium">{error}</p>
          )}
        </div>
      </div>
    );
  }

  // ── STRING / NUMBER → Text Input ──
  return (
    <div className="grid grid-cols-[33%_67%] items-start gap-x-4 gap-y-2">
      <div className="pt-2.5">
        <label className="text-xs font-bold text-[#767685] uppercase tracking-wider block">
          {label}
          {required && <span className="text-[#ba1a1a] ml-0.5">*</span>}
        </label>
        {subLabel && <span className="text-[0.65rem] text-[#767685] font-mono mt-0.5 block">{subLabel}</span>}
      </div>
      <div>
        <input
          type={dataType === 'NUMBER' ? 'text' : 'text'}
          inputMode={dataType === 'NUMBER' ? 'decimal' : 'text'}
          value={value}
          onChange={(e) => handleChange(e.target.value)}
          onBlur={handleBlur}
          disabled={disabled}
          placeholder={`Enter ${label.toLowerCase()}`}
          className={`w-full px-4 py-2.5 rounded-md text-sm font-sans text-[#141c29]
            bg-white transition-all duration-200
            ${error && touched
              ? 'border-2 border-[#ba1a1a] shadow-[0_0_0_3px_rgba(186,26,26,0.08)]'
              : 'border border-[rgba(198,197,213,0.2)] focus:border-2 focus:border-[#002a40] focus:shadow-[0_0_0_3px_rgba(0,42,64,0.08)]'
            }
            outline-none disabled:opacity-50 disabled:cursor-not-allowed`}
        />
        {error && touched && (
          <p className="mt-1 text-xs text-[#ba1a1a] font-medium">{error}</p>
        )}
      </div>
    </div>
  );
}
