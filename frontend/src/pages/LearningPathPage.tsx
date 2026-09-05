import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Compass,
  RotateCw,
  Sparkles,
  HelpCircle,
  CheckCircle2,
  Play,
  RefreshCw,
  AlertCircle,
} from 'lucide-react';
import { learningPathApi } from '../api/learningPathApi';
import { TodayLearningPathResponse, LearningPathItemResponse } from '../types/learningPath';
import { useToast } from '../context/ToastContext';
import { useAuth } from '../context/AuthContext';
import { AppShell } from '../components/layout/AppShell';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { ProgressBar } from '../components/ui/ProgressBar';
import { LoadingSpinner } from '../components/ui/LoadingSpinner';
import { ErrorState } from '../components/ui/ErrorState';

export const LearningPathPage: React.FC = () => {
  const navigate = useNavigate();
  const { addToast } = useToast();
  const { refreshUser } = useAuth();

  const [path, setPath] = useState<TodayLearningPathResponse | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [isRegenerating, setIsRegenerating] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

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
        // Update local status
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
    } catch (err: any) {
      addToast({
        type: 'error',
        title: 'Error',
        message: err?.response?.data?.message || 'Could not start item.',
      });
    }
  };

  const handleCompleteItem = async (item: LearningPathItemResponse) => {
    if (item.type === 'QUIZ' && item.quizId) {
      navigate(`/quiz/${item.quizId}`);
      return;
    }

    try {
      const res = await learningPathApi.completeItem(item.id, {
        correct: true,
        responseTimeMs: 1200,
        algorithm: 'SM2',
      });

      if (res.success && res.data) {
        addToast({
          type: 'xp',
          title: 'Lesson Completed!',
          message: res.data.message || 'Great job practicing your vocabulary.',
          xpAmount: 20,
        });

        // Refresh user XP/streak in context and reload path
        await refreshUser();
        await fetchTodayPath();
      }
    } catch (err: any) {
      addToast({
        type: 'error',
        title: 'Error',
        message: err?.response?.data?.message || 'Failed to complete item.',
      });
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
                  className={`p-5 rounded-3xl border transition-all duration-200 flex flex-col sm:flex-row sm:items-center justify-between gap-4 ${
                    isCompleted
                      ? 'bg-[#FBFBF9] border-black/[0.04] opacity-80'
                      : 'bg-white border-black/[0.08] shadow-card hover:border-black/[0.12]'
                  }`}
                >
                  {/* Left: Icon & Description */}
                  <div className="flex items-start sm:items-center gap-4 min-w-0">
                    <div
                      className={`w-11 h-11 rounded-2xl flex items-center justify-center shrink-0 ${
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
                        <span className="text-base font-extrabold text-memora-dark tracking-tight">
                          {item.word || (item.type === 'QUIZ' ? 'Daily Retention Quiz' : 'Word')}
                        </span>
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
                          {item.priority} PRIORITY
                        </Badge>
                        <span className="text-[11px] text-stone-400 font-semibold">
                          #{item.orderIndex}
                        </span>
                      </div>
                      <p className="text-xs text-memora-text-muted leading-relaxed line-clamp-2">
                        {item.notes || item.meaning || `${item.type} vocabulary task`}
                      </p>
                    </div>
                  </div>

                  {/* Right: Actions */}
                  <div className="flex items-center gap-2.5 shrink-0 self-end sm:self-center">
                    {isCompleted ? (
                      <Badge variant="green" size="md">
                        <CheckCircle2 className="w-3.5 h-3.5" /> Completed
                      </Badge>
                    ) : isInProgress ? (
                      <Button
                        variant="primary"
                        size="sm"
                        onClick={() => handleCompleteItem(item)}
                        rightIcon={<CheckCircle2 className="w-4 h-4" />}
                      >
                        Complete Item
                      </Button>
                    ) : (
                      <div className="flex items-center gap-2">
                        {item.type === 'REVIEW' && (
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => navigate('/review')}
                            leftIcon={<RotateCw className="w-3.5 h-3.5 text-blue-600" />}
                          >
                            Practice
                          </Button>
                        )}
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => handleStartItem(item.id)}
                          leftIcon={<Play className="w-3.5 h-3.5 text-stone-600" />}
                        >
                          Start
                        </Button>
                        <Button
                          variant="primary"
                          size="sm"
                          onClick={() => handleCompleteItem(item)}
                        >
                          Mark Done
                        </Button>
                      </div>
                    )}
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
    </AppShell>
  );
};

export default LearningPathPage;
