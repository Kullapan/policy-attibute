interface TopNavProps {
  title: string;
}

export default function TopNav({ title }: TopNavProps) {
  return (
    <header
      className="sticky top-0 z-40 h-16 flex items-center justify-between px-8
        bg-[rgba(249,249,255,0.75)] backdrop-blur-[20px]"
    >
      <h2 className="font-display text-xl font-semibold text-[#141c29]">{title}</h2>

      {/* Placeholder for future RBAC user info */}
      <div className="flex items-center gap-3">
        <div className="w-8 h-8 rounded-full bg-gradient-to-br from-[#000051] to-[#00008f]
          flex items-center justify-center text-white text-xs font-bold">
          S
        </div>
        <span className="text-sm text-[#454653] font-medium">SYSTEM</span>
      </div>
    </header>
  );
}
