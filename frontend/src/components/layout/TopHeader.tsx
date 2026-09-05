import React from 'react';
import { Flame, Sparkles, Compass } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { Link } from 'react-router-dom';

interface TopHeaderProps {
  title: string;
  subtitle?: string;
  titleClassName?: string;
}

export const TopHeader: React.FC<TopHeaderProps> = ({ title, subtitle, titleClassName }) => {
  const { user } = useAuth();

  return (
    <header className="h-20 bg-white/80 backdrop-blur-md border-b border-black/[0.06] px-6 flex items-center justify-between sticky top-0 z-30">
      <div>
        <h1 className={`text-xl font-extrabold text-memora-dark tracking-tight ${titleClassName || ''}`}>{title}</h1>
        {subtitle && <p className="text-xs text-memora-text-muted mt-0.5">{subtitle}</p>}
      </div>

      {/* Gamification Stats in Header */}
      <div className="flex items-center gap-3">
        {/* Streak Pill */}
        <div className="flex items-center gap-1.5 px-3 py-1.5 bg-orange-50/90 border border-orange-200/70 rounded-full text-orange-700 shadow-sm">
          <Flame className="w-4 h-4 fill-orange-500 text-orange-500 animate-pulse" />
          <span className="text-xs font-bold tracking-tight">
            {user?.streak ?? 0} {user?.streak === 1 ? 'day' : 'days'}
          </span>
        </div>

        {/* XP Pill */}
        <div className="flex items-center gap-1.5 px-3 py-1.5 bg-[#EAF2DE] border border-[#D5E6BE] rounded-full text-[#425E16] shadow-sm">
          <Sparkles className="w-4 h-4 text-memora-green" />
          <span className="text-xs font-bold tracking-tight">{user?.xp ?? 0} XP</span>
        </div>

        {/* Level Badge */}
        <div className="hidden sm:flex items-center gap-1 px-2.5 py-1.5 bg-stone-100 border border-stone-200 rounded-full text-stone-700 text-xs font-bold">
          <span>{user?.currentLevel || 'Unplaced'}</span>
        </div>

        {/* Diagnostic assessment trigger if not placed */}
        {(!user?.currentLevel || user?.currentLevel === null) && (
          <Link
            to="/assessment"
            className="hidden md:inline-flex items-center gap-1.5 text-xs font-bold bg-memora-green text-white px-3 py-1.5 rounded-full hover:bg-memora-green-hover transition shadow-sm"
          >
            <Compass className="w-3.5 h-3.5" />
            <span>Take Placement</span>
          </Link>
        )}
      </div>
    </header>
  );
};

export default TopHeader;
