import { NavLink } from 'react-router-dom';

const navItems = [
  { to: '/', label: 'Dictionary', icon: '📋' },
  { to: '/policies', label: 'Policy Dashboard', icon: '📊' },
  { to: '/policy-mapping', label: 'Policy Mapping', icon: '🔗' },
  { to: '/bulk-upload', label: 'Bulk Upload', icon: '📤' },
];

export default function Sidebar() {
  return (
    <aside className="w-60 min-h-screen bg-[#f0f3ff] flex flex-col py-6 shrink-0">
      {/* Brand */}
      <div className="px-6 mb-8">
        <h1 className="font-display text-lg font-bold text-[#000051] leading-tight">
          Policy Attribute
          <br />
          <span className="text-[0.8125rem] font-medium text-[#454653]">Manager</span>
        </h1>
      </div>

      {/* Nav Items */}
      <nav className="flex flex-col gap-1">
        {navItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.to === '/'}
            className={({ isActive }) =>
              `flex items-center gap-3 px-6 py-2.5 text-sm font-medium transition-colors duration-150
              rounded-r-md mr-3
              ${isActive
                ? 'bg-[#e7eeff] text-[#000051] font-semibold'
                : 'text-[#454653] hover:bg-[#e0e8fb]'
              }`
            }
          >
            <span className="text-base">{item.icon}</span>
            {item.label}
          </NavLink>
        ))}
      </nav>

      {/* Footer spacer */}
      <div className="mt-auto px-6 pt-6">
        <div className="text-[0.6875rem] text-[#767685]">PAMS v1.0</div>
      </div>
    </aside>
  );
}
