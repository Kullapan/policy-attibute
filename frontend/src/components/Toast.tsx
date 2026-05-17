import { useEffect, useState } from 'react';

interface ToastProps {
  message: string;
  type: 'success' | 'error' | 'info';
  onClose: () => void;
  duration?: number;
}

export default function Toast({ message, type, onClose, duration = 4000 }: ToastProps) {
  const [visible, setVisible] = useState(true);

  useEffect(() => {
    const timer = setTimeout(() => {
      setVisible(false);
      setTimeout(onClose, 300);
    }, duration);
    return () => clearTimeout(timer);
  }, [duration, onClose]);

  const bgClass =
    type === 'success'
      ? 'bg-[#e0e0ff] text-[#00006e]'
      : type === 'error'
        ? 'bg-[#ffdad6] text-[#93000a]'
        : 'bg-[#c9e6ff] text-[#001e2f]';

  return (
    <div
      className={`fixed top-6 right-6 z-[9999] px-5 py-3 rounded-md shadow-dropdown font-sans text-sm font-medium
        transition-all duration-300 ${visible ? 'opacity-100 translate-y-0' : 'opacity-0 -translate-y-2'}
        ${bgClass}`}
    >
      <div className="flex items-center gap-3">
        <span>{message}</span>
        <button
          onClick={() => { setVisible(false); setTimeout(onClose, 300); }}
          className="ml-2 opacity-60 hover:opacity-100 transition-opacity text-lg leading-none"
        >
          ×
        </button>
      </div>
    </div>
  );
}
