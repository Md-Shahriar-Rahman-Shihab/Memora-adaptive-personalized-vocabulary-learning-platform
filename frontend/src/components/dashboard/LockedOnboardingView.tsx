import React from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Compass,
  ArrowRight,
  Clock,
  HelpCircle,
  Lock,
  BookA,
  Trophy,
  Sparkles,
  Layers,
  RotateCw,
  TrendingUp,
  Award,
  CheckCircle2,
} from 'lucide-react';
import { Card } from '../ui/Card';
import { Button } from '../ui/Button';
import { Badge } from '../ui/Badge';

interface LockedOnboardingViewProps {
  onStartAssessment: () => void;
  userName?: string;
}

export const LockedOnboardingView: React.FC<LockedOnboardingViewProps> = ({
  onStartAssessment,
  userName = 'Learner',
}) => {
  const navigate = useNavigate();

  const lockedFeatures = [
    {
      title: 'Adaptive Learning Path',
      desc: 'Personalized daily curriculum balancing new vocabulary and spaced reviews tailored to your level.',
      icon: <Compass className="w-5 h-5" />,
    },
    {
      title: 'Spaced Repetition Review',
      desc: 'SuperMemo-2 memory engine forecasting retention decay and scheduling reviews before you forget.',
      icon: <RotateCw className="w-5 h-5" />,
    },
    {
      title: 'Polymorphic Quizzes',
      desc: 'Multiple choice, sentence translation, and fill-in-the-blank questions with instant feedback.',
      icon: <HelpCircle className="w-5 h-5" />,
    },
    {
      title: 'Memory Health & Analytics',
      desc: 'Longitudinal retention decay curves, CEFR tier distribution, and weak-word diagnosis.',
      icon: <TrendingUp className="w-5 h-5" />,
    },
    {
      title: 'Gamification & Badges',
      desc: 'Calendar-day streaks, auditable XP transaction ledger, and 10 unlockable milestone achievements.',
      icon: <Award className="w-5 h-5" />,
    },
  ];

  return (
    <div className="space-y-8 max-w-5xl mx-auto">
      {/* Primary Hero Onboarding Card */}
      <Card
        variant="default"
        className="p-8 sm:p-10 bg-gradient-to-br from-[#141A14] via-[#1A231A] to-[#141A14] text-white border-0 shadow-xl relative overflow-hidden"
      >
        {/* Subtle decorative glowing background accent */}
        <div className="absolute top-0 right-0 w-96 h-96 bg-memora-green/15 rounded-full blur-3xl pointer-events-none -mr-20 -mt-20" />

        <div className="relative z-10 max-w-2xl space-y-6">
          <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full bg-white/10 text-emerald-200 text-xs font-bold tracking-wide backdrop-blur-sm">
            <Sparkles className="w-3.5 h-3.5 fill-current text-memora-green-light" />
            <span>Step 1 of 2 • Initial Diagnostic Calibration</span>
          </div>

          <div className="space-y-2">
            <h2 className="text-3xl sm:text-4xl font-black tracking-tight text-white">
              Welcome to Memora, {userName.split(' ')[0]} 👋
            </h2>
            <p className="text-base sm:text-lg text-stone-300 leading-relaxed">
              Complete the Initial Diagnostic Calibration to calibrate your starting level and
              estimate your learning frontier. Memora will build your personalized learning path and
              calibrate your adaptive memory engine.
            </p>
          </div>

          {/* Quick Metrics Bar */}
          <div className="flex flex-wrap gap-4 py-2 text-xs font-semibold text-stone-300">
            <div className="flex items-center gap-2 bg-white/10 px-3.5 py-2 rounded-xl backdrop-blur-sm">
              <HelpCircle className="w-4 h-4 text-memora-green-light" />
              <span>10 questions</span>
            </div>
            <div className="flex items-center gap-2 bg-white/10 px-3.5 py-2 rounded-xl backdrop-blur-sm">
              <Clock className="w-4 h-4 text-memora-green-light" />
              <span>~2 minutes</span>
            </div>
            <div className="flex items-center gap-2 bg-white/10 px-3.5 py-2 rounded-xl backdrop-blur-sm">
              <Layers className="w-4 h-4 text-memora-green-light" />
              <span>CEFR A1 – C1</span>
            </div>
          </div>

          {/* Call to Action */}
          <div className="pt-2">
            <Button
              variant="primary"
              size="lg"
              onClick={onStartAssessment}
              rightIcon={<ArrowRight className="w-5 h-5" />}
              className="bg-memora-green hover:bg-[#5B7B26] text-white font-bold px-8 shadow-lg shadow-memora-green/30"
            >
              Start Assessment
            </Button>
          </div>
        </div>
      </Card>

      {/* 2-Step Onboarding Journey */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <Card variant="default" className="p-6 space-y-2.5">
          <div className="w-10 h-10 rounded-2xl bg-memora-green-light text-memora-green flex items-center justify-center font-black text-sm">
            1
          </div>
          <h4 className="text-sm font-bold text-memora-dark">Calibrate Your Level</h4>
          <p className="text-xs text-memora-text-muted leading-relaxed">
            Complete the 10-question diagnostic assessment.
          </p>
        </Card>

        <Card variant="default" className="p-6 space-y-2.5">
          <div className="w-10 h-10 rounded-2xl bg-memora-green-light text-memora-green flex items-center justify-center font-black text-sm">
            2
          </div>
          <h4 className="text-sm font-bold text-memora-dark">Build Your Learning Path</h4>
          <p className="text-xs text-memora-text-muted leading-relaxed">
            Memora creates the personalized learning path and unlocks the adaptive learning experience.
          </p>
        </Card>
      </div>

      {/* Features Locked Before Assessment */}
      <div className="space-y-4 pt-2">
        <div className="flex items-center justify-between">
          <div>
            <h3 className="text-base font-extrabold text-memora-dark">
              Core Learning Features (Locked)
            </h3>
            <p className="text-xs text-memora-text-muted">
              Complete your 10-question placement test to unlock all learning modules below.
            </p>
          </div>
          <Badge variant="amber" size="sm">
            <Lock className="w-3 h-3 mr-1" />
            Assessment Required
          </Badge>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {lockedFeatures.map((item, idx) => (
            <Card
              key={idx}
              variant="default"
              className="p-5 bg-stone-50/70 border border-black/[0.06] space-y-3 relative group"
            >
              <div className="flex items-center justify-between">
                <div className="w-9 h-9 rounded-xl bg-stone-200/70 text-stone-600 flex items-center justify-center">
                  {item.icon}
                </div>
                <div className="flex items-center gap-1 px-2 py-0.5 rounded-full bg-stone-200/60 text-stone-600 text-[10px] font-bold">
                  <Lock className="w-2.5 h-2.5" />
                  <span>Locked</span>
                </div>
              </div>

              <div>
                <h4 className="text-sm font-bold text-memora-dark mb-1">{item.title}</h4>
                <p className="text-xs text-memora-text-muted leading-relaxed">{item.desc}</p>
              </div>
            </Card>
          ))}
        </div>
      </div>

      {/* Available Now Section */}
      <Card variant="default" className="p-6 bg-[#F8F8F5] border border-black/[0.06]">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div className="space-y-1">
            <span className="text-xs font-bold uppercase tracking-wider text-memora-green block">
              Available Now
            </span>
            <h4 className="text-sm font-extrabold text-memora-dark">
              Explore open features while you prepare
            </h4>
            <p className="text-xs text-memora-text-muted">
              You can already explore words in the Collegiate Dictionary and check community rankings.
            </p>
          </div>

          <div className="flex items-center gap-3 shrink-0">
            <Button
              variant="outline"
              size="sm"
              onClick={() => navigate('/dictionary')}
              leftIcon={<BookA className="w-4 h-4 text-memora-green" />}
              className="border-black/[0.08] hover:bg-white"
            >
              Dictionary
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={() => navigate('/leaderboard')}
              leftIcon={<Trophy className="w-4 h-4 text-amber-500" />}
              className="border-black/[0.08] hover:bg-white"
            >
              Leaderboard
            </Button>
          </div>
        </div>
      </Card>
    </div>
  );
};

export default LockedOnboardingView;
