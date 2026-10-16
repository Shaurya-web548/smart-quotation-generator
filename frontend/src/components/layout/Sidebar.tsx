import React from 'react';
import { NavLink } from 'react-router-dom';
import { LayoutDashboard, FilePlus, History, Settings, Users } from 'lucide-react';

interface SidebarProps {
  className?: string;
}

export const Sidebar: React.FC<SidebarProps> = ({ className = '' }) => {
  const links = [
    { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { to: '/quotations/new', label: 'New Quotation', icon: FilePlus },
    { to: '/quotations', label: 'Quotation History', icon: History },
    { to: '/settings', label: 'Company Settings', icon: Settings },
  ];

  return (
    <aside className={`w-64 bg-white border-r border-slate-200 flex flex-col py-6 px-4 shrink-0 ${className}`}>
      <nav className="space-y-1">
        {links.map((item) => {
          const Icon = item.icon;
          return (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                `flex items-center space-x-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                  isActive
                    ? 'bg-blue-50 text-blue-700 shadow-xs'
                    : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50'
                }`
              }
            >
              <Icon className="w-5 h-5" />
              <span>{item.label}</span>
            </NavLink>
          );
        })}
      </nav>

      <div className="mt-auto pt-6 border-t border-slate-100">
        <div className="bg-slate-50 rounded-lg p-3 text-xs text-slate-500">
          <p className="font-semibold text-slate-700 mb-1">Smart Quotation v1.0</p>
          <p>Multi-tenant production business web application.</p>
        </div>
      </div>
    </aside>
  );
};

