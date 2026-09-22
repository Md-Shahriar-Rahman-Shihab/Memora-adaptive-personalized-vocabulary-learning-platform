import React, { useEffect, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { Award, CheckCircle2, ArrowRight, Sparkles, TrendingUp, Trophy, User } from 'lucide-react';
import { assessmentApi } from '../api/assessmentApi';
import { learningPathApi } from '../api/learningPathApi';
import { onboardingApi } from '../api/onboardingApi';
import { useAuth } from '../context/AuthContext';
import { PlacementResultResponse } from '../types/assessment';
import { AppShell } from '../components/layout/AppShell';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { ProgressBar } from '../components/ui/ProgressBar';
import { LoadingSpinner } from '../components/ui/LoadingSpinner';
import { ErrorState } from '../components/ui/ErrorState';

export const AssessmentResultPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { refreshUser } = useAuth();

  const assessmentId = searchParams.get('assessmentId');
  const [result, setResult] = useState<PlacementResultResponse | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [isStartingPath, setIsStartingPath] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchResult = async () => {
      let resolvedId = assessmentId ? Number(assessmentId) : null;

      if (!resolvedId) {
        try {
          const stateRes = await onboardingApi.getState();
          if (stateRes.success && stateRes.data?.assessmentId) {
            resolvedId = stateRes.data.assessmentId;
          } else {
            const historyRes = await assessmentApi.getHistory();
            if (historyRes.success && historyRes.data && historyRes.data.length > 0) {
              resolvedId = historyRes.data[0].assessmentId;
            }
          }
        } catch {
          // Ignore and handle missing ID below
        }
      }

      if (!resolvedId) {
        setError('No completed assessment found. Please complete an assessment first.');
        setIsLoading(false);
        return;
      }

      try {
        const res = await assessmentApi.getResult(resolvedId);
        if (res.success && res.data) {
          setResult(res.data);
          // Refresh auth context so header reflects new CEFR level
          await refreshUser();
        }
      } catch (err: any) {
        setError(err?.response?.data?.message || 'Failed to retrieve assessment placement results.');
      } finally {
        setIsLoading(false);
      }
    };

    fetchResult();
  }, [assessmentId]);

  const handleStartLearningPath = async () => {
    if (isStartingPath) return; // Prevent duplicate clicks
    setIsStartingPath(true);
    try {
      await learningPathApi.startPath();
      navigate('/learn-path');
    } catch {
      // If already started or race condition, gracefully navigate to learning path
      navigate('/learn-path');
    } finally {
      setIsStartingPath(false);
    }
  };

  if (isLoading) {
    return (
      <AppShell title="Assessment Result" subtitle="Calculating your CEFR placement...">
        <div className="py-20 flex justify-center">
          <LoadingSpinner size="lg" label="Computing explainable CEFR placement algorithm..." />
        </div>
      </AppShell>
    );
  }

  if (error || !result) {
    return (
      <AppShell title="Assessment Result" subtitle="Error loading result">
        <ErrorState message={error || 'Could not find placement result.'} onRetry={() => navigate('/assessment')} />
      </AppShell>
    );
  }

  return (
    <AppShell title="Diagnostic Results" subtitle="Your personalized CEFR placement is ready">
      <div className="max-w-2xl mx-auto py-6 space-y-8">
        {/* Celebratory Hero Card */}
        <Card variant="default" className="p-8 sm:p-10 text-center space-y-6 shadow-card">
          <div className="w-20 h-20 rounded-full bg-gradient-to-tr from-memora-green to-[#9DD349] text-white flex items-center justify-center mx-auto shadow-glow">
            <Award className="w-10 h-10" />
          </div>

          <div>
            <span className="text-xs font-bold uppercase tracking-wider text-memora-green block mb-1">
              Assessment Completed
            </span>
            <h2 className="text-3xl sm:text-4xl font-extrabold text-memora-dark tracking-tight">
              Your Memora Level: {result.estimatedLevel}
            </h2>
            <p className="text-sm text-memora-text-muted mt-2 max-w-md mx-auto">
              Our placement algorithm analyzed your accuracy and response latency across CEFR tiers
              to place you accurately.
            </p>
          </div>

          {/* Key Metrics */}
          <div className="grid grid-cols-3 gap-4 py-4 max-w-md mx-auto border-y border-black/[0.06]">
            <div>
              <span className="text-2xl font-black text-memora-dark block">
                {result.estimatedLevel}
              </span>
              <span className="text-xs text-memora-text-muted">Target Level</span>
            </div>
            <div>
              <span className="text-2xl font-black text-memora-dark block">
                {Math.round(result.confidenceScore)}%
              </span>
              <span className="text-xs text-memora-text-muted">Confidence</span>
            </div>
            <div>
              <span className="text-2xl font-black text-memora-dark block">
                {result.correctAnswers}/{result.totalQuestions}
              </span>
              <span className="text-xs text-memora-text-muted">Correct</span>
            </div>
          </div>

          {/* Level Performance Breakdown */}
          {result.levelPerformance && (
            <div className="text-left space-y-3 pt-2">
              <h4 className="text-xs font-bold text-memora-dark uppercase tracking-wider">
                Performance by CEFR Difficulty Tier
              </h4>
              <div className="space-y-2">
                {Object.entries(result.levelPerformance).map(([level, acc]) => (
                  <div key={level} className="space-y-1">
                    <div className="flex justify-between text-xs font-semibold text-stone-700">
                      <span>Level {level}</span>
                      <span>{Math.round(Number(acc))}%</span>
                    </div>
                    <ProgressBar
                      value={Number(acc)}
                      size="sm"
                      barClassName={Number(acc) >= 75 ? 'bg-memora-green' : 'bg-stone-400'}
                    />
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Action Navigation */}
          <div className="pt-4 space-y-3">
            <Button
              variant="primary"
              size="lg"
              onClick={handleStartLearningPath}
              isLoading={isStartingPath}
              rightIcon={<ArrowRight className="w-5 h-5" />}
              className="w-full justify-center shadow-lg"
            >
              Build My Learning Path
            </Button>

            <div className="grid grid-cols-2 gap-3">
              <Button
                variant="outline"
                size="md"
                onClick={() => navigate('/leaderboard')}
                leftIcon={<Trophy className="w-4 h-4 text-amber-500" />}
                className="w-full justify-center border-black/[0.08] hover:bg-stone-50"
              >
                View Leaderboard
              </Button>
              <Button
                variant="outline"
                size="md"
                onClick={() => navigate('/profile')}
                leftIcon={<User className="w-4 h-4 text-memora-green" />}
                className="w-full justify-center border-black/[0.08] hover:bg-stone-50"
              >
                View Profile
              </Button>
            </div>
          </div>
        </Card>
      </div>
    </AppShell>
  );
};

export default AssessmentResultPage;
