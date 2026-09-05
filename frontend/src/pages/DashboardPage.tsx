import React, { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import {
  Flame,
  Sparkles,
  BookOpen,
  Award,
  ArrowRight,
  AlertTriangle,
  RotateCw,
  CheckCircle2,
  Clock,
  HelpCircle,
  TrendingUp,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { profileApi } from '../api/profileApi';
import { learningPathApi } from '../api/learningPathApi';
import { memoryApi } from '../api/memoryApi';
import { LearnerProfileResponse } from '../types/gamification';
import { TodayLearningPathResponse } from '../types/learningPath';
import { MemoryWordResponse } from '../types/memory';
import { AppShell } from '../components/layout/AppShell';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { ProgressBar } from '../components/ui/ProgressBar';
import { LoadingSpinner } from '../components/ui/LoadingSpinner';

export const DashboardPage: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [profile, setProfile] = useState<LearnerProfileResponse | null>(null);
  const [todayPath, setTodayPath] = useState<TodayLearningPathResponse | null>(null);
  const [weakWords, setWeakWords] = useState<MemoryWordResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  // Time-based greeting
  const getGreeting = () => {
    const hour = new Date().getHours();
    if (hour < 12) return 'Good morning';
    if (hour < 18) return 'Good afternoon';
    return 'Good evening';
  };

  useEffect(() => {
    const fetchDashboardData = async () => {
      setIsLoading(true);
      try {
        const [profileRes, pathRes, weakRes] = await Promise.allSettled([
          profileApi.getProfile(),
          learningPathApi.getTodayPath(),
          memoryApi.getWeakWords(),
        ]);

        if (profileRes.status === 'fulfilled' && profileRes.value.success) {
          setProfile(profileRes.value.data);
        }
        if (pathRes.status === 'fulfilled' && pathRes.value.success) {
          setTodayPath(pathRes.value.data);
        }
        if (weakRes.status === 'fulfilled' && weakRes.value.success) {
          setWeakWords(weakRes.value.data);
        }
      } finally {
        setIsLoading(false);
      }
    };

    fetchDashboardData();
  }, []);

  if (isLoading) {
    return (
      <AppShell title="Dashboard" subtitle="Loading your learning workspace...">
        <div className="py-20 flex justify-center">
          <LoadingSpinner size="lg" label="Synchronizing your adaptive memory state..." />
        </div>
      </AppShell>
    );
  }

  const displayName = profile?.name || user?.name || 'Learner';
  const levelText = profile?.level || user?.currentLevel || 'Unassessed';
  const totalXp = profile?.xp ?? user?.xp ?? 0;
  const streak = profile?.currentStreak ?? user?.streak ?? 0;
  const wordsLearned = profile?.wordsLearned ?? 0;

  return (
    <AppShell
      title={`${getGreeting()}, ${displayName.split(' ')[0]}`}
      subtitle="Ready to continue your personalized learning journey?"
    >
      <div className="space-y-8">
        {/* Placement Assessment Banner if user has not taken assessment */}
        {(!user?.currentLevel || user?.currentLevel === null) && (
          <div className="p-6 rounded-3xl bg-gradient-to-r from-emerald-800 to-[#4B6D1A] text-white flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 shadow-lg">
            <div>
              <span className="text-xs font-bold uppercase tracking-wider text-emerald-200 block mb-1">
                Diagnostic Placement Available
              </span>
              <h3 className="text-lg sm:text-xl font-bold">Discover your CEFR vocabulary level</h3>
              <p className="text-xs sm:text-sm text-emerald-100/90 mt-1 max-w-xl">
                Take our 20-question diagnostic test to establish your baseline and generate an
                adaptive learning path tailored to you.
              </p>
            </div>
            <Button
              variant="primary"
              size="md"
              onClick={() => navigate('/assessment')}
              className="bg-white text-emerald-900 hover:bg-emerald-50 shrink-0 font-bold"
            >
              Start Placement Test
            </Button>
          </div>
        )}

        {/* 4 Primary Top Stat Cards */}
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
          {/* Card 1: Level */}
          <Card variant="default" className="p-5 hover-lift">
            <div className="flex items-center justify-between text-stone-400 mb-2">
              <span className="text-xs font-bold uppercase tracking-wider text-memora-text-muted">
                Current Level
              </span>
              <Award className="w-4 h-4 text-memora-green" />
            </div>
            <div className="flex items-baseline gap-2">
              <span className="text-2xl sm:text-3xl font-black text-memora-dark tracking-tight">
                {levelText}
              </span>
              <span className="text-xs text-memora-text-muted font-semibold">CEFR</span>
            </div>
          </Card>

          {/* Card 2: XP */}
          <Card variant="default" className="p-5">
            <div className="flex items-center justify-between text-stone-400 mb-2">
              <span className="text-xs font-bold uppercase tracking-wider text-memora-text-muted">
                Total XP
              </span>
              <Sparkles className="w-4 h-4 text-memora-green" />
            </div>
            <div className="flex items-baseline gap-2">
              <span className="text-2xl sm:text-3xl font-black text-memora-dark tracking-tight">
                {totalXp.toLocaleString()}
              </span>
              <span className="text-xs text-memora-text-muted font-semibold">Points</span>
            </div>
          </Card>

          {/* Card 3: Streak */}
          <Card variant="default" className="p-5">
            <div className="flex items-center justify-between text-stone-400 mb-2">
              <span className="text-xs font-bold uppercase tracking-wider text-memora-text-muted">
                Day Streak
              </span>
              <Flame className="w-4 h-4 text-orange-500 fill-orange-500" />
            </div>
            <div className="flex items-baseline gap-2">
              <span className="text-2xl sm:text-3xl font-black text-memora-dark tracking-tight">
                {streak}
              </span>
              <span className="text-xs text-memora-text-muted font-semibold">
                {streak === 1 ? 'day' : 'days'}
              </span>
            </div>
          </Card>

          {/* Card 4: Words Learned */}
          <Card variant="default" className="p-5">
            <div className="flex items-center justify-between text-stone-400 mb-2">
              <span className="text-xs font-bold uppercase tracking-wider text-memora-text-muted">
                Words Learned
              </span>
              <BookOpen className="w-4 h-4 text-memora-green" />
            </div>
            <div className="flex items-baseline gap-2">
              <span className="text-2xl sm:text-3xl font-black text-memora-dark tracking-tight">
                {wordsLearned}
              </span>
              <span className="text-xs text-memora-text-muted font-semibold">
                / {profile?.masteredWords ?? 0} Mastered
              </span>
            </div>
          </Card>
        </div>

        {/* Main Dashboard Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
          {/* Left Column: Today's Path + Memory Risk Explanation (7 cols) */}
          <div className="lg:col-span-7 space-y-8">
            {/* Today's Learning Path Card */}
            <Card variant="default" className="p-6 md:p-8 space-y-6">
              <div className="flex items-center justify-between flex-wrap gap-2">
                <div>
                  <h3 className="text-lg font-bold text-memora-dark tracking-tight">
                    Today's Learning Path
                  </h3>
                  <p className="text-xs text-memora-text-muted mt-0.5">
                    {todayPath
                      ? `Day ${todayPath.currentDay} • ${todayPath.completedItems} of ${todayPath.totalItems} completed`
                      : 'Personalized daily curriculum'}
                  </p>
                </div>

                <Button
                  variant="primary"
                  size="sm"
                  onClick={() => navigate('/learn-path')}
                  rightIcon={<ArrowRight className="w-4 h-4" />}
                >
                  Continue Learning
                </Button>
              </div>

              {/* Progress bar */}
              {todayPath && (
                <ProgressBar
                  value={todayPath.completedItems}
                  max={todayPath.totalItems || 10}
                  size="md"
                  showLabel
                />
              )}

              {/* Path Items List */}
              <div className="space-y-3 pt-2">
                {todayPath && todayPath.items && todayPath.items.length > 0 ? (
                  todayPath.items.slice(0, 4).map((item) => (
                    <div
                      key={item.id}
                      className="p-3.5 rounded-2xl bg-[#F8F8F5] border border-black/[0.04] flex items-center justify-between gap-3 hover:bg-stone-100/60 transition"
                    >
                      <div className="flex items-center gap-3 min-w-0">
                        <div className="w-8 h-8 rounded-xl bg-white border border-black/[0.06] flex items-center justify-center shrink-0">
                          {item.type === 'REVIEW' && (
                            <RotateCw className="w-4 h-4 text-blue-600" />
                          )}
                          {item.type === 'NEW_WORD' && (
                            <Sparkles className="w-4 h-4 text-emerald-600" />
                          )}
                          {item.type === 'QUIZ' && (
                            <HelpCircle className="w-4 h-4 text-purple-600" />
                          )}
                        </div>
                        <div className="min-w-0">
                          <p className="text-sm font-bold text-memora-dark truncate">
                            {item.word || (item.type === 'QUIZ' ? 'Consolidating Quiz' : 'Word Task')}
                          </p>
                          <p className="text-xs text-memora-text-muted truncate">
                            {item.notes || (item.meaning ? item.meaning : item.type)}
                          </p>
                        </div>
                      </div>

                      <div className="flex items-center gap-2 shrink-0">
                        {item.status === 'COMPLETED' ? (
                          <Badge variant="green" size="sm">
                            <CheckCircle2 className="w-3 h-3" /> Done
                          </Badge>
                        ) : (
                          <Badge variant={item.priority === 'HIGH' ? 'red' : 'neutral'} size="sm">
                            {item.status}
                          </Badge>
                        )}
                      </div>
                    </div>
                  ))
                ) : (
                  <div className="text-center py-6 text-xs text-memora-text-muted bg-stone-50 rounded-2xl">
                    No items pending today. Click "Continue Learning" to generate today's curriculum.
                  </div>
                )}
              </div>
            </Card>

            {/* Why You're Seeing These Words (Core Memora USP) */}
            <Card variant="default" className="p-6 md:p-8 space-y-4">
              <div className="flex items-center gap-2.5 text-memora-dark">
                <div className="w-8 h-8 rounded-xl bg-amber-100 flex items-center justify-center text-amber-700">
                  <AlertTriangle className="w-4 h-4" />
                </div>
                <div>
                  <h3 className="text-base font-bold">Why you're seeing these words</h3>
                  <p className="text-xs text-memora-text-muted">
                    Memora detected that these words have high forgetting risk or need reinforcement
                  </p>
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 pt-2">
                {weakWords.length > 0 ? (
                  weakWords.slice(0, 4).map((w) => (
                    <div
                      key={w.wordId}
                      className="p-4 rounded-2xl bg-[#FBFBF9] border border-black/[0.05] space-y-2 hover:border-black/[0.12] transition"
                    >
                      <div className="flex items-center justify-between">
                        <span className="font-extrabold text-sm text-memora-dark">{w.word}</span>
                        <Badge
                          variant={
                            w.forgettingRisk === 'HIGH' || w.forgettingRisk === 'CRITICAL'
                              ? 'red'
                              : 'amber'
                          }
                          size="sm"
                        >
                          {w.forgettingRisk} RISK
                        </Badge>
                      </div>
                      <p className="text-xs text-memora-text-muted line-clamp-1">{w.meaning}</p>
                      <div className="flex items-center justify-between text-[11px] text-stone-400 pt-1 border-t border-black/[0.04]">
                        <span>Mastery: {Math.round(w.masteryScore)}%</span>
                        <span className="flex items-center gap-1 text-memora-green font-semibold">
                          <Clock className="w-3 h-3" />
                          Review Due
                        </span>
                      </div>
                    </div>
                  ))
                ) : (
                  <div className="col-span-2 py-6 text-center text-xs text-memora-text-muted">
                    Great job! All your studied vocabulary words are currently in strong memory states.
                  </div>
                )}
              </div>
            </Card>
          </div>

          {/* Right Column: Memory Map Widget + Achievements Preview + Quick Actions (5 cols) */}
          <div className="lg:col-span-5 space-y-8">
            {/* Memory Map Preview Widget */}
            <Card variant="subtle" className="p-6 space-y-4">
              <div className="flex items-center justify-between">
                <div>
                  <h4 className="text-sm font-bold text-memora-dark">Memory Network</h4>
                  <p className="text-[11px] text-memora-text-muted">Active vocabulary neural map</p>
                </div>
                <Badge variant="green" size="sm">
                  Live
                </Badge>
              </div>

              {/* Minimal Node representation */}
              <div className="h-44 bg-white rounded-2xl border border-black/[0.04] p-3 flex items-center justify-center relative overflow-hidden">
                <div className="w-12 h-12 rounded-full bg-memora-green text-white flex items-center justify-center font-bold text-xs shadow-glow z-10">
                  {levelText}
                </div>
                {/* Orbital nodes */}
                <div className="absolute top-4 left-6 px-2.5 py-0.5 bg-[#EAF2DE] text-[#425E16] rounded-full text-[10px] font-bold shadow-sm">
                  curious
                </div>
                <div className="absolute top-5 right-6 px-2.5 py-0.5 bg-[#FEF3E2] text-[#B45309] rounded-full text-[10px] font-bold shadow-sm">
                  meticulous
                </div>
                <div className="absolute bottom-5 left-8 px-2.5 py-0.5 bg-[#E6F0FA] text-[#1D4ED8] rounded-full text-[10px] font-bold shadow-sm">
                  scrutinize
                </div>
                <div className="absolute bottom-4 right-8 px-2.5 py-0.5 bg-[#EAF2DE] text-[#425E16] rounded-full text-[10px] font-bold shadow-sm">
                  lucid
                </div>

                {/* SVG connection lines */}
                <svg className="absolute inset-0 w-full h-full pointer-events-none stroke-stone-300 stroke-dashed [stroke-dasharray:3,3]">
                  <line x1="50%" y1="50%" x2="25%" y2="25%" />
                  <line x1="50%" y1="50%" x2="75%" y2="25%" />
                  <line x1="50%" y1="50%" x2="30%" y2="75%" />
                  <line x1="50%" y1="50%" x2="70%" y2="75%" />
                </svg>
              </div>

              <div className="flex items-center justify-between text-xs text-memora-text-muted pt-1">
                <span>{profile?.masteredWords ?? 0} Mastered</span>
                <span>{profile?.wordsLearned ?? 0} In Progress</span>
              </div>
            </Card>

            {/* Recent Badges Showcase */}
            <Card variant="default" className="p-6 space-y-4">
              <div className="flex items-center justify-between">
                <div>
                  <h4 className="text-sm font-bold text-memora-dark">Achievements</h4>
                  <p className="text-[11px] text-memora-text-muted">Recent milestones unlocked</p>
                </div>
                <Link
                  to="/achievements"
                  className="text-xs font-bold text-memora-green hover:underline"
                >
                  View all
                </Link>
              </div>

              <div className="space-y-2.5">
                {profile?.achievements && profile.achievements.length > 0 ? (
                  profile.achievements.slice(0, 3).map((ach) => (
                    <div
                      key={ach.code}
                      className="p-3 rounded-2xl bg-[#F8F8F5] border border-black/[0.04] flex items-center justify-between"
                    >
                      <div className="flex items-center gap-3">
                        <div className="w-8 h-8 rounded-xl bg-purple-100 text-purple-700 flex items-center justify-center shrink-0">
                          <Award className="w-4 h-4" />
                        </div>
                        <div>
                          <p className="text-xs font-bold text-memora-dark">{ach.title}</p>
                          <p className="text-[11px] text-memora-text-muted">{ach.description}</p>
                        </div>
                      </div>
                      <span className="text-[11px] font-bold text-memora-green shrink-0">
                        +{ach.xpBonus} XP
                      </span>
                    </div>
                  ))
                ) : (
                  <div className="text-center py-6 text-xs text-memora-text-muted bg-stone-50 rounded-2xl">
                    Complete your first lesson or quiz to unlock achievements!
                  </div>
                )}
              </div>
            </Card>

            {/* Quick Navigation Actions */}
            <div className="grid grid-cols-2 gap-3">
              <Button
                variant="outline"
                size="md"
                onClick={() => navigate('/review')}
                leftIcon={<RotateCw className="w-4 h-4 text-memora-green" />}
                className="justify-center text-xs py-3"
              >
                Review Words
              </Button>
              <Button
                variant="outline"
                size="md"
                onClick={() => navigate('/leaderboard')}
                leftIcon={<TrendingUp className="w-4 h-4 text-memora-green" />}
                className="justify-center text-xs py-3"
              >
                Leaderboard
              </Button>
            </div>
          </div>
        </div>
      </div>
    </AppShell>
  );
};

export default DashboardPage;
