import React, { useState, useRef, useEffect } from 'react';
import { createPortal } from 'react-dom';
import { useNavigate } from 'react-router-dom';
import {
  Search,
  Volume2,
  Sparkles,
  ArrowRight,
  Play,
  CheckCircle2,
  RotateCw,
  AlertCircle,
  Loader2,
  X,
  BookOpen,
  Lock,
  ExternalLink,
  Star,
  Brain,
  Zap,
} from 'lucide-react';
import { Button } from '../ui/Button';
import { Badge } from '../ui/Badge';
import { dictionaryApi } from '../../api/dictionaryApi';
import { DictionaryResponse } from '../../types/dictionary';
import { useAuth } from '../../context/AuthContext';
import {
  MEMORA_GUEST_DICTIONARY_SEARCH_LIMIT,
  getGuestSearchCount,
  incrementGuestSearchCount,
} from '../../utils/guestSearchLimit';

export const HeroSection: React.FC = () => {
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();

  // Search input and response state
  const [query, setQuery] = useState('');
  const [searchedWord, setSearchedWord] = useState('');
  const [result, setResult] = useState<DictionaryResponse | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [validationError, setValidationError] = useState<string | null>(null);

  // Audio state
  const [isPlayingAudio, setIsPlayingAudio] = useState(false);
  const audioRef = useRef<HTMLAudioElement | null>(null);

  // Guest search quota state
  const [guestCount, setGuestCount] = useState<number>(() => getGuestSearchCount());
  const [showLimitModal, setShowLimitModal] = useState(false);

  // Request cancellation ref
  const abortControllerRef = useRef<AbortController | null>(null);

  // Quick word suggestion pills
  const sampleWords = ['meticulous', 'resilience', 'serendipity', 'pragmatic', 'eloquent'];

  // Sync count on mount or auth change
  useEffect(() => {
    if (!isAuthenticated) {
      setGuestCount(getGuestSearchCount());
    }
  }, [isAuthenticated]);

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

  const playAudio = (audioUrl?: string | null, fallbackWord?: string) => {
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
          if (fallbackWord) {
            handleSpeak(fallbackWord);
          }
        };

        audio.play().catch(() => {
          setIsPlayingAudio(false);
          if (fallbackWord) {
            handleSpeak(fallbackWord);
          }
        });
      } catch {
        if (fallbackWord) {
          handleSpeak(fallbackWord);
        }
      }
    } else if (fallbackWord) {
      handleSpeak(fallbackWord);
    }
  };

  const handleSearchSubmit = async (wordToSearch?: string) => {
    const term = (wordToSearch !== undefined ? wordToSearch : query).trim();

    setValidationError(null);
    setErrorMessage(null);

    if (!term) {
      setValidationError('Please enter a word to search.');
      return;
    }

    if (term.length > 64) {
      setValidationError('Search term cannot exceed 64 characters.');
      return;
    }

    if (!/^[\p{L}\s'’-]+$/u.test(term)) {
      setValidationError('Word can only contain letters, hyphens, and apostrophes.');
      return;
    }

    // Guest quota enforcement
    if (!isAuthenticated) {
      const currentCount = getGuestSearchCount();
      if (currentCount >= MEMORA_GUEST_DICTIONARY_SEARCH_LIMIT) {
        setShowLimitModal(true);
        return;
      }
    }

    // Cancel prior in-flight request
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
    }
    const controller = new AbortController();
    abortControllerRef.current = controller;

    if (isLoading) return;

    setIsLoading(true);
    setSearchedWord(term);
    setQuery(term);

    // Stop existing audio
    if (audioRef.current) {
      audioRef.current.pause();
      audioRef.current = null;
    }
    setIsPlayingAudio(false);

    try {
      const res = await dictionaryApi.lookupWord(term, controller.signal);

      if (res.success && res.data) {
        setResult(res.data);

        // Only increment counter for successful guest searches
        if (!isAuthenticated) {
          const nextCount = incrementGuestSearchCount();
          setGuestCount(nextCount);
        }
      } else {
        setResult(null);
        setErrorMessage(res.message || `No definition found for "${term}". Try another word.`);
      }
    } catch (err: any) {
      if (err.name === 'CanceledError' || err.name === 'AbortError' || err.code === 'ERR_CANCELED') {
        return;
      }
      setResult(null);
      if (err.response?.status === 404) {
        setErrorMessage(`"${term}" was not found in collegiate lexicon. Check spelling or try a related word.`);
      } else if (err.response?.status === 429) {
        setErrorMessage('Dictionary rate limit reached. Please wait a moment.');
      } else {
        setErrorMessage(err.response?.data?.message || 'Dictionary lookup failed. Please try again.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleFormSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    handleSearchSubmit();
  };

  const clearSearch = () => {
    setQuery('');
    setResult(null);
    setErrorMessage(null);
    setValidationError(null);
    if (audioRef.current) {
      audioRef.current.pause();
      audioRef.current = null;
    }
    setIsPlayingAudio(false);
  };

  const remainingSearches = Math.max(0, MEMORA_GUEST_DICTIONARY_SEARCH_LIMIT - guestCount);

  return (
    <section id="home" className="relative pt-4 sm:pt-6 pb-12 sm:pb-16 lg:pb-20 overflow-hidden">
      {/* Background ambient lighting */}
      <div className="absolute top-0 right-1/4 -translate-y-1/3 w-[600px] h-[400px] bg-gradient-to-br from-emerald-100/40 via-amber-50/30 to-transparent rounded-full blur-3xl -z-10 pointer-events-none" />

      <div className="max-w-7xl mx-auto px-4 sm:px-6">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 lg:gap-12 items-center">
          {/* Left Column: Value Proposition & Hero Word Search */}
          <div className="lg:col-span-7 flex flex-col space-y-5 sm:space-y-6">
            {/* AI pill / Badge */}
            <div className="inline-flex items-center gap-2 self-start px-3.5 py-1.5 rounded-full bg-[#EAF2DE] border border-[#D5E6BE] text-[#4D6D1A] text-xs font-bold tracking-wide shadow-xs">
              <Sparkles className="w-3.5 h-3.5 fill-current" />
              <span>AI-Powered Vocabulary Learning</span>
            </div>

            {/* Editorial Headline */}
            <h1 className="text-4xl sm:text-5xl lg:text-6xl font-extrabold text-memora-dark tracking-tight leading-[1.1] sm:leading-[1.08]">
              Words that{' '}
              <span className="text-memora-green inline-block relative underline decoration-memora-green/20 decoration-wavy decoration-2 underline-offset-8">
                stay
              </span>{' '}
              with you.
            </h1>

            {/* Subtitle */}
            <p className="text-base sm:text-lg text-memora-text-muted max-w-xl font-normal leading-relaxed">
              Memora uses adaptive spaced repetition and memory-based revision to help you learn vocabulary smarter and remember it longer.
            </p>

            {/* ============================================================== */}
            {/* HERO WORD SEARCH (PROMINENT ABOVE-THE-FOLD INTERACTION) */}
            {/* ============================================================== */}
            <div className="w-full max-w-2xl pt-1">
              <form
                onSubmit={handleFormSubmit}
                role="search"
                aria-label="Hero Dictionary Word Search"
                className="relative flex items-center bg-white rounded-2xl border-2 border-stone-200/90 hover:border-stone-300 focus-within:border-memora-green focus-within:ring-4 focus-within:ring-memora-green/15 shadow-lg shadow-emerald-950/5 transition-all p-1.5 sm:p-2"
              >
                <div className="pl-2 sm:pl-3 pr-2 text-stone-400 flex items-center pointer-events-none">
                  <Search className="w-5 h-5 text-stone-400 shrink-0" aria-hidden="true" />
                </div>

                <label htmlFor="hero-word-search-input" className="sr-only">
                  Search any word to explore
                </label>
                <input
                  id="hero-word-search-input"
                  type="text"
                  value={query}
                  onChange={(e) => {
                    setQuery(e.target.value);
                    if (validationError) setValidationError(null);
                  }}
                  placeholder="Search a word to learn it better..."
                  autoComplete="off"
                  spellCheck="false"
                  aria-describedby="hero-search-hint"
                  className="flex-1 min-w-0 bg-transparent text-sm sm:text-base text-memora-dark placeholder:text-stone-400 font-medium focus:outline-hidden px-1 py-1 sm:py-1.5"
                />

                {query && (
                  <button
                    type="button"
                    onClick={clearSearch}
                    className="p-1.5 text-stone-400 hover:text-stone-600 rounded-full hover:bg-stone-100 transition-colors mr-1"
                    title="Clear input"
                    aria-label="Clear search input"
                  >
                    <X className="w-4 h-4" />
                  </button>
                )}

                <Button
                  type="submit"
                  variant="primary"
                  size="md"
                  disabled={isLoading}
                  className="shrink-0 px-4 sm:px-6 py-2 sm:py-2.5 font-bold shadow-md text-sm rounded-xl"
                  id="hero-search-button"
                >
                  {isLoading ? (
                    <>
                      <Loader2 className="w-4 h-4 animate-spin mr-1.5" />
                      <span>Searching...</span>
                    </>
                  ) : (
                    <>
                      <span>Search</span>
                      <ArrowRight className="w-4 h-4 ml-1 hidden sm:inline-block" />
                    </>
                  )}
                </Button>
              </form>

              <span id="hero-search-hint" className="sr-only">
                Type any English word and press Enter to instantly see definitions, pronunciations, and examples.
              </span>

              {/* Validation & Error Messages */}
              {validationError && (
                <div className="flex items-center gap-1.5 text-xs sm:text-sm text-amber-600 font-medium mt-2 px-1">
                  <AlertCircle className="w-4 h-4 shrink-0" />
                  <span>{validationError}</span>
                </div>
              )}

              {errorMessage && (
                <div className="flex items-center gap-1.5 text-xs sm:text-sm text-rose-600 font-medium mt-2 px-1">
                  <AlertCircle className="w-4 h-4 shrink-0" />
                  <span>{errorMessage}</span>
                </div>
              )}

              {/* Suggestions row & Guest search quota indicator */}
              <div className="flex flex-wrap items-center justify-between gap-2 mt-3 px-1 text-xs text-memora-text-muted">
                <div className="flex flex-wrap items-center gap-1.5">
                  <span className="font-semibold text-stone-500">Try:</span>
                  {sampleWords.map((word) => (
                    <button
                      key={word}
                      type="button"
                      onClick={() => handleSearchSubmit(word)}
                      className="px-2.5 py-1 rounded-full bg-stone-100 hover:bg-emerald-50 hover:text-memora-green hover:border-emerald-200 border border-stone-200/80 transition-colors text-stone-700 font-medium cursor-pointer"
                    >
                      {word}
                    </button>
                  ))}
                </div>

                {!isAuthenticated && (
                  <div className="flex items-center gap-1 text-[11px] text-stone-500 font-medium">
                    {remainingSearches > 0 ? (
                      <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md bg-stone-100 text-stone-600 border border-stone-200">
                        <Sparkles className="w-3 h-3 text-memora-green" />
                        {remainingSearches} free {remainingSearches === 1 ? 'search' : 'searches'} left
                      </span>
                    ) : (
                      <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md bg-amber-50 text-amber-700 border border-amber-200">
                        <Lock className="w-3 h-3 text-amber-600" />
                        Guest limit reached
                      </span>
                    )}
                  </div>
                )}
              </div>

              {/* ========================================================== */}
              {/* INSTANT RESULT PREVIEW CARD (WHEN WORD IS SEARCHED) */}
              {/* ========================================================== */}
              {result && (
                <div className="mt-4 p-4 sm:p-5 rounded-2xl bg-white border border-stone-200/90 shadow-md shadow-emerald-950/5 animate-in fade-in slide-in-from-top-2 duration-300">
                  <div className="flex items-start justify-between gap-3">
                    <div className="space-y-1">
                      <div className="flex flex-wrap items-center gap-2 sm:gap-3">
                        <h3 className="text-xl sm:text-2xl font-black text-memora-dark tracking-tight capitalize">
                          {result.headword || result.word}
                        </h3>
                        {result.pronunciation && (
                          <span className="text-xs sm:text-sm text-stone-500 font-mono">
                            /{result.pronunciation}/
                          </span>
                        )}
                        {result.partsOfSpeech?.[0]?.partOfSpeech && (
                          <Badge variant="green" size="sm">
                            {result.partsOfSpeech[0].partOfSpeech}
                          </Badge>
                        )}
                        <button
                          type="button"
                          onClick={() => playAudio(result.audioUrl, result.word)}
                          className={`p-1.5 rounded-full transition-colors ${
                            isPlayingAudio
                              ? 'bg-memora-green text-white animate-pulse'
                              : 'text-stone-500 hover:text-memora-green hover:bg-emerald-50'
                          }`}
                          title={`Pronounce ${result.word}`}
                          aria-label={`Listen to pronunciation of ${result.word}`}
                        >
                          <Volume2 className="w-4 h-4" />
                        </button>
                      </div>
                    </div>

                    <button
                      type="button"
                      onClick={() => setResult(null)}
                      className="text-stone-400 hover:text-stone-600 p-1 rounded-lg hover:bg-stone-100 transition-colors"
                      title="Dismiss preview"
                      aria-label="Close word preview"
                    >
                      <X className="w-4 h-4" />
                    </button>
                  </div>

                  {/* Primary definition & example */}
                  <div className="mt-2 text-xs sm:text-sm text-stone-700 leading-relaxed">
                    <p className="font-medium text-stone-800 line-clamp-2">
                      {result.shortDefinitions?.[0] ||
                        result.partsOfSpeech?.[0]?.definitions?.[0]?.definition ||
                        'Definition currently unavailable.'}
                    </p>
                    {result.partsOfSpeech?.[0]?.definitions?.[0]?.examples?.[0] && (
                      <p className="mt-1 text-stone-500 italic text-[11px] sm:text-xs line-clamp-2 border-l-2 border-emerald-300 pl-2">
                        "{result.partsOfSpeech[0].definitions[0].examples[0]}"
                      </p>
                    )}
                  </div>

                  {/* Action buttons */}
                  <div className="mt-3 pt-3 border-t border-stone-100 flex flex-wrap items-center justify-between gap-2">
                    <span className="text-[11px] text-stone-400 font-medium">
                      Merriam-Webster verified collegiate lexicon
                    </span>
                    <button
                      type="button"
                      onClick={() => {
                        if (isAuthenticated) {
                          navigate(`/word/${encodeURIComponent(result.word)}`);
                        } else {
                          navigate(`/register`);
                        }
                      }}
                      className="inline-flex items-center gap-1.5 text-xs font-bold text-memora-green hover:text-memora-green-dark hover:underline transition-all"
                    >
                      <span>{isAuthenticated ? 'Open Word Studio' : 'Save & Practice in Memora'}</span>
                      <ExternalLink className="w-3 h-3" />
                    </button>
                  </div>
                </div>
              )}
            </div>

            {/* Primary CTAs & Social Proof */}
            <div className="flex flex-wrap items-center gap-3 sm:gap-4 pt-1 sm:pt-2">
              <Button
                variant="primary"
                size="lg"
                onClick={() => navigate('/register')}
                rightIcon={<ArrowRight className="w-5 h-5" />}
                className="shadow-md text-sm sm:text-base font-bold"
              >
                Start Learning
              </Button>

              <Button
                variant="outline"
                size="lg"
                onClick={() => {
                  const el = document.getElementById('how-it-works');
                  if (el) {
                    el.scrollIntoView({ behavior: 'smooth' });
                  }
                }}
                leftIcon={<Play className="w-4 h-4 fill-current" />}
                className="text-sm sm:text-base font-semibold"
              >
                Watch demo
              </Button>
            </div>

            {/* Trust and Social Proof Signal */}
            <div className="pt-2 flex flex-wrap items-center gap-4 text-xs text-stone-500">
              <div className="flex items-center gap-1 text-amber-500 font-semibold">
                <div className="flex text-amber-400">
                  {[...Array(5)].map((_, i) => (
                    <Star key={i} className="w-3.5 h-3.5 fill-current" />
                  ))}
                </div>
                <span className="text-stone-700 ml-1">4.9/5</span>
              </div>
              <span className="text-stone-300">•</span>
              <span className="text-stone-600 font-medium">12,000+ active learners mastering vocabulary</span>
              <span className="text-stone-300">•</span>
              <span className="text-stone-600 font-medium">Scientifically-backed SRS</span>
            </div>
          </div>

          {/* Right Column: 3D Memory Ecosystem Visual & Floating Concept Cards */}
          <div className="lg:col-span-5 relative hidden lg:flex flex-col items-center justify-center select-none">
            {/* Visual Container */}
            <div className="relative w-full max-w-[440px] aspect-square flex items-center justify-center">
              {/* Subtle ambient back-glow behind the 3D model */}
              <div className="absolute inset-0 bg-gradient-to-tr from-emerald-200/40 via-amber-100/30 to-emerald-100/20 rounded-full blur-2xl transform scale-90 -z-10" />

              {/* 3D Memory Ecosystem asset */}
              <img
                src="/hero-brain-ecosystem.webp"
                alt="Memora Adaptive Memory Ecosystem"
                className="w-full h-auto object-contain drop-shadow-2xl relative z-10 transition-transform duration-700 hover:scale-[1.02]"
                loading="eager"
                fetchPriority="high"
                width={500}
                height={500}
              />

              {/* Floating Card 1: Resilience (Top Right) */}
              <div className="absolute top-2 -right-4 z-20 bg-white/95 backdrop-blur-md p-3 rounded-xl border border-stone-200/80 shadow-card max-w-[190px] transition-all hover:scale-105 duration-300">
                <div className="flex items-center justify-between gap-2 mb-1">
                  <span className="text-xs font-bold text-memora-dark">resilience</span>
                  <button
                    type="button"
                    onClick={() => handleSpeak('resilience')}
                    className="text-stone-400 hover:text-memora-green transition-colors"
                    title="Pronounce 'resilience'"
                    aria-label="Pronounce resilience"
                  >
                    <Volume2 className="w-3.5 h-3.5" />
                  </button>
                </div>
                <p className="text-[10px] text-stone-500 line-clamp-2 leading-snug mb-1.5">
                  the capacity to recover quickly from difficulties.
                </p>
                <div className="flex items-center gap-1 text-[10px] font-semibold text-emerald-700 bg-emerald-50 px-1.5 py-0.5 rounded">
                  <CheckCircle2 className="w-3 h-3 text-emerald-600" />
                  <span>Mastered • SRS Stage 4</span>
                </div>
              </div>

              {/* Floating Card 2: Memory Retention Rate (Bottom Left) */}
              <div className="absolute -bottom-2 -left-4 z-20 bg-white/95 backdrop-blur-md p-3 rounded-xl border border-stone-200/80 shadow-card max-w-[195px] transition-all hover:scale-105 duration-300">
                <div className="flex items-center gap-2 mb-1.5">
                  <div className="w-6 h-6 rounded-lg bg-emerald-100 flex items-center justify-center text-memora-green">
                    <Brain className="w-3.5 h-3.5" />
                  </div>
                  <div>
                    <p className="text-xs font-bold text-memora-dark">Adaptive Retention</p>
                    <p className="text-[10px] text-stone-400">Memory Decay Curve</p>
                  </div>
                </div>
                <div className="w-full bg-stone-100 rounded-full h-1.5 overflow-hidden mb-1">
                  <div className="bg-memora-green h-full rounded-full w-[94%]" />
                </div>
                <p className="text-[10px] text-stone-500 font-medium flex justify-between">
                  <span>94% recall rate</span>
                  <span className="text-memora-green font-bold">+18% vs cramming</span>
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* ============================================================== */}
      {/* PORTALIZED GUEST SEARCH LIMIT MODAL (5TH SEARCH PROMPT) */}
      {/* ============================================================== */}
      {showLimitModal &&
        createPortal(
          <div
            className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs animate-in fade-in duration-200"
            role="dialog"
            aria-modal="true"
            aria-labelledby="hero-guest-limit-title"
          >
            <div
              className="relative w-full max-w-md bg-white rounded-3xl p-6 sm:p-8 shadow-2xl border border-stone-100 text-center space-y-5 animate-in zoom-in-95 duration-200"
              onClick={(e) => e.stopPropagation()}
            >
              <button
                type="button"
                onClick={() => setShowLimitModal(false)}
                className="absolute top-4 right-4 p-2 text-stone-400 hover:text-stone-600 rounded-full hover:bg-stone-100 transition-colors"
                aria-label="Close dialog"
              >
                <X className="w-5 h-5" />
              </button>

              <div className="w-14 h-14 bg-amber-100 rounded-2xl flex items-center justify-center mx-auto text-amber-700 shadow-inner">
                <Lock className="w-7 h-7" />
              </div>

              <div className="space-y-2">
                <h3 id="hero-guest-limit-title" className="text-2xl font-extrabold text-memora-dark tracking-tight">
                  You've reached your free searches!
                </h3>
                <p className="text-sm text-memora-text-muted leading-relaxed">
                  Guests can search up to <span className="font-semibold text-stone-700">{MEMORA_GUEST_DICTIONARY_SEARCH_LIMIT} words</span>. Create a free Memora account for unlimited searches, adaptive spaced-repetition drills, and personal word studios.
                </p>
              </div>

              <div className="space-y-2.5 pt-2">
                <Button
                  variant="primary"
                  size="lg"
                  onClick={() => navigate('/register')}
                  className="w-full font-bold shadow-md"
                >
                  Create Free Account
                </Button>
                <Button
                  variant="outline"
                  size="lg"
                  onClick={() => navigate('/login')}
                  className="w-full font-semibold"
                >
                  Log In
                </Button>
              </div>

              <p className="text-[11px] text-stone-400 pt-1">
                Free forever. No credit card required.
              </p>
            </div>
          </div>,
          document.body
        )}
    </section>
  );
};

export default HeroSection;
