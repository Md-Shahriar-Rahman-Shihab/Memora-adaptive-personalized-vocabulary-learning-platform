import React, { useEffect, useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { RotateCw, Volume2, CheckCircle2, Clock, Sparkles, HelpCircle, ArrowRight } from 'lucide-react';
import { memoryApi } from '../api/memoryApi';
import { MemoryWordResponse, WordReviewResponse } from '../types/memory';
import { useToast } from '../context/ToastContext';
import { useAuth } from '../context/AuthContext';
import { AppShell } from '../components/layout/AppShell';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { LoadingSpinner } from '../components/ui/LoadingSpinner';
import { EmptyState } from '../components/ui/EmptyState';

export const ReviewPage: React.FC = () => {
  const navigate = useNavigate();
  const { addToast } = useToast();
  const { refreshUser } = useAuth();

  const [words, setWords] = useState<MemoryWordResponse[]>([]);
  const [currentIndex, setCurrentIndex] = useState<number>(0);
  const [isFlipped, setIsFlipped] = useState<boolean>(false);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [lastResult, setLastResult] = useState<WordReviewResponse | null>(null);

  const cardStartTimeRef = useRef<number>(Date.now());

  useEffect(() => {
    const fetchWords = async () => {
      setIsLoading(true);
      try {
        const res = await memoryApi.getDueWords();
        if (res.success && res.data && res.data.length > 0) {
          setWords(res.data);
        } else {
          // If no due reviews, fall back to weak words pool
          const weakRes = await memoryApi.getWeakWords();
          if (weakRes.success && weakRes.data) {
            setWords(weakRes.data);
          }
        }
      } finally {
        setIsLoading(false);
      }
    };

    fetchWords();
  }, []);

  const currentWord = words[currentIndex] || null;

  useEffect(() => {
    setIsFlipped(false);
    setLastResult(null);
    cardStartTimeRef.current = Date.now();
  }, [currentIndex]);

  const handleReviewAttempt = async (correct: boolean) => {
    if (!currentWord || isSubmitting) return;

    const latencyMs = Math.max(200, Date.now() - cardStartTimeRef.current);
    setIsSubmitting(true);

    try {
      const res = await memoryApi.recordReview({
        vocabularyWordId: currentWord.wordId,
        correct,
        responseTimeMs: latencyMs,
        algorithm: 'SM2',
      });

      if (res.success && res.data) {
        setLastResult(res.data);
        addToast({
          type: 'xp',
          title: correct ? 'Memory Reinforced!' : 'Keep Practicing',
          message: `Next review: ${new Date(res.data.nextReviewAt).toLocaleDateString()}`,
          xpAmount: correct ? 15 : 5,
        });

        await refreshUser();

        // Move to next word after a short pause
        setTimeout(() => {
          if (currentIndex + 1 < words.length) {
            setCurrentIndex((prev) => prev + 1);
          } else {
            setWords([]);
          }
        }, 1200);
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleSpeak = (text: string) => {
    if ('speechSynthesis' in window) {
      const utterance = new SpeechSynthesisUtterance(text);
      utterance.lang = 'en-US';
      window.speechSynthesis.speak(utterance);
    }
  };

  if (isLoading) {
    return (
      <AppShell title="Spaced Repetition Review" subtitle="Loading due review words...">
        <div className="py-20 flex justify-center">
          <LoadingSpinner size="lg" label="Checking memory retention intervals..." />
        </div>
      </AppShell>
    );
  }

  if (!currentWord || words.length === 0) {
    return (
      <AppShell title="Spaced Repetition Review" subtitle="All reviews completed">
        <div className="max-w-xl mx-auto py-12">
          <EmptyState
            icon={<CheckCircle2 className="w-12 h-12 text-memora-green" />}
            title="Memory is fully reinforced! 🎉"
            description="No words are scheduled for spaced repetition review right now. Come back later as the SuperMemo-2 algorithm schedules your next retention checkpoints."
            actionText="Go to Dashboard"
            onAction={() => navigate('/dashboard')}
          />
        </div>
      </AppShell>
    );
  }

  return (
    <AppShell
      title="Spaced Repetition Review"
      subtitle={`Card ${currentIndex + 1} of ${words.length} • Strengthen your memory retention`}
    >
      <div className="max-w-2xl mx-auto py-6 space-y-6">
        {/* Memory status info banner */}
        <div className="flex items-center justify-between text-xs text-memora-text-muted px-2">
          <span className="flex items-center gap-1.5 font-bold text-memora-dark">
            <Sparkles className="w-4 h-4 text-memora-green" />
            SuperMemo-2 Memory Engine
          </span>
          <span>
            {words.length - currentIndex} {words.length - currentIndex === 1 ? 'card' : 'cards'}{' '}
            remaining
          </span>
        </div>

        {/* Flashcard Card Shell */}
        <Card
          variant="default"
          className="p-8 sm:p-12 text-center min-h-[380px] flex flex-col justify-between cursor-pointer border border-black/[0.08] shadow-card hover:border-black/[0.12] transition-all"
          onClick={() => !isFlipped && setIsFlipped(true)}
        >
          {/* Top metadata */}
          <div className="flex items-center justify-between w-full">
            <Badge variant="neutral" size="sm">
              Level {currentWord.difficultyLevel}
            </Badge>
            <Badge
              variant={
                currentWord.forgettingRisk === 'HIGH' || currentWord.forgettingRisk === 'CRITICAL'
                  ? 'red'
                  : 'amber'
              }
              size="sm"
            >
              {currentWord.forgettingRisk} FORGETTING RISK
            </Badge>
          </div>

          {/* Card Body */}
          <div className="my-auto py-6 space-y-4">
            <div className="flex items-center justify-center gap-3">
              <h2 className="text-4xl sm:text-5xl font-extrabold text-memora-dark tracking-tight">
                {currentWord.word}
              </h2>
              <button
                type="button"
                onClick={(e) => {
                  e.stopPropagation();
                  handleSpeak(currentWord.word);
                }}
                className="w-10 h-10 rounded-full bg-stone-100 hover:bg-memora-green-light hover:text-memora-green flex items-center justify-center text-stone-500 transition"
                aria-label="Listen to pronunciation"
              >
                <Volume2 className="w-5 h-5" />
              </button>
            </div>

            {/* Revealed content */}
            {isFlipped ? (
              <div className="space-y-3 animate-flip-reveal pt-2">
                <p className="text-lg font-semibold text-stone-800">{currentWord.meaning}</p>
                <div className="text-xs text-memora-text-muted bg-[#F8F8F5] p-3.5 rounded-2xl border border-black/[0.04] max-w-md mx-auto">
                  Mastery: {Math.round(currentWord.masteryScore)}% • Consecutive Correct:{' '}
                  {currentWord.consecutiveCorrect}
                </div>
              </div>
            ) : (
              <p className="text-xs text-memora-text-muted font-semibold flex items-center justify-center gap-1.5 pt-4">
                <HelpCircle className="w-4 h-4" /> Click card to reveal meaning & memory status
              </p>
            )}
          </div>

          {/* Last Result feedback if rated */}
          {lastResult && (
            <div className="text-xs font-bold text-emerald-700 bg-emerald-50 py-2 px-4 rounded-xl border border-emerald-100 animate-scale-up">
              ✓ Retention scheduled: Next review in {lastResult.reviewIntervalDays}{' '}
              {lastResult.reviewIntervalDays === 1 ? 'day' : 'days'}
            </div>
          )}

          {/* Action Buttons (Only visible when flipped) */}
          {isFlipped && !lastResult && (
            <div
              className="grid grid-cols-2 sm:grid-cols-3 gap-3 w-full pt-4 border-t border-black/[0.06] animate-fade-in"
              onClick={(e) => e.stopPropagation()}
            >
              <Button
                variant="outline"
                size="md"
                onClick={() => handleReviewAttempt(false)}
                disabled={isSubmitting}
                className="border-red-200 text-red-700 hover:bg-red-50 text-xs py-3 font-bold"
              >
                Forgot / Difficult
              </Button>
              <Button
                variant="secondary"
                size="md"
                onClick={() => handleReviewAttempt(true)}
                disabled={isSubmitting}
                className="text-xs py-3 font-bold"
              >
                Remembered
              </Button>
              <Button
                variant="primary"
                size="md"
                onClick={() => handleReviewAttempt(true)}
                disabled={isSubmitting}
                className="col-span-2 sm:col-span-1 text-xs py-3 font-bold shadow-sm"
              >
                Easy / Mastered
              </Button>
            </div>
          )}
        </Card>
      </div>
    </AppShell>
  );
};

export default ReviewPage;
