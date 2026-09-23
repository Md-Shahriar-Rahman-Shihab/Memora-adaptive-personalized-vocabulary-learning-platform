import React, { useEffect, useState, useRef } from 'react';
import { createPortal } from 'react-dom';
import {
  X,
  Volume2,
  VolumeX,
  CheckCircle2,
  Sparkles,
  ArrowRight,
  RefreshCw,
  BookOpen,
  AlertCircle,
  Award,
} from 'lucide-react';
import { LearningPathItemResponse, LearningItemCompletionResponse } from '../../types/learningPath';
import { VocabularyWordResponse } from '../../types/vocabulary';
import { DictionaryResponse } from '../../types/dictionary';
import { vocabularyApi } from '../../api/vocabularyApi';
import { dictionaryApi } from '../../api/dictionaryApi';
import { learningPathApi } from '../../api/learningPathApi';
import { WordStudyAiActions } from './WordStudyAiActions';
import { Badge } from '../ui/Badge';
import { Button } from '../ui/Button';

export interface WordStudyModalProps {
  isOpen: boolean;
  item: LearningPathItemResponse | null;
  targetLevel?: string;
  nextItem?: LearningPathItemResponse | null;
  onClose: () => void;
  onCompleted: (response: LearningItemCompletionResponse, completedItemId: number) => void;
  onNavigateToItem?: (item: LearningPathItemResponse) => void;
}

export const WordStudyModal: React.FC<WordStudyModalProps> = ({
  isOpen,
  item,
  targetLevel,
  nextItem,
  onClose,
  onCompleted,
  onNavigateToItem,
}) => {
  // Word & Dictionary state
  const [wordDetails, setWordDetails] = useState<VocabularyWordResponse | null>(null);
  const [dictionaryDetails, setDictionaryDetails] = useState<DictionaryResponse | null>(null);
  const [isDictionaryLoading, setIsDictionaryLoading] = useState<boolean>(false);
  const [dictionaryError, setDictionaryError] = useState<boolean>(false);

  // Audio state
  const [audioStatus, setAudioStatus] = useState<'idle' | 'playing' | 'error' | 'unavailable'>('idle');
  const audioRef = useRef<HTMLAudioElement | null>(null);

  // Content scroll container ref for resetting scroll on opening/switching words
  const contentRef = useRef<HTMLDivElement | null>(null);

  // Completion state
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [completionResult, setCompletionResult] = useState<LearningItemCompletionResponse | null>(null);
  const [completionError, setCompletionError] = useState<string | null>(null);

  // Reset & load data when item changes or modal opens
  useEffect(() => {
    if (!isOpen || !item) {
      setWordDetails(null);
      setDictionaryDetails(null);
      setCompletionResult(null);
      setCompletionError(null);
      setAudioStatus('idle');
      if (audioRef.current) {
        audioRef.current.pause();
        audioRef.current = null;
      }
      return;
    }

    // Always reset scroll to the top when opening or switching words
    if (contentRef.current) {
      contentRef.current.scrollTop = 0;
      contentRef.current.scrollTo({
        top: 0,
        behavior: 'instant',
      });
    }

    const raf = requestAnimationFrame(() => {
      if (contentRef.current) {
        contentRef.current.scrollTop = 0;
        contentRef.current.scrollTo({
          top: 0,
          behavior: 'instant',
        });
      }
    });

    // Reset for the new item
    setCompletionResult(null);
    setCompletionError(null);
    setAudioStatus('idle');

    // Fetch database word details if wordId is available
    if (item.wordId) {
      vocabularyApi
        .getWordById(item.wordId)
        .then((res) => {
          if (res.success && res.data) {
            setWordDetails(res.data);
          }
        })
        .catch(() => {
          // Non-blocking fallback
        });
    }

    // Fetch official Merriam-Webster dictionary data via Memora backend
    const wordText = item.word || '';
    if (wordText) {
      setIsDictionaryLoading(true);
      setDictionaryError(false);
      dictionaryApi
        .lookupWord(wordText)
        .then((res) => {
          if (res.success && res.data) {
            setDictionaryDetails(res.data);
            if (!res.data.audioUrl) {
              setAudioStatus('unavailable');
            }
          } else {
            setDictionaryError(true);
            setAudioStatus('unavailable');
          }
        })
        .catch(() => {
          setDictionaryError(true);
          setAudioStatus('unavailable');
        })
        .finally(() => {
          setIsDictionaryLoading(false);
        });
    }

    return () => cancelAnimationFrame(raf);
  }, [isOpen, item?.id, item?.word]);

  // Cleanup audio on unmount
  useEffect(() => {
    return () => {
      if (audioRef.current) {
        audioRef.current.pause();
        audioRef.current = null;
      }
    };
  }, []);

  // Background page scroll-lock and keyboard navigation: Escape to close
  useEffect(() => {
    if (!isOpen) return;

    const originalBodyOverflow = document.body.style.overflow;
    const originalHtmlOverflow = document.documentElement.style.overflow;

    document.body.style.overflow = 'hidden';
    document.documentElement.style.overflow = 'hidden';

    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        onClose();
      }
    };
    window.addEventListener('keydown', handleKeyDown);

    return () => {
      document.body.style.overflow = originalBodyOverflow;
      document.documentElement.style.overflow = originalHtmlOverflow;
      window.removeEventListener('keydown', handleKeyDown);
    };
  }, [isOpen, onClose]);

  if (!isOpen || !item) return null;

  // Resolve best fields available
  const word = item.word || wordDetails?.word || 'Word';
  const pronunciation =
    dictionaryDetails?.pronunciation || item.pronunciation || wordDetails?.pronunciation || null;
  const partOfSpeech =
    dictionaryDetails?.partsOfSpeech?.[0]?.partOfSpeech ||
    wordDetails?.category?.replace(/_/g, ' ').toLowerCase() ||
    null;
  const cefr = wordDetails?.difficultyLevel || targetLevel || 'B1';

  const definition =
    dictionaryDetails?.shortDefinitions?.[0] ||
    dictionaryDetails?.partsOfSpeech?.[0]?.definitions?.[0]?.definition ||
    wordDetails?.definition ||
    item.definition ||
    wordDetails?.meaning ||
    item.meaning ||
    null;

  const exampleSentence =
    wordDetails?.exampleSentence ||
    item.exampleSentence ||
    dictionaryDetails?.partsOfSpeech?.[0]?.definitions?.[0]?.examples?.[0] ||
    null;

  const audioUrl = dictionaryDetails?.audioUrl || null;

  // Handle playing pronunciation audio from backend-resolved CDN URL
  const handlePlayAudio = () => {
    if (audioStatus === 'playing') {
      if (audioRef.current) {
        audioRef.current.pause();
        audioRef.current.currentTime = 0;
      }
      setAudioStatus('idle');
      return;
    }

    if (audioUrl) {
      try {
        if (audioRef.current) {
          audioRef.current.pause();
        }
        const audio = new Audio(audioUrl);
        audioRef.current = audio;
        setAudioStatus('playing');

        audio.onended = () => {
          setAudioStatus('idle');
        };
        audio.onerror = () => {
          // Gracefully fall back to browser Web Speech API
          fallbackSpeak(word);
        };

        audio.play().catch(() => {
          fallbackSpeak(word);
        });
      } catch {
        fallbackSpeak(word);
      }
    } else {
      // If no CDN audio exists, try browser speech synthesis
      fallbackSpeak(word);
    }
  };

  const fallbackSpeak = (text: string) => {
    if ('speechSynthesis' in window && text) {
      window.speechSynthesis.cancel();
      const utterance = new SpeechSynthesisUtterance(text);
      utterance.lang = 'en-US';
      utterance.rate = 0.9;
      setAudioStatus('playing');
      utterance.onend = () => setAudioStatus('idle');
      utterance.onerror = () => setAudioStatus('unavailable');
      window.speechSynthesis.speak(utterance);
    } else {
      setAudioStatus('unavailable');
    }
  };

  // Handle Mark Complete with double-click prevention
  const handleMarkComplete = async () => {
    if (isSubmitting) return;

    // If item was already completed, don't re-submit
    if (item.status === 'COMPLETED' || completionResult) {
      return;
    }

    setIsSubmitting(true);
    setCompletionError(null);

    try {
      const res = await learningPathApi.completeItem(item.id, {
        correct: true,
        responseTimeMs: 1500,
        algorithm: 'SM2',
      });

      if (res.success && res.data) {
        setCompletionResult(res.data);
        onCompleted(res.data, item.id);
      } else {
        setCompletionError('Failed to complete item. Please try again.');
      }
    } catch (err: any) {
      setCompletionError(
        err?.response?.data?.message || 'Network error while completing word. Please try again.'
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleNextWordClick = () => {
    if (nextItem && onNavigateToItem) {
      onNavigateToItem(nextItem);
    } else {
      onClose();
    }
  };

  if (!isOpen || !item) return null;
  if (typeof document === 'undefined') return null;

  const isAlreadyCompleted = item.status === 'COMPLETED' || completionResult !== null;
  const xpEarned = completionResult?.xpEarned ?? (isAlreadyCompleted ? 0 : 15);

  return createPortal(
    <div
      role="dialog"
      aria-modal="true"
      aria-labelledby="word-study-title"
      className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-4 md:p-6 bg-black/50 backdrop-blur-sm animate-fade-in"
      onClick={(e) => {
        if (e.target === e.currentTarget) {
          onClose();
        }
      }}
    >
      <div className="w-full max-w-2xl max-h-[calc(100dvh-2rem)] sm:max-h-[calc(100dvh-4rem)] flex flex-col min-h-0 bg-white rounded-2xl sm:rounded-3xl shadow-2xl border border-stone-200 overflow-hidden transform transition-all animate-scale-up">
        {/* Header Bar */}
        <div className="flex items-center justify-between px-5 sm:px-7 py-3.5 sm:py-4 border-b border-stone-100 bg-[#FAF9F6] shrink-0">
          <div className="flex items-center gap-2">
            <span className="flex items-center justify-center w-7 h-7 rounded-xl bg-emerald-100 text-emerald-800 font-bold text-xs">
              <Sparkles className="w-4 h-4 text-emerald-700" />
            </span>
            <span className="text-xs font-bold uppercase tracking-wider text-stone-600">
              Word Study Experience
            </span>
          </div>

          <div className="flex items-center gap-2">
            {isAlreadyCompleted && (
              <Badge variant="green" size="sm">
                <CheckCircle2 className="w-3 h-3" /> Completed
              </Badge>
            )}
            <button
              type="button"
              onClick={onClose}
              className="p-1.5 rounded-xl text-stone-400 hover:text-stone-700 hover:bg-stone-200/60 transition"
              aria-label="Close word study"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* Scrollable Content */}
        <div
          ref={contentRef}
          className="flex-1 min-h-0 overflow-y-auto overscroll-contain p-5 sm:p-7 space-y-5 sm:space-y-6 focus:outline-none"
        >
          {/* Main Word Presentation Banner */}
          <div className="rounded-2xl sm:rounded-3xl bg-gradient-to-br from-emerald-50/70 via-white to-stone-50/50 border border-emerald-100/90 p-4 sm:p-6 space-y-3 shadow-xs">
            <div className="flex flex-wrap items-center justify-between gap-3">
              {/* Word & Pronunciation */}
              <div className="space-y-1">
                <div className="flex items-center gap-3 flex-wrap">
                  <h1
                    id="word-study-title"
                    className="text-3xl sm:text-4xl font-black text-memora-dark tracking-tight break-words capitalize"
                  >
                    {word}
                  </h1>

                  {/* Audio Listen Button */}
                  {audioStatus !== 'unavailable' ? (
                    <button
                      type="button"
                      onClick={handlePlayAudio}
                      disabled={audioStatus === 'playing'}
                      className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-semibold transition shadow-xs ${
                        audioStatus === 'playing'
                          ? 'bg-emerald-600 text-white animate-pulse'
                          : 'bg-emerald-100/90 hover:bg-emerald-200 text-emerald-900'
                      }`}
                      title={audioUrl ? 'Listen to Merriam-Webster pronunciation' : 'Listen to pronunciation'}
                      aria-label="Pronounce word"
                    >
                      <Volume2
                        className={`w-4 h-4 ${audioStatus === 'playing' ? 'animate-bounce' : ''}`}
                      />
                      <span>{audioStatus === 'playing' ? 'Playing...' : 'Listen'}</span>
                    </button>
                  ) : (
                    <span
                      className="inline-flex items-center gap-1 text-[11px] font-medium text-stone-400 px-2 py-1 rounded-lg bg-stone-100"
                      title="Pronunciation audio unavailable for this word"
                    >
                      <VolumeX className="w-3.5 h-3.5 text-stone-400" />
                      <span>Audio unavailable</span>
                    </span>
                  )}
                </div>

                {/* Phonetic Pronunciation & Part of Speech */}
                <div className="flex items-center gap-2.5 flex-wrap pt-0.5">
                  {pronunciation && (
                    <span className="text-sm font-mono text-stone-500 bg-white/80 px-2.5 py-0.5 rounded-lg border border-stone-200/60 shadow-2xs">
                      /{pronunciation}/
                    </span>
                  )}
                  {partOfSpeech && (
                    <span className="text-xs font-semibold italic text-stone-600 bg-stone-100/80 px-2.5 py-0.5 rounded-lg">
                      {partOfSpeech}
                    </span>
                  )}
                </div>
              </div>

              {/* CEFR Level Badge */}
              <div className="flex items-center gap-2 shrink-0">
                <Badge variant="green" size="md" className="font-bold tracking-wider">
                  CEFR {cefr}
                </Badge>
              </div>
            </div>

            {/* Primary Definition */}
            {definition ? (
              <div className="pt-2 border-t border-emerald-100/70 space-y-1">
                <span className="text-[10px] font-black uppercase tracking-wider text-emerald-800">
                  Definition
                </span>
                <p className="text-sm sm:text-base text-stone-800 leading-relaxed font-medium">
                  {definition}
                </p>
              </div>
            ) : isDictionaryLoading ? (
              <div className="py-2 flex items-center gap-2 text-xs text-stone-500">
                <RefreshCw className="w-3.5 h-3.5 animate-spin text-emerald-600" />
                <span>Fetching dictionary definition...</span>
              </div>
            ) : null}
          </div>

          {/* Example Sentence Section */}
          <div className="rounded-2xl bg-[#FBFBFA] border border-stone-200/80 p-4 sm:p-5 space-y-2">
            <div className="flex items-center justify-between">
              <span className="text-[11px] font-bold uppercase tracking-wider text-stone-500 flex items-center gap-1.5">
                <BookOpen className="w-3.5 h-3.5 text-stone-500" />
                Contextual Example
              </span>
              {exampleSentence && (
                <button
                  type="button"
                  onClick={() => fallbackSpeak(exampleSentence)}
                  className="text-xs text-stone-500 hover:text-stone-800 flex items-center gap-1 font-semibold transition"
                  title="Listen to example sentence"
                >
                  <Volume2 className="w-3.5 h-3.5" />
                  <span>Listen</span>
                </button>
              )}
            </div>

            {exampleSentence ? (
              <p className="text-xs sm:text-sm font-medium italic text-stone-800 leading-relaxed bg-white p-3 rounded-xl border border-stone-200/60 shadow-2xs">
                "{exampleSentence}"
              </p>
            ) : (
              <p className="text-xs text-stone-500 italic">
                No example sentence in local database. Click{' '}
                <span className="font-semibold text-stone-700">"Personalized Example"</span> below to generate one with AI!
              </p>
            )}
          </div>

          {/* Optional AI Learning Tools (Non-blocking) */}
          <div className="border-t border-stone-100 pt-4">
            <WordStudyAiActions
              word={word}
              wordId={wordDetails?.id || item.wordId}
              cefrLevel={cefr}
              onSpeak={fallbackSpeak}
            />
          </div>

          {/* Completion Error Banner */}
          {completionError && (
            <div className="flex items-center justify-between p-3.5 rounded-2xl bg-red-50 border border-red-200 text-red-800 text-xs">
              <div className="flex items-center gap-2">
                <AlertCircle className="w-4 h-4 shrink-0 text-red-600" />
                <span>{completionError}</span>
              </div>
              <button
                type="button"
                onClick={handleMarkComplete}
                className="px-2.5 py-1 rounded-lg bg-white border border-red-300 text-red-700 hover:bg-red-50 font-semibold shadow-xs transition shrink-0"
              >
                Retry
              </button>
            </div>
          )}

          {/* Completion Success Banner */}
          {completionResult && (
            <div className="p-4 sm:p-5 rounded-2xl bg-gradient-to-r from-emerald-500/15 via-emerald-500/5 to-transparent border border-emerald-500/30 flex items-center justify-between gap-4 animate-fade-in">
              <div className="space-y-0.5">
                <div className="flex items-center gap-2">
                  <CheckCircle2 className="w-5 h-5 text-emerald-600 shrink-0" />
                  <span className="text-sm sm:text-base font-extrabold text-emerald-950">
                    Word completed!
                  </span>
                  {xpEarned > 0 && (
                    <span className="inline-flex items-center gap-1 text-xs font-black px-2.5 py-0.5 rounded-full bg-emerald-600 text-white shadow-xs">
                      <Award className="w-3 h-3" /> +{xpEarned} XP
                    </span>
                  )}
                </div>
                <p className="text-xs text-stone-600">
                  {completionResult.pathCompleted
                    ? '🎉 You have completed all scheduled tasks for today!'
                    : 'Scheduled for retention review in your spaced-repetition memory engine.'}
                </p>
              </div>

              {nextItem ? (
                <Button
                  variant="primary"
                  size="sm"
                  onClick={handleNextWordClick}
                  rightIcon={<ArrowRight className="w-4 h-4" />}
                  className="shrink-0 shadow-sm"
                >
                  Next Word
                </Button>
              ) : (
                <Button
                  variant="outline"
                  size="sm"
                  onClick={onClose}
                  className="shrink-0 text-xs"
                >
                  Done
                </Button>
              )}
            </div>
          )}
        </div>

        {/* Modal Footer Actions */}
        <div className="px-5 sm:px-7 py-3.5 sm:py-4 border-t border-stone-100 bg-[#FAF9F6] flex items-center justify-between gap-3 shrink-0">
          <Button
            variant="ghost"
            size="sm"
            onClick={onClose}
            className="text-stone-500 hover:text-stone-800 text-xs sm:text-sm font-medium"
          >
            Close
          </Button>

          <div className="flex items-center gap-2">
            {!isAlreadyCompleted ? (
              <Button
                variant="primary"
                size="md"
                isLoading={isSubmitting}
                onClick={handleMarkComplete}
                leftIcon={<CheckCircle2 className="w-4 h-4" />}
                className="shadow-sm min-w-[130px] sm:min-w-[150px] font-bold text-xs sm:text-sm"
              >
                Mark Complete
              </Button>
            ) : nextItem ? (
              <Button
                variant="primary"
                size="md"
                onClick={handleNextWordClick}
                rightIcon={<ArrowRight className="w-4 h-4" />}
                className="shadow-sm font-bold text-xs sm:text-sm"
              >
                Next Word ({nextItem.word})
              </Button>
            ) : (
              <Button
                variant="primary"
                size="md"
                onClick={onClose}
                className="shadow-sm font-bold text-xs sm:text-sm"
              >
                Finish Session
              </Button>
            )}
          </div>
        </div>
      </div>
    </div>,
    document.body
  );
};

export default WordStudyModal;
