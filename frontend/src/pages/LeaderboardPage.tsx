import React, { useEffect, useState } from 'react';
import { Crown, Flame, Sparkles, Trophy, Medal } from 'lucide-react';
import { leaderboardApi } from '../api/leaderboardApi';
import { useAuth } from '../context/AuthContext';
import { LeaderboardEntryResponse } from '../types/gamification';
import { AppShell } from '../components/layout/AppShell';
import { Card } from '../components/ui/Card';
import { Badge } from '../components/ui/Badge';
import { LoadingSpinner } from '../components/ui/LoadingSpinner';

export const LeaderboardPage: React.FC = () => {
  const { user } = useAuth();
  const [entries, setEntries] = useState<LeaderboardEntryResponse[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  useEffect(() => {
    const fetchLeaderboard = async () => {
      setIsLoading(true);
      try {
        const res = await leaderboardApi.getLeaderboard(20);
        if (res.success && res.data) {
          setEntries(res.data);
        }
      } finally {
        setIsLoading(false);
      }
    };

    fetchLeaderboard();
  }, []);

  return (
    <AppShell
      title="Global Leaderboard"
      subtitle="Top learners ranked deterministically by total XP and consecutive streaks"
    >
      <div className="max-w-4xl mx-auto space-y-8">
        {/* Podium Banner for Top 3 */}
        {!isLoading && entries.length >= 3 && (
          <div className="grid grid-cols-3 gap-4 pt-4 items-end">
            {/* 2nd Place */}
            <Card
              variant="default"
              className="p-5 text-center flex flex-col items-center justify-between min-h-[200px] border-stone-200"
            >
              <div className="w-10 h-10 rounded-full bg-stone-100 flex items-center justify-center font-extrabold text-stone-600 text-sm mb-2">
                #2
              </div>
              <div>
                <h4 className="font-extrabold text-sm text-memora-dark truncate max-w-[120px]">
                  {entries[1].displayName}
                </h4>
                <p className="text-xs font-bold text-memora-green mt-0.5">
                  {entries[1].totalXp.toLocaleString()} XP
                </p>
              </div>
              <div className="flex items-center gap-1 text-xs text-orange-600 font-bold mt-2">
                <Flame className="w-3.5 h-3.5 fill-current" />
                {entries[1].currentStreak}d
              </div>
            </Card>

            {/* 1st Place */}
            <Card
              variant="default"
              className="p-6 text-center flex flex-col items-center justify-between min-h-[230px] border-amber-300 bg-gradient-to-b from-amber-50/40 to-white shadow-card hover-lift"
            >
              <div className="w-12 h-12 rounded-full bg-amber-100 flex items-center justify-center text-amber-600 mb-2 shadow-sm">
                <Crown className="w-6 h-6 fill-current" />
              </div>
              <div>
                <span className="text-[10px] font-black uppercase tracking-wider text-amber-700 bg-amber-100/80 px-2 py-0.5 rounded-full inline-block mb-1">
                  Leader
                </span>
                <h4 className="font-extrabold text-base text-memora-dark truncate max-w-[140px]">
                  {entries[0].displayName}
                </h4>
                <p className="text-sm font-extrabold text-memora-green mt-0.5">
                  {entries[0].totalXp.toLocaleString()} XP
                </p>
              </div>
              <div className="flex items-center gap-1 text-xs text-orange-600 font-bold mt-2">
                <Flame className="w-3.5 h-3.5 fill-current" />
                {entries[0].currentStreak}d streak
              </div>
            </Card>

            {/* 3rd Place */}
            <Card
              variant="default"
              className="p-5 text-center flex flex-col items-center justify-between min-h-[190px] border-stone-200"
            >
              <div className="w-10 h-10 rounded-full bg-stone-100 flex items-center justify-center font-extrabold text-amber-700 text-sm mb-2">
                #3
              </div>
              <div>
                <h4 className="font-extrabold text-sm text-memora-dark truncate max-w-[120px]">
                  {entries[2].displayName}
                </h4>
                <p className="text-xs font-bold text-memora-green mt-0.5">
                  {entries[2].totalXp.toLocaleString()} XP
                </p>
              </div>
              <div className="flex items-center gap-1 text-xs text-orange-600 font-bold mt-2">
                <Flame className="w-3.5 h-3.5 fill-current" />
                {entries[2].currentStreak}d
              </div>
            </Card>
          </div>
        )}

        {/* Leaderboard Table Card */}
        <Card variant="default" className="p-6 md:p-8 shadow-card">
          <div className="flex items-center justify-between mb-6 pb-4 border-b border-black/[0.06]">
            <div>
              <h3 className="text-lg font-bold text-memora-dark">Rankings</h3>
              <p className="text-xs text-memora-text-muted mt-0.5">
                Calculated strictly from server-audited XP transactions
              </p>
            </div>
            <div className="flex items-center gap-1 text-xs font-bold text-memora-text-muted bg-stone-100 px-3 py-1 rounded-full">
              <Trophy className="w-3.5 h-3.5 text-stone-500" />
              <span>Global Top 20</span>
            </div>
          </div>

          {isLoading ? (
            <div className="py-16 flex justify-center">
              <LoadingSpinner size="lg" label="Sorting global leaderboard..." />
            </div>
          ) : (
            <div className="divide-y divide-black/[0.04]">
              {entries.map((entry) => {
                const isCurrentUser = user && user.id === entry.userId;

                return (
                  <div
                    key={entry.userId}
                    className={`py-3.5 px-4 rounded-2xl flex items-center justify-between transition-all duration-200 ${
                      isCurrentUser
                        ? 'bg-memora-green-light border border-memora-green/30'
                        : 'hover:bg-stone-50/80'
                    }`}
                  >
                    <div className="flex items-center gap-4 min-w-0">
                      {/* Rank badge */}
                      <span
                        className={`w-7 text-center font-extrabold text-sm shrink-0 ${
                          entry.rank === 1
                            ? 'text-amber-500'
                            : entry.rank === 2
                            ? 'text-stone-400'
                            : entry.rank === 3
                            ? 'text-amber-700'
                            : 'text-stone-400'
                        }`}
                      >
                        #{entry.rank}
                      </span>

                      {/* Avatar initial */}
                      <div className="w-9 h-9 rounded-xl bg-white border border-black/[0.06] flex items-center justify-center font-extrabold text-xs text-memora-dark shrink-0 shadow-sm">
                        {entry.displayName.charAt(0).toUpperCase()}
                      </div>

                      <div className="min-w-0">
                        <div className="flex items-center gap-2">
                          <span className="text-sm font-bold text-memora-dark truncate">
                            {entry.displayName}
                          </span>
                          {isCurrentUser && (
                            <Badge variant="green" size="sm">
                              You
                            </Badge>
                          )}
                        </div>
                      </div>
                    </div>

                    <div className="flex items-center gap-6 shrink-0 text-right">
                      <div className="flex items-center gap-1.5 text-xs font-bold text-orange-600 bg-orange-50/80 px-2.5 py-1 rounded-full">
                        <Flame className="w-3.5 h-3.5 fill-current" />
                        <span>{entry.currentStreak}d</span>
                      </div>

                      <div className="min-w-[80px]">
                        <span className="text-sm font-extrabold text-memora-dark">
                          {entry.totalXp.toLocaleString()}
                        </span>
                        <span className="text-[10px] text-memora-text-muted block">XP</span>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </Card>
      </div>
    </AppShell>
  );
};

export default LeaderboardPage;
