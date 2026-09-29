import React, { useEffect, useState, useMemo } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import {
  Compass,
  RotateCw,
  Sparkles,
  HelpCircle,
  CheckCircle2,
  Play,
  RefreshCw,
  BookOpen,
  ArrowRight,
} from 'lucide-react';
import { learningPathApi } from '../api/learningPathApi';
import { TodayLearningPathResponse, LearningPathItemResponse, LearningItemCompletionResponse } from '../types/learningPath';
import { useToast } from '../context/ToastContext';
import { useAuth } from '../context/AuthContext';
import { AppShell } from '../components/layout/AppShell';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { ProgressBar } from '../components/ui/ProgressBar';
import { LoadingSpinner } from '../components/ui/LoadingSpinner';
import { WordStudyModal } from '../components/vocabulary/WordStudyModal';

export const LearningPathPage: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const { addToast } = useToast();
  const { refreshUser } = useAuth();

  const [path, setPath] = useState<TodayLearningPathResponse | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [isRegenerating, setIsRegenerating] = useState<boolean>(false);
  const [isAdvancing, setIsAdvancing] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  // Word Study Experience Modal State
  const [studyingItem, setStudyingItem] = useState<LearningPathItemResponse | null>(null);

  const fetchTodayPath = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const res = await learningPathApi.getTodayPath();
      if (res.success && res.data) {
        setPath(res.data);
      }
    } catch (err: any) {
      setError(
        err?.response?.data?.message || 'Could not load learning path. You may need to initialize it.'
      );
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchTodayPath();
  }, []);

  // Sync with ?study=itemId search param for persistence on refresh
  useEffect(() => {
    const studyId = searchParams.get('study');
    if (studyId && path?.items) {
      const targetItem = path.items.find((it) => it.id === Number(studyId));
      if (targetItem && targetItem.type === 'NEW_WORD') {
        setStudyingItem(targetItem);
      }
    }
  }, [path, searchParams]);

  const handleStartPath = async () => {
    setIsLoading(true);
    try {
      await learningPathApi.startPath();
      await fetchTodayPath();
      addToast({
        type: 'success',
        title: 'Learning Path Initiated',
        message: "Today's personalized curriculum has been generated.",
      });
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to start learning path.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleStartItem = async (itemId: number) => {
    try {
      const res = await learningPathApi.startItem(itemId);
      if (res.success) {
        setPath((prev) => {
          if (!prev) return prev;
          return {
            ...prev,
            items: prev.items.map((it) =>
              it.id === itemId ? { ...it, status: 'IN_PROGRESS' } : it
            ),
          };
        });
      }
    } catch {
      // Non-blocking
    }
  };

  const handleOpenStudy = (item: LearningPathItemResponse) => {
    if (item.status === 'PENDING') {
      handleStartItem(item.id);
    }
    const targetWord = item.word || '';
    if (targetWord) {
      navigate(`/word/${encodeURIComponent(targetWord)}?pathItemId=${item.id}&from=learning-path`);
    } else {
      setStudyingItem(item);
    }
  };

  const handleCloseStudy = () => {
    setStudyingItem(null);
    const newParams = new URLSearchParams(searchParams);
    newParams.delete('study');
    setSearchParams(newParams, { replace: true });
  };

  const handleWordStudyCompleted = async (
    res: LearningItemCompletionResponse,
    completedItemId: number
  ) => {
    // 1. Update local path state
    setPath((prev) => {
      if (!prev) return prev;
      return {
        ...prev,
        completedItems: Math.min(prev.totalItems, prev.completedItems + 1),
        items: prev.items.map((it) =>
          it.id === completedItemId ? { ...it, status: 'COMPLETED' } : it
        ),
      };
    });

    // 2. Add toast notification with real backend XP
    addToast({
      type: 'xp',
      title: 'Word Mastered!',
      message: res.message || 'Great job completing your word study.',
      xpAmount: res.xpEarned ?? 15,
    });

    // 3. Refresh user profile (streaks, total XP, achievements)
    await refreshUser();
    // 4. Silently refresh path to ensure full sync
    try {
      const updated = await learningPathApi.getTodayPath();
      if (updated.success && updated.data) {
        setPath(updated.data);
      }
    } catch {
      // Keep optimistic state
    }
  };

  // Find next pending NEW_WORD item for the Next Word button
  const nextStudyItem = useMemo(() => {
    if (!studyingItem || !path?.items) return null;
    return (
      path.items.find(
        (it) => it.type === 'NEW_WORD' && it.status !== 'COMPLETED' && it.id !== studyingItem.id
      ) || null
    );
  }, [path?.items, studyingItem]);

  const handleNavigateToNextItem = (nextItem: LearningPathItemResponse) => {
    handleOpenStudy(nextItem);
  };

  const handleItemClick = (item: LearningPathItemResponse) => {
    if (item.type === 'QUIZ') {
      navigate(item.quizId ? `/quiz/${item.quizId}` : '/quiz');
      return;
    }

    if (item.type === 'REVIEW') {
      navigate('/review');
      return;
    }

    if (item.type === 'NEW_WORD') {
      handleOpenStudy(item);
      return;
    }
  };

  const handleRegenerate = async () => {
    setIsRegenerating(true);
    try {
      const res = await learningPathApi.regeneratePath();
      if (res.success) {
        addToast({
          type: 'info',
          title: 'Curriculum Updated',
          message: 'Your learning path was dynamically balanced based on your latest retention.',
        });
        await fetchTodayPath();
      }
    } catch (err: any) {
      addToast({
        type: 'error',
        title: 'Error',
        message: err?.response?.data?.message || 'Failed to regenerate learning path.',
      });
    } finally {
      setIsRegenerating(false);
    }
  };

  const handleAdvanceDay = async () => {
    setIsAdvancing(true);
    try {
      const res = await learningPathApi.advanceToNextDay();
      if (res.success) {
        addToast({
          type: 'success',
          title: `Welcome to Day ${res.data?.currentDay || (path ? path.currentDay + 1 : '')}!`,
          message: 'New vocabulary and adaptive lessons generated for you.',
        });
        await fetchTodayPath();
      }
    } catch (err: any) {
      addToast({
        type: 'error',
        title: 'Error',
        message: err?.response?.data?.message || 'Could not advance to next day.',
      });
    } finally {
      setIsAdvancing(false);
    }
  };

  if (isLoading) {
    return (
      <AppShell title="Learning Path" subtitle="Loading your personalized curriculum...">
        <div className="py-20 flex justify-center">
          <LoadingSpinner size="lg" label="Balancing review and discovery items..." />
        </div>
      </AppShell>
    );
  }

  if (error || !path) {
    return (
      <AppShell title="Learning Path" subtitle="Curriculum status">
        <div className="max-w-xl mx-auto py-12 text-center">
          <Card variant="default" className="p-8 space-y-6">
            <div className="w-14 h-14 rounded-3xl bg-emerald-100 text-memora-green flex items-center justify-center mx-auto">
              <Compass className="w-7 h-7" />
            </div>
            <div>
              <h3 className="text-xl font-bold text-memora-dark">No Active Learning Path</h3>
              <p className="text-xs sm:text-sm text-memora-text-muted mt-1 max-w-md mx-auto">
                Generate your personalized daily vocabulary path tailored to your CEFR level and
                memory retention metrics.
              </p>
            </div>
            <Button
              variant="primary"
              size="lg"
              onClick={handleStartPath}
              className="shadow-md"
            >
              Generate Today's Path
            </Button>
          </Card>
        </div>
      </AppShell>
    );
  }

  return (
    <AppShell
      title="Your Learning Path"
      subtitle="Built around what you know — and what you're about to forget."
    >
      <div className="max-w-4xl mx-auto space-y-8">
        {/* Progress & Summary Header Card */}
        <Card variant="default" className="p-6 md:p-8 space-y-5">
          <div className="flex flex-wrap items-center justify-between gap-4">
            <div>
              <div className="flex items-center gap-2 mb-1">
                <span className="text-xs font-bold uppercase tracking-wider text-memora-green">
                  Day {path.currentDay} Curriculum
                </span>
                <Badge variant="neutral" size="sm">
                  CEFR {path.targetLevel}
                </Badge>
              </div>
              <h2 className="text-2xl font-extrabold text-memora-dark tracking-tight">
                Today's Vocabulary Tasks
              </h2>
            </div>

            <Button
              variant="outline"
              size="sm"
              onClick={handleRegenerate}
              isLoading={isRegenerating}
              leftIcon={<RefreshCw className="w-3.5 h-3.5" />}
              className="text-xs"
            >
              Regenerate Path
            </Button>
          </div>

          <ProgressBar
            value={path.completedItems}
            max={path.totalItems || 10}
            size="md"
            showLabel
          />

          <div className="flex items-center justify-between text-xs text-memora-text-muted pt-1 border-t border-black/[0.04]">
            <span>
              {path.completedItems} of {path.totalItems} items finished
            </span>
            <span>
              {path.totalItems - path.completedItems} remaining today
            </span>
          </div>
        </Card>

        {/* Day Completion Banner */}
        {((path.totalItems > 0 && path.completedItems >= path.totalItems) ||
          path.items.some((it) => it.type === 'QUIZ' && it.status === 'COMPLETED')) && (
          <div className="p-6 rounded-3xl bg-gradient-to-r from-emerald-500/15 via-emerald-500/5 to-transparent border border-emerald-500/30 shadow-sm flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div className="space-y-1">
              <div className="flex items-center gap-2">
                <Sparkles className="w-5 h-5 text-memora-green" />
                <span className="font-extrabold text-memora-dark text-lg">
                  Day {path.currentDay} Curriculum Completed! 🎉
                </span>
              </div>
              <p className="text-xs sm:text-sm text-memora-text-muted">
                You've completed your daily lessons and retention quiz. Ready for brand new vocabulary and challenges?
              </p>
            </div>
            <Button
              variant="primary"
              size="md"
              isLoading={isAdvancing}
              onClick={handleAdvanceDay}
              rightIcon={<ArrowRight className="w-4 h-4" />}
              className="shrink-0 shadow-md"
            >
              Start Day {path.currentDay + 1} (New Learning)
            </Button>
          </div>
        )}

        {/* Learning Items Stack */}
        <div className="space-y-3">
          <h3 className="text-xs font-bold uppercase tracking-wider text-memora-dark px-1">
            Prioritized Daily Items
          </h3>

          {path.items && path.items.length > 0 ? (
            path.items.map((item) => {
              const isCompleted = item.status === 'COMPLETED';
              const isInProgress = item.status === 'IN_PROGRESS';

              return (
                <div
                  key={item.id}
                  className={`rounded-3xl border transition-all duration-200 overflow-hidden ${
                    isCompleted
                      ? 'bg-[#FBFBF9] border-black/[0.04] opacity-85'
                      : 'bg-white border-black/[0.08] shadow-card hover:border-black/[0.15]'
                  }`}
                >
                  {/* Top Item Summary Row */}
                  <div className="p-5 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                    {/* Left: Icon & Title */}
                    <div
                      onClick={() => handleItemClick(item)}
                      className="flex items-start sm:items-center gap-4 min-w-0 cursor-pointer group flex-1"
                    >
                      <div
                        className={`w-11 h-11 rounded-2xl flex items-center justify-center shrink-0 transition-transform group-hover:scale-105 ${
                          item.type === 'REVIEW'
                            ? 'bg-blue-100 text-blue-700'
                            : item.type === 'NEW_WORD'
                            ? 'bg-emerald-100 text-emerald-700'
                            : 'bg-purple-100 text-purple-700'
                        }`}
                      >
                        {item.type === 'REVIEW' && <RotateCw className="w-5 h-5" />}
                        {item.type === 'NEW_WORD' && <Sparkles className="w-5 h-5" />}
                        {item.type === 'QUIZ' && <HelpCircle className="w-5 h-5" />}
                      </div>

                      <div className="min-w-0 space-y-1">
                        <div className="flex items-center gap-2 flex-wrap">
                          <span className="text-base font-extrabold text-memora-dark tracking-tight group-hover:text-memora-green transition-colors">
                            {item.word || (item.type === 'QUIZ' ? 'Daily Retention Quiz' : 'Word')}
                          </span>

                          <Badge
                            variant={
                              item.type === 'NEW_WORD'
                                ? 'green'
                                : item.type === 'REVIEW'
                                ? 'blue'
                                : 'purple'
                            }
                            size="sm"
                          >
                            {item.type === 'NEW_WORD'
                              ? 'NEW WORD'
                              : item.type === 'REVIEW'
                              ? 'REVIEW'
                              : 'QUIZ'}
                          </Badge>

                          <Badge
                            variant={
                              item.priority === 'HIGH'
                                ? 'red'
                                : item.priority === 'MEDIUM'
                                ? 'amber'
                                : 'neutral'
                            }
                            size="sm"
                          >
                            {item.priority}
                          </Badge>

                          <span className="text-[11px] text-stone-400 font-semibold">
                            #{item.orderIndex}
                          </span>
                        </div>

                        <p className="text-xs text-memora-text-muted leading-relaxed line-clamp-1">
                          {item.meaning || item.notes || `${item.type} vocabulary task`}
                        </p>
                      </div>
                    </div>

                    {/* Right: Actions */}
                    <div className="flex items-center gap-2.5 shrink-0 self-end sm:self-center">
                      {isCompleted ? (
                        <div className="flex items-center gap-2">
                          <Badge variant="green" size="md">
                            <CheckCircle2 className="w-3.5 h-3.5" /> Completed
                          </Badge>
                          {item.type === 'NEW_WORD' && (
                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={() => handleOpenStudy(item)}
                              className="text-xs text-stone-500 hover:text-stone-800 font-semibold"
                            >
                              Review
                            </Button>
                          )}
                        </div>
                      ) : item.type === 'NEW_WORD' ? (
                        <Button
                          variant="primary"
                          size="sm"
                          onClick={() => handleOpenStudy(item)}
                          leftIcon={
                            isInProgress ? (
                              <BookOpen className="w-4 h-4" />
                            ) : (
                              <Play className="w-3.5 h-3.5 fill-current" />
                            )
                          }
                          className="shadow-sm font-semibold"
                        >
                          {isInProgress ? 'Study Word' : 'Study Word'}
                        </Button>
                      ) : item.type === 'REVIEW' ? (
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => navigate('/review')}
                          leftIcon={<RotateCw className="w-3.5 h-3.5" />}
                          className="text-xs font-semibold text-blue-700 hover:bg-blue-50 border-blue-200"
                        >
                          Review Now
                        </Button>
                      ) : (
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() =>
                            navigate(item.quizId ? `/quiz/${item.quizId}` : '/quiz')
                          }
                          leftIcon={<HelpCircle className="w-3.5 h-3.5" />}
                          className="text-xs font-semibold text-purple-700 hover:bg-purple-50 border-purple-200"
                        >
                          Take Quiz
                        </Button>
                      )}
                    </div>
                  </div>
                </div>
              );
            })
          ) : (
            <div className="text-center py-12 text-sm text-memora-text-muted">
              No items for today. All scheduled tasks completed! 🎉
            </div>
          )}
        </div>
      </div>

      {/* Focused Word Study Experience Modal */}
      <WordStudyModal
        isOpen={studyingItem !== null}
        item={studyingItem}
        targetLevel={path.targetLevel}
        nextItem={nextStudyItem}
        onClose={handleCloseStudy}
        onCompleted={handleWordStudyCompleted}
        onNavigateToItem={handleNavigateToNextItem}
      />
    </AppShell>
  );
};

export default LearningPathPage;
