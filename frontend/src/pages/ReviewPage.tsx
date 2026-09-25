import React, { useEffect, useState, useRef, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  RotateCw,
  Volume2,
  CheckCircle2,
  XCircle,
  Clock,
  Sparkles,
  HelpCircle,
  ArrowRight,
  TrendingUp,
  Award,
  Flame,
  ChevronRight,
  AlertCircle,
  BookOpen,
  Zap,
  Lightbulb,
} from 'lucide-react';
import { memoryApi } from '../api/memoryApi';
import { vocabularyApi } from '../api/vocabularyApi';
import { dictionaryApi } from '../api/dictionaryApi';
import { MemoryWordResponse, WordReviewResponse } from '../types/memory';
import { VocabularyWordResponse } from '../types/vocabulary';
import { DictionaryResponse } from '../types/dictionary';
import { useToast } from '../context/ToastContext';
import { useAuth } from '../context/AuthContext';
import { AppShell } from '../components/layout/AppShell';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { ProgressBar } from '../components/ui/ProgressBar';
import { LoadingSpinner } from '../components/ui/LoadingSpinner';
import { EmptyState } from '../components/ui/EmptyState';
import { WordStudyAiActions } from '../components/vocabulary/WordStudyAiActions';

interface ReviewOption {
  id: string;
  text: string;
  isCorrect: boolean;
}

interface CompletedReviewRecord {
  word: MemoryWordResponse;
  isCorrect: boolean;
  result: WordReviewResponse;
  latencyMs: number;
}

const MAX_SESSION_SIZE = 10;

export const ReviewPage: React.FC = () => {
  const navigate = useNavigate();
  const { addToast } = useToast();
  const { refreshUser } = useAuth();

  // Session dataset
  const [sessionWords, setSessionWords] = useState<MemoryWordResponse[]>([]);
  const [vocabularyCatalog, setVocabularyCatalog] = useState<VocabularyWordResponse[]>([]);
  const [currentIndex, setCurrentIndex] = useState<number>(0);
  const [completedRecords, setCompletedRecords] = useState<CompletedReviewRecord[]>([]);

  // State flags
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [isSessionComplete, setIsSessionComplete] = useState<boolean>(false);
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [submissionError, setSubmissionError] = useState<string | null>(null);

  // Active card state
  const [currentOptions, setCurrentOptions] = useState<ReviewOption[]>([]);
  const [selectedOptionId, setSelectedOptionId] = useState<string | null>(null);
  const [hasSubmittedAnswer, setHasSubmittedAnswer] = useState<boolean>(false);
  const [lastReviewResult, setLastReviewResult] = useState<WordReviewResponse | null>(null);
  const [showAiDeepDive, setShowAiDeepDive] = useState<boolean>(false);

  // Dictionary & audio details
  const [dictionaryDetails, setDictionaryDetails] = useState<DictionaryResponse | null>(null);
  const [audioStatus, setAudioStatus] = useState<'idle' | 'playing' | 'error'>('idle');
  const audioRef = useRef<HTMLAudioElement | null>(null);
  const cardStartTimeRef = useRef<number>(Date.now());

  // Clean audio on unmount
  useEffect(() => {
    return () => {
      if (audioRef.current) {
        audioRef.current.pause();
        audioRef.current = null;
      }
      if ('speechSynthesis' in window) {
        window.speechSynthesis.cancel();
      }
    };
  }, []);

  // Fetch due and weak words to construct the session
  const fetchSessionWords = useCallback(async () => {
    setIsLoading(true);
    setLoadError(null);
    setIsSessionComplete(false);
    setCurrentIndex(0);
    setCompletedRecords([]);

    try {
      // 1. Fetch due words and weak words in parallel
      const [dueRes, weakRes, catalogRes] = await Promise.all([
        memoryApi.getDueWords().catch(() => null),
        memoryApi.getWeakWords().catch(() => null),
        vocabularyApi.getAllVocabulary().catch(() => null),
      ]);

      if (catalogRes?.success && catalogRes.data) {
        setVocabularyCatalog(catalogRes.data);
      }

      const dueList: MemoryWordResponse[] = dueRes?.success && dueRes.data ? dueRes.data : [];
      const weakList: MemoryWordResponse[] = weakRes?.success && weakRes.data ? weakRes.data : [];

      // Combine prioritizing due words, then weak words (deduping by wordId)
      const combinedMap = new Map<number, MemoryWordResponse>();
      dueList.forEach((w) => combinedMap.set(w.wordId, w));
      weakList.forEach((w) => {
        if (!combinedMap.has(w.wordId)) {
          combinedMap.set(w.wordId, w);
        }
      });

      const sessionItems = Array.from(combinedMap.values()).slice(0, MAX_SESSION_SIZE);
      setSessionWords(sessionItems);
    } catch (err: unknown) {
      console.error('Error initializing review session:', err);
      setLoadError('Failed to load review words. Please check your network and try again.');
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchSessionWords();
  }, [fetchSessionWords]);

  const currentWord: MemoryWordResponse | null = sessionWords[currentIndex] || null;

  // Generate 4 active-recall options whenever currentWord changes
  useEffect(() => {
    if (!currentWord) return;

    setSelectedOptionId(null);
    setHasSubmittedAnswer(false);
    setLastReviewResult(null);
    setSubmissionError(null);
    setShowAiDeepDive(false);
    cardStartTimeRef.current = Date.now();

    // Reset audio state
    setAudioStatus('idle');
    if (audioRef.current) {
      audioRef.current.pause();
      audioRef.current = null;
    }
    if ('speechSynthesis' in window) {
      window.speechSynthesis.cancel();
    }

    // Determine correct definition
    const correctDef = currentWord.definition || currentWord.meaning;

    // Pick 3 distractors from vocabulary catalog or other session words
    const distractorCandidates: string[] = [];

    // From catalog
    vocabularyCatalog.forEach((vocab) => {
      if (vocab.id !== currentWord.wordId && vocab.word.toLowerCase() !== currentWord.word.toLowerCase()) {
        const candidate = vocab.definition || vocab.meaning;
        if (candidate && candidate !== correctDef && !distractorCandidates.includes(candidate)) {
          distractorCandidates.push(candidate);
        }
      }
    });

    // From other session words if catalog candidates are few
    sessionWords.forEach((item) => {
      if (item.wordId !== currentWord.wordId && item.word.toLowerCase() !== currentWord.word.toLowerCase()) {
        const candidate = item.definition || item.meaning;
        if (candidate && candidate !== correctDef && !distractorCandidates.includes(candidate)) {
          distractorCandidates.push(candidate);
        }
      }
    });

    // Fallbacks if pool is empty
    const genericDistractors = [
      'To make a firm decision or resolve an ongoing problem',
      'Having or showing keen discernment and sound judgment',
      'Occurring, operating, or done at the same time',
      'Relating to or characteristic of a large, busy city',
    ];
    genericDistractors.forEach((d) => {
      if (d !== correctDef && !distractorCandidates.includes(d)) {
        distractorCandidates.push(d);
      }
    });

    // Shuffle and pick 3
    const shuffledDistractors = [...distractorCandidates].sort(() => 0.5 - Math.random()).slice(0, 3);

    const options: ReviewOption[] = [
      { id: 'correct', text: correctDef, isCorrect: true },
      ...shuffledDistractors.map((text, idx) => ({
        id: `distractor-${idx}`,
        text,
        isCorrect: false,
      })),
    ].sort(() => 0.5 - Math.random());

    setCurrentOptions(options);

    // Fetch dictionary audio/details lazily
    let isMounted = true;
    dictionaryApi
      .lookupWord(currentWord.word)
      .then((res) => {
        if (isMounted && res.success && res.data) {
          setDictionaryDetails(res.data);
        }
      })
      .catch(() => {
        if (isMounted) setDictionaryDetails(null);
      });

    return () => {
      isMounted = false;
    };
  }, [currentIndex, currentWord, vocabularyCatalog, sessionWords]);

  // Audio playback handler (Merriam-Webster audio CDN URL with Web Speech API fallback)
  const handlePlayAudio = () => {
    if (!currentWord) return;

    if (audioStatus === 'playing') {
      if (audioRef.current) {
        audioRef.current.pause();
        audioRef.current.currentTime = 0;
      }
      if ('speechSynthesis' in window) {
        window.speechSynthesis.cancel();
      }
      setAudioStatus('idle');
      return;
    }

    setAudioStatus('playing');

    const audioUrl = dictionaryDetails?.audioUrl;
    if (audioUrl) {
      if (audioRef.current) {
        audioRef.current.pause();
      }
      const audio = new Audio(audioUrl);
      audioRef.current = audio;
      audio.onended = () => setAudioStatus('idle');
      audio.onerror = () => {
        fallbackToSpeechSynthesis(currentWord.word);
      };
      audio.play().catch(() => {
        fallbackToSpeechSynthesis(currentWord.word);
      });
    } else {
      fallbackToSpeechSynthesis(currentWord.word);
    }
  };

  const fallbackToSpeechSynthesis = (wordToSpeak: string) => {
    if ('speechSynthesis' in window && wordToSpeak) {
      window.speechSynthesis.cancel();
      const utterance = new SpeechSynthesisUtterance(wordToSpeak);
      utterance.lang = 'en-US';
      utterance.rate = 0.9;
      utterance.onend = () => setAudioStatus('idle');
      utterance.onerror = () => setAudioStatus('idle');
      window.speechSynthesis.speak(utterance);
    } else {
      setAudioStatus('idle');
    }
  };

  // Submit Answer & Update Memory Engine
  const handleSubmitReview = async (overrideCorrect?: boolean) => {
    if (!currentWord || isSubmitting || hasSubmittedAnswer) return;

    let isCorrect = false;
    if (typeof overrideCorrect === 'boolean') {
      isCorrect = overrideCorrect;
    } else {
      if (!selectedOptionId) return;
      const selected = currentOptions.find((opt) => opt.id === selectedOptionId);
      isCorrect = !!selected?.isCorrect;
    }

    const latencyMs = Math.max(200, Date.now() - cardStartTimeRef.current);
    setIsSubmitting(true);
    setSubmissionError(null);

    try {
      const res = await memoryApi.recordReview({
        vocabularyWordId: currentWord.wordId,
        correct: isCorrect,
        responseTimeMs: latencyMs,
        algorithm: 'SM2',
        awardXp: true,
      });

      if (res.success && res.data) {
        const reviewData = res.data;
        setLastReviewResult(reviewData);
        setHasSubmittedAnswer(true);

        // Record for session completion summary
        setCompletedRecords((prev) => [
          ...prev,
          {
            word: currentWord,
            isCorrect,
            result: reviewData,
            latencyMs,
          },
        ]);

        // Trigger XP Toast
        const xpEarned = reviewData.xpEarned ?? (isCorrect ? 5 : 5);
        addToast({
          type: 'xp',
          title: isCorrect ? 'Memory Reinforced! 🎉' : 'Retention Scheduled 🔄',
          message: `Next review: ${new Date(reviewData.nextReviewAt).toLocaleDateString()}`,
          xpAmount: xpEarned,
        });

        // Sync header XP & streak
        await refreshUser().catch(() => null);
      } else {
        setSubmissionError(res.message || 'Failed to submit review. Please try again.');
      }
    } catch (err: unknown) {
      console.error('Error submitting review:', err);
      setSubmissionError('Server connection error. Your review was not recorded. Please click Try Again.');
    } finally {
      setIsSubmitting(false);
    }
  };

  // Advance to Next Word or Finish
  const handleNextWord = () => {
    if (currentIndex + 1 < sessionWords.length) {
      setCurrentIndex((prev) => prev + 1);
    } else {
      setIsSessionComplete(true);
    }
  };

  // Keyboard shortcut listener (1-4 to select, Enter to check / advance)
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      // Don't intercept if inside an input or textarea
      if (
        document.activeElement?.tagName === 'INPUT' ||
        document.activeElement?.tagName === 'TEXTAREA'
      ) {
        return;
      }

      if (!hasSubmittedAnswer) {
        if (['1', '2', '3', '4'].includes(e.key)) {
          const index = parseInt(e.key, 10) - 1;
          if (currentOptions[index]) {
            setSelectedOptionId(currentOptions[index].id);
          }
        } else if (e.key === 'Enter' && selectedOptionId && !isSubmitting) {
          e.preventDefault();
          handleSubmitReview();
        }
      } else {
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault();
          handleNextWord();
        }
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [hasSubmittedAnswer, currentOptions, selectedOptionId, isSubmitting, currentIndex, sessionWords.length]);

  // Loading State
  if (isLoading) {
    return (
      <AppShell title="Review Session" subtitle="Loading due spaced repetition words...">
        <div className="py-24 flex flex-col items-center justify-center">
          <LoadingSpinner size="lg" label="Checking memory retention intervals..." />
          <p className="text-xs text-stone-400 mt-4">
            Consulting SuperMemo-2 algorithm for scheduled checkpoints...
          </p>
        </div>
      </AppShell>
    );
  }

  // Load Error State
  if (loadError) {
    return (
      <AppShell title="Review Session" subtitle="Spaced Repetition Review">
        <div className="max-w-md mx-auto py-16 text-center">
          <Card className="p-8 space-y-4 border-red-200 bg-red-50/40">
            <AlertCircle className="w-12 h-12 text-red-600 mx-auto" />
            <h2 className="text-lg font-bold text-stone-900">Review Session Unavailable</h2>
            <p className="text-sm text-stone-600">{loadError}</p>
            <div className="pt-2 flex justify-center gap-3">
              <Button variant="primary" size="md" onClick={fetchSessionWords}>
                Try Again
              </Button>
              <Button variant="outline" size="md" onClick={() => navigate('/dashboard')}>
                Back to Dashboard
              </Button>
            </div>
          </Card>
        </div>
      </AppShell>
    );
  }

  // Empty State: All caught up
  if (sessionWords.length === 0) {
    return (
      <AppShell title="Review Session" subtitle="All reviews completed">
        <div className="max-w-xl mx-auto py-12">
          <EmptyState
            icon={<CheckCircle2 className="w-14 h-14 text-emerald-600" />}
            title="You're all caught up."
            description="No words are currently due for spaced repetition review. SuperMemo-2 will schedule your next retention checkpoints as memory intervals elapse."
            actionText="Continue Learning Path"
            onAction={() => navigate('/learning-path')}
          />
          <div className="mt-4 text-center">
            <Button
              variant="ghost"
              size="sm"
              onClick={() => navigate('/dashboard')}
              className="text-stone-500 hover:text-stone-800 text-xs"
            >
              Back to Dashboard
            </Button>
          </div>
        </div>
      </AppShell>
    );
  }

  // Completion State
  if (isSessionComplete) {
    const totalReviewed = completedRecords.length;
    const correctCount = completedRecords.filter((r) => r.isCorrect).length;
    const accuracy = totalReviewed > 0 ? Math.round((correctCount / totalReviewed) * 100) : 0;
    const totalXpEarned = completedRecords.reduce(
      (sum, r) => sum + (r.result.xpEarned ?? (r.isCorrect ? 5 : 5)),
      0
    );
    const missedWords = completedRecords.filter((r) => !r.isCorrect);

    return (
      <AppShell title="Review Complete" subtitle="Spaced Repetition Session Summary">
        <div className="max-w-2xl mx-auto py-6 space-y-6">
          {/* Header Banner Card */}
          <Card className="p-8 text-center bg-gradient-to-b from-emerald-50/70 to-white border-emerald-200/80 shadow-sm space-y-4">
            <div className="w-16 h-16 rounded-full bg-emerald-100 flex items-center justify-center mx-auto text-emerald-700 shadow-inner">
              <Award className="w-8 h-8" />
            </div>
            <div>
              <span className="text-xs uppercase tracking-widest font-extrabold text-emerald-800">
                Session Accomplished
              </span>
              <h1 className="text-3xl font-extrabold text-stone-900 mt-1">Review Complete</h1>
              <p className="text-sm text-stone-600 max-w-md mx-auto mt-2">
                Your memory schedule has been updated across your active vocabulary.
              </p>
            </div>

            {/* Metrics Grid */}
            <div className="grid grid-cols-3 gap-3 pt-4 border-t border-emerald-100 max-w-lg mx-auto">
              <div className="bg-white p-3.5 rounded-xl border border-stone-100 shadow-2xs">
                <span className="text-[11px] font-semibold text-stone-500 uppercase tracking-wider block">
                  Reviewed
                </span>
                <span className="text-2xl font-black text-stone-900">{totalReviewed}</span>
                <span className="text-[10px] text-stone-400 block">words</span>
              </div>
              <div className="bg-white p-3.5 rounded-xl border border-stone-100 shadow-2xs">
                <span className="text-[11px] font-semibold text-stone-500 uppercase tracking-wider block">
                  Accuracy
                </span>
                <span className="text-2xl font-black text-emerald-700">{accuracy}%</span>
                <span className="text-[10px] text-stone-400 block">{correctCount} correct</span>
              </div>
              <div className="bg-white p-3.5 rounded-xl border border-stone-100 shadow-2xs">
                <span className="text-[11px] font-semibold text-stone-500 uppercase tracking-wider block">
                  Earned
                </span>
                <span className="text-2xl font-black text-amber-600">+{totalXpEarned}</span>
                <span className="text-[10px] text-stone-400 block">XP gained</span>
              </div>
            </div>
          </Card>

          {/* Missed Words / Needs Reinforcement section */}
          {missedWords.length > 0 && (
            <Card className="p-6 border-amber-200/80 bg-amber-50/30 space-y-3">
              <div className="flex items-center gap-2 text-amber-900 font-bold text-sm">
                <RotateCw className="w-4 h-4 text-amber-600" />
                <span>Scheduled for Earlier Re-Review ({missedWords.length})</span>
              </div>
              <p className="text-xs text-stone-600">
                The algorithm automatically decreased the retention interval for these items so you can reinforce them soon:
              </p>
              <div className="divide-y divide-amber-200/60 rounded-xl bg-white border border-amber-100 overflow-hidden">
                {missedWords.map((rec) => (
                  <div key={rec.word.wordId} className="p-3 flex items-center justify-between text-xs">
                    <div className="space-y-0.5">
                      <div className="flex items-center gap-2">
                        <span className="font-bold text-stone-900 text-sm">{rec.word.word}</span>
                        <Badge variant="neutral" size="sm">
                          {rec.word.difficultyLevel}
                        </Badge>
                      </div>
                      <span className="text-stone-500 line-clamp-1">
                        {rec.word.definition || rec.word.meaning}
                      </span>
                    </div>
                    <div className="text-right shrink-0 ml-3">
                      <span className="text-[11px] font-semibold text-amber-700 block">
                        Next in {rec.result.reviewIntervalDays} {rec.result.reviewIntervalDays === 1 ? 'day' : 'days'}
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            </Card>
          )}

          {/* Action Buttons */}
          <div className="flex flex-col sm:flex-row items-center justify-between gap-3 pt-2">
            <Button
              variant="outline"
              size="lg"
              onClick={() => navigate('/dashboard')}
              className="w-full sm:w-auto text-xs font-bold"
            >
              Back to Dashboard
            </Button>
            <div className="flex items-center gap-2 w-full sm:w-auto">
              <Button
                variant="secondary"
                size="lg"
                onClick={fetchSessionWords}
                className="w-full sm:w-auto text-xs font-bold"
              >
                Review More
              </Button>
              <Button
                variant="primary"
                size="lg"
                onClick={() => navigate('/learning-path')}
                className="w-full sm:w-auto text-xs font-bold shadow-sm"
              >
                Continue Learning <ArrowRight className="w-4 h-4 ml-1.5" />
              </Button>
            </div>
          </div>
        </div>
      </AppShell>
    );
  }

  // Active Review Word Card
  const remainingCount = sessionWords.length - currentIndex;
  const progressPercent = Math.round(((currentIndex + 1) / sessionWords.length) * 100);

  // Derived styling for forgetting risk
  const getRiskBadgeVariant = (risk?: string): 'red' | 'amber' | 'green' | 'neutral' => {
    if (risk === 'CRITICAL' || risk === 'HIGH') return 'red';
    if (risk === 'MEDIUM') return 'amber';
    if (risk === 'LOW' || risk === 'OPTIMAL') return 'green';
    return 'neutral';
  };

  return (
    <AppShell
      title="Review Session"
      subtitle={`Word ${currentIndex + 1} of ${sessionWords.length} • Adaptive Spaced Repetition`}
    >
      <div className="max-w-2xl mx-auto py-4 space-y-5">
        {/* Top Session Progress Bar & Counter */}
        <div className="space-y-2">
          <div className="flex items-center justify-between text-xs">
            <span className="font-bold text-stone-800 flex items-center gap-1.5">
              <Sparkles className="w-3.5 h-3.5 text-emerald-600" />
              Session Progress
            </span>
            <span className="text-stone-500 font-medium">
              <strong className="text-stone-900">{currentIndex + 1}</strong> of{' '}
              <strong className="text-stone-900">{sessionWords.length}</strong> words ({remainingCount} remaining)
            </span>
          </div>
          <ProgressBar value={progressPercent} size="sm" barClassName="bg-emerald-600" />
        </div>

        {/* Main Review Card */}
        <Card className="p-6 sm:p-10 border border-stone-200/80 shadow-sm relative overflow-hidden bg-white space-y-6">
          {/* Header Metadata */}
          <div className="flex items-center justify-between flex-wrap gap-2">
            <div className="flex items-center gap-2">
              <Badge variant="neutral" size="sm">
                CEFR {currentWord?.difficultyLevel}
              </Badge>
              {(dictionaryDetails?.partsOfSpeech?.[0]?.partOfSpeech || currentWord?.category) && (
                <span className="text-[11px] font-bold text-stone-500 italic lowercase bg-stone-100 px-2 py-0.5 rounded-full border border-stone-200">
                  {dictionaryDetails?.partsOfSpeech?.[0]?.partOfSpeech || currentWord?.category}
                </span>
              )}
            </div>

            <div className="flex items-center gap-2">
              <Badge variant={getRiskBadgeVariant(currentWord?.forgettingRisk)} size="sm">
                {currentWord?.forgettingRisk} RISK
              </Badge>
              <span className="text-[11px] text-stone-400 font-mono">
                {Math.round(currentWord?.masteryScore || 0)}% Mastery
              </span>
            </div>
          </div>

          {/* Prominent Word Presentation & Pronunciation */}
          <div className="text-center py-2 space-y-2">
            <div className="inline-flex items-center justify-center gap-3">
              <h2 className="text-4xl sm:text-5xl font-extrabold text-stone-900 tracking-tight font-serif">
                {currentWord?.word}
              </h2>
              <button
                type="button"
                onClick={handlePlayAudio}
                className={`w-11 h-11 rounded-full flex items-center justify-center transition border ${
                  audioStatus === 'playing'
                    ? 'bg-emerald-100 text-emerald-700 border-emerald-300 scale-105 animate-pulse'
                    : 'bg-stone-50 hover:bg-emerald-50 text-stone-600 hover:text-emerald-700 border-stone-200 hover:border-emerald-200'
                }`}
                title={dictionaryDetails?.audioUrl ? 'Listen to Merriam-Webster pronunciation' : 'Listen to pronunciation'}
                aria-label="Listen to pronunciation"
              >
                <Volume2 className={`w-5 h-5 ${audioStatus === 'playing' ? 'animate-bounce' : ''}`} />
              </button>
            </div>

            {/* Pronunciation IPA */}
            {(currentWord?.pronunciation || dictionaryDetails?.pronunciation) && (
              <p className="text-sm font-mono text-stone-500">
                /{currentWord?.pronunciation || dictionaryDetails?.pronunciation}/
              </p>
            )}
          </div>

          {/* Submission Failure Alert */}
          {submissionError && (
            <div className="p-3.5 bg-red-50 border border-red-200 rounded-xl text-xs text-red-800 flex items-center justify-between gap-3">
              <div className="flex items-center gap-2">
                <AlertCircle className="w-4 h-4 shrink-0 text-red-600" />
                <span>{submissionError}</span>
              </div>
              <Button
                variant="outline"
                size="sm"
                onClick={() => handleSubmitReview()}
                className="shrink-0 border-red-300 text-red-800 hover:bg-red-100 text-xs py-1"
              >
                Retry
              </Button>
            </div>
          )}

          {/* Active Recall Interaction: Options */}
          <div className="space-y-3">
            <div className="flex items-center justify-between text-xs text-stone-500 font-medium px-1">
              <span>Choose the matching definition:</span>
              <span className="hidden sm:inline text-[11px] text-stone-400">Keys 1-4 to select</span>
            </div>

            <div className="grid grid-cols-1 gap-2.5">
              {currentOptions.map((opt, idx) => {
                const label = ['A', 'B', 'C', 'D'][idx];
                const isSelected = selectedOptionId === opt.id;

                let optionStyles = 'border-stone-200 hover:border-stone-300 bg-white text-stone-800';

                if (hasSubmittedAnswer) {
                  if (opt.isCorrect) {
                    optionStyles = 'border-emerald-400 bg-emerald-50 text-emerald-950 font-semibold ring-1 ring-emerald-400';
                  } else if (isSelected && !opt.isCorrect) {
                    optionStyles = 'border-rose-400 bg-rose-50 text-rose-950 ring-1 ring-rose-400';
                  } else {
                    optionStyles = 'border-stone-100 bg-stone-50 text-stone-400 opacity-60';
                  }
                } else if (isSelected) {
                  optionStyles = 'border-emerald-600 bg-emerald-50/50 text-stone-950 ring-1 ring-emerald-600 shadow-2xs';
                }

                return (
                  <button
                    key={opt.id}
                    type="button"
                    disabled={hasSubmittedAnswer || isSubmitting}
                    onClick={() => setSelectedOptionId(opt.id)}
                    className={`w-full text-left p-3.5 sm:p-4 rounded-xl border text-sm transition flex items-start gap-3 select-none ${optionStyles}`}
                  >
                    <span
                      className={`w-6 h-6 rounded-md flex items-center justify-center text-xs font-bold shrink-0 mt-0.5 ${
                        hasSubmittedAnswer && opt.isCorrect
                          ? 'bg-emerald-600 text-white'
                          : hasSubmittedAnswer && isSelected && !opt.isCorrect
                          ? 'bg-rose-600 text-white'
                          : isSelected
                          ? 'bg-emerald-600 text-white'
                          : 'bg-stone-100 text-stone-600'
                      }`}
                    >
                      {hasSubmittedAnswer && opt.isCorrect ? (
                        <CheckCircle2 className="w-4 h-4" />
                      ) : hasSubmittedAnswer && isSelected && !opt.isCorrect ? (
                        <XCircle className="w-4 h-4" />
                      ) : (
                        label
                      )}
                    </span>
                    <span className="flex-1 leading-snug">{opt.text}</span>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Feedback & Memory Engine Impact Section */}
          {hasSubmittedAnswer && lastReviewResult && (
            <div className="space-y-4 pt-4 border-t border-stone-200 animate-fade-in">
              {/* Feedback Banner */}
              <div
                className={`p-4 rounded-xl flex items-center justify-between text-sm ${
                  completedRecords[completedRecords.length - 1]?.isCorrect
                    ? 'bg-emerald-50 text-emerald-950 border border-emerald-200'
                    : 'bg-amber-50 text-amber-950 border border-amber-200'
                }`}
              >
                <div className="flex items-center gap-2.5">
                  {completedRecords[completedRecords.length - 1]?.isCorrect ? (
                    <CheckCircle2 className="w-5 h-5 text-emerald-600 shrink-0" />
                  ) : (
                    <RotateCw className="w-5 h-5 text-amber-600 shrink-0" />
                  )}
                  <div>
                    <span className="font-bold block">
                      {completedRecords[completedRecords.length - 1]?.isCorrect
                        ? 'Correct! Spaced repetition updated.'
                        : 'Needs practice! Scheduled for earlier review.'}
                    </span>
                    <span className="text-xs opacity-80">
                      Next review scheduled in{' '}
                      <strong>
                        {lastReviewResult.reviewIntervalDays}{' '}
                        {lastReviewResult.reviewIntervalDays === 1 ? 'day' : 'days'}
                      </strong>{' '}
                      ({new Date(lastReviewResult.nextReviewAt).toLocaleDateString()})
                    </span>
                  </div>
                </div>

                <div className="flex items-center gap-1.5 font-bold text-xs bg-white/80 px-2.5 py-1 rounded-full border border-black/[0.06] shadow-2xs">
                  <Flame className="w-3.5 h-3.5 text-amber-500 fill-amber-500" />
                  <span>+{lastReviewResult.xpEarned ?? 5} XP</span>
                </div>
              </div>

              {/* Lexical Details: Definition & Bengali Meaning */}
              <div className="bg-[#FAF8F5] p-4 rounded-xl border border-stone-200/80 space-y-2 text-xs">
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <span className="text-[10px] font-bold uppercase tracking-wider text-stone-400 block mb-0.5">
                      Bengali Meaning
                    </span>
                    <p className="text-sm font-semibold text-stone-900">{currentWord?.meaning}</p>
                  </div>
                  {currentWord?.definition && (
                    <div className="text-right">
                      <span className="text-[10px] font-bold uppercase tracking-wider text-stone-400 block mb-0.5">
                        Contextual Definition
                      </span>
                      <p className="text-xs text-stone-700 max-w-sm">{currentWord.definition}</p>
                    </div>
                  )}
                </div>

                {/* Example sentence if available */}
                {currentWord?.exampleSentence && (
                  <div className="pt-2 border-t border-stone-200/60">
                    <span className="text-[10px] font-bold uppercase tracking-wider text-stone-400 block mb-0.5">
                      Example
                    </span>
                    <p className="text-xs text-stone-700 italic">
                      "{currentWord.exampleSentence}"
                    </p>
                  </div>
                )}
              </div>

              {/* Memory Metrics Delta Bar */}
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 text-center text-xs">
                <div className="p-2.5 bg-stone-50 rounded-lg border border-stone-200/60">
                  <span className="text-[10px] text-stone-400 block">Mastery Score</span>
                  <span className="font-bold text-stone-900">
                    {Math.round(lastReviewResult.masteryScore)}%
                  </span>
                  {lastReviewResult.previousMasteryScore !== undefined && (
                    <span
                      className={`text-[10px] font-semibold block ${
                        lastReviewResult.masteryScore >= lastReviewResult.previousMasteryScore
                          ? 'text-emerald-700'
                          : 'text-amber-700'
                      }`}
                    >
                      {lastReviewResult.masteryScore >= lastReviewResult.previousMasteryScore ? '+' : ''}
                      {Math.round(lastReviewResult.masteryScore - lastReviewResult.previousMasteryScore)}%
                    </span>
                  )}
                </div>

                <div className="p-2.5 bg-stone-50 rounded-lg border border-stone-200/60">
                  <span className="text-[10px] text-stone-400 block">Forgetting Risk</span>
                  <span className="font-bold text-stone-900">
                    {lastReviewResult.forgettingRisk}
                  </span>
                </div>

                <div className="p-2.5 bg-stone-50 rounded-lg border border-stone-200/60">
                  <span className="text-[10px] text-stone-400 block">Review Interval</span>
                  <span className="font-bold text-stone-900">
                    {lastReviewResult.reviewIntervalDays}d
                  </span>
                </div>

                <div className="p-2.5 bg-stone-50 rounded-lg border border-stone-200/60">
                  <span className="text-[10px] text-stone-400 block">Retention Engine</span>
                  <span className="font-bold text-stone-900 font-mono text-[11px]">
                    {lastReviewResult.algorithm || 'SM-2'}
                  </span>
                </div>
              </div>

              {/* Optional AI Assistant Accordion */}
              <div className="pt-1">
                <button
                  type="button"
                  onClick={() => setShowAiDeepDive((prev) => !prev)}
                  className="flex items-center justify-between w-full py-2 px-3 text-xs font-semibold text-emerald-800 bg-emerald-50/60 hover:bg-emerald-50 rounded-lg border border-emerald-200/60 transition"
                >
                  <span className="flex items-center gap-1.5">
                    <Sparkles className="w-3.5 h-3.5 text-emerald-600" />
                    {showAiDeepDive ? 'Hide AI Learning Insights' : 'Explore with AI Learning Assistant'}
                  </span>
                  <span className="text-[11px] underline">
                    {showAiDeepDive ? 'Collapse' : 'Show Tips & Examples'}
                  </span>
                </button>

                {showAiDeepDive && (
                  <div className="mt-3 p-4 rounded-xl border border-stone-200 bg-white">
                    <WordStudyAiActions
                      word={currentWord?.word || ''}
                      wordId={currentWord?.wordId}
                      cefrLevel={currentWord?.difficultyLevel}
                    />
                  </div>
                )}
              </div>
            </div>
          )}

          {/* Action Footer */}
          <div className="pt-2 flex items-center justify-between gap-3 flex-wrap">
            {!hasSubmittedAnswer ? (
              <>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => handleSubmitReview(false)}
                  disabled={isSubmitting}
                  className="text-stone-500 hover:text-stone-800 text-xs py-2"
                >
                  <HelpCircle className="w-3.5 h-3.5 mr-1" />
                  Forgot / Don't Know
                </Button>

                <Button
                  variant="primary"
                  size="md"
                  onClick={() => handleSubmitReview()}
                  disabled={!selectedOptionId || isSubmitting}
                  isLoading={isSubmitting}
                  className="ml-auto text-xs font-bold px-6 shadow-sm"
                >
                  Check Answer <span className="hidden sm:inline ml-1 font-mono text-[10px] opacity-70">↵</span>
                </Button>
              </>
            ) : (
              <div className="w-full flex items-center justify-between gap-3">
                <span className="text-xs text-stone-400 hidden sm:inline">
                  Press <kbd className="px-1.5 py-0.5 bg-stone-100 border border-stone-300 rounded text-[10px]">Enter ↵</kbd> or click next
                </span>
                <Button
                  variant="primary"
                  size="md"
                  onClick={handleNextWord}
                  className="w-full sm:w-auto ml-auto text-xs font-bold px-7 shadow-sm"
                >
                  {currentIndex + 1 < sessionWords.length ? (
                    <>
                      Next Word <ArrowRight className="w-4 h-4 ml-1.5" />
                    </>
                  ) : (
                    <>
                      Complete Review <Award className="w-4 h-4 ml-1.5" />
                    </>
                  )}
                </Button>
              </div>
            )}
          </div>
        </Card>
      </div>
    </AppShell>
  );
};

export default ReviewPage;
