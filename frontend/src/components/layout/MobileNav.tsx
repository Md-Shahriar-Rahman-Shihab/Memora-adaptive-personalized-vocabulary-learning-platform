import React from 'react';
import { NavLink } from 'react-router-dom';
import { LayoutDashboard, Compass, RotateCw, HelpCircle, User, BookA, Lock, Users } from 'lucide-react';
import { useOnboarding } from '../../context/OnboardingContext';
import { useToast } from '../../context/ToastContext';

interface MobileNavItem {
  label: string;
  path: string;
  icon: React.ReactNode;
  requiresLearningUnlocked?: boolean;
  requiresPlacement?: boolean;
}

export const MobileNav: React.FC = () => {
  const { isLearningUnlocked, isPlacementCompleted } = useOnboarding();
  const { addToast } = useToast();

  const items: MobileNavItem[] = [
    { label: 'Dashboard', path: '/dashboard', icon: <LayoutDashboard className="w-5 h-5" /> },
    { label: 'Learn', path: '/learn-path', icon: <Compass className="w-5 h-5" />, requiresLearningUnlocked: true },
    { label: 'Review', path: '/review', icon: <RotateCw className="w-5 h-5" />, requiresLearningUnlocked: true },
    { label: 'Dictionary', path: '/dictionary', icon: <BookA className="w-5 h-5" /> },
    { label: 'Partners', path: '/partners', icon: <Users className="w-5 h-5" />, requiresPlacement: true },
    { label: 'Quiz', path: '/quiz', icon: <HelpCircle className="w-5 h-5" />, requiresLearningUnlocked: true },
    { label: 'Profile', path: '/profile', icon: <User className="w-5 h-5" /> },
  ];

  const handleLockedClick = (e: React.MouseEvent, item: MobileNavItem) => {
    e.preventDefault();
    e.stopPropagation();
    addToast({
      type: 'info',
      title: `${item.label} is Locked 🔒`,
      message: 'Complete your 10-question placement assessment first to unlock this feature.',
    });
  };

  return (
    <nav className="md:hidden fixed bottom-0 left-0 right-0 z-40 bg-white/95 backdrop-blur-md border-t border-black/[0.08] px-1 sm:px-3 py-1.5 flex items-center justify-around">
      {items.map((item) => {
        const isLocked =
          (item.requiresLearningUnlocked && !isLearningUnlocked) ||
          (item.requiresPlacement && !isPlacementCompleted);

        return (
          <NavLink
            key={item.path}
            to={isLocked ? '#' : item.path}
            onClick={isLocked ? (e) => handleLockedClick(e, item) : undefined}
            className={({ isActive }) =>
              `flex flex-col items-center gap-0.5 p-1 sm:p-1.5 rounded-xl text-[9px] sm:text-[10px] font-semibold transition relative min-w-0 ${
                isActive
                  ? 'text-memora-green font-bold'
                  : isLocked
                  ? 'text-stone-400 hover:text-stone-500 cursor-pointer'
                  : 'text-memora-text-muted hover:text-memora-dark'
              }`
            }
          >
            <div className="relative">
              {item.icon}
              {isLocked && (
                <span className="absolute -top-1 -right-1 bg-stone-200 text-stone-600 rounded-full p-0.5">
                  <Lock className="w-2.5 h-2.5" />
                </span>
              )}
            </div>
            <span className="truncate max-w-[48px] sm:max-w-none">{item.label}</span>
          </NavLink>
        );
      })}
    </nav>
  );
};

export default MobileNav;
