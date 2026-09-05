import React from 'react';
import { Compass, Sparkles, RotateCw, Target, Flame, ArrowRight, CheckCircle2 } from 'lucide-react';
import { Card } from '../ui/Card';
import { Badge } from '../ui/Badge';
import { ProgressBar } from '../ui/ProgressBar';

export const HowItWorksSection: React.FC = () => {
  const steps = [
    {
      title: 'Assess',
      description: 'We discover your current vocabulary level and learning strengths.',
      icon: <Compass className="w-5 h-5 text-emerald-700" />,
      bg: 'bg-emerald-100/70',
      border: 'border-emerald-200/50',
    },
    {
      title: 'Adapt',
      description: 'Memora builds a personalized learning path just for you.',
      icon: <Sparkles className="w-5 h-5 text-purple-700" />,
      bg: 'bg-purple-100/70',
      border: 'border-purple-200/50',
    },
    {
      title: 'Remember',
      description: 'Smart revision brings the right words back before you forget them.',
      icon: <RotateCw className="w-5 h-5 text-amber-700" />,
      bg: 'bg-amber-100/70',
      border: 'border-amber-200/50',
    },
    {
      title: 'Master',
      description: 'Build long-term vocabulary and track your real progress.',
      icon: <Target className="w-5 h-5 text-blue-700" />,
      bg: 'bg-blue-100/70',
      border: 'border-blue-200/50',
    },
  ];

  return (
    <section id="how-it-works" className="py-20 bg-white/50 border-t border-black/[0.04]">
      <div className="max-w-7xl mx-auto px-6">
        {/* Section Heading */}
        <div className="max-w-2xl mb-14">
          <h2 className="text-3xl sm:text-4xl md:text-5xl font-extrabold text-memora-dark tracking-tight leading-tight">
            Discover how <span className="text-memora-green">Memora</span> <br />
            learns you.
          </h2>
          <p className="text-memora-text-muted text-base mt-3 leading-relaxed">
            Our AI system analyzes your performance and memory patterns to create the perfect
            learning experience for you.
          </p>
        </div>

        {/* Grid: 4 Step Cards & Memory Map Visualization */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-10 items-start">
          {/* Left: 4 Step Feature Cards */}
          <div className="lg:col-span-5 grid grid-cols-2 gap-4">
            {steps.map((step) => (
              <Card
                key={step.title}
                variant="subtle"
                hover
                className="p-5 flex flex-col justify-between min-h-[170px]"
              >
                <div
                  className={`w-11 h-11 rounded-2xl ${step.bg} ${step.border} border flex items-center justify-center mb-3`}
                >
                  {step.icon}
                </div>
                <div>
                  <h3 className="text-base font-bold text-memora-dark mb-1">{step.title}</h3>
                  <p className="text-xs text-memora-text-muted leading-relaxed">
                    {step.description}
                  </p>
                </div>
              </Card>
            ))}
          </div>

          {/* Right: "Your Memory Map" Interactive Ecosystem Card (Matching Approved Visual) */}
          <div className="lg:col-span-7 bg-white rounded-3xl border border-black/[0.08] shadow-card p-6 md:p-8">
            <div className="grid grid-cols-1 md:grid-cols-12 gap-6 items-start">
              {/* Main Memory Map Graph */}
              <div className="md:col-span-8 flex flex-col justify-between">
                <div className="flex items-center justify-between mb-4">
                  <h4 className="text-base font-bold text-memora-dark">Your Memory Map</h4>
                  <span className="text-xs font-semibold text-memora-text-muted hover:text-memora-dark cursor-pointer">
                    View all
                  </span>
                </div>

                {/* Node cluster */}
                <div className="relative h-60 w-full bg-[#FBFBF9] rounded-2xl border border-black/[0.04] p-4 flex items-center justify-center overflow-hidden">
                  {/* Central Memora Node */}
                  <div className="w-16 h-16 rounded-full bg-gradient-to-tr from-memora-green to-[#A0D44D] flex items-center justify-center text-white shadow-glow z-10">
                    <svg
                      className="w-8 h-8"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="2.2"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                    >
                      <path d="M9.5 2A2.5 2.5 0 0 1 12 4.5v15a2.5 2.5 0 0 1-4.96.44 2.5 2.5 0 0 1-2.96-3.08 3 3 0 0 1-.34-5.58 2.5 2.5 0 0 1 1.32-4.24 2.5 2.5 0 0 1 4.44-2.04Z" />
                      <path d="M14.5 2A2.5 2.5 0 0 0 12 4.5v15a2.5 2.5 0 0 0 4.96.44 2.5 2.5 0 0 0 2.96-3.08 3 3 0 0 0 .34-5.58 2.5 2.5 0 0 0-1.32-4.24 2.5 2.5 0 0 0-4.44-2.04Z" />
                    </svg>
                  </div>

                  {/* Satellite connected words */}
                  <div className="absolute top-6 left-10 bg-white/90 border border-black/[0.08] px-3 py-1 rounded-full text-xs font-semibold text-memora-dark shadow-sm">
                    happy
                  </div>
                  <div className="absolute top-6 right-10 bg-white/90 border border-black/[0.08] px-3 py-1 rounded-full text-xs font-semibold text-memora-dark shadow-sm">
                    joyful
                  </div>
                  <div className="absolute bottom-6 left-12 bg-white/90 border border-black/[0.08] px-3 py-1 rounded-full text-xs font-semibold text-memora-dark shadow-sm">
                    elated
                  </div>
                  <div className="absolute bottom-6 right-12 bg-white/90 border border-black/[0.08] px-3 py-1 rounded-full text-xs font-semibold text-memora-dark shadow-sm">
                    cheerful
                  </div>
                  <div className="absolute top-1/2 right-4 -translate-y-1/2 bg-white/90 border border-black/[0.08] px-3 py-1 rounded-full text-xs font-semibold text-memora-dark shadow-sm">
                    glad
                  </div>
                  <div className="absolute bottom-1/2 left-4 translate-y-1/2 bg-white/90 border border-black/[0.08] px-3 py-1 rounded-full text-xs font-semibold text-memora-dark shadow-sm">
                    excited
                  </div>

                  {/* Connection lines (SVG overlay) */}
                  <svg className="absolute inset-0 w-full h-full pointer-events-none stroke-stone-300 stroke-dashed [stroke-dasharray:3,3]">
                    <line x1="50%" y1="50%" x2="25%" y2="20%" />
                    <line x1="50%" y1="50%" x2="75%" y2="20%" />
                    <line x1="50%" y1="50%" x2="28%" y2="80%" />
                    <line x1="50%" y1="50%" x2="72%" y2="80%" />
                    <line x1="50%" y1="50%" x2="88%" y2="50%" />
                    <line x1="50%" y1="50%" x2="15%" y2="50%" />
                  </svg>
                </div>

                {/* Subcard: Next Review */}
                <div className="mt-4 p-4 rounded-2xl bg-[#F8F8F5] border border-black/[0.04]">
                  <div className="flex items-center justify-between mb-2">
                    <div>
                      <span className="text-xs font-bold text-memora-dark block">Next Review</span>
                      <span className="text-[11px] text-memora-text-muted">2 words due for review</span>
                    </div>
                    <Badge variant="green" size="sm" className="cursor-pointer hover:bg-emerald-200">
                      Review Now
                    </Badge>
                  </div>
                  <div className="space-y-1.5 pt-1">
                    <div className="flex items-center justify-between text-xs">
                      <span className="font-semibold text-stone-800">enthusiastic</span>
                      <span className="text-stone-400 text-[11px]">/ɪnˌθjuːziˈæstɪk/ • Due in 2h</span>
                    </div>
                    <div className="flex items-center justify-between text-xs">
                      <span className="font-semibold text-stone-800">persistent</span>
                      <span className="text-stone-400 text-[11px]">/pəˈsɪstənt/ • Due in 5h</span>
                    </div>
                  </div>
                </div>
              </div>

              {/* Right Side Widgets (Streak, XP, Level) */}
              <div className="md:col-span-4 flex flex-col gap-3">
                {/* Streak widget */}
                <div className="p-4 rounded-2xl bg-[#F8F8F5] border border-black/[0.04]">
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-medium text-memora-text-muted">Streak</span>
                    <span className="text-stone-300">→</span>
                  </div>
                  <div className="flex items-baseline justify-between mt-1">
                    <div className="flex items-baseline gap-1">
                      <span className="text-2xl font-extrabold text-memora-dark">12</span>
                      <span className="text-xs text-memora-text-muted">days</span>
                    </div>
                    <Flame className="w-6 h-6 fill-orange-500 text-orange-500" />
                  </div>
                </div>

                {/* XP Earned widget */}
                <div className="p-4 rounded-2xl bg-[#F8F8F5] border border-black/[0.04]">
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-medium text-memora-text-muted">XP Earned</span>
                    <span className="text-stone-300">→</span>
                  </div>
                  <div className="mt-1">
                    <span className="text-2xl font-extrabold text-memora-dark">1,250</span>
                    <span className="text-[11px] text-memora-text-muted block">this week</span>
                  </div>
                  {/* Mini Sparkline graph */}
                  <div className="mt-2 h-6 flex items-end gap-1">
                    <div className="w-2 h-2 bg-emerald-200 rounded-sm" />
                    <div className="w-2 h-3 bg-emerald-300 rounded-sm" />
                    <div className="w-2 h-5 bg-memora-green rounded-sm" />
                    <div className="w-2 h-4 bg-emerald-400 rounded-sm" />
                    <div className="w-2 h-6 bg-memora-green rounded-sm" />
                  </div>
                </div>

                {/* Level widget */}
                <div className="p-4 rounded-2xl bg-[#F8F8F5] border border-black/[0.04]">
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-medium text-memora-text-muted">Level</span>
                    <span className="text-stone-300">→</span>
                  </div>
                  <div className="mt-1 mb-2">
                    <span className="text-base font-extrabold text-memora-dark">Advanced</span>
                    <span className="text-[11px] text-memora-text-muted block">CEFR B2/C1</span>
                  </div>
                  <ProgressBar value={82} size="sm" barClassName="bg-memora-green" />
                  <span className="text-[10px] text-right font-bold text-memora-text-muted block mt-1">
                    82%
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};

export default HowItWorksSection;
