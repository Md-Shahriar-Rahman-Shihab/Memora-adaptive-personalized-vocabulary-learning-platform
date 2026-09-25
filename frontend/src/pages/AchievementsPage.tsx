import React, { useEffect, useState } from 'react';
import { Award, Lock, CheckCircle2, Sparkles, Flame, BookOpen, Compass } from 'lucide-react';
import { achievementApi } from '../api/achievementApi';
import { profileApi } from '../api/profileApi';
import { Achievement, UserAchievementResponse, BadgeCategory } from '../types/gamification';
import { AppShell } from '../components/layout/AppShell';
import { Card } from '../components/ui/Card';
import { Badge } from '../components/ui/Badge';
import { LoadingSpinner } from '../components/ui/LoadingSpinner';

export const AchievementsPage: React.FC = () => {
  const [catalog, setCatalog] = useState<Achievement[]>([]);
  const [unlocked, setUnlocked] = useState<UserAchievementResponse[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<string>('ALL');
  const [isLoading, setIsLoading] = useState<boolean>(true);

  useEffect(() => {
    const fetchAchievements = async () => {
      setIsLoading(true);
      try {
        const [catalogRes, userRes] = await Promise.allSettled([
          achievementApi.getAllAchievements(),
          profileApi.getAchievements(),
        ]);

        if (catalogRes.status === 'fulfilled' && catalogRes.value.success) {
          setCatalog(catalogRes.value.data);
        }
        if (userRes.status === 'fulfilled' && userRes.value.success) {
          setUnlocked(userRes.value.data);
        }
      } finally {
        setIsLoading(false);
      }
    };

    fetchAchievements();
  }, []);

  const getCategory = (badge: Achievement): BadgeCategory => {
    if (badge.badgeCategory) return badge.badgeCategory;
    const code = badge.code;
    if (code === 'STREAK_7' || code === 'STREAK_30') return 'STREAK';
    if (code === 'MEMORY_MASTER' || code === 'WORD_STARTER') return 'MASTERY';
    if (code === 'FIRST_QUIZ' || code === 'PERFECT_SCORE' || code === 'QUIZ_MASTER') return 'QUIZ';
    if (code === 'VOCABULARY_EXPLORER' || code === 'CENTURY') return 'EXPLORATION';
    return 'MILESTONE';
  };

  const unlockedCodes = new Set(
    unlocked
      .filter((u) => u.unlocked !== false)
      .map((u) => u.code)
  );

  const filteredCatalog =
    selectedCategory === 'ALL'
      ? catalog
      : catalog.filter((a) => getCategory(a) === selectedCategory);

  const categories = ['ALL', 'STREAK', 'MASTERY', 'QUIZ', 'EXPLORATION', 'MILESTONE'];

  return (
    <AppShell
      title="Achievements & Badges"
      subtitle={`${unlockedCodes.size} of ${catalog.length} unlocked • Earn XP by reaching learning milestones`}
    >
      <div className="max-w-5xl mx-auto space-y-8">
        {/* Category Tabs */}
        <div className="flex items-center gap-2 overflow-x-auto pb-2 scrollbar-none">
          {categories.map((cat) => (
            <button
              key={cat}
              onClick={() => setSelectedCategory(cat)}
              className={`px-4 py-2 rounded-full text-xs font-bold transition whitespace-nowrap ${
                selectedCategory === cat
                  ? 'bg-memora-dark text-white shadow-sm'
                  : 'bg-white text-stone-600 border border-black/[0.06] hover:bg-stone-50'
              }`}
            >
              {cat}
            </button>
          ))}
        </div>

        {isLoading ? (
          <div className="py-20 flex justify-center">
            <LoadingSpinner size="lg" label="Loading milestone achievements..." />
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
            {filteredCatalog.map((badge) => {
              const isUnlocked = unlockedCodes.has(badge.code);
              const userBadge = unlocked.find((u) => u.code === badge.code);
              const badgeTitle = badge.title || badge.name || badge.code;
              const badgeXp = badge.xpBonus ?? userBadge?.xpBonus ?? 25;
              const category = getCategory(badge);
              const unlockedDate = userBadge?.unlockedAt || userBadge?.earnedAt;

              return (
                <Card
                  key={badge.code}
                  variant={isUnlocked ? 'default' : 'subtle'}
                  className={`p-6 flex flex-col justify-between space-y-4 border transition-all ${
                    isUnlocked
                      ? 'border-emerald-200/80 bg-gradient-to-b from-white to-[#F9FCF5] shadow-card hover-lift animate-badge-shine'
                      : 'border-black/[0.04] opacity-75 grayscale hover:grayscale-0 hover:opacity-100'
                  }`}
                >
                  <div className="flex items-start justify-between">
                    <div
                      className={`w-12 h-12 rounded-2xl flex items-center justify-center ${
                        isUnlocked
                          ? 'bg-purple-100 text-purple-700 shadow-sm'
                          : 'bg-stone-200 text-stone-500'
                      }`}
                    >
                      {category === 'STREAK' ? (
                        <Flame className="w-6 h-6" />
                      ) : category === 'MASTERY' ? (
                        <Award className="w-6 h-6" />
                      ) : category === 'EXPLORATION' ? (
                        <Compass className="w-6 h-6" />
                      ) : category === 'QUIZ' ? (
                        <Sparkles className="w-6 h-6" />
                      ) : (
                        <BookOpen className="w-6 h-6" />
                      )}
                    </div>

                    <div className="flex items-center gap-1.5">
                      {isUnlocked ? (
                        <Badge variant="green" size="sm">
                          <CheckCircle2 className="w-3 h-3" /> Unlocked
                        </Badge>
                      ) : (
                        <Badge variant="neutral" size="sm">
                          <Lock className="w-3 h-3 text-stone-400" /> Locked
                        </Badge>
                      )}
                    </div>
                  </div>

                  <div>
                    <h4 className="text-base font-extrabold text-memora-dark tracking-tight mb-1">
                      {badgeTitle}
                    </h4>
                    <p className="text-xs text-memora-text-muted leading-relaxed">
                      {badge.description}
                    </p>
                  </div>

                  <div className="pt-3 border-t border-black/[0.04] flex items-center justify-between text-xs">
                    <span className="font-bold text-memora-green">+{badgeXp} XP</span>
                    {isUnlocked && unlockedDate && (
                      <span className="text-[11px] text-stone-400">
                        Unlocked {new Date(unlockedDate).toLocaleDateString()}
                      </span>
                    )}
                  </div>
                </Card>
              );
            })}
          </div>
        )}
      </div>
    </AppShell>
  );
};

export default AchievementsPage;
