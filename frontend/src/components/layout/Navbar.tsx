import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { FileText, LogOut, Settings, Building2, User as UserIcon } from 'lucide-react';

export const Navbar: React.FC = () => {
  const { user, company, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <header className="bg-white border-b border-slate-200 sticky top-0 z-30">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16 items-center">
          {/* Brand */}
          <div className="flex items-center space-x-3">
            <Link to="/dashboard" className="flex items-center space-x-2">
              <div className="w-10 h-10 rounded-lg bg-blue-600 flex items-center justify-center text-white shadow-sm">
                <FileText className="w-6 h-6" />
              </div>
              <div className="flex flex-col">
                <span className="font-bold text-lg text-slate-900 leading-tight">SmartQuote</span>
                <span className="text-xs text-slate-500 font-medium">Business Edition</span>
              </div>
            </Link>

            {company && (
              <div className="hidden sm:flex items-center space-x-2 ml-4 pl-4 border-l border-slate-200">
                <Building2 className="w-4 h-4 text-slate-400" />
                <span className="text-sm font-semibold text-slate-700">{company.name}</span>
                <span className="text-xs px-2 py-0.5 rounded-full bg-blue-50 text-blue-700 font-medium">
                  {company.currency} ({company.currencySymbol})
                </span>
              </div>
            )}
          </div>

          {/* Right Navigation & Profile */}
          <div className="flex items-center space-x-4">
            <Link
              to="/settings"
              className="p-2 text-slate-500 hover:text-slate-800 hover:bg-slate-100 rounded-lg transition-colors"
              title="Company Settings"
            >
              <Settings className="w-5 h-5" />
            </Link>

            <div className="flex items-center space-x-3 pl-2 border-l border-slate-200">
              <div className="hidden md:flex flex-col text-right">
                <span className="text-sm font-medium text-slate-900">{user?.fullName}</span>
                <span className="text-xs text-slate-500">{user?.email}</span>
              </div>

              <div className="w-9 h-9 rounded-full bg-slate-100 border border-slate-200 flex items-center justify-center text-slate-600 font-semibold text-sm">
                {user?.fullName?.charAt(0).toUpperCase() || <UserIcon className="w-4 h-4" />}
              </div>

              <button
                onClick={handleLogout}
                className="p-2 text-slate-500 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                title="Sign out"
              >
                <LogOut className="w-5 h-5" />
              </button>
            </div>
          </div>
        </div>
      </div>
    </header>
  );
};
