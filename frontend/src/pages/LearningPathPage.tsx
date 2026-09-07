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
  Volume2,
  BookOpen,
  ArrowRight,
} from 'lucide-react';
import { learningPathApi } from '../api/learningPathApi';
import { vocabularyApi } from '../api/vocabularyApi';
import { TodayLearningPathResponse, LearningPathItemResponse } from '../types/learningPath';
import { VocabularyWordResponse } from '../types/vocabulary';
import { useToast } from '../context/ToastContext';
import { useAuth } from '../context/AuthContext';
import { AppShell } from '../components/layout/AppShell';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { ProgressBar } from '../components/ui/ProgressBar';
import { LoadingSpinner } from '../components/ui/LoadingSpinner';
import { ErrorState } from '../components/ui/ErrorState';
import { WordStudyAiActions } from '../components/vocabulary/WordStudyAiActions';

export const LearningPathPage: React.FC = () => {
  const navigate = useNavigate();
  const { addToast } = useToast();
  const { refreshUser } = useAuth();

  const [path, setPath] = useState<TodayLearningPathResponse | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [isRegenerating, setIsRegenerating] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  // Inline Vocabulary Learning Card State (no background blur, pops open right in list)
  const [expandedItemId, setExpandedItemId] = useState<number | null>(null);
  const [wordDetailsMap, setWordDetailsMap] = useState<Record<number, VocabularyWordResponse>>({});
  const [isWordLoading, setIsWordLoading] = useState<boolean>(false);
  const [isCompletingId, setIsCompletingId] = useState<number | null>(null);

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

  const handleSpeak = (text: string) => {
    if ('speechSynthesis' in window && text) {
      window.speechSynthesis.cancel();
      const utterance = new SpeechSynthesisUtterance(text);
      utterance.lang = 'en-US';
      utterance.rate = 0.9;
      window.speechSynthesis.speak(utterance);
    }
  };

  const toggleLearnItem = async (item: LearningPathItemResponse) => {
    if (item.type === 'QUIZ') {
      if (item.quizId) {
        navigate(`/quiz/${item.quizId}`);
      } else {
        navigate('/quiz');
      }
      return;
    }

    // If currently expanded, toggle it closed
    if (expandedItemId === item.id) {
      setExpandedItemId(null);
      return;
    }

    // Expand this item right in the list
    setExpandedItemId(item.id);

    // If item is pending, mark it as started on the server
    if (item.status === 'PENDING') {
      handleStartItem(item.id);
    }

    // Fetch full word details if not already loaded in memory
    if (item.wordId && !wordDetailsMap[item.wordId]) {
      setIsWordLoading(true);
      try {
        const res = await vocabularyApi.getWordById(item.wordId);
        if (res.success && res.data) {
          setWordDetailsMap((prev) => ({ ...prev, [item.wordId!]: res.data }));
        }
      } catch {
        // Fallback to existing item attributes
      } finally {
        setIsWordLoading(false);
      }
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
          xpAmount: 15,
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

  const handleCompleteAndCollapse = async (item: LearningPathItemResponse) => {
    setIsCompletingId(item.id);
    try {
      await handleCompleteItem(item);
      setExpandedItemId(null);
    } finally {
      setIsCompletingId(null);
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

  const [isAdvancing, setIsAdvancing] = useState<boolean>(false);

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
              const isExpanded = expandedItemId === item.id;
              const wordInfo = item.wordId ? wordDetailsMap[item.wordId] : null;

              return (
                <div
                  key={item.id}
                  className={`rounded-3xl border transition-all duration-200 overflow-hidden ${
                    isExpanded
                      ? 'bg-white border-memora-green ring-4 ring-memora-green/10 shadow-lg'
                      : isCompleted
                      ? 'bg-[#FBFBF9] border-black/[0.04] opacity-85'
                      : 'bg-white border-black/[0.08] shadow-card hover:border-black/[0.15]'
                  }`}
                >
                  {/* Top Item Summary Row */}
                  <div className="p-5 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                    {/* Left: Icon & Title */}
                    <div
                      onClick={() => toggleLearnItem(item)}
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
                        {!isExpanded && (
                          <p className="text-xs text-memora-text-muted leading-relaxed line-clamp-1">
                            {item.meaning || item.notes || `${item.type} vocabulary task`}
                          </p>
                        )}
                      </div>
                    </div>

                    {/* Right: Actions */}
                    <div className="flex items-center gap-2.5 shrink-0 self-end sm:self-center">
                      {isCompleted ? (
                        <div className="flex items-center gap-2">
                          <Badge variant="green" size="md">
                            <CheckCircle2 className="w-3.5 h-3.5" /> Completed
                          </Badge>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => toggleLearnItem(item)}
                            className="text-xs text-stone-500 hover:text-stone-800 font-semibold"
                          >
                            {isExpanded ? 'Hide' : 'Review'}
                          </Button>
                        </div>
                      ) : isExpanded ? (
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => setExpandedItemId(null)}
                          className="text-xs text-stone-500"
                        >
                          Hide Details
                        </Button>
                      ) : isInProgress ? (
                        <Button
                          variant="primary"
                          size="sm"
                          onClick={() => toggleLearnItem(item)}
                          leftIcon={<BookOpen className="w-4 h-4" />}
                          className="shadow-sm"
                        >
                          Study & Complete
                        </Button>
                      ) : (
                        <div className="flex items-center gap-2">
                          <Button
                            variant="primary"
                            size="sm"
                            onClick={() => toggleLearnItem(item)}
                            leftIcon={<Play className="w-3.5 h-3.5 fill-current" />}
                            className="shadow-sm"
                          >
                            Start
                          </Button>
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => handleCompleteItem(item)}
                            className="text-xs text-stone-500 hover:text-stone-800"
                          >
                            Mark Done
                          </Button>
                        </div>
                      )}
                    </div>
                  </div>

                  {/* Inline Study Pop-Up Card (in the same place, no background blur) */}
                  {isExpanded && (
                    <div className="px-6 pb-6 pt-3 border-t border-emerald-100 bg-gradient-to-b from-emerald-50/25 to-white space-y-5 animate-fade-in">
                      {/* Word, Category, Pronunciation */}
                      <div className="flex items-start justify-between gap-4 pt-1">
                        <div>
                          <div className="flex items-center gap-3">
                            <h3 className="text-2xl sm:text-3xl font-black text-memora-dark tracking-tight">
                              {wordInfo?.word || item.word}
                            </h3>
                            <button
                              type="button"
                              onClick={() => handleSpeak(wordInfo?.word || item.word || '')}
                              className="w-9 h-9 rounded-xl bg-emerald-100/80 hover:bg-emerald-200 text-emerald-800 flex items-center justify-center transition shadow-sm hover:scale-105 active:scale-95"
                              title="Listen to pronunciation"
                            >
                              <Volume2 className="w-4 h-4" />
                            </button>
                          </div>
                          {(wordInfo?.pronunciation || item.pronunciation) && (
                            <p className="text-xs sm:text-sm font-mono text-stone-500 mt-1">
                              /{wordInfo?.pronunciation || item.pronunciation}/
                            </p>
                          )}
                        </div>

                        <div className="flex items-center gap-2">
                          {wordInfo?.category && (
                            <Badge variant="neutral" size="sm">
                              {wordInfo.category.replace(/_/g, ' ')}
                            </Badge>
                          )}
                          <Badge variant="green" size="sm">
                            CEFR {wordInfo?.difficultyLevel || path.targetLevel}
                          </Badge>
                        </div>
                      </div>

                      {/* Meaning Box */}
                      <div className="p-4 rounded-2xl bg-emerald-50/90 border border-emerald-200/80 space-y-1">
                        <span className="text-[10px] font-black uppercase tracking-wider text-emerald-800 block">
                          Meaning
                        </span>
                        <p className="text-base font-bold text-emerald-950 leading-relaxed">
                          {wordInfo?.meaning || item.meaning || 'No meaning provided'}
                        </p>
                      </div>

                      {/* Detailed Definition */}
                      {(wordInfo?.definition || item.definition) && (
                        <div className="space-y-1 px-1">
                          <span className="text-[11px] font-bold uppercase tracking-wider text-memora-text-muted block">
                            Definition
                          </span>
                          <p className="text-xs sm:text-sm text-stone-700 leading-relaxed">
                            {wordInfo?.definition || item.definition}
                          </p>
                        </div>
                      )}

                      {/* Example Sentence Box */}
                      {(wordInfo?.exampleSentence || item.exampleSentence) && (
                        <div className="p-4 rounded-2xl bg-stone-50 border border-stone-200/70 space-y-1.5">
                          <div className="flex items-center justify-between">
                            <span className="text-[11px] font-bold uppercase tracking-wider text-stone-500">
                              Example in Context
                            </span>
                            <button
                              type="button"
                              onClick={() =>
                                handleSpeak(wordInfo?.exampleSentence || item.exampleSentence || '')
                              }
                              className="text-xs text-stone-500 hover:text-stone-800 flex items-center gap-1 font-semibold transition"
                            >
                              <Volume2 className="w-3.5 h-3.5" /> Listen
                            </button>
                          </div>
                          <p className="text-xs sm:text-sm font-medium italic text-stone-800 leading-relaxed">
                            "{wordInfo?.exampleSentence || item.exampleSentence}"
                          </p>
                        </div>
                      )}

                      {/* AI Word Study Tools */}
                      <WordStudyAiActions
                        word={wordInfo?.word || item.word || ''}
                        wordId={wordInfo?.id || item.wordId}
                        cefrLevel={wordInfo?.difficultyLevel || path?.targetLevel}
                        onSpeak={handleSpeak}
                      />

                      {/* Notes / Discovery Tip */}
                      {item.notes && (
                        <div className="flex items-center gap-2 text-xs text-stone-500 bg-stone-50 px-3.5 py-2 rounded-xl">
                          <Sparkles className="w-3.5 h-3.5 text-amber-500 shrink-0" />
                          <span>{item.notes}</span>
                        </div>
                      )}

                      {/* Action Buttons */}
                      <div className="flex items-center justify-end gap-3 pt-3 border-t border-black/[0.06]">
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => setExpandedItemId(null)}
                        >
                          Close
                        </Button>

                        {item.status !== 'COMPLETED' ? (
                          <Button
                            variant="primary"
                            size="sm"
                            isLoading={isCompletingId === item.id}
                            onClick={() => handleCompleteAndCollapse(item)}
                            leftIcon={<CheckCircle2 className="w-4 h-4" />}
                            className="shadow-sm"
                          >
                            Mark as Complete (+15 XP)
                          </Button>
                        ) : (
                          <Badge variant="green" size="md">
                            <CheckCircle2 className="w-3.5 h-3.5 mr-1" /> Completed
                          </Badge>
                        )}
                      </div>
                    </div>
                  )}
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
