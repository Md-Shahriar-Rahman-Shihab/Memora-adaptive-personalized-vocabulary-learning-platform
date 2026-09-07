import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Activity, Clock, AlertTriangle, CheckCircle2, RotateCw } from 'lucide-react';
import { InsightMetrics } from '../../types/insight';
import { MemoryWordResponse } from '../../types/memory';
import { Card } from '../ui/Card';
import { Badge } from '../ui/Badge';
import { Button } from '../ui/Button';

interface MemoryHealthWidgetProps {
  metrics?: InsightMetrics;
  weakWords?: MemoryWordResponse[];
  activeLevel?: string;
}

export const MemoryHealthWidget: React.FC<MemoryHealthWidgetProps> = ({
  metrics,
  weakWords = [],
  activeLevel,
}) => {
  const navigate = useNavigate();

  // Normalize retention rate to percentage (0 - 100)
  const rawRetention = metrics?.retentionRate ?? 0;
  const retentionPercent = Math.round(
    rawRetention > 1 ? rawRetention : rawRetention * 100
  );

  const dueCount = metrics?.dueReviewsCount ?? 0;
  const weakCount = metrics?.weakWordsCount ?? weakWords.length;
  const avgResponseSec = metrics?.avgResponseTimeMs
    ? (metrics.avgResponseTimeMs / 1000).toFixed(1)
    : '0.0';

  const isAllCaughtUp = dueCount === 0 && weakCount === 0 && weakWords.length === 0;

  return (
    <Card variant="subtle" className="p-6 space-y-5 border border-stone-200/80">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h4 className="text-sm font-bold text-memora-dark flex items-center gap-1.5">
            <Activity className="w-4 h-4 text-emerald-600" />
            Memory Network Health
          </h4>
          <p className="text-[11px] text-memora-text-muted">
            Continuous SM-2 retention tracking
          </p>
        </div>
        <Badge variant={isAllCaughtUp ? 'green' : dueCount > 0 ? 'amber' : 'blue'} size="sm">
          {isAllCaughtUp ? 'Optimal' : `${retentionPercent}% Retention`}
        </Badge>
      </div>

      {/* Main Retention Score Card */}
      <div className="bg-white rounded-2xl border border-stone-200/80 p-4 space-y-3">
        <div className="flex items-baseline justify-between">
          <span className="text-xs font-bold text-stone-500 uppercase tracking-wider">
            Memory Retention
          </span>
          <span className="text-2xl font-black text-memora-dark tracking-tight">
            {retentionPercent}%
          </span>
        </div>

        {/* Retention Visual Bar */}
        <div className="w-full h-2.5 bg-stone-100 rounded-full overflow-hidden">
          <div
            className={`h-full transition-all duration-500 ${
              retentionPercent >= 80
                ? 'bg-emerald-500'
                : retentionPercent >= 60
                ? 'bg-amber-500'
                : 'bg-red-500'
            }`}
            style={{ width: `${Math.min(100, Math.max(5, retentionPercent))}%` }}
          />
        </div>

        {/* Real Metrics Grid */}
        <div className="grid grid-cols-3 gap-2 pt-2 border-t border-stone-100 text-center">
          <div className="p-2 rounded-xl bg-stone-50/80">
            <span className="text-[10px] uppercase font-bold text-stone-400 block">
              Due
            </span>
            <span className="text-sm font-black text-memora-dark">
              {dueCount}
            </span>
          </div>
          <div className="p-2 rounded-xl bg-stone-50/80">
            <span className="text-[10px] uppercase font-bold text-stone-400 block">
              High Risk
            </span>
            <span className="text-sm font-black text-stone-900">
              {weakCount}
            </span>
          </div>
          <div className="p-2 rounded-xl bg-stone-50/80">
            <span className="text-[10px] uppercase font-bold text-stone-400 block">
              Speed
            </span>
            <span className="text-sm font-black text-memora-dark">
              {avgResponseSec}s
            </span>
          </div>
        </div>
      </div>

      {/* Active High-Risk Words or Caught Up State */}
      {isAllCaughtUp ? (
        <div className="p-4 rounded-2xl bg-emerald-50/60 border border-emerald-200/70 text-center space-y-1.5">
          <CheckCircle2 className="w-5 h-5 text-emerald-600 mx-auto" />
          <p className="text-xs font-bold text-emerald-950">You're all caught up! 🎉</p>
          <p className="text-[11px] text-emerald-800/90 leading-relaxed">
            All active vocabulary words are securely stored in your long-term memory network.
          </p>
        </div>
      ) : weakWords.length > 0 ? (
        <div className="space-y-2">
          <div className="flex items-center justify-between text-[11px] text-stone-500 font-bold uppercase tracking-wider">
            <span className="flex items-center gap-1">
              <AlertTriangle className="w-3 h-3 text-amber-600" />
              Words Requiring Reinforcement
            </span>
            <span>{activeLevel || metrics?.activeLevel || ''}</span>
          </div>
          <div className="flex flex-wrap gap-1.5">
            {weakWords.slice(0, 6).map((w) => (
              <span
                key={w.wordId}
                className="inline-flex items-center gap-1 px-2.5 py-1 rounded-xl bg-white border border-stone-200 text-xs font-bold text-stone-800 shadow-2xs hover:border-amber-300 transition"
              >
                <span>{w.word}</span>
                <span className="text-[10px] font-semibold text-amber-700 bg-amber-50 px-1 rounded">
                  {Math.round(w.masteryScore)}%
                </span>
              </span>
            ))}
          </div>
        </div>
      ) : null}

      {/* Review Action */}
      {dueCount > 0 && (
        <Button
          variant="primary"
          size="sm"
          onClick={() => navigate('/review')}
          leftIcon={<RotateCw className="w-3.5 h-3.5" />}
          className="w-full justify-center text-xs py-2.5"
        >
          Review {dueCount} Due Word{dueCount > 1 ? 's' : ''} Now
        </Button>
      )}
    </Card>
  );
};

export default MemoryHealthWidget;
