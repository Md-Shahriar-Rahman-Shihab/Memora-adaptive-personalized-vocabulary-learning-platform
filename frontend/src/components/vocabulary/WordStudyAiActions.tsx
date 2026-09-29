import React, { useState } from 'react';
import { Sparkles, BookOpen, Lightbulb, Compass, Volume2, RefreshCw, AlertCircle } from 'lucide-react';
import { aiApi } from '../../api/aiApi';
import {
  AiExplanationResponse,
  AiExampleResponse,
  AiMemoryTipResponse,
  AiUsageResponse,
} from '../../types/ai';

export interface WordStudyAiActionsProps {
  word: string;
  wordId?: string | number;
  cefrLevel?: string;
  onSpeak?: (text: string) => void;
}

type TabType = 'explanation' | 'example' | 'tip' | 'usage';

export const WordStudyAiActions: React.FC<WordStudyAiActionsProps> = ({
  word,
  wordId,
  cefrLevel,
  onSpeak,
}) => {
  const [activeTab, setActiveTab] = useState<TabType | null>(null);
  const [loadingTab, setLoadingTab] = useState<TabType | null>(null);
  const [errorTab, setErrorTab] = useState<TabType | null>(null);

  // Cached responses per word
  const [explanation, setExplanation] = useState<AiExplanationResponse | null>(null);
  const [example, setExample] = useState<AiExampleResponse | null>(null);
  const [tip, setTip] = useState<AiMemoryTipResponse | null>(null);
  const [usage, setUsage] = useState<AiUsageResponse | null>(null);

  const handleSpeech = (text: string) => {
    if (onSpeak) {
      onSpeak(text);
      return;
    }
    if ('speechSynthesis' in window && text) {
      window.speechSynthesis.cancel();
      const utterance = new SpeechSynthesisUtterance(text);
      utterance.lang = 'en-US';
      utterance.rate = 0.9;
      window.speechSynthesis.speak(utterance);
    }
  };

  const fetchTabContent = async (tab: TabType) => {
    setLoadingTab(tab);
    setErrorTab(null);

    const numericWordId = typeof wordId === 'number' ? wordId : Number(wordId) || undefined;
    const req = {
      word,
      wordId: numericWordId,
      cefrLevel,
    };

    try {
      if (tab === 'explanation') {
        const res = await aiApi.getWordExplanation(req);
        if (res.success && res.data) {
          setExplanation(res.data);
        } else {
          setErrorTab('explanation');
        }
      } else if (tab === 'example') {
        const res = await aiApi.getWordExample(req);
        if (res.success && res.data) {
          setExample(res.data);
        } else {
          setErrorTab('example');
        }
      } else if (tab === 'tip') {
        const res = await aiApi.getMemoryTip(req);
        if (res.success && res.data) {
          setTip(res.data);
        } else {
          setErrorTab('tip');
        }
      } else if (tab === 'usage') {
        const res = await aiApi.getContextualUsage(req);
        if (res.success && res.data) {
          setUsage(res.data);
        } else {
          setErrorTab('usage');
        }
      }
    } catch {
      setErrorTab(tab);
    } finally {
      setLoadingTab(null);
    }
  };

  const handleTabClick = (tab: TabType) => {
    if (activeTab === tab) {
      // Toggle close if already open
      setActiveTab(null);
      return;
    }

    setActiveTab(tab);

    // Check if we need to fetch
    const needsFetch =
      (tab === 'explanation' && !explanation) ||
      (tab === 'example' && !example) ||
      (tab === 'tip' && !tip) ||
      (tab === 'usage' && !usage);

    if (needsFetch) {
      fetchTabContent(tab);
    }
  };

  const tabs: { id: TabType; label: string; icon: React.ReactNode }[] = [
    { id: 'explanation', label: 'AI Explanation', icon: <Sparkles className="w-3.5 h-3.5 text-purple-600" /> },
    { id: 'example', label: 'Personalized Example', icon: <BookOpen className="w-3.5 h-3.5 text-blue-600" /> },
    { id: 'tip', label: 'Memory Tip', icon: <Lightbulb className="w-3.5 h-3.5 text-amber-600" /> },
    { id: 'usage', label: 'Contextual Usage', icon: <Compass className="w-3.5 h-3.5 text-emerald-600" /> },
  ];

  return (
    <div className="space-y-3 pt-1">
      <div className="flex items-center justify-between">
        <span className="text-[11px] font-bold uppercase tracking-wider text-memora-text-muted flex items-center gap-1.5">
          <Sparkles className="w-3.5 h-3.5 text-purple-600" />
          AI Learning Assistant
        </span>
        {activeTab && (
          <button
            type="button"
            onClick={() => setActiveTab(null)}
            className="text-[11px] text-stone-500 hover:text-stone-700 font-medium transition"
          >
            Hide details
          </button>
        )}
      </div>

      {/* Action Buttons / Pills */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
        {tabs.map((tab) => {
          const isActive = activeTab === tab.id;
          const isLoading = loadingTab === tab.id;

          return (
            <button
              key={tab.id}
              type="button"
              onClick={() => handleTabClick(tab.id)}
              className={`flex items-center justify-center gap-1.5 px-3 py-2 rounded-xl text-xs font-semibold transition-all border ${
                isActive
                  ? 'bg-purple-50 text-purple-900 border-purple-300 shadow-sm'
                  : 'bg-white hover:bg-stone-50 text-stone-700 border-stone-200 hover:border-stone-300 shadow-xs'
              }`}
            >
              {isLoading ? (
                <RefreshCw className="w-3.5 h-3.5 animate-spin text-purple-600" />
              ) : (
                tab.icon
              )}
              <span>{tab.label}</span>
            </button>
          );
        })}
      </div>

      {/* Tab Content Display */}
      {activeTab && (
        <div className="rounded-2xl border border-purple-200/80 bg-gradient-to-b from-purple-50/40 to-white p-4 shadow-sm animate-fade-in space-y-3">
          {/* Loading State */}
          {loadingTab === activeTab && (
            <div className="flex flex-col items-center justify-center py-6 text-center space-y-2">
              <RefreshCw className="w-6 h-6 animate-spin text-purple-600" />
              <p className="text-xs font-medium text-stone-600">
                Generating personalized AI insight for <span className="font-bold text-stone-900">"{word}"</span>...
              </p>
            </div>
          )}

          {/* Error State */}
          {errorTab === activeTab && loadingTab !== activeTab && (
            <div className="flex items-center justify-between p-3 rounded-xl bg-red-50 border border-red-200 text-red-800 text-xs">
              <div className="flex items-center gap-2">
                <AlertCircle className="w-4 h-4 shrink-0 text-red-600" />
                <span>Couldn't generate this right now. Please try again.</span>
              </div>
              <button
                type="button"
                onClick={() => fetchTabContent(activeTab)}
                className="px-2.5 py-1 rounded-lg bg-white border border-red-300 text-red-700 hover:bg-red-50 font-semibold shadow-xs transition shrink-0"
              >
                Try again
              </button>
            </div>
          )}

          {/* 1. Explanation Tab (Reference working implementation) */}
          {activeTab === 'explanation' && explanation && loadingTab !== 'explanation' && (
            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <span className="text-[11px] font-bold uppercase tracking-wider text-purple-800">
                  Adaptive Explanation
                </span>
              </div>
              <p className="text-xs sm:text-sm text-stone-800 leading-relaxed font-normal">
                {explanation.explanation}
              </p>
              {explanation.breakdown && (
                <p className="text-xs text-stone-500 italic pt-1 border-t border-purple-100/60">
                  {explanation.breakdown}
                </p>
              )}
            </div>
          )}

          {/* 2. Example Tab (Fixed field mapping: exampleSentence + context) */}
          {activeTab === 'example' && example && loadingTab !== 'example' && (() => {
            const sentence = example.exampleSentence || example.sentence;
            const contextDesc = example.context;

            if (!sentence && !contextDesc) {
              return (
                <div className="flex items-center justify-between p-3 rounded-xl bg-amber-50 border border-amber-200 text-amber-900 text-xs">
                  <span>No example sentence available for this word.</span>
                  <button
                    type="button"
                    onClick={() => fetchTabContent('example')}
                    className="px-2.5 py-1 rounded-lg bg-white border border-amber-300 text-amber-800 hover:bg-amber-50 font-semibold shadow-xs transition shrink-0"
                  >
                    Try again
                  </button>
                </div>
              );
            }

            return (
              <div className="space-y-2.5">
                <div className="flex items-center justify-between">
                  <span className="text-[11px] font-bold uppercase tracking-wider text-blue-800">
                    Contextual Example Sentence
                  </span>
                </div>
                <div className="p-3 rounded-xl bg-blue-50/60 border border-blue-100 space-y-1.5">
                  {sentence && (
                    <div className="flex items-start justify-between gap-2">
                      <p className="text-xs sm:text-sm font-medium text-stone-900 italic leading-relaxed">
                        "{sentence}"
                      </p>
                      <button
                        type="button"
                        onClick={() => handleSpeech(sentence)}
                        className="p-1.5 rounded-lg bg-blue-100 hover:bg-blue-200 text-blue-800 transition shrink-0"
                        title="Pronounce example sentence"
                      >
                        <Volume2 className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  )}
                  {contextDesc && (
                    <p className="text-xs text-stone-600 pt-1 border-t border-blue-100">
                      {contextDesc}
                    </p>
                  )}
                </div>
              </div>
            );
          })()}

          {/* 3. Memory Tip Tab (Fixed field mapping: memoryTip + association) */}
          {activeTab === 'tip' && tip && loadingTab !== 'tip' && (() => {
            const tipText = tip.memoryTip || tip.tip;
            const associationText = tip.association || tip.technique;

            if (!tipText && !associationText) {
              return (
                <div className="flex items-center justify-between p-3 rounded-xl bg-amber-50 border border-amber-200 text-amber-900 text-xs">
                  <span>No memory tip available for this word.</span>
                  <button
                    type="button"
                    onClick={() => fetchTabContent('tip')}
                    className="px-2.5 py-1 rounded-lg bg-white border border-amber-300 text-amber-800 hover:bg-amber-50 font-semibold shadow-xs transition shrink-0"
                  >
                    Try again
                  </button>
                </div>
              );
            }

            return (
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <span className="text-[11px] font-bold uppercase tracking-wider text-amber-800">
                    Mnemonic Memory Hack
                  </span>
                </div>
                {tipText && (
                  <div className="p-3 rounded-xl bg-amber-50/70 border border-amber-200/80">
                    <p className="text-xs sm:text-sm text-stone-900 font-medium leading-relaxed">
                      💡 {tipText}
                    </p>
                  </div>
                )}
                {associationText && (
                  <p className="text-xs text-stone-600 px-1 italic">
                    {associationText}
                  </p>
                )}
              </div>
            );
          })()}

          {/* 4. Contextual Usage Tab (Fixed field mapping: usageNotes + collocations Array + register) */}
          {activeTab === 'usage' && usage && loadingTab !== 'usage' && (() => {
            const notes = usage.usageNotes || usage.context;
            const registerText = usage.register || usage.nuances;
            const collocationsList: string[] = Array.isArray(usage.collocations)
              ? usage.collocations
              : typeof usage.collocations === 'string'
              ? (usage.collocations as string).split(/[,;]+/).map((c) => c.trim()).filter(Boolean)
              : [];

            if (!notes && !registerText && collocationsList.length === 0) {
              return (
                <div className="flex items-center justify-between p-3 rounded-xl bg-amber-50 border border-amber-200 text-amber-900 text-xs">
                  <span>No contextual usage information available for this word.</span>
                  <button
                    type="button"
                    onClick={() => fetchTabContent('usage')}
                    className="px-2.5 py-1 rounded-lg bg-white border border-amber-300 text-amber-800 hover:bg-amber-50 font-semibold shadow-xs transition shrink-0"
                  >
                    Try again
                  </button>
                </div>
              );
            }

            return (
              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <span className="text-[11px] font-bold uppercase tracking-wider text-emerald-800">
                    Context & Collocations
                  </span>
                  {registerText && (
                    <span className="text-[10px] font-semibold px-2 py-0.5 rounded-full bg-stone-100 text-stone-800 border border-stone-200">
                      {registerText}
                    </span>
                  )}
                </div>

                {notes && (
                  <div className="space-y-1">
                    <span className="text-[10px] font-bold text-stone-500 uppercase tracking-wider">
                      How it is used
                    </span>
                    <p className="text-xs sm:text-sm text-stone-800 leading-relaxed">
                      {notes}
                    </p>
                  </div>
                )}

                {collocationsList.length > 0 && (
                  <div className="space-y-1.5">
                    <span className="text-[10px] font-bold text-stone-500 uppercase tracking-wider">
                      Common Collocations
                    </span>
                    <div className="flex flex-wrap gap-1.5">
                      {collocationsList.map((item, idx) => (
                        <span
                          key={idx}
                          className="px-2 py-0.5 rounded-md bg-stone-100 text-stone-700 text-xs font-mono font-medium border border-stone-200"
                        >
                          {item}
                        </span>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            );
          })()}
        </div>
      )}
    </div>
  );
};

export default WordStudyAiActions;
