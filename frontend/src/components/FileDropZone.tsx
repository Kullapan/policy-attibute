import { useCallback, useState } from 'react';

interface FileDropZoneProps {
  onFileDrop: (file: File) => void;
  accept?: string;
  disabled?: boolean;
}

export default function FileDropZone({
  onFileDrop,
  accept = '.csv',
  disabled = false,
}: FileDropZoneProps) {
  const [dragActive, setDragActive] = useState(false);
  const [fileName, setFileName] = useState<string | null>(null);

  const handleDragOver = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (!disabled) setDragActive(true);
  }, [disabled]);

  const handleDragLeave = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
  }, []);

  const handleDrop = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
    if (disabled) return;
    const file = e.dataTransfer.files[0];
    if (file) {
      setFileName(file.name);
      onFileDrop(file);
    }
  }, [disabled, onFileDrop]);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setFileName(file.name);
      onFileDrop(file);
    }
  };

  return (
    <div
      onDragOver={handleDragOver}
      onDragLeave={handleDragLeave}
      onDrop={handleDrop}
      className={`relative flex flex-col items-center justify-center p-12 rounded-lg
        transition-all duration-200 cursor-pointer
        bg-white
        ${dragActive
          ? 'outline-2 outline-dashed outline-[#000051] bg-[#e7eeff]'
          : 'outline-1 outline-dashed outline-[rgba(198,197,213,0.4)] hover:outline-[rgba(198,197,213,0.6)]'
        }
        ${disabled ? 'opacity-50 cursor-not-allowed' : ''}
      `}
    >
      <input
        type="file"
        accept={accept}
        onChange={handleInputChange}
        disabled={disabled}
        className="absolute inset-0 w-full h-full opacity-0 cursor-pointer disabled:cursor-not-allowed"
      />

      <div className="text-4xl mb-3">📄</div>
      {fileName ? (
        <p className="text-sm font-medium text-[#141c29]">{fileName}</p>
      ) : (
        <>
          <p className="text-sm font-medium text-[#141c29]">
            Drop your CSV file here, or <span className="text-[#000051] underline">browse</span>
          </p>
          <p className="text-xs text-[#767685] mt-1">Supports .csv files up to 10MB</p>
        </>
      )}
    </div>
  );
}
