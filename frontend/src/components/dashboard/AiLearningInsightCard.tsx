import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Sparkles, ArrowRight, ChevronDown, ChevronUp, AlertCircle, Compass, Zap } from 'lucide-react';
import { AdaptiveInsightResponse, InsightItem } from '../../types/insight';
import { Card } from '../ui/Card';
import { Button } from '../ui/Button';

interface AiLearningInsightCardProps {
  insight: AdaptiveInsightResponse | null;
  isLoading?: boolean;
}

const getPriorityBadge = (priority: string) => {
  const p = priority?.toUpperCase();
  switch (p) {
    case 'CRITICAL':
      return (
        <span className="inline-flex items-center gap-1 text-[11px] font-bold px-2.5 py-0.5 rounded-full bg-red-100 text-red-800 border border-red-200">
          <AlertCircle className="w-3 h-3" /> Critical Priority
        </span>
      );
    case 'HIGH':
      return (
        <span className="inline-flex items-center gap-1 text-[11px] font-bold px-2.5 py-0.5 rounded-full bg-amber-100 text-amber-900 border border-amber-200">
          <Zap className="w-3 h-3 text-amber-600" /> High Priority
        </span>
      );
    case 'MEDIUM':
      return (
        <span className="inline-flex items-center gap-1 text-[11px] font-semibold px-2.5 py-0.5 rounded-full bg-blue-100 text-blue-800 border border-blue-200">
          Recommended
        </span>
      );
    default:
      return (
        <span className="inline-flex items-center gap-1 text-[11px] font-semibold px-2.5 py-0.5 rounded-full bg-emerald-100 text-emerald-800 border border-emerald-200">
          On Track
        </span>
      );
  }
};

export const AiLearningInsightCard: React.FC<AiLearningInsightCardProps> = ({
  insight,
  isLoading = false,
}) => {
  const navigate = useNavigate();
  const [showSecondary, setShowSecondary] = useState(false);

  if (isLoading) {
    return (
      <Card variant="default" className="p-6 md:p-8 animate-pulse space-y-4">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-2xl bg-purple-100" />
          <div className="space-y-2 flex-1">
            <div className="h-4 bg-stone-200 rounded w-1/4" />
            <div className="h-3 bg-stone-100 rounded w-1/2" />
          </div>
        </div>
        <div className="h-16 bg-stone-100 rounded-2xl" />
      </Card>
    );
  }

  if (!insight || !insight.primaryInsight) {
    return null;
  }

  const primary = insight.primaryInsight;
  const secondaries = insight.secondaryInsights || [];

  return (
    <Card
      variant="default"
      className="p-6 md:p-8 relative overflow-hidden border border-purple-200/80 bg-gradient-to-br from-white via-purple-50/20 to-emerald-50/20 shadow-sm"
    >
      {/* Glow highlight */}
      <div className="absolute top-0 right-0 -mt-8 -mr-8 w-36 h-36 bg-purple-200/40 rounded-full blur-2xl pointer-events-none" />

      <div className="relative space-y-5">
        {/* Header Tag */}
        <div className="flex items-center justify-between flex-wrap gap-2">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-xl bg-purple-100 border border-purple-200 text-purple-800 flex items-center justify-center shrink-0">
              <Sparkles className="w-4 h-4" />
            </div>
            <div>
              <span className="text-[11px] font-black uppercase tracking-wider text-purple-900 block">
                Adaptive AI Intelligence
              </span>
              <p className="text-xs text-stone-500">
                Personalized based on your retention rate and forgetting risk curves
              </p>
            </div>
          </div>

          <div>{getPriorityBadge(primary.priority)}</div>
        </div>

        {/* Primary Insight Box */}
        <div className="p-5 rounded-2xl bg-white/90 border border-purple-100 shadow-xs space-y-3">
          <div>
            <h3 className="text-lg sm:text-xl font-black text-memora-dark tracking-tight">
              {primary.title}
            </h3>
            <p className="text-xs sm:text-sm text-stone-700 mt-1 leading-relaxed">
              {primary.description}
            </p>
          </div>

          <div className="flex items-center justify-between flex-wrap gap-3 pt-2 border-t border-purple-50">
            <div className="flex items-center gap-2 text-xs text-stone-500">
              <Compass className="w-3.5 h-3.5 text-purple-600" />
              <span>Recommended Next Action</span>
            </div>

            {primary.actionLabel && primary.actionRoute && (
              <Button
                variant="primary"
                size="sm"
                onClick={() => navigate(primary.actionRoute)}
                rightIcon={<ArrowRight className="w-4 h-4" />}
                className="bg-purple-700 hover:bg-purple-800 text-white shadow-sm"
              >
                {primary.actionLabel}
              </Button>
            )}
          </div>
        </div>

        {/* Secondary Insights Expandable Section */}
        {secondaries.length > 0 && (
          <div className="space-y-3 pt-1">
            <button
              type="button"
              onClick={() => setShowSecondary((prev) => !prev)}
              className="flex items-center justify-between w-full text-left text-xs font-bold text-stone-600 hover:text-stone-900 py-1 transition"
            >
              <span className="flex items-center gap-1.5">
                <span>{secondaries.length} additional learning insight{secondaries.length > 1 ? 's' : ''}</span>
              </span>
              <span className="flex items-center gap-1 text-purple-700">
                {showSecondary ? (
                  <>
                    <span>Hide</span>
                    <ChevronUp className="w-3.5 h-3.5" />
                  </>
                ) : (
                  <>
                    <span>View all</span>
                    <ChevronDown className="w-3.5 h-3.5" />
                  </>
                )}
              </span>
            </button>

            {showSecondary && (
              <div className="space-y-2.5 animate-fade-in">
                {secondaries.map((sec: InsightItem, idx: number) => (
                  <div
                    key={idx}
                    className="p-4 rounded-xl bg-white border border-stone-200/80 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 hover:border-stone-300 transition"
                  >
                    <div className="space-y-1">
                      <div className="flex items-center gap-2">
                        <span className="font-bold text-xs sm:text-sm text-memora-dark">
                          {sec.title}
                        </span>
                        {getPriorityBadge(sec.priority)}
                      </div>
                      <p className="text-xs text-stone-600 leading-relaxed max-w-xl">
                        {sec.description}
                      </p>
                    </div>

                    {sec.actionLabel && sec.actionRoute && (
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => navigate(sec.actionRoute)}
                        className="shrink-0 text-xs text-purple-800 border-purple-200 hover:bg-purple-50"
                      >
                        {sec.actionLabel}
                      </Button>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>
        )}
      </div>
    </Card>
  );
};

export default AiLearningInsightCard;
