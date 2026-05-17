import type { AttributeStatus } from '../types';

interface StatusChipProps {
  status: AttributeStatus | string;
}

export default function StatusChip({ status }: StatusChipProps) {
  const isActive = status === 'ACTIVE';

  return (
    <span
      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-[0.6875rem] font-semibold tracking-wider
        ${isActive
          ? 'bg-[#e0e0ff] text-[#00006e]'       /* primary_fixed / on_primary_fixed */
          : 'bg-[#ffdbce] text-[#370e00]'        /* secondary_fixed / on_secondary_fixed */
        }`}
    >
      <span className={`w-1.5 h-1.5 rounded-full mr-1.5 ${isActive ? 'bg-[#00006e]' : 'bg-[#7f2b00]'}`} />
      {status}
    </span>
  );
}
