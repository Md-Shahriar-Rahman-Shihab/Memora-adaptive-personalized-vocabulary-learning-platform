import React, { useState, useEffect, useRef, useMemo } from 'react';
import { useParams, useNavigate, useSearchParams } from 'react-router-dom';
import {
  ArrowLeft,
  Volume2,
  VolumeX,
  Sparkles,
  BookOpen,
  CheckCircle2,
  AlertCircle,
  HelpCircle,
  Brain,
  Calendar,
  Zap,
  TrendingUp,
  RefreshCw,
  Search,
  ExternalLink,
  ChevronRight,
  ShieldAlert,
} from 'lucide-react';
import Navbar from '../components/layout/Navbar';
import TopHeader from '../components/layout/TopHeader';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { WordFamilyTree } from '../components/vocabulary/WordFamilyTree';
import { WordStudyAiActions } from '../components/vocabulary/WordStudyAiActions';
import { dictionaryApi } from '../api/dictionaryApi';
import { vocabularyApi } from '../api/vocabularyApi';
import { learningPathApi } from '../api/learningPathApi';
import { aiApi } from '../api/aiApi';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { DictionaryResponse } from '../types/dictionary';
import { VocabularyWordResponse, UserWordProgressResponse } from '../types/vocabulary';
import { LearningPathItemResponse, LearningItemCompletionResponse } from '../types/learningPath';

export const WordDetailPage: React.FC = () => {
  const { word } = useParams<{ word: string }>();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { isAuthenticated, user, refreshUser } = useAuth();
  const { addToast } = useToast();

  const pathItemId = searchParams.get('pathItemId');
  const fromSource = searchParams.get('from');

  const decodedWord = useMemo(() => {
    return word ? decodeURIComponent(word).trim() : '';
  }, [word]);

  // Loading & Error states
  const [isLoading, setIsLoading] = useState(true);
  const [isNotFound, setIsNotFound] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  // Data states
  const [dictEntry, setDictEntry] = useState<DictionaryResponse | null>(null);
  const [vocabWord, setVocabWord] = useState<VocabularyWordResponse | null>(null);
  const [wordProgress, setWordProgress] = useState<UserWordProgressResponse | null>(null);
  const [learningItem, setLearningItem] = useState<LearningPathItemResponse | null>(null);

  // Enriched relations (synonyms, antonyms, word family)
  const [synonymsList, setSynonymsList] = useState<string[]>([]);
  const [antonymsList, setAntonymsList] = useState<string[]>([]);
  const [familyMap, setFamilyMap] = useState<Record<string, string>>({});

  // Learning completion state
  const [isSubmittingCompletion, setIsSubmittingCompletion] = useState(false);
  const [completionResult, setCompletionResult] = useState<LearningItemCompletionResponse | null>(null);
  const [nextPathItem, setNextPathItem] = useState<LearningPathItemResponse | null>(null);

  // Audio playback state
  const [isPlayingAudio, setIsPlayingAudio] = useState(false);
  const audioRef = useRef<HTMLAudioElement | null>(null);

  // Contextual search in not-found state
  const [altSearchQuery, setAltSearchQuery] = useState('');

  // Audio cleanup on unmount
  useEffect(() => {
    return () => {
      if (audioRef.current) {
        audioRef.current.pause();
        audioRef.current = null;
      }
    };
  }, []);

  const handleSpeak = (text: string) => {
    if ('speechSynthesis' in window && text) {
      window.speechSynthesis.cancel();
      const utterance = new SpeechSynthesisUtterance(text);
      utterance.lang = 'en-US';
      utterance.rate = 0.9;
      setIsPlayingAudio(true);
      utterance.onend = () => setIsPlayingAudio(false);
      utterance.onerror = () => setIsPlayingAudio(false);
      window.speechSynthesis.speak(utterance);
    }
  };

  const playAudio = (audioUrl?: string | null, fallbackText?: string) => {
    if (audioRef.current) {
      audioRef.current.pause();
      audioRef.current = null;
    }

    if (isPlayingAudio) {
      setIsPlayingAudio(false);
      return;
    }

    if (audioUrl) {
      try {
        const audio = new Audio(audioUrl);
        audioRef.current = audio;
        setIsPlayingAudio(true);

        audio.onended = () => {
          setIsPlayingAudio(false);
          audioRef.current = null;
        };

        audio.onerror = () => {
          setIsPlayingAudio(false);
          if (fallbackText) handleSpeak(fallbackText);
        };

        audio.play().catch(() => {
          setIsPlayingAudio(false);
          if (fallbackText) handleSpeak(fallbackText);
        });
      } catch {
        if (fallbackText) handleSpeak(fallbackText);
      }
    } else if (fallbackText) {
      handleSpeak(fallbackText);
    }
  };

  // Main data loader
  useEffect(() => {
    if (!decodedWord) {
      setIsNotFound(true);
      setIsLoading(false);
      return;
    }

    let isMounted = true;
    setIsLoading(true);
    setIsNotFound(false);
    setErrorMessage(null);
    setCompletionResult(null);

    const loadData = async () => {
      let resolvedDictionary: DictionaryResponse | null = null;
      let matchedVocab: VocabularyWordResponse | null = null;

      // 1. Fetch official dictionary data
      try {
        const dictRes = await dictionaryApi.lookupWord(decodedWord);
        if (dictRes.success && dictRes.data) {
          resolvedDictionary = dictRes.data;
          if (isMounted) setDictEntry(dictRes.data);
        }
      } catch (err: any) {
        if (err.response?.status === 404) {
          // Check spelling or suggestions
        } else {
          // Dictionary error
        }
      }

      // 2. Search database catalog for matching CEFR and metadata
      try {
        const vocabRes = await vocabularyApi.searchVocabulary(decodedWord);
        if (vocabRes.success && vocabRes.data && vocabRes.data.length > 0) {
          const exact = vocabRes.data.find(
            (v) => v.word.toLowerCase() === decodedWord.toLowerCase()
          );
          matchedVocab = exact || vocabRes.data[0];
          if (isMounted) setVocabWord(matchedVocab);
        }
      } catch {
        // Non-blocking catalog search
      }

      // If neither dictionary nor vocabulary was found
      if (!resolvedDictionary && !matchedVocab) {
        if (isMounted) {
          setIsNotFound(true);
          setIsLoading(false);
        }
        return;
      }

      // 3. If authenticated, fetch memory retention progress
      if (isAuthenticated && matchedVocab?.id) {
        try {
          const progRes = await vocabularyApi.getWordProgress(matchedVocab.id);
          if (progRes.success && progRes.data && isMounted) {
            setWordProgress(progRes.data);
          }
        } catch {
          // Non-blocking
        }
      }

      // 4. If pathItemId is present, fetch today's learning path to link progress
      if (isAuthenticated && pathItemId) {
        try {
          const pathRes = await learningPathApi.getTodayPath();
          if (pathRes.success && pathRes.data && isMounted) {
            const currentItem = pathRes.data.items.find(
              (it) => it.id === Number(pathItemId) || it.word?.toLowerCase() === decodedWord.toLowerCase()
            );
            if (currentItem) {
              setLearningItem(currentItem);
              // Find next pending item
              const nextItem = pathRes.data.items.find(
                (it) =>
                  it.type === 'NEW_WORD' &&
                  it.status !== 'COMPLETED' &&
                  it.id !== currentItem.id
              );
              setNextPathItem(nextItem || null);
            }
          }
        } catch {
          // Non-blocking
        }
      }

      // 5. Initialize synonyms, antonyms, and word family from dictionary data
      const initialSynonyms = (resolvedDictionary?.synonyms || []).filter(
        (s) => s && s.toLowerCase() !== decodedWord.toLowerCase() && s.trim().length > 0
      );
      const initialAntonyms = (resolvedDictionary?.antonyms || []).filter(
        (a) => a && a.toLowerCase() !== decodedWord.toLowerCase() && a.trim().length > 0
      );
      const initialFamily = resolvedDictionary?.wordFamily || {};

      if (isMounted) {
        setSynonymsList(initialSynonyms);
        setAntonymsList(initialAntonyms);
        setFamilyMap(initialFamily);
      }

      // 6. Check if AI relations are needed (Priority 2):
      // If Merriam-Webster already provided both reliable synonyms and antonyms, avoid unnecessary AI calls
      const needsAiRelations =
        initialSynonyms.length < 2 ||
        initialAntonyms.length === 0 ||
        Object.keys(initialFamily).length <= 1;

      if (needsAiRelations) {
        try {
          const resolvedPos =
            resolvedDictionary?.partsOfSpeech?.[0]?.partOfSpeech ||
            (matchedVocab?.category ? matchedVocab.category.toLowerCase().replace('_', ' ') : undefined);

          const resolvedDefinition =
            resolvedDictionary?.shortDefinitions?.[0] ||
            resolvedDictionary?.partsOfSpeech?.[0]?.definitions?.[0]?.definition ||
            matchedVocab?.definition ||
            matchedVocab?.meaning ||
            undefined;

          const relationsRes = await aiApi.getWordRelations({
            word: decodedWord,
            wordId: matchedVocab?.id,
            cefrLevel: matchedVocab?.difficultyLevel || 'B2',
            partOfSpeech: resolvedPos,
            definition: resolvedDefinition,
          });

          if (relationsRes.success && relationsRes.data && isMounted) {
            const aiData = relationsRes.data;

            // Merge AI synonyms if dictionary had none
            if (initialSynonyms.length === 0 && aiData.synonyms && aiData.synonyms.length > 0) {
              const cleanAiSynonyms = aiData.synonyms.filter(
                (s) => s && s.toLowerCase() !== decodedWord.toLowerCase() && s.trim().length > 0
              );
              setSynonymsList(Array.from(new Set(cleanAiSynonyms)));
            }

            // Merge AI antonyms if dictionary had none
            if (initialAntonyms.length === 0 && aiData.antonyms && aiData.antonyms.length > 0) {
              const cleanAiAntonyms = aiData.antonyms.filter(
                (a) => a && a.toLowerCase() !== decodedWord.toLowerCase() && a.trim().length > 0
              );
              setAntonymsList(Array.from(new Set(cleanAiAntonyms)));
            }

            // Merge AI word family forms with existing dictionary forms (dictionary takes precedence)
            if (aiData.wordFamily && Object.keys(aiData.wordFamily).length > 0) {
              setFamilyMap((prev) => ({
                ...aiData.wordFamily,
                ...prev, // Dictionary forms take precedence
              }));
            }
          }
        } catch {
          // Non-blocking AI fallback
        }
      }

      if (isMounted) {
        setIsLoading(false);
      }
    };

    loadData();

    return () => {
      isMounted = false;
    };
  }, [decodedWord, pathItemId, isAuthenticated]);

  // Handle Mark as Learned / Complete Learning Item
  const handleCompleteWord = async () => {
    if (!learningItem || isSubmittingCompletion) return;

    setIsSubmittingCompletion(true);
    try {
      const res = await learningPathApi.completeItem(learningItem.id, {
        correct: true,
        responseTimeMs: 1500,
        algorithm: 'SM2',
      });

      if (res.success && res.data) {
        setCompletionResult(res.data);
        setLearningItem((prev) => (prev ? { ...prev, status: 'COMPLETED' } : null));

        // Toast notification
        addToast({
          type: 'xp',
          title: 'Word Mastered! 🎉',
          message: res.data.message || `You've learned "${decodedWord}".`,
          xpAmount: res.data.xpEarned ?? 15,
        });

        // Refresh user profile (XP, streaks, achievements)
        await refreshUser();
      }
    } catch (err: any) {
      addToast({
        type: 'error',
        title: 'Completion Error',
        message: err?.response?.data?.message || 'Failed to mark word as learned.',
      });
    } finally {
      setIsSubmittingCompletion(false);
    }
  };

  // Derive display values safely
  const headwordDisplay = dictEntry?.headword || vocabWord?.word || decodedWord;
  const pronunciationText = dictEntry?.pronunciation || vocabWord?.pronunciation || null;
  const audioUrl = dictEntry?.audioUrl || null;

  const partOfSpeechDisplay =
    dictEntry?.partsOfSpeech?.[0]?.partOfSpeech ||
    (vocabWord?.category ? vocabWord.category.toLowerCase().replace('_', ' ') : 'vocabulary');

  const cefrDisplay =
    vocabWord?.difficultyLevel ||
    (learningItem ? 'C1' : 'B2');

  const definitionDisplay =
    dictEntry?.shortDefinitions?.[0] ||
    dictEntry?.partsOfSpeech?.[0]?.definitions?.[0]?.definition ||
    vocabWord?.definition ||
    vocabWord?.meaning ||
    'Definition unavailable.';

  const exampleDisplay =
    dictEntry?.partsOfSpeech?.[0]?.definitions?.[0]?.examples?.[0] ||
    vocabWord?.exampleSentence ||
    null;

  // Render Skeleton Loading State
  if (isLoading) {
    return (
      <div className="min-h-screen bg-[#FBFBF9] text-[#141A14] flex flex-col">
        {isAuthenticated ? <TopHeader title="Word Details" subtitle="Loading word definitions..." /> : <Navbar />}
        <main className="flex-1 max-w-4xl w-full mx-auto px-6 py-10 space-y-6 animate-pulse">
          <div className="h-6 w-36 bg-stone-200 rounded-lg" />
          <div className="p-8 rounded-3xl bg-white border border-stone-200 space-y-4">
            <div className="h-10 w-48 bg-stone-200 rounded-xl" />
            <div className="h-4 w-32 bg-stone-100 rounded-md" />
            <div className="h-16 w-full bg-stone-100 rounded-xl" />
          </div>
          <div className="p-6 rounded-3xl bg-white border border-stone-200 space-y-3">
            <div className="h-4 w-28 bg-stone-200 rounded-md" />
            <div className="h-12 w-full bg-stone-100 rounded-xl" />
          </div>
        </main>
      </div>
    );
  }

  // Render Not Found State
  if (isNotFound) {
    return (
      <div className="min-h-screen bg-[#FBFBF9] text-[#141A14] flex flex-col">
        {isAuthenticated ? <TopHeader title="Word Search" subtitle="Vocabulary lookup" /> : <Navbar />}
        <main className="flex-1 max-w-xl w-full mx-auto px-6 py-16 text-center space-y-6">
          <Card variant="default" className="p-8 sm:p-10 space-y-6 bg-white border border-black/[0.08] rounded-3xl shadow-card">
            <div className="w-14 h-14 rounded-2xl bg-amber-100 text-amber-800 flex items-center justify-center mx-auto">
              <AlertCircle className="w-7 h-7" />
            </div>

            <div className="space-y-2">
              <h2 className="text-2xl sm:text-3xl font-extrabold text-memora-dark tracking-tight">
                We couldn't find "{decodedWord}"
              </h2>
              <p className="text-xs sm:text-sm text-memora-text-muted leading-relaxed max-w-sm mx-auto">
                Please check the spelling or explore another vocabulary term from our curriculum.
              </p>
            </div>

            {/* Quick search input */}
            <form
              onSubmit={(e) => {
                e.preventDefault();
                if (altSearchQuery.trim()) {
                  navigate(`/word/${encodeURIComponent(altSearchQuery.trim())}`);
                }
              }}
              className="flex items-center gap-2"
            >
              <input
                type="text"
                value={altSearchQuery}
                onChange={(e) => setAltSearchQuery(e.target.value)}
                placeholder="Search another word..."
                className="flex-1 px-4 py-2.5 rounded-xl border border-stone-300 text-sm focus:outline-hidden focus:ring-2 focus:ring-memora-green/20 focus:border-memora-green"
              />
              <Button type="submit" variant="primary" size="md">
                Search
              </Button>
            </form>

            <div className="pt-2">
              <Button
                variant="outline"
                size="md"
                onClick={() => {
                  if (fromSource === 'learning-path') {
                    navigate('/learn-path');
                  } else {
                    navigate(-1);
                  }
                }}
                leftIcon={<ArrowLeft className="w-4 h-4" />}
                className="mx-auto"
              >
                Go back
              </Button>
            </div>
          </Card>
        </main>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[#FBFBF9] text-[#141A14] flex flex-col">
      {isAuthenticated ? <TopHeader title="Word Details" subtitle="Comprehensive linguistic and memory profile" /> : <Navbar />}

      <main className="flex-1 max-w-4xl w-full mx-auto px-4 sm:px-6 py-8 sm:py-10 space-y-6 sm:space-y-8">
        {/* Back Navigation Bar */}
        <div className="flex items-center justify-between gap-4">
          <button
            type="button"
            onClick={() => {
              if (fromSource === 'learning-path' || isAuthenticated) {
                navigate('/learn-path');
              } else {
                navigate('/');
              }
            }}
            className="inline-flex items-center gap-2 text-xs sm:text-sm font-semibold text-stone-600 hover:text-memora-green transition-colors group cursor-pointer"
          >
            <ArrowLeft className="w-4 h-4 transition-transform group-hover:-translate-x-1" />
            <span>
              {fromSource === 'learning-path'
                ? 'Back to Learning Path'
                : isAuthenticated
                ? 'Back to Learning Path'
                : 'Back to Home'}
            </span>
          </button>

          {/* Path Item Status Pill */}
          {learningItem && (
            <Badge
              variant={
                learningItem.status === 'COMPLETED' || completionResult
                  ? 'green'
                  : 'amber'
              }
              size="md"
            >
              {learningItem.status === 'COMPLETED' || completionResult ? (
                <>
                  <CheckCircle2 className="w-3.5 h-3.5" /> Path Item Completed
                </>
              ) : (
                <>Curriculum Item</>
              )}
            </Badge>
          )}
        </div>

        {/* SECTION A: Word Header Card */}
        <Card
          variant="default"
          className="p-6 sm:p-9 bg-white border border-black/[0.08] shadow-card rounded-3xl space-y-5"
        >
          <div className="flex flex-wrap items-start justify-between gap-4 border-b border-black/[0.06] pb-6">
            <div className="space-y-2">
              {/* Badges row */}
              <div className="flex items-center gap-2 flex-wrap">
                <Badge variant="neutral" size="sm">
                  CEFR {cefrDisplay}
                </Badge>
                <Badge variant="green" size="sm">
                  {partOfSpeechDisplay}
                </Badge>
              </div>

              {/* Large Headword */}
              <h1 className="text-3xl sm:text-4xl md:text-5xl font-extrabold text-memora-dark tracking-tight">
                {headwordDisplay}
              </h1>

              {/* Pronunciation & Audio */}
              <div className="flex items-center gap-3 pt-1">
                {pronunciationText && (
                  <span className="font-mono text-sm sm:text-base text-stone-500">
                    /{pronunciationText}/
                  </span>
                )}

                <button
                  type="button"
                  onClick={() => playAudio(audioUrl, decodedWord)}
                  className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-semibold transition-all ${
                    isPlayingAudio
                      ? 'bg-memora-green text-white shadow-xs'
                      : 'bg-stone-100 hover:bg-stone-200 text-stone-700'
                  }`}
                  title="Listen to pronunciation"
                  aria-label="Listen to pronunciation"
                >
                  {isPlayingAudio ? (
                    <>
                      <VolumeX className="w-3.5 h-3.5" />
                      <span>Playing</span>
                    </>
                  ) : (
                    <>
                      <Volume2 className="w-3.5 h-3.5 text-memora-green" />
                      <span>Listen</span>
                    </>
                  )}
                </button>
              </div>
            </div>

            {/* Completion / Study Action Button */}
            {learningItem && (
              <div className="shrink-0 space-y-1 text-right">
                {learningItem.status === 'COMPLETED' || completionResult ? (
                  <div className="flex items-center gap-2">
                    <span className="inline-flex items-center gap-1.5 px-4 py-2 rounded-2xl bg-emerald-100 text-emerald-800 text-xs font-bold border border-emerald-200">
                      <CheckCircle2 className="w-4 h-4" /> Learned (+15 XP)
                    </span>
                    {nextPathItem && (
                      <Button
                        variant="primary"
                        size="md"
                        onClick={() =>
                          navigate(
                            `/word/${encodeURIComponent(nextPathItem.word || '')}?pathItemId=${nextPathItem.id}&from=learning-path`
                          )
                        }
                        rightIcon={<ChevronRight className="w-4 h-4" />}
                        className="shadow-sm"
                      >
                        Next Word
                      </Button>
                    )}
                  </div>
                ) : (
                  <Button
                    variant="primary"
                    size="lg"
                    onClick={handleCompleteWord}
                    isLoading={isSubmittingCompletion}
                    leftIcon={<CheckCircle2 className="w-5 h-5" />}
                    className="shadow-md font-bold text-sm"
                  >
                    Mark as Learned (+15 XP)
                  </Button>
                )}
              </div>
            )}
          </div>

          {/* Definition */}
          <div className="space-y-1.5">
            <span className="text-[11px] font-bold uppercase tracking-wider text-memora-text-muted">
              Definition
            </span>
            <p className="text-base sm:text-lg text-stone-900 font-normal leading-relaxed">
              "{definitionDisplay}"
            </p>
          </div>
        </Card>

        {/* SECTION B: Contextual Example */}
        {exampleDisplay && (
          <Card
            variant="default"
            className="p-6 sm:p-7 bg-white border border-black/[0.08] shadow-xs rounded-3xl space-y-3"
          >
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold uppercase tracking-wider text-memora-text-muted flex items-center gap-1.5">
                <BookOpen className="w-3.5 h-3.5 text-blue-600" />
                Contextual Example
              </span>
              <span className="text-[11px] font-semibold px-2 py-0.5 rounded-full bg-blue-50 text-blue-800 border border-blue-100">
                Academic & Professional
              </span>
            </div>

            <div className="p-4 rounded-2xl bg-blue-50/40 border border-blue-100/80 flex items-start justify-between gap-3">
              <p className="text-sm sm:text-base text-stone-900 italic font-medium leading-relaxed">
                "{exampleDisplay}"
              </p>
              <button
                type="button"
                onClick={() => handleSpeak(exampleDisplay)}
                className="p-2 rounded-xl bg-white hover:bg-blue-100 text-blue-800 transition shrink-0 shadow-2xs"
                title="Pronounce example sentence"
              >
                <Volume2 className="w-4 h-4" />
              </button>
            </div>
          </Card>
        )}

        {/* SECTION C & D: Synonyms & Antonyms (Grid Layout) */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* Synonyms */}
          <Card
            variant="default"
            className="p-6 bg-white border border-black/[0.08] shadow-xs rounded-3xl space-y-3"
          >
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold uppercase tracking-wider text-memora-text-muted">
                Synonyms
              </span>
            </div>

            {synonymsList.length > 0 ? (
              <div className="flex flex-wrap gap-2 pt-1">
                {synonymsList.map((syn) => (
                  <button
                    key={syn}
                    type="button"
                    onClick={() => navigate(`/word/${encodeURIComponent(syn)}`)}
                    className="px-3 py-1.5 rounded-xl bg-[#FBFBF9] hover:bg-emerald-50 border border-black/[0.08] hover:border-memora-green text-xs font-semibold text-stone-700 hover:text-memora-green transition-all shadow-2xs hover:scale-105 active:scale-95"
                  >
                    {syn}
                  </button>
                ))}
              </div>
            ) : (
              <p className="text-xs text-stone-400 italic py-2">
                No reliable synonyms available.
              </p>
            )}
          </Card>

          {/* Antonyms */}
          <Card
            variant="default"
            className="p-6 bg-white border border-black/[0.08] shadow-xs rounded-3xl space-y-3"
          >
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold uppercase tracking-wider text-memora-text-muted">
                Antonyms
              </span>
            </div>

            {antonymsList.length > 0 ? (
              <div className="flex flex-wrap gap-2 pt-1">
                {antonymsList.map((ant) => (
                  <button
                    key={ant}
                    type="button"
                    onClick={() => navigate(`/word/${encodeURIComponent(ant)}`)}
                    className="px-3 py-1.5 rounded-xl bg-[#FBFBF9] hover:bg-amber-50 border border-black/[0.08] hover:border-amber-400 text-xs font-semibold text-stone-700 hover:text-amber-800 transition-all shadow-2xs hover:scale-105 active:scale-95"
                  >
                    {ant}
                  </button>
                ))}
              </div>
            ) : (
              <p className="text-xs text-stone-400 italic py-2">
                No reliable antonyms available.
              </p>
            )}
          </Card>
        </div>

        {/* SECTION E: Word Family Tree */}
        <section aria-label="Word Family">
          <WordFamilyTree
            currentWord={decodedWord}
            wordFamily={familyMap}
          />
        </section>

        {/* SECTION F: AI Learning Assistance */}
        <section className="space-y-3" aria-label="AI Learning Assistant">
          {isAuthenticated ? (
            <Card
              variant="default"
              className="p-6 sm:p-7 bg-white border border-black/[0.08] shadow-xs rounded-3xl space-y-4"
            >
              <WordStudyAiActions
                word={decodedWord}
                wordId={vocabWord?.id}
                cefrLevel={cefrDisplay}
                onSpeak={handleSpeak}
              />
            </Card>
          ) : (
            <Card
              variant="default"
              className="p-6 sm:p-8 bg-gradient-to-r from-purple-50/60 via-white to-purple-50/60 border border-purple-200/80 rounded-3xl shadow-xs space-y-4 text-center sm:text-left"
            >
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                <div className="space-y-1.5">
                  <div className="flex items-center justify-center sm:justify-start gap-2">
                    <Sparkles className="w-5 h-5 text-purple-600" />
                    <h3 className="font-extrabold text-base sm:text-lg text-memora-dark">
                      AI Learning Assistant
                    </h3>
                  </div>
                  <p className="text-xs sm:text-sm text-memora-text-muted max-w-md">
                    Sign in or create a free account to unlock personalized mnemonic memory tips,
                    adaptive explanations, and usage collocations tailored to your CEFR level.
                  </p>
                </div>

                <div className="flex flex-wrap items-center justify-center sm:justify-end gap-2.5 shrink-0">
                  <Button
                    variant="primary"
                    size="md"
                    onClick={() => navigate('/register')}
                    className="font-bold shadow-xs text-xs"
                  >
                    Unlock with Free Account
                  </Button>
                  <Button
                    variant="outline"
                    size="md"
                    onClick={() => navigate('/login')}
                    className="text-xs"
                  >
                    Log In
                  </Button>
                </div>
              </div>
            </Card>
          )}
        </section>

        {/* SECTION G: Memory Retention & Learning Progress */}
        <section aria-label="Memory retention">
          {isAuthenticated ? (
            <Card
              variant="default"
              className="p-6 sm:p-7 bg-white border border-black/[0.08] shadow-xs rounded-3xl space-y-5"
            >
              <div className="flex items-center justify-between border-b border-black/[0.06] pb-4">
                <span className="text-xs font-bold uppercase tracking-wider text-memora-text-muted flex items-center gap-1.5">
                  <Brain className="w-3.5 h-3.5 text-memora-green" />
                  Memory Retention Metrics
                </span>
                <Badge variant="neutral" size="sm">
                  SM-2 Adaptive Engine
                </Badge>
              </div>

              {wordProgress ? (
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
                  {/* Mastery Score */}
                  <div className="p-4 rounded-2xl bg-stone-50 border border-stone-200/80 space-y-1">
                    <span className="text-[11px] font-semibold text-stone-500 uppercase tracking-wider block">
                      Mastery Score
                    </span>
                    <span className="text-xl sm:text-2xl font-extrabold text-memora-dark">
                      {Math.round(wordProgress.masteryScore || 75)}%
                    </span>
                  </div>

                  {/* Forgetting Risk */}
                  <div className="p-4 rounded-2xl bg-stone-50 border border-stone-200/80 space-y-1">
                    <span className="text-[11px] font-semibold text-stone-500 uppercase tracking-wider block">
                      Forgetting Risk
                    </span>
                    <span
                      className={`text-sm sm:text-base font-extrabold ${
                        wordProgress.forgettingRisk === 'HIGH' || wordProgress.forgettingRisk === 'CRITICAL'
                          ? 'text-red-700'
                          : wordProgress.forgettingRisk === 'MEDIUM'
                          ? 'text-amber-700'
                          : 'text-emerald-700'
                      }`}
                    >
                      {wordProgress.forgettingRisk || 'LOW'}
                    </span>
                  </div>

                  {/* Total Reviews */}
                  <div className="p-4 rounded-2xl bg-stone-50 border border-stone-200/80 space-y-1">
                    <span className="text-[11px] font-semibold text-stone-500 uppercase tracking-wider block">
                      Reviews
                    </span>
                    <span className="text-xl sm:text-2xl font-extrabold text-memora-dark">
                      {wordProgress.totalAttempts || 0}
                    </span>
                  </div>

                  {/* Next Review */}
                  <div className="p-4 rounded-2xl bg-stone-50 border border-stone-200/80 space-y-1">
                    <span className="text-[11px] font-semibold text-stone-500 uppercase tracking-wider block">
                      Next Review
                    </span>
                    <span className="text-xs sm:text-sm font-extrabold text-memora-dark flex items-center gap-1">
                      <Calendar className="w-3.5 h-3.5 text-stone-400" />
                      {wordProgress.nextReviewAt
                        ? new Date(wordProgress.nextReviewAt).toLocaleDateString()
                        : 'Scheduled'}
                    </span>
                  </div>
                </div>
              ) : (
                <div className="p-5 rounded-2xl bg-[#FBFBF9] border border-black/[0.05] flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                  <div className="space-y-1">
                    <p className="text-xs sm:text-sm font-bold text-memora-dark">
                      Not yet scheduled in your memory review engine
                    </p>
                    <p className="text-xs text-memora-text-muted">
                      Complete learning this word or add it to your daily curriculum to initiate spaced repetition tracking.
                    </p>
                  </div>
                  {vocabWord?.id && (
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={async () => {
                        try {
                          await vocabularyApi.initWordProgress(vocabWord.id);
                          const updated = await vocabularyApi.getWordProgress(vocabWord.id);
                          if (updated.success && updated.data) {
                            setWordProgress(updated.data);
                            addToast({
                              type: 'info',
                              title: 'Added to Memory',
                              message: `"${decodedWord}" is now tracked in your retention engine.`,
                            });
                          }
                        } catch {
                          // Non-blocking
                        }
                      }}
                      className="shrink-0 text-xs font-semibold"
                    >
                      Track in Spaced Repetition
                    </Button>
                  )}
                </div>
              )}
            </Card>
          ) : (
            <Card
              variant="default"
              className="p-6 bg-stone-50 border border-stone-200 rounded-3xl space-y-2 text-center"
            >
              <h4 className="text-sm font-bold text-memora-dark">
                Track Retention & Spaced Repetition
              </h4>
              <p className="text-xs text-memora-text-muted max-w-sm mx-auto">
                Memora calculates forgetting curves and schedules smart revisions right before you forget.
              </p>
              <div className="pt-2">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => navigate('/register')}
                  className="font-bold text-xs"
                >
                  Create Free Account
                </Button>
              </div>
            </Card>
          )}
        </section>
      </main>
    </div>
  );
};

export default WordDetailPage;
