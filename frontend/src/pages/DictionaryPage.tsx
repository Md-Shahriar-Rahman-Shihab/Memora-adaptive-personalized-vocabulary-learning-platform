import React, { useState, useRef, useEffect } from 'react';
import {
  Search,
  Volume2,
  VolumeX,
  BookA,
  Sparkles,
  BookOpen,
  Quote,
  CornerDownRight,
  AlertCircle,
  Loader2,
  HelpCircle,
  Bookmark,
} from 'lucide-react';
import { AppShell } from '../components/layout/AppShell';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { dictionaryApi } from '../api/dictionaryApi';
import { DictionaryResponse, PartOfSpeechItem, DefinitionItem } from '../types/dictionary';

export const DictionaryPage: React.FC = () => {
  const [searchQuery, setSearchQuery] = useState('');
  const [searchedWord, setSearchedWord] = useState('');
  const [entry, setEntry] = useState<DictionaryResponse | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [validationError, setValidationError] = useState<string | null>(null);

  // Audio playback state
  const [isPlayingAudio, setIsPlayingAudio] = useState(false);
  const [audioError, setAudioError] = useState(false);
  const audioRef = useRef<HTMLAudioElement | null>(null);

  // In-flight request cancellation
  const abortControllerRef = useRef<AbortController | null>(null);

  // Suggested exploration words for empty state
  const quickWords = ['eloquent', 'resilience', 'ephemeral', 'serendipity', 'pragmatic', 'happy'];

  const handleSearch = async (wordToSearch?: string) => {
    const term = (wordToSearch !== undefined ? wordToSearch : searchQuery).trim();

    // Reset previous errors & audio
    setValidationError(null);
    setErrorMessage(null);
    setAudioError(false);

    // Validation
    if (!term) {
      setValidationError('Please enter a word to search.');
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

    // Cancel any previous in-flight request
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
    }
    const controller = new AbortController();
    abortControllerRef.current = controller;

    // Prevent duplicate simultaneous submissions
    if (isLoading) return;

    setIsLoading(true);
    setSearchedWord(term);
    setSearchQuery(term);

    // Stop currently playing audio
    if (audioRef.current) {
      audioRef.current.pause();
      audioRef.current = null;
    }
    setIsPlayingAudio(false);

    try {
      const res = await dictionaryApi.lookupWord(term, controller.signal);
      if (res.success && res.data) {
        setEntry(res.data);
      } else {
        setEntry(null);
        setErrorMessage(res.message || 'Word not found. Try checking the spelling.');
      }
    } catch (err: any) {
      // Ignore manual request cancellations
      if (err.name === 'CanceledError' || err.name === 'AbortError' || err.code === 'ERR_CANCELED') {
        return;
      }
      setEntry(null);
      if (err.response?.status === 404) {
        setErrorMessage(err.response?.data?.message || 'Word not found. Try checking the spelling.');
      } else if (err.response?.status === 400) {
        setErrorMessage(err.response?.data?.message || 'Invalid word. Please check your spelling.');
      } else if (err.response?.status === 429) {
        setErrorMessage('Dictionary service rate limit reached. Please wait a moment and try again.');
      } else if (err.response?.status === 504 || err.response?.status === 408) {
        setErrorMessage('Dictionary service took too long to respond. Please try again.');
      } else if (err.response?.status === 503) {
        setErrorMessage(
          err.response?.data?.message ||
            'Dictionary service is temporarily unavailable. Please try again.'
        );
      } else {
        setErrorMessage(
          err.response?.data?.message ||
            'Dictionary service is temporarily unavailable. Please try again.'
        );
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleFormSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!isLoading) {
      handleSearch();
    }
  };

  const playAudio = (audioUrl?: string | null) => {
    if (!audioUrl) return;

    if (audioRef.current) {
      audioRef.current.pause();
      audioRef.current = null;
    }

    setIsPlayingAudio(true);
    setAudioError(false);

    try {
      const audio = new Audio(audioUrl);
      audioRef.current = audio;

      audio.onended = () => {
        setIsPlayingAudio(false);
        audioRef.current = null;
      };

      audio.onerror = () => {
        setIsPlayingAudio(false);
        setAudioError(true);
        audioRef.current = null;
      };

      audio.play().catch(() => {
        setIsPlayingAudio(false);
        setAudioError(true);
        audioRef.current = null;
      });
    } catch {
      setIsPlayingAudio(false);
      setAudioError(true);
      audioRef.current = null;
    }
  };

  // Cleanup on unmount
  useEffect(() => {
    return () => {
      if (audioRef.current) {
        audioRef.current.pause();
        audioRef.current = null;
      }
      if (abortControllerRef.current) {
        abortControllerRef.current.abort();
      }
    };
  }, []);

  // Has suggestions but no definitions (exact match not found)
  const isSuggestionsOnly =
    entry &&
    entry.suggestions &&
    entry.suggestions.length > 0 &&
    (!entry.partsOfSpeech || entry.partsOfSpeech.length === 0);

  return (
    <AppShell
      title="Dictionary"
      subtitle="Verified Merriam-Webster definitions, pronunciation, etymology, and usage"
    >
      <div className="max-w-4xl mx-auto space-y-8 pb-12">
        {/* HERO SEARCH SECTION */}
        <div className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-white via-[#FBFBF9] to-memora-green-light/20 border border-black/[0.06] p-6 md:p-10 shadow-card">
          <div className="relative z-10 max-w-2xl">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-memora-green/10 text-memora-green text-xs font-extrabold uppercase tracking-wider mb-3">
              <BookA className="w-3.5 h-3.5" />
              <span>Merriam-Webster Collegiate Dictionary</span>
            </div>

            <h1 className="text-3xl md:text-4xl font-extrabold text-memora-dark tracking-tight mb-2">
              Find the meaning. Remember the word.
            </h1>

            <p className="text-sm md:text-base text-memora-text-muted leading-relaxed mb-6">
              Explore authoritative definitions, pronunciation audio, contextual examples, and etymology.
            </p>

            {/* SEARCH FORM */}
            <form onSubmit={handleFormSubmit} className="space-y-2">
              <div className="flex flex-col sm:flex-row gap-2.5">
                <div className="relative flex-1">
                  <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none text-stone-400">
                    <Search className="w-5 h-5" />
                  </div>
                  <input
                    id="dictionary-search-input"
                    type="text"
                    value={searchQuery}
                    onChange={(e) => {
                      setSearchQuery(e.target.value);
                      if (validationError) setValidationError(null);
                    }}
                    placeholder="Search for a word (e.g. happy, resilience)..."
                    aria-label="Search for a word"
                    disabled={isLoading}
                    className={`w-full pl-11 pr-4 py-3.5 text-base bg-white border ${
                      validationError
                        ? 'border-red-400 ring-2 ring-red-100'
                        : 'border-black/[0.12] focus:border-memora-green focus:ring-4 focus:ring-memora-green/15'
                    } rounded-2xl text-memora-dark placeholder-stone-400 shadow-sm transition-all duration-200 outline-none`}
                  />
                </div>

                <Button
                  id="dictionary-search-button"
                  type="submit"
                  variant="primary"
                  size="lg"
                  disabled={isLoading}
                  className="rounded-2xl px-7 py-3.5 text-base font-bold shadow-md shadow-memora-green/25 shrink-0 flex items-center justify-center gap-2"
                >
                  {isLoading ? (
                    <>
                      <Loader2 className="w-4 h-4 animate-spin text-white" />
                      <span>Searching...</span>
                    </>
                  ) : (
                    <>
                      <Search className="w-4 h-4" />
                      <span>Search</span>
                    </>
                  )}
                </Button>
              </div>

              {/* Validation alert */}
              {validationError && (
                <p className="text-xs font-semibold text-red-600 pl-2 flex items-center gap-1.5 animate-fade-in">
                  <AlertCircle className="w-3.5 h-3.5 shrink-0" />
                  <span>{validationError}</span>
                </p>
              )}
            </form>

            {/* Quick Explore Chips */}
            <div className="mt-5 flex flex-wrap items-center gap-2 pt-2 border-t border-black/[0.04]">
              <span className="text-xs font-semibold text-stone-400 flex items-center gap-1">
                <Sparkles className="w-3.5 h-3.5 text-memora-green" />
                <span>Suggestions:</span>
              </span>
              {quickWords.map((word) => (
                <button
                  key={word}
                  type="button"
                  onClick={() => handleSearch(word)}
                  disabled={isLoading}
                  className="px-3 py-1 rounded-xl text-xs font-medium bg-white/80 hover:bg-memora-green-light hover:text-memora-green text-stone-600 border border-black/[0.06] transition-all hover:scale-105 active:scale-95 cursor-pointer shadow-2xs"
                >
                  {word}
                </button>
              ))}
            </div>
          </div>
        </div>

        {/* LOADING STATE */}
        {isLoading && (
          <div className="py-16 text-center space-y-4 animate-fade-in">
            <div className="w-14 h-14 rounded-2xl bg-memora-green-light flex items-center justify-center text-memora-green mx-auto shadow-sm animate-pulse">
              <BookOpen className="w-7 h-7 animate-bounce" />
            </div>
            <div>
              <h3 className="text-lg font-bold text-memora-dark">Searching the dictionary...</h3>
              <p className="text-xs text-memora-text-muted mt-1">
                Retrieving verified Merriam-Webster definitions, pronunciation, and examples
              </p>
            </div>
          </div>
        )}

        {/* ERROR STATE */}
        {!isLoading && errorMessage && (
          <div className="p-8 rounded-3xl bg-red-50/60 border border-red-200 text-center max-w-lg mx-auto space-y-4 animate-fade-in shadow-soft">
            <div className="w-12 h-12 rounded-2xl bg-red-100 text-red-600 flex items-center justify-center mx-auto">
              <AlertCircle className="w-6 h-6" />
            </div>
            <div>
              <h3 className="text-base font-bold text-red-950 mb-1">
                {errorMessage.includes('not found') ? 'Word Not Found' : 'Service Notice'}
              </h3>
              <p className="text-xs text-red-700/90 leading-relaxed max-w-sm mx-auto">
                {errorMessage}
              </p>
            </div>
            <div className="pt-1">
              <Button
                variant="outline"
                size="sm"
                onClick={() => {
                  setErrorMessage(null);
                  document.getElementById('dictionary-search-input')?.focus();
                }}
                className="border-red-200 text-red-800 hover:bg-red-100/60"
              >
                Try Another Word
              </Button>
            </div>
          </div>
        )}

        {/* SPELLING SUGGESTIONS ONLY STATE */}
        {!isLoading && !errorMessage && isSuggestionsOnly && entry && (
          <div className="p-8 rounded-3xl bg-amber-50/70 border border-amber-200 text-center max-w-xl mx-auto space-y-5 animate-fade-in shadow-soft">
            <div className="w-12 h-12 rounded-2xl bg-amber-100 text-amber-700 flex items-center justify-center mx-auto">
              <HelpCircle className="w-6 h-6" />
            </div>
            <div>
              <h3 className="text-base font-bold text-amber-950 mb-1">
                Did you mean one of these words?
              </h3>
              <p className="text-xs text-amber-800/90 leading-relaxed">
                We couldn't find an exact match for <span className="font-bold">"{searchedWord}"</span>. Choose a suggestion below to search:
              </p>
            </div>
            <div className="flex flex-wrap justify-center gap-2 pt-1">
              {entry.suggestions.map((suggestion, idx) => (
                <button
                  key={idx}
                  type="button"
                  onClick={() => handleSearch(suggestion)}
                  className="px-4 py-2 rounded-xl bg-white hover:bg-amber-100/80 border border-amber-300/80 text-amber-900 font-semibold text-sm transition-all hover:scale-105 active:scale-95 cursor-pointer shadow-2xs"
                >
                  {suggestion}
                </button>
              ))}
            </div>
          </div>
        )}

        {/* EMPTY READY STATE */}
        {!isLoading && !errorMessage && !entry && (
          <div className="text-center py-16 px-6 rounded-3xl bg-white/70 border border-black/[0.05] shadow-card max-w-md mx-auto space-y-4 animate-fade-in">
            <div className="w-16 h-16 rounded-3xl bg-[#F8F8F5] border border-black/[0.04] flex items-center justify-center text-memora-green mx-auto shadow-2xs">
              <BookA className="w-8 h-8" />
            </div>
            <div>
              <h3 className="text-lg font-bold text-memora-dark">Ready to Explore</h3>
              <p className="text-sm text-memora-text-muted mt-1 leading-relaxed">
                Search a word to explore its definition, pronunciation audio, and usage.
              </p>
            </div>
          </div>
        )}

        {/* WORD RESULT DISPLAY */}
        {!isLoading && !errorMessage && entry && !isSuggestionsOnly && (
          <div className="space-y-6 animate-fade-in">
            {/* WORD RESULT HEADER CARD */}
            <Card className="p-6 md:p-8 rounded-3xl bg-white border border-black/[0.06] shadow-card">
              <div className="flex flex-col md:flex-row md:items-center justify-between gap-6 pb-6 border-b border-black/[0.06]">
                <div className="space-y-2">
                  <div className="flex items-baseline gap-3 flex-wrap">
                    <h2 className="text-4xl md:text-5xl font-black text-memora-dark tracking-tight capitalize">
                      {entry.headword || entry.word}
                    </h2>
                    {entry.pronunciation && (
                      <span className="text-xl md:text-2xl font-serif text-memora-text-muted">
                        \{entry.pronunciation}\
                      </span>
                    )}
                  </div>

                  {entry.headword && entry.headword !== entry.word && (
                    <p className="text-xs font-semibold text-stone-400">
                      Standard form: <span className="font-bold text-stone-600">{entry.word}</span>
                    </p>
                  )}
                </div>

                {/* Audio Pronunciation Control */}
                <div>
                  {entry.audioUrl ? (
                    <button
                      type="button"
                      onClick={() => playAudio(entry.audioUrl)}
                      aria-label={`Listen to pronunciation of ${entry.word}`}
                      className={`inline-flex items-center gap-2.5 px-5 py-3 rounded-2xl font-bold text-sm transition-all duration-200 shadow-sm cursor-pointer ${
                        isPlayingAudio
                          ? 'bg-memora-green text-white ring-4 ring-memora-green/20'
                          : 'bg-memora-green-light hover:bg-memora-green text-memora-green hover:text-white'
                      }`}
                    >
                      <Volume2 className={`w-5 h-5 ${isPlayingAudio ? 'animate-bounce' : ''}`} />
                      <span>{isPlayingAudio ? 'Playing...' : '🔊 Listen'}</span>
                    </button>
                  ) : (
                    <div className="inline-flex items-center gap-2 px-4 py-2.5 rounded-2xl bg-stone-50 text-stone-400 text-xs font-semibold border border-black/[0.04]">
                      <VolumeX className="w-4 h-4" />
                      <span>Audio unavailable</span>
                    </div>
                  )}

                  {audioError && (
                    <p className="text-[11px] text-red-500 font-medium mt-1 text-center">
                      Audio playback failed
                    </p>
                  )}
                </div>
              </div>

              {/* SHORT DEFINITIONS HIGHLIGHTS */}
              {entry.shortDefinitions && entry.shortDefinitions.length > 0 && (
                <div className="mt-6 pt-2">
                  <h4 className="text-xs font-extrabold uppercase tracking-wider text-memora-green mb-2 flex items-center gap-1.5">
                    <Bookmark className="w-3.5 h-3.5" />
                    <span>Quick Summary</span>
                  </h4>
                  <ul className="space-y-1.5 pl-2">
                    {entry.shortDefinitions.slice(0, 3).map((sd, idx) => (
                      <li key={idx} className="text-sm text-stone-700 flex items-start gap-2">
                        <span className="w-1.5 h-1.5 rounded-full bg-memora-green mt-2 shrink-0" />
                        <span className="font-medium">{sd}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              )}

              {/* ORIGIN & ETYMOLOGY */}
              {entry.etymology && entry.etymology.trim().length > 0 && (
                <div className="mt-6 pt-4 border-t border-black/[0.04]">
                  <h4 className="text-xs font-extrabold uppercase tracking-wider text-memora-green mb-1.5 flex items-center gap-1.5">
                    <Quote className="w-3.5 h-3.5" />
                    <span>Origin & Etymology</span>
                  </h4>
                  <p className="text-sm text-stone-700 italic bg-[#FBFBF9] p-4 rounded-2xl border border-black/[0.04] leading-relaxed">
                    {entry.etymology}
                  </p>
                </div>
              )}
            </Card>

            {/* DETAILED MEANINGS SECTION */}
            {entry.partsOfSpeech && entry.partsOfSpeech.length > 0 && (
              <div className="space-y-6">
                <div className="flex items-center justify-between">
                  <h3 className="text-xl font-extrabold text-memora-dark tracking-tight">
                    Definitions & Contextual Usage
                  </h3>
                  <span className="text-xs font-semibold text-memora-text-muted">
                    {entry.partsOfSpeech.length}{' '}
                    {entry.partsOfSpeech.length === 1 ? 'part of speech' : 'parts of speech'}
                  </span>
                </div>

                {entry.partsOfSpeech.map((posItem: PartOfSpeechItem, pIndex: number) => (
                  <Card
                    key={pIndex}
                    className="p-6 md:p-8 rounded-3xl bg-white border border-black/[0.06] shadow-soft space-y-6"
                  >
                    {/* Part of speech header */}
                    <div className="flex items-center justify-between pb-4 border-b border-black/[0.05]">
                      <div className="flex items-center gap-2.5">
                        <span className="px-3.5 py-1 rounded-xl bg-memora-green text-white font-extrabold text-xs tracking-wider uppercase shadow-2xs">
                          {posItem.partOfSpeech || 'General'}
                        </span>
                        <span className="text-xs text-memora-text-muted font-medium">
                          {posItem.definitions.length}{' '}
                          {posItem.definitions.length === 1 ? 'definition' : 'definitions'}
                        </span>
                      </div>
                    </div>

                    {/* Definitions List */}
                    <ol className="space-y-5">
                      {posItem.definitions.map((def: DefinitionItem, dIndex: number) => (
                        <li key={dIndex} className="flex items-start gap-3.5 group">
                          <span className="w-6 h-6 rounded-full bg-stone-100 text-stone-600 font-bold text-xs flex items-center justify-center shrink-0 mt-0.5 border border-black/[0.04] group-hover:bg-memora-green-light group-hover:text-memora-green transition-colors">
                            {dIndex + 1}
                          </span>
                          <div className="flex-1 space-y-2.5 min-w-0">
                            {/* Definition Text */}
                            <p className="text-base text-memora-dark font-medium leading-relaxed">
                              {def.definition}
                            </p>

                            {/* Examples */}
                            {def.examples && def.examples.length > 0 && (
                              <div className="space-y-1.5 pt-1">
                                {def.examples.map((example, eIdx) => (
                                  <div
                                    key={eIdx}
                                    className="flex items-start gap-2 text-sm text-stone-600 italic bg-[#FBFBF9] px-3.5 py-2.5 rounded-xl border border-black/[0.04]"
                                  >
                                    <CornerDownRight className="w-3.5 h-3.5 text-memora-green shrink-0 mt-1" />
                                    <span>"{example}"</span>
                                  </div>
                                ))}
                              </div>
                            )}
                          </div>
                        </li>
                      ))}
                    </ol>
                  </Card>
                ))}
              </div>
            )}

            {/* FOOTER ATTRIBUTION */}
            <div className="pt-4 text-center">
              <p className="text-xs text-stone-400">
                Definitions powered by Merriam-Webster Collegiate® Dictionary
              </p>
            </div>
          </div>
        )}
      </div>
    </AppShell>
  );
};

export default DictionaryPage;
