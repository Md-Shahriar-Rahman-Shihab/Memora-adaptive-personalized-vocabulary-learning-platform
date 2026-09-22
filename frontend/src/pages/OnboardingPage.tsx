import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Sparkles,
  ArrowRight,
  Compass,
  Zap,
  RotateCw,
  Check,
  AlertCircle,
  RefreshCw,
  BookOpen,
} from 'lucide-react';
import { onboardingApi, getDestinationForOnboardingState } from '../api/onboardingApi';
import { OnboardingStateResponse } from '../types/onboarding';
import { Button } from '../components/ui/Button';
import { Card } from '../components/ui/Card';
import { LoadingSpinner } from '../components/ui/LoadingSpinner';

export const OnboardingPage: React.FC = () => {
  const navigate = useNavigate();

  const [stateData, setStateData] = useState<OnboardingStateResponse | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [isStarting, setIsStarting] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const checkStatus = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const res = await onboardingApi.getState();
      if (res.success && res.data) {
        const data = res.data;
        // If state is not ONBOARDING_REQUIRED, immediately redirect to appropriate page
        if (data.state !== 'ONBOARDING_REQUIRED') {
          const dest = getDestinationForOnboardingState(data);
          navigate(dest, { replace: true });
          return;
        }
        setStateData(data);
      } else {
        setError('Something went wrong while loading your learning status.');
      }
    } catch {
      setError('Something went wrong while loading your learning status.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    checkStatus();
  }, []);

  const handleStartAssessment = () => {
    if (isStarting) return;
    setIsStarting(true);
    // Reuse existing Assessment flow
    navigate('/assessment');
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-[#FBFBF9] flex items-center justify-center p-6">
        <LoadingSpinner size="lg" label="Preparing your personalized learning plan..." />
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-[#FBFBF9] flex items-center justify-center p-6">
        <Card variant="default" className="p-8 sm:p-10 max-w-md w-full text-center space-y-6 shadow-card border border-black/[0.08]">
          <div className="w-14 h-14 rounded-2xl bg-red-50 text-red-600 flex items-center justify-center mx-auto">
            <AlertCircle className="w-7 h-7" />
          </div>
          <div>
            <h2 className="text-xl font-bold text-memora-dark tracking-tight">Learning Status Error</h2>
            <p className="text-sm text-memora-text-muted mt-2">{error}</p>
          </div>
          <Button
            variant="primary"
            size="md"
            onClick={checkStatus}
            leftIcon={<RefreshCw className="w-4 h-4" />}
            className="w-full justify-center"
          >
            Try Again
          </Button>
        </Card>
      </div>
    );
  }

  const benefits = [
    {
      title: 'Discover your vocabulary level',
      description: 'Take a short assessment to understand your current CEFR level.',
      icon: <Compass className="w-5 h-5 text-memora-green" />,
    },
    {
      title: 'Learn at the right difficulty',
      description: 'Memora builds a personalized learning path based on your performance.',
      icon: <Zap className="w-5 h-5 text-memora-green" />,
    },
    {
      title: 'Remember what you learn',
      description: 'Smart review scheduling helps you retain words over time.',
      icon: <RotateCw className="w-5 h-5 text-memora-green" />,
    },
  ];

  return (
    <div className="min-h-screen bg-[#FBFBF9] flex flex-col justify-center items-center py-12 px-6 sm:px-8">
      {/* Brand Header */}
      <div className="text-center mb-8">
        <div className="inline-flex items-center gap-2.5 mb-3 group">
          <div className="w-11 h-11 rounded-2xl bg-memora-green-light flex items-center justify-center text-memora-green shadow-sm">
            <Sparkles className="w-6 h-6 fill-current" />
          </div>
          <span className="text-2xl font-black tracking-tight text-memora-dark">Memora</span>
        </div>
      </div>

      {/* Main Composition Card */}
      <Card
        variant="default"
        className="max-w-xl w-full p-8 sm:p-12 shadow-card border border-black/[0.08] text-center space-y-8 bg-white relative overflow-hidden"
      >
        {/* Subtle decorative glow */}
        <div className="absolute top-0 left-1/2 -translate-x-1/2 w-72 h-32 bg-memora-green/10 blur-3xl pointer-events-none rounded-full" />

        {/* Header copy */}
        <div className="space-y-3 relative z-10">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-memora-green-light text-memora-green text-xs font-bold uppercase tracking-wider">
            <BookOpen className="w-3.5 h-3.5" />
            <span>Diagnostic Placement</span>
          </div>

          <h1 className="text-3xl sm:text-4xl font-extrabold text-memora-dark tracking-tight leading-tight">
            Welcome to Memora
          </h1>

          <p className="text-base text-memora-text-muted leading-relaxed max-w-md mx-auto">
            Let’s build a vocabulary plan that actually remembers what you learn.
          </p>
        </div>

        {/* Primary CTA */}
        <div className="pt-2 relative z-10">
          <Button
            variant="primary"
            size="lg"
            onClick={handleStartAssessment}
            isLoading={isStarting}
            rightIcon={<ArrowRight className="w-5 h-5" />}
            className="w-full sm:w-auto px-8 py-4 text-base font-bold shadow-glow hover:shadow-lg transition-all justify-center"
          >
            Start Assessment
          </Button>
          <p className="text-xs text-memora-text-muted mt-2.5">
            20 adaptive questions · ~5 minutes · CEFR A1–C1
          </p>
        </div>

        {/* Three Benefits List */}
        <div className="text-left space-y-4 pt-4 border-t border-black/[0.06] relative z-10">
          {benefits.map((b, idx) => (
            <div
              key={idx}
              className="flex items-start gap-4 p-3.5 rounded-2xl bg-[#F8F8F5] border border-black/[0.04] transition-colors hover:bg-stone-100/70"
            >
              <div className="w-9 h-9 rounded-xl bg-memora-green-light text-memora-green flex items-center justify-center shrink-0 mt-0.5">
                <Check className="w-4 h-4 stroke-[3]" />
              </div>
              <div className="space-y-0.5 min-w-0">
                <h3 className="text-sm font-bold text-memora-dark">{b.title}</h3>
                <p className="text-xs text-memora-text-muted leading-relaxed">{b.description}</p>
              </div>
            </div>
          ))}
        </div>
      </Card>
    </div>
  );
};

export default OnboardingPage;
