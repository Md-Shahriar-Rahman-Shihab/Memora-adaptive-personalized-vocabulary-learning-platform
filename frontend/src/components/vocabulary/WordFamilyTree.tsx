import React from 'react';
import { useNavigate } from 'react-router-dom';
import { GitBranch, ArrowRight, Check } from 'lucide-react';
import { Badge } from '../ui/Badge';

export interface WordFamilyTreeProps {
  currentWord: string;
  wordFamily: Record<string, string>;
  isAiAssisted?: boolean;
}

interface FamilyMember {
  pos: 'noun' | 'verb' | 'adjective' | 'adverb';
  label: string;
  word: string;
  color: string;
  badgeBg: string;
}

export const WordFamilyTree: React.FC<WordFamilyTreeProps> = ({
  currentWord,
  wordFamily,
}) => {
  const navigate = useNavigate();

  // Normalize grammatical keys
  const posConfigs: {
    key: 'noun' | 'verb' | 'adjective' | 'adverb';
    label: string;
    color: string;
    badgeBg: string;
  }[] = [
    { key: 'noun', label: 'Noun', color: 'text-blue-700 border-blue-200 bg-blue-50/50', badgeBg: 'bg-blue-100 text-blue-800' },
    { key: 'verb', label: 'Verb', color: 'text-emerald-700 border-emerald-200 bg-emerald-50/50', badgeBg: 'bg-emerald-100 text-emerald-800' },
    { key: 'adjective', label: 'Adjective', color: 'text-purple-700 border-purple-200 bg-purple-50/50', badgeBg: 'bg-purple-100 text-purple-800' },
    { key: 'adverb', label: 'Adverb', color: 'text-amber-700 border-amber-200 bg-amber-50/50', badgeBg: 'bg-amber-100 text-amber-800' },
  ];

  // Filter only family members that genuinely exist
  const existingMembers: FamilyMember[] = [];
  for (const cfg of posConfigs) {
    const rawVal = wordFamily[cfg.key];
    if (rawVal && rawVal.trim().length > 0) {
      existingMembers.push({
        pos: cfg.key,
        label: cfg.label,
        word: rawVal.trim(),
        color: cfg.color,
        badgeBg: cfg.badgeBg,
      });
    }
  }

  if (existingMembers.length === 0) {
    return (
      <div className="p-5 rounded-2xl bg-stone-50 border border-stone-200 text-center space-y-1 text-xs text-stone-500">
        <p className="font-medium text-stone-700">No extended word family forms available for this word.</p>
        <p>This word operates primarily in its singular grammatical form.</p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {/* Header bar */}
      <div className="flex items-center justify-between flex-wrap gap-2">
        <div className="flex items-center gap-2">
          <span className="text-xs font-bold uppercase tracking-wider text-memora-text-muted flex items-center gap-1.5">
            <GitBranch className="w-3.5 h-3.5 text-memora-green" />
            Grammatical Family Tree
          </span>
        </div>
        <span className="text-[11px] text-stone-400">
          Click any derivative to open its word detail
        </span>
      </div>

      {/* Visual Tree Layout */}
      <div className="rounded-3xl border border-black/[0.07] bg-white p-5 sm:p-7 shadow-xs">
        {/* Tree Root (Current Headword) */}
        <div className="flex flex-col items-center">
          <div className="inline-flex items-center gap-2 px-5 py-2.5 rounded-2xl bg-[#141A14] text-white shadow-md">
            <span className="text-xs uppercase tracking-widest font-mono text-emerald-400 font-semibold">
              Root
            </span>
            <span className="text-sm sm:text-base font-extrabold tracking-tight">
              {currentWord.toUpperCase()}
            </span>
          </div>

          {/* Stem connector */}
          <div className="w-0.5 h-6 bg-stone-300" />
        </div>

        {/* Desktop / Tablet: Horizontal Tree Branch */}
        <div className="hidden sm:block">
          {/* Horizontal crossbar line connecting children */}
          <div className="relative mx-auto max-w-[90%]">
            <div className="w-full h-0.5 bg-stone-300 rounded-full" />
          </div>

          {/* Child cards row */}
          <div
            className={`grid gap-3 pt-4 ${
              existingMembers.length === 1
                ? 'grid-cols-1 max-w-xs mx-auto'
                : existingMembers.length === 2
                ? 'grid-cols-2 max-w-md mx-auto'
                : existingMembers.length === 3
                ? 'grid-cols-3'
                : 'grid-cols-4'
            }`}
          >
            {existingMembers.map((member) => {
              const isCurrent = member.word.toLowerCase() === currentWord.toLowerCase();

              return (
                <div key={member.pos} className="flex flex-col items-center">
                  {/* Vertical drop line from crossbar */}
                  <div className="w-0.5 h-4 bg-stone-300 -mt-4 mb-2" />

                  <button
                    type="button"
                    onClick={() => {
                      if (!isCurrent) {
                        navigate(`/word/${encodeURIComponent(member.word)}`);
                      }
                    }}
                    disabled={isCurrent}
                    className={`w-full text-left p-3.5 rounded-2xl border transition-all duration-200 ${
                      isCurrent
                        ? 'border-memora-green bg-emerald-50/60 shadow-xs cursor-default ring-2 ring-memora-green/20'
                        : `${member.color} hover:shadow-md hover:-translate-y-0.5 hover:border-black/20 cursor-pointer`
                    }`}
                  >
                    <div className="flex items-center justify-between gap-1 mb-1.5">
                      <span
                        className={`text-[10px] font-extrabold uppercase tracking-wider px-2 py-0.5 rounded-md ${member.badgeBg}`}
                      >
                        {member.label}
                      </span>
                      {isCurrent ? (
                        <span className="flex items-center gap-0.5 text-[10px] font-bold text-memora-green">
                          <Check className="w-3 h-3" /> Current
                        </span>
                      ) : (
                        <ArrowRight className="w-3 h-3 text-stone-400 group-hover:text-stone-700" />
                      )}
                    </div>

                    <p className="font-extrabold text-xs sm:text-sm text-memora-dark tracking-tight break-words">
                      {member.word}
                    </p>
                  </button>
                </div>
              );
            })}
          </div>
        </div>

        {/* Mobile: Clean Vertical Flow (Zero horizontal overflow) */}
        <div className="sm:hidden space-y-2 pt-2">
          {existingMembers.map((member) => {
            const isCurrent = member.word.toLowerCase() === currentWord.toLowerCase();

            return (
              <button
                key={member.pos}
                type="button"
                onClick={() => {
                  if (!isCurrent) {
                    navigate(`/word/${encodeURIComponent(member.word)}`);
                  }
                }}
                disabled={isCurrent}
                className={`w-full flex items-center justify-between p-3 rounded-2xl border transition-all ${
                  isCurrent
                    ? 'border-memora-green bg-emerald-50/70 shadow-xs'
                    : 'border-stone-200 bg-white hover:bg-stone-50 active:bg-stone-100'
                }`}
              >
                <div className="flex items-center gap-2.5 min-w-0">
                  <span
                    className={`text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded-md shrink-0 ${member.badgeBg}`}
                  >
                    {member.label}
                  </span>
                  <span className="font-bold text-xs text-memora-dark truncate">
                    {member.word}
                  </span>
                </div>

                {isCurrent ? (
                  <Badge variant="green" size="sm">
                    Current
                  </Badge>
                ) : (
                  <span className="text-xs text-memora-green font-semibold flex items-center gap-1">
                    Explore <ArrowRight className="w-3 h-3" />
                  </span>
                )}
              </button>
            );
          })}
        </div>
      </div>
    </div>
  );
};

export default WordFamilyTree;
