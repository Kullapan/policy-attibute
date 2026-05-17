import type { ReactNode } from 'react';

interface ModalProps {
  open: boolean;
  onClose: () => void;
  title: string;
  children: ReactNode;
  width?: string;
}

export default function Modal({ open, onClose, title, children, width = 'max-w-xl' }: ModalProps) {
  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      {/* Overlay — glassmorphism per DESIGN.md */}
      <div
        className="absolute inset-0 bg-[rgba(41,49,62,0.4)] backdrop-blur-[4px]"
        onClick={onClose}
      />

      {/* Modal panel — glassmorphism */}
      <div
        className={`relative ${width} w-full mx-4 p-8 rounded-lg shadow-ambient
          bg-[rgba(249,249,255,0.92)] backdrop-blur-[20px]`}
      >
        {/* Header */}
        <div className="flex items-center justify-between mb-6">
          <h2 className="font-display text-xl font-semibold text-[#141c29]">{title}</h2>
          <button
            onClick={onClose}
            className="w-8 h-8 flex items-center justify-center rounded-md text-[#454653]
              hover:bg-[#e0e8fb] transition-colors text-lg"
          >
            ×
          </button>
        </div>

        {/* Content */}
        {children}
      </div>
    </div>
  );
}
