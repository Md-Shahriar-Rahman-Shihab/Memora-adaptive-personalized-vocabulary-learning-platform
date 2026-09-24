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
  BookA,
  Lock,
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { useOnboarding } from '../../context/OnboardingContext';
import { useToast } from '../../context/ToastContext';

interface NavItem {
  label: string;
  path: string;
  icon: React.ReactNode;
  requiresLearningUnlocked?: boolean;
}

export const Sidebar: React.FC = () => {
  const { user, logout } = useAuth();
  const { isLearningUnlocked } = useOnboarding();
  const { addToast } = useToast();

  const navItems: NavItem[] = [
    { label: 'Dashboard', path: '/dashboard', icon: <LayoutDashboard className="w-5 h-5" /> },
    { label: 'Learn Path', path: '/learn-path', icon: <Compass className="w-5 h-5" />, requiresLearningUnlocked: true },
    { label: 'Review', path: '/review', icon: <RotateCw className="w-5 h-5" />, requiresLearningUnlocked: true },
    { label: 'Quiz', path: '/quiz', icon: <HelpCircle className="w-5 h-5" />, requiresLearningUnlocked: true },
    { label: 'Dictionary', path: '/dictionary', icon: <BookA className="w-5 h-5" /> },
    { label: 'Progress', path: '/progress', icon: <TrendingUp className="w-5 h-5" />, requiresLearningUnlocked: true },
    { label: 'Achievements', path: '/achievements', icon: <Award className="w-5 h-5" />, requiresLearningUnlocked: true },
    { label: 'Leaderboard', path: '/leaderboard', icon: <Crown className="w-5 h-5" /> },
    { label: 'Profile', path: '/profile', icon: <UserIcon className="w-5 h-5" /> },
  ];

  const handleLockedClick = (e: React.MouseEvent, item: NavItem) => {
    e.preventDefault();
    e.stopPropagation();
    addToast({
      type: 'info',
      title: `${item.label} is Locked 🔒`,
      message: 'Complete your 10-question placement assessment first to unlock this feature.',
    });
  };

  return (
    <aside className="w-64 bg-white border-r border-black/[0.06] flex flex-col justify-between shrink-0 h-screen sticky top-0 hidden md:flex z-30">
      {/* Top Section: Logo and Nav Items */}
      <div className="flex-1 flex flex-col min-h-0 overflow-y-auto">
        {/* Logo */}
        <div className="h-20 px-6 flex items-center border-b border-black/[0.04] shrink-0">
          <Link to="/dashboard" className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-2xl bg-memora-green-light flex items-center justify-center text-memora-green">
              <Sparkles className="w-5 h-5 fill-current" />
            </div>
            <span className="text-xl font-extrabold tracking-tight text-memora-dark">Memora</span>
          </Link>
        </div>

        {/* Navigation Items */}
        <nav className="p-4 space-y-1.5 flex-1">
          {navItems.map((item) => {
            const isLocked = item.requiresLearningUnlocked && !isLearningUnlocked;

            return (
              <NavLink
                key={item.path}
                to={isLocked ? '#' : item.path}
                onClick={isLocked ? (e) => handleLockedClick(e, item) : undefined}
                className={({ isActive }) =>
                  `flex items-center gap-3.5 px-4 py-3 rounded-2xl text-sm font-semibold transition-all duration-150 ${
                    isActive
                      ? 'bg-memora-green text-white shadow-sm shadow-memora-green/20'
                      : isLocked
                      ? 'text-stone-400 hover:text-stone-600 hover:bg-stone-50 cursor-pointer'
                      : 'text-memora-text-muted hover:text-memora-dark hover:bg-stone-100/70'
                  }`
                }
              >
                {item.icon}
                <span className="flex-1">{item.label}</span>
                {isLocked && (
                  <span className="text-stone-400 p-1" title="Locked until assessment completion">
                    <Lock className="w-3.5 h-3.5" />
                  </span>
                )}
              </NavLink>
            );
          })}
        </nav>
      </div>

      {/* User Footer & Logout */}
      <div className="p-4 border-t border-black/[0.06] shrink-0 bg-white space-y-2.5">
        <div className="flex items-center gap-3 px-3 py-2.5 bg-[#F8F8F5] rounded-2xl border border-black/[0.04]">
          <div className="w-9 h-9 rounded-xl bg-memora-green text-white flex items-center justify-center font-bold text-sm shrink-0 shadow-sm">
            {user?.name ? user.name.charAt(0).toUpperCase() : 'M'}
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-sm font-bold text-memora-dark truncate">{user?.name || 'Learner'}</p>
            <p className="text-xs text-memora-text-muted truncate">{user?.email}</p>
          </div>
        </div>

        <button
          onClick={logout}
          className="w-full flex items-center justify-center gap-2 px-3 py-2 rounded-xl text-xs font-semibold text-stone-500 hover:text-red-600 hover:bg-red-50/80 transition-colors"
        >
          <LogOut className="w-4 h-4" />
          <span>Sign Out</span>
        </button>
      </div>
    </aside>
  );
};

export default Sidebar;
