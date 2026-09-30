import React, { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Search,
  Volume2,
  VolumeX,
  Sparkles,
  BookOpen,
  ArrowRight,
  AlertCircle,
  Loader2,
  CheckCircle2,
  Lock,
  X,
  CornerDownRight,
} from 'lucide-react';
import { Button } from '../ui/Button';
import { Badge } from '../ui/Badge';
import { Card } from '../ui/Card';
import { dictionaryApi } from '../../api/dictionaryApi';
import { DictionaryResponse } from '../../types/dictionary';
import { useAuth } from '../../context/AuthContext';
import {
  MEMORA_GUEST_DICTIONARY_SEARCH_LIMIT,
  getGuestSearchCount,
  incrementGuestSearchCount,
  isGuestSearchLimitReached,
} from '../../utils/guestSearchLimit';

export const LandingWordSearchSection: React.FC = () => {
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();

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
      setValidationError('Please enter a word to explore.');
      return;
    }

    if (term.length > 64) {
      setValidationError('Search word cannot exceed 64 characters.');
      return;
    }

    if (!/^[\p{L}\s'’-]+$/u.test(term)) {
      setValidationError('Word can only contain letters, hyphens, and apostrophes.');
      return;
    }

    // Guest quota enforcement: check if guest user has reached the free search limit
    if (!isAuthenticated) {
      const currentCount = getGuestSearchCount();
      if (currentCount >= MEMORA_GUEST_DICTIONARY_SEARCH_LIMIT) {
        setShowLimitModal(true);
        return;
      }
    }

    // Cancel in-flight requests
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
    }
    const controller = new AbortController();
    abortControllerRef.current = controller;

    if (isLoading) return;

    setIsLoading(true);
    setSearchedWord(term);
    setQuery(term);

    // Stop audio
    if (audioRef.current) {
      audioRef.current.pause();
      audioRef.current = null;
    }
    setIsPlayingAudio(false);

    try {
      const res = await dictionaryApi.lookupWord(term, controller.signal);

      if (res.success && res.data) {
        setResult(res.data);

        // Only count valid, successful submitted searches for guests
        if (!isAuthenticated) {
          const nextCount = incrementGuestSearchCount();
          setGuestCount(nextCount);
        }
      } else {
        setResult(null);
        setErrorMessage(res.message || 'Word not found. Check spelling or try a related word.');
      }
    } catch (err: any) {
      if (err.name === 'CanceledError' || err.name === 'AbortError' || err.code === 'ERR_CANCELED') {
        return;
      }
      setResult(null);
      if (err.response?.status === 404) {
        setErrorMessage('Word not found. Try checking the spelling.');
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

  const freeSearchesRemaining = Math.max(
    0,
    MEMORA_GUEST_DICTIONARY_SEARCH_LIMIT - guestCount
  );

  return (
    <section id="word-search" className="relative py-16 sm:py-20 border-t border-black/[0.06] overflow-hidden">
      <span id="explore-word" className="sr-only" aria-hidden="true" />
      {/* Soft background ambient gradient */}
      <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[700px] h-[350px] bg-gradient-to-r from-emerald-100/50 via-amber-50/40 to-emerald-100/50 rounded-full blur-3xl -z-10 pointer-events-none" />

      <div className="max-w-4xl mx-auto px-6 space-y-8">
        {/* Section Header */}
        <div className="text-center space-y-3">
          <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-[#EAF2DE] border border-[#D5E6BE] text-[#4D6D1A] text-xs font-bold shadow-xs">
            <BookOpen className="w-3.5 h-3.5" />
            <span>Instant Vocabulary Lookup</span>
          </div>

          <h2 className="text-3xl sm:text-4xl lg:text-5xl font-extrabold text-memora-dark tracking-tight">
            Explore any word
          </h2>

          <p className="text-sm sm:text-base text-memora-text-muted max-w-xl mx-auto font-normal leading-relaxed">
            Discover definitions, examples, pronunciation and more.
          </p>

          {/* Guest Search Counter Banner */}
          {!isAuthenticated && (
            <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-stone-100 border border-stone-200 text-stone-700">
              <Sparkles className="w-3.5 h-3.5 text-amber-600" />
              <span>
                {freeSearchesRemaining > 0 ? (
                  <>
                    Guest Preview:{' '}
                    <span className="font-bold text-memora-dark">
                      {freeSearchesRemaining} of {MEMORA_GUEST_DICTIONARY_SEARCH_LIMIT}
                    </span>{' '}
                    free searches remaining
                  </>
                ) : (
                  <>Free guest searches reached — sign in for unlimited lookup</>
                )}
              </span>
            </div>
          )}
        </div>

        {/* Search Bar Container */}
        <div className="max-w-2xl mx-auto">
          <form onSubmit={handleFormSubmit} className="relative">
            <div className="relative flex items-center rounded-2xl bg-white border border-stone-200 shadow-card hover:border-stone-300 focus-within:border-memora-green focus-within:ring-3 focus-within:ring-memora-green/15 transition-all p-1.5 sm:p-2">
              <div className="pl-3.5 pr-2 text-stone-400 flex items-center pointer-events-none">
                <Search className="w-5 h-5" />
              </div>

              <input
                type="text"
                value={query}
                onChange={(e) => {
                  setQuery(e.target.value);
                  if (validationError) setValidationError(null);
                }}
                placeholder="Search a word... (e.g. meticulous)"
                maxLength={64}
                className="w-full bg-transparent text-sm sm:text-base font-medium text-memora-dark placeholder-stone-400 focus:outline-hidden py-2"
                aria-label="Search vocabulary word"
              />

              {query && (
                <button
                  type="button"
                  onClick={() => setQuery('')}
                  className="p-1.5 rounded-lg text-stone-400 hover:text-stone-700 transition mr-1"
                  aria-label="Clear search input"
                >
                  <X className="w-4 h-4" />
                </button>
              )}

              <Button
                type="submit"
                variant="primary"
                size="md"
                isLoading={isLoading}
                disabled={isLoading}
                className="shrink-0 rounded-xl px-5 font-bold shadow-sm"
              >
                Search
              </Button>
            </div>
          </form>

          {/* Validation Message */}
          {validationError && (
            <p className="mt-2 text-xs text-red-600 font-medium px-2 flex items-center gap-1.5 animate-fade-in">
              <AlertCircle className="w-3.5 h-3.5 shrink-0" />
              {validationError}
            </p>
          )}

          {/* Quick Word Suggestion Pills */}
          <div className="mt-3 flex flex-wrap items-center justify-center gap-2 text-xs">
            <span className="text-stone-400 font-medium">Try:</span>
            {sampleWords.map((sample) => (
              <button
                key={sample}
                type="button"
                onClick={() => {
                  setQuery(sample);
                  handleSearchSubmit(sample);
                }}
                className="px-2.5 py-1 rounded-full bg-white/80 hover:bg-white border border-stone-200 hover:border-memora-green text-stone-600 hover:text-memora-green font-medium transition-all shadow-2xs hover:scale-105 active:scale-95"
              >
                {sample}
              </button>
            ))}
          </div>
        </div>

        {/* Search Result Card (Compact & Editorial) */}
        {isLoading && (
          <div className="max-w-2xl mx-auto py-10 flex flex-col items-center justify-center space-y-3">
            <Loader2 className="w-8 h-8 animate-spin text-memora-green" />
            <p className="text-xs sm:text-sm font-medium text-stone-600">
              Retrieving definition and audio from verified dictionary...
            </p>
          </div>
        )}

        {/* Error / Not Found */}
        {errorMessage && !isLoading && (
          <div className="max-w-2xl mx-auto p-4 sm:p-5 rounded-2xl bg-amber-50/70 border border-amber-200 text-stone-800 text-xs sm:text-sm flex items-start gap-3">
            <AlertCircle className="w-5 h-5 text-amber-700 shrink-0 mt-0.5" />
            <div className="space-y-1">
              <p className="font-semibold text-amber-900">{errorMessage}</p>
              <p className="text-stone-600 text-xs">
                Check for typing errors or search one of our curated vocabulary words above.
              </p>
            </div>
          </div>
        )}

        {/* Result Display */}
        {result && !isLoading && (
          <div className="max-w-2xl mx-auto animate-fade-in">
            <Card
              variant="default"
              className="p-6 sm:p-8 space-y-5 bg-white border border-black/[0.08] shadow-card rounded-3xl"
            >
              {/* Word Header */}
              <div className="flex flex-wrap items-start justify-between gap-4 border-b border-black/[0.06] pb-5">
                <div className="space-y-1.5">
                  <div className="flex items-center gap-3 flex-wrap">
                    <h3 className="text-2xl sm:text-3xl font-extrabold text-memora-dark tracking-tight">
                      {result.headword || result.word}
                    </h3>

                    {result.partsOfSpeech?.[0]?.partOfSpeech && (
                      <Badge variant="green" size="md">
                        {result.partsOfSpeech[0].partOfSpeech}
                      </Badge>
                    )}
                  </div>

                  {result.pronunciation && (
                    <div className="flex items-center gap-2 text-stone-500 font-mono text-xs sm:text-sm">
                      <span>/{result.pronunciation}/</span>
                      <button
                        type="button"
                        onClick={() => playAudio(result.audioUrl, result.word)}
                        className={`p-1.5 rounded-full transition-colors ${
                          isPlayingAudio
                            ? 'bg-memora-green text-white'
                            : 'bg-stone-100 text-stone-600 hover:bg-stone-200'
                        }`}
                        title="Listen to pronunciation"
                        aria-label="Listen to pronunciation"
                      >
                        {isPlayingAudio ? (
                          <VolumeX className="w-3.5 h-3.5" />
                        ) : (
                          <Volume2 className="w-3.5 h-3.5" />
                        )}
                      </button>
                    </div>
                  )}
                </div>

                {/* Primary CTA */}
                <Button
                  variant="primary"
                  size="md"
                  onClick={() => navigate(`/word/${encodeURIComponent(result.word)}`)}
                  rightIcon={<ArrowRight className="w-4 h-4" />}
                  className="shrink-0 shadow-sm"
                >
                  Study this word
                </Button>
              </div>

              {/* Definition Section */}
              <div className="space-y-2">
                <span className="text-[11px] font-bold uppercase tracking-wider text-memora-text-muted">
                  Definition
                </span>
                <p className="text-sm sm:text-base text-stone-800 font-normal leading-relaxed">
                  {result.shortDefinitions?.[0] ||
                    result.partsOfSpeech?.[0]?.definitions?.[0]?.definition ||
                    'Definition currently unavailable.'}
                </p>
              </div>

              {/* Example Section */}
              {(() => {
                const exampleText =
                  result.partsOfSpeech?.[0]?.definitions?.[0]?.examples?.[0] || null;

                if (!exampleText) return null;

                return (
                  <div className="space-y-1.5 pt-1">
                    <span className="text-[11px] font-bold uppercase tracking-wider text-memora-text-muted">
                      Contextual Example
                    </span>
                    <div className="p-3.5 rounded-2xl bg-[#FBFBF9] border border-black/[0.05] italic text-xs sm:text-sm text-stone-700 leading-relaxed">
                      "{exampleText}"
                    </div>
                  </div>
                );
              })()}

              {/* Bottom Quick Links / Secondary CTA */}
              <div className="pt-2 flex flex-wrap items-center justify-between gap-3 text-xs border-t border-black/[0.04]">
                <span className="text-stone-400">
                  Verified Merriam-Webster Collegiate Dictionary
                </span>
                <button
                  type="button"
                  onClick={() => navigate(`/word/${encodeURIComponent(result.word)}`)}
                  className="font-bold text-memora-green hover:underline flex items-center gap-1"
                >
                  <span>View full word details & family tree</span>
                  <CornerDownRight className="w-3 h-3" />
                </button>
              </div>
            </Card>
          </div>
        )}
      </div>

      {/* Guest Search Limit Modal */}
      {showLimitModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-xs animate-fade-in">
          <div className="relative w-full max-w-md bg-white rounded-3xl p-6 sm:p-8 shadow-2xl border border-black/[0.08] space-y-6 animate-scale-up">
            <button
              type="button"
              onClick={() => setShowLimitModal(false)}
              className="absolute top-4 right-4 p-2 rounded-full text-stone-400 hover:text-stone-700 hover:bg-stone-100 transition"
              aria-label="Close modal"
            >
              <X className="w-4 h-4" />
            </button>

            <div className="w-12 h-12 rounded-2xl bg-amber-100 text-amber-700 flex items-center justify-center mx-auto">
              <Sparkles className="w-6 h-6" />
            </div>

            <div className="text-center space-y-2">
              <h3 className="text-xl sm:text-2xl font-extrabold text-memora-dark tracking-tight">
                You've explored a few words.
              </h3>
              <p className="text-xs sm:text-sm text-memora-text-muted leading-relaxed">
                Create a free account to continue exploring vocabulary and unlock personalized
                learning, adaptive revision, and memory retention tracking.
              </p>
            </div>

            <div className="space-y-3 pt-2">
              <Button
                variant="primary"
                size="lg"
                onClick={() => navigate('/register')}
                className="w-full shadow-md font-bold"
                rightIcon={<ArrowRight className="w-4 h-4" />}
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

            <p className="text-center text-[11px] text-stone-400">
              No credit card required. Instant access.
            </p>
          </div>
        </div>
      )}
    </section>
  );
};

export default LandingWordSearchSection;
