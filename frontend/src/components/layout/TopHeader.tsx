import React from 'react';
import { Flame, Sparkles, LogOut } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { useOnboarding } from '../../context/OnboardingContext';

interface TopHeaderProps {
  title: string;
  subtitle?: string;
  titleClassName?: string;
}

export const TopHeader: React.FC<TopHeaderProps> = ({ title, subtitle, titleClassName }) => {
  const { user, requestLogout } = useAuth();
  const { isLearningUnlocked, onboardingState } = useOnboarding();

  // A learner is calibrated only if their learning path is active or required after assessment completion
  const isCalibrated = isLearningUnlocked || onboardingState?.state === 'LEARNING_PATH_REQUIRED';

  return (
    <header className="h-20 bg-white/80 backdrop-blur-md border-b border-black/[0.06] px-4 sm:px-6 flex items-center justify-between sticky top-0 z-30">
      <div className="min-w-0 flex-1 mr-3">
        <h1 className={`text-lg sm:text-xl font-extrabold text-memora-dark tracking-tight truncate ${titleClassName || ''}`}>{title}</h1>
        {subtitle && <p className="text-xs text-memora-text-muted mt-0.5 truncate">{subtitle}</p>}
      </div>

      {/* Gamification Stats & Mobile Actions */}
      <div className="flex items-center gap-2 sm:gap-3 shrink-0">
        {/* Streak Pill */}
        <div className="flex items-center gap-1 sm:gap-1.5 px-2.5 sm:px-3 py-1.5 bg-orange-50/90 border border-orange-200/70 rounded-full text-orange-700 shadow-sm shrink-0">
          <Flame className="w-3.5 h-3.5 sm:w-4 sm:h-4 fill-orange-500 text-orange-500 animate-pulse" />
          <span className="text-xs font-bold tracking-tight">
            {user?.streak ?? 0} {user?.streak === 1 ? 'day' : 'days'}
          </span>
        </div>

        {/* XP Pill */}
        <div className="flex items-center gap-1 sm:gap-1.5 px-2.5 sm:px-3 py-1.5 bg-[#EAF2DE] border border-[#D5E6BE] rounded-full text-[#425E16] shadow-sm shrink-0">
          <Sparkles className="w-3.5 h-3.5 sm:w-4 sm:h-4 text-memora-green" />
          <span className="text-xs font-bold tracking-tight">{user?.xp ?? 0} XP</span>
        </div>

        {/* Level Badge */}
        <div className="hidden sm:flex items-center gap-1 px-2.5 py-1.5 bg-stone-100 border border-stone-200 rounded-full text-stone-700 text-xs font-bold shrink-0">
          <span>{isCalibrated ? (user?.currentLevel || 'A1') : 'Not calibrated'}</span>
        </div>

        {/* Mobile Log Out Action */}
        <button
          type="button"
          onClick={requestLogout}
          className="md:hidden flex items-center justify-center w-11 h-11 min-w-[44px] min-h-[44px] rounded-2xl bg-stone-100/90 hover:bg-red-50 text-stone-600 hover:text-red-600 active:scale-95 transition-all border border-black/[0.04] shadow-xs cursor-pointer shrink-0"
          aria-label="Log Out"
          title="Log Out"
        >
          <LogOut className="w-4 h-4" />
        </button>
      </div>
    </header>
  );
};

export default TopHeader;
