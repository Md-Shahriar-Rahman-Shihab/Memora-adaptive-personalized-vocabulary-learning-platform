import React from 'react';
import { NavLink, Link } from 'react-router-dom';
import {
  LayoutDashboard,
  Compass,
  RotateCw,
  HelpCircle,
  TrendingUp,
  Award,
  Crown,
  User as UserIcon,
  LogOut,
  Sparkles,
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

interface NavItem {
  label: string;
  path: string;
  icon: React.ReactNode;
}

export const Sidebar: React.FC = () => {
  const { user, logout } = useAuth();

  const navItems: NavItem[] = [
    { label: 'Dashboard', path: '/dashboard', icon: <LayoutDashboard className="w-5 h-5" /> },
    { label: 'Learn Path', path: '/learn-path', icon: <Compass className="w-5 h-5" /> },
    { label: 'Review', path: '/review', icon: <RotateCw className="w-5 h-5" /> },
    { label: 'Quiz', path: '/quiz', icon: <HelpCircle className="w-5 h-5" /> },
    { label: 'Progress', path: '/progress', icon: <TrendingUp className="w-5 h-5" /> },
    { label: 'Achievements', path: '/achievements', icon: <Award className="w-5 h-5" /> },
    { label: 'Leaderboard', path: '/leaderboard', icon: <Crown className="w-5 h-5" /> },
    { label: 'Profile', path: '/profile', icon: <UserIcon className="w-5 h-5" /> },
  ];

  return (
    <aside className="w-64 bg-white border-r border-black/[0.06] flex flex-col justify-between shrink-0 min-h-screen hidden md:flex">
      <div>
        {/* Logo */}
        <div className="h-20 px-6 flex items-center border-b border-black/[0.04]">
          <Link to="/dashboard" className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-2xl bg-memora-green-light flex items-center justify-center text-memora-green">
              <Sparkles className="w-5 h-5 fill-current" />
            </div>
            <span className="text-xl font-extrabold tracking-tight text-memora-dark">Memora</span>
          </Link>
        </div>

        {/* Navigation Items */}
        <nav className="p-4 space-y-1.5">
          {navItems.map((item) => (
            <NavLink
              key={item.path}
              to={item.path}
              className={({ isActive }) =>
                `flex items-center gap-3.5 px-4 py-3 rounded-2xl text-sm font-semibold transition-all duration-150 ${
                  isActive
                    ? 'bg-memora-green text-white shadow-sm shadow-memora-green/20'
                    : 'text-memora-text-muted hover:text-memora-dark hover:bg-stone-100/70'
                }`
              }
            >
              {item.icon}
              <span>{item.label}</span>
            </NavLink>
          ))}
        </nav>
      </div>

      {/* User Footer & Logout */}
      <div className="p-4 border-t border-black/[0.06] space-y-3">
        <div className="flex items-center gap-3 px-3 py-2 bg-[#F8F8F5] rounded-2xl border border-black/[0.04]">
          <div className="w-9 h-9 rounded-xl bg-memora-green text-white flex items-center justify-center font-bold text-sm shrink-0">
            {user?.name ? user.name.charAt(0).toUpperCase() : 'M'}
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-sm font-bold text-memora-dark truncate">{user?.name || 'Learner'}</p>
            <p className="text-xs text-memora-text-muted truncate">{user?.email}</p>
          </div>
        </div>

        <button
          onClick={logout}
          className="w-full flex items-center gap-2.5 px-3 py-2 rounded-xl text-xs font-semibold text-red-600 hover:bg-red-50 transition"
        >
          <LogOut className="w-4 h-4" />
          <span>Sign Out</span>
        </button>
      </div>
    </aside>
  );
};

export default Sidebar;
