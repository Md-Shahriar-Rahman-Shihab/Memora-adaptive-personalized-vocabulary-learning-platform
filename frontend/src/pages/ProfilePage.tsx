import React, { useEffect, useState } from 'react';
import {
  User as UserIcon,
  Flame,
  Sparkles,
  Award,
  BookOpen,
  CheckCircle2,
  Calendar,
  ChevronLeft,
  ChevronRight,
  TrendingUp,
} from 'lucide-react';
import { profileApi } from '../api/profileApi';
import { useAuth } from '../context/AuthContext';
import { LearnerProfileResponse, XpTransactionResponse } from '../types/gamification';
import { AppShell } from '../components/layout/AppShell';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { LoadingSpinner } from '../components/ui/LoadingSpinner';

export const ProfilePage: React.FC = () => {
  const { user } = useAuth();
  const [profile, setProfile] = useState<LearnerProfileResponse | null>(null);
  const [history, setHistory] = useState<XpTransactionResponse[]>([]);
  const [page, setPage] = useState<number>(0);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [isHistoryLoading, setIsHistoryLoading] = useState<boolean>(false);

  useEffect(() => {
    const fetchProfile = async () => {
      setIsLoading(true);
      try {
        const res = await profileApi.getProfile();
        if (res.success && res.data) {
          setProfile(res.data);
        }
      } finally {
        setIsLoading(false);
      }
    };

    fetchProfile();
  }, []);

  useEffect(() => {
    const fetchXpHistory = async () => {
      setIsHistoryLoading(true);
      try {
        const res = await profileApi.getXpHistory(page, 10);
        if (res.success && res.data) {
          setHistory(res.data);
        }
      } finally {
        setIsHistoryLoading(false);
      }
    };

    fetchXpHistory();
  }, [page]);

  if (isLoading) {
    return (
      <AppShell title="Learner Profile" subtitle="Loading profile details...">
        <div className="py-20 flex justify-center">
          <LoadingSpinner size="lg" label="Retrieving learner record..." />
        </div>
      </AppShell>
    );
  }

  const name = profile?.name || user?.name || 'Learner';
  const email = profile?.email || user?.email || '';
  const level = profile?.level || user?.currentLevel || 'Unplaced';

  return (
    <AppShell title="Learner Profile" subtitle="Manage your profile, statistics, and XP ledger">
      <div className="max-w-4xl mx-auto space-y-8">
        {/* Profile Header Card */}
        <Card variant="default" className="p-8 shadow-card flex flex-col sm:flex-row items-start sm:items-center justify-between gap-6">
          <div className="flex items-center gap-5">
            <div className="w-18 h-18 sm:w-20 sm:h-20 rounded-3xl bg-memora-green text-white flex items-center justify-center font-black text-2xl shadow-glow">
              {name.charAt(0).toUpperCase()}
            </div>
            <div>
              <div className="flex items-center gap-2 mb-1">
                <h2 className="text-2xl font-extrabold text-memora-dark tracking-tight">{name}</h2>
                <Badge variant="green" size="sm">
                  {level}
                </Badge>
              </div>
              <p className="text-xs text-memora-text-muted">{email}</p>
              <div className="flex items-center gap-4 mt-3 text-xs text-stone-600 font-semibold">
                <span className="flex items-center gap-1">
                  <Flame className="w-4 h-4 fill-orange-500 text-orange-500" />
                  {profile?.currentStreak ?? 0} Day Streak (Best: {profile?.longestStreak ?? 0}d)
                </span>
                <span className="flex items-center gap-1">
                  <Sparkles className="w-4 h-4 text-memora-green" />
                  {(profile?.xp ?? 0).toLocaleString()} Total XP
                </span>
              </div>
            </div>
          </div>
        </Card>

        {/* Activity & Performance Counters Grid */}
        <div>
          <h3 className="text-xs font-bold uppercase tracking-wider text-memora-dark mb-3 px-1">
            Learning Activity Metrics
          </h3>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
            <Card variant="subtle" className="p-5">
              <span className="text-xs font-bold text-memora-text-muted uppercase tracking-wider block mb-1">
                Quizzes Finished
              </span>
              <span className="text-2xl font-black text-memora-dark">
                {profile?.quizzesCompleted ?? 0}
              </span>
            </Card>

            <Card variant="subtle" className="p-5">
              <span className="text-xs font-bold text-memora-text-muted uppercase tracking-wider block mb-1">
                Reviews Done
              </span>
              <span className="text-2xl font-black text-memora-dark">
                {profile?.reviewsCompleted ?? 0}
              </span>
            </Card>

            <Card variant="subtle" className="p-5">
              <span className="text-xs font-bold text-memora-text-muted uppercase tracking-wider block mb-1">
                Lessons Finished
              </span>
              <span className="text-2xl font-black text-memora-dark">
                {profile?.wordsLearned ?? 0}
              </span>
            </Card>

            <Card variant="subtle" className="p-5">
              <span className="text-xs font-bold text-memora-text-muted uppercase tracking-wider block mb-1">
                Overall Accuracy
              </span>
              <span className="text-2xl font-black text-memora-dark">
                {Math.round(profile?.accuracy ?? 0)}%
              </span>
            </Card>
          </div>
        </div>

        {/* Immutable XP Audit History Ledger */}
        <Card variant="default" className="p-6 md:p-8 shadow-card space-y-4">
          <div className="flex items-center justify-between pb-3 border-b border-black/[0.06]">
            <div>
              <h3 className="text-lg font-bold text-memora-dark">XP Transaction Ledger</h3>
              <p className="text-xs text-memora-text-muted">
                Audit trail recording every XP award generated by the reward strategy system
              </p>
            </div>
            <span className="text-xs font-bold text-memora-green bg-memora-green-light px-3 py-1 rounded-full">
              Page {page + 1}
            </span>
          </div>

          {isHistoryLoading ? (
            <div className="py-8 flex justify-center">
              <LoadingSpinner size="sm" label="Loading transactions..." />
            </div>
          ) : history.length > 0 ? (
            <div className="divide-y divide-black/[0.04]">
              {history.map((tx) => {
                const activityName = (tx.activityType || tx.sourceActivity || 'ACTIVITY').toString().replace(/_/g, ' ');
                const earned = tx.amount ?? tx.xpEarned ?? 0;
                const balance = tx.balanceAfter ?? tx.resultingTotalXp ?? 0;

                return (
                  <div key={tx.id} className="py-3 flex items-center justify-between gap-4 hover:bg-stone-50/60 rounded-xl px-2 -mx-2 transition-colors">
                    <div className="space-y-0.5">
                      <p className="text-sm font-bold text-memora-dark">{tx.description}</p>
                      <div className="flex items-center gap-2 text-xs text-memora-text-muted">
                        <Badge variant="neutral" size="sm">
                          {activityName}
                        </Badge>
                        <span>• {new Date(tx.createdAt).toLocaleDateString()}</span>
                      </div>
                    </div>

                    <div className="text-right shrink-0">
                      <span className="text-sm font-black text-memora-green block">
                        +{earned} XP
                      </span>
                      <span className="text-[11px] text-stone-400">
                        Balance: {balance}
                      </span>
                    </div>
                  </div>
                );
              })}
            </div>
          ) : (
            <div className="py-8 text-center text-xs text-memora-text-muted">
              No transactions on this page.
            </div>
          )}

          {/* Pagination Controls */}
          <div className="flex items-center justify-between pt-4 border-t border-black/[0.06]">
            <Button
              variant="outline"
              size="sm"
              onClick={() => setPage((p) => Math.max(0, p - 1))}
              disabled={page === 0 || isHistoryLoading}
              leftIcon={<ChevronLeft className="w-4 h-4" />}
            >
              Previous
            </Button>
            <span className="text-xs text-memora-text-muted font-semibold">Page {page + 1}</span>
            <Button
              variant="outline"
              size="sm"
              onClick={() => setPage((p) => p + 1)}
              disabled={history.length < 10 || isHistoryLoading}
              rightIcon={<ChevronRight className="w-4 h-4" />}
            >
              Next
            </Button>
          </div>
        </Card>
      </div>
    </AppShell>
  );
};

export default ProfilePage;
