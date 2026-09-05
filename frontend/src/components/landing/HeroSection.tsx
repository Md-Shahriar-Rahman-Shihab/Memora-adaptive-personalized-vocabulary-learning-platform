import React from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowRight, Play, Sparkles, Volume2, CheckCircle2, RotateCw } from 'lucide-react';
import { Button } from '../ui/Button';
import { Badge } from '../ui/Badge';

export const HeroSection: React.FC = () => {
  const navigate = useNavigate();

  return (
    <section id="home" className="relative pt-8 pb-20 overflow-hidden">
      <div className="max-w-7xl mx-auto px-6 grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
        {/* Left Column: Editorial Headline & CTAs */}
        <div className="lg:col-span-6 space-y-6">
          {/* AI pill */}
          <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-[#EAF2DE] border border-[#D5E6BE] text-[#4D6D1A] text-xs font-bold shadow-sm">
            <Sparkles className="w-3.5 h-3.5 fill-current" />
            <span>AI-Powered Vocabulary Learning</span>
          </div>

          {/* Large Editorial Headline */}
          <h1 className="text-5xl sm:text-6xl md:text-7xl font-extrabold text-memora-dark tracking-tight leading-[1.08]">
            Words that <br />
            <span className="text-memora-green inline-block hover:scale-[1.02] transition-transform duration-300">
              stay
            </span>{' '}
            with you.
          </h1>

          {/* Subtitle */}
          <p className="text-base sm:text-lg text-memora-text-muted max-w-xl font-normal leading-relaxed">
            Memora uses adaptive learning and memory-based revision to help you learn vocabulary
            smarter and remember them longer.
          </p>

          {/* Action Buttons */}
          <div className="flex flex-wrap items-center gap-4 pt-2">
            <Button
              variant="primary"
              size="lg"
              onClick={() => navigate('/register')}
              rightIcon={<ArrowRight className="w-5 h-5" />}
              className="shadow-md"
            >
              Start Learning
            </Button>

            <Button
              variant="outline"
              size="lg"
              onClick={() => {
                const el = document.getElementById('how-it-works');
                el?.scrollIntoView({ behavior: 'smooth' });
              }}
              leftIcon={<Play className="w-4 h-4 fill-stone-700 text-stone-700" />}
            >
              Watch demo
            </Button>
          </div>

          {/* Social Proof */}
          <div className="pt-6 flex flex-wrap items-center gap-6 border-t border-black/[0.06]">
            <div className="flex items-center gap-3">
              <div className="flex -space-x-2.5 overflow-hidden">
                <img
                  className="inline-block h-10 w-10 rounded-full ring-2 ring-white object-cover shadow-sm"
                  src="https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&h=100&fit=crop&crop=face"
                  alt="Learner"
                />
                <img
                  className="inline-block h-10 w-10 rounded-full ring-2 ring-white object-cover shadow-sm"
                  src="https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100&h=100&fit=crop&crop=face"
                  alt="Learner"
                />
                <img
                  className="inline-block h-10 w-10 rounded-full ring-2 ring-white object-cover shadow-sm"
                  src="https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100&h=100&fit=crop&crop=face"
                  alt="Learner"
                />
              </div>
              <div className="text-xs">
                <span className="font-bold text-memora-dark block">Join 10,000+ learners</span>
                <span className="text-memora-text-muted">who are improving every day</span>
              </div>
            </div>

            <div className="h-8 w-px bg-black/[0.08] hidden sm:block" />

            <div className="flex flex-col text-xs">
              <span className="font-extrabold text-memora-dark text-sm tracking-tight">4.9/5</span>
              <div className="text-amber-500 font-bold flex gap-0.5 text-[11px]">
                ★★★★★
              </div>
            </div>
          </div>
        </div>

        {/* Right Column: 3D Memory Ecosystem & Floating Vocabulary Cards */}
        <div className="lg:col-span-6 relative flex items-center justify-center min-h-[460px] md:min-h-[520px]">
          {/* Soft ambient background glow */}
          <div className="absolute w-80 h-80 rounded-full bg-emerald-100/60 filter blur-3xl -z-10 animate-pulse-glow" />

          {/* Central organic 3D brain visual */}
          <div className="relative w-64 h-64 md:w-80 md:h-80 flex items-center justify-center">
            <img
              src="/hero-brain-3d.jpg"
              alt="Organic AI memory brain visualization"
              className="w-full h-full object-contain drop-shadow-2xl rounded-full"
              loading="eager"
            />
            {/* Neural connector ring */}
            <div className="absolute inset-0 rounded-full border border-memora-green/20 animate-spin [animation-duration:40s] pointer-events-none" />
          </div>

          {/* Floating Card 1: curious (Top Right) */}
          <div className="absolute -top-3 right-4 md:right-12 z-20 bg-white/95 backdrop-blur-md p-3.5 rounded-2xl border border-black/[0.08] shadow-card max-w-[170px] animate-float-slow">
            <div className="flex items-center justify-between gap-1 mb-1">
              <span className="font-bold text-sm text-memora-dark">curious</span>
              <Volume2 className="w-3.5 h-3.5 text-stone-400 cursor-pointer hover:text-memora-green" />
            </div>
            <p className="text-[11px] text-memora-text-muted italic mb-1.5">/ˈkjʊəriəs/</p>
            <p className="text-[11px] text-stone-600 line-clamp-2 leading-tight mb-2">
              eager to know or learn something.
            </p>
            <Badge variant="green" size="sm">
              <CheckCircle2 className="w-2.5 h-2.5" />
              Mastered
            </Badge>
          </div>

          {/* Floating Card 2: explore (Top Left) */}
          <div className="absolute top-10 left-0 md:-left-4 z-20 bg-white/95 backdrop-blur-md p-3.5 rounded-2xl border border-black/[0.08] shadow-card max-w-[165px] animate-float-delayed">
            <div className="flex items-center justify-between gap-1 mb-1">
              <span className="font-bold text-sm text-memora-dark">explore</span>
              <Volume2 className="w-3.5 h-3.5 text-stone-400 cursor-pointer hover:text-memora-green" />
            </div>
            <p className="text-[11px] text-memora-text-muted italic mb-1.5">/ɪkˈsplɔːr/</p>
            <p className="text-[11px] text-stone-600 line-clamp-2 leading-tight mb-2">
              to travel through an area to learn.
            </p>
            <Badge variant="amber" size="sm">
              <RotateCw className="w-2.5 h-2.5" />
              Learning
            </Badge>
          </div>

          {/* Floating Card 3: focus (Middle Right) */}
          <div className="absolute top-1/2 -right-2 md:right-0 z-20 -translate-y-1/2 bg-white/95 backdrop-blur-md p-3.5 rounded-2xl border border-black/[0.08] shadow-card max-w-[165px] animate-float-slow">
            <div className="flex items-center justify-between gap-1 mb-1">
              <span className="font-bold text-sm text-memora-dark">focus</span>
              <Volume2 className="w-3.5 h-3.5 text-stone-400 cursor-pointer hover:text-memora-green" />
            </div>
            <p className="text-[11px] text-memora-text-muted italic mb-1.5">/ˈfəʊkəs/</p>
            <p className="text-[11px] text-stone-600 line-clamp-2 leading-tight mb-2">
              to pay attention to something important.
            </p>
            <Badge variant="blue" size="sm">
              Review
            </Badge>
          </div>

          {/* Floating Card 4: improve (Bottom Left) */}
          <div className="absolute -bottom-2 left-2 md:left-6 z-20 bg-white/95 backdrop-blur-md p-3.5 rounded-2xl border border-black/[0.08] shadow-card max-w-[165px] animate-float-delayed">
            <div className="flex items-center justify-between gap-1 mb-1">
              <span className="font-bold text-sm text-memora-dark">improve</span>
              <Volume2 className="w-3.5 h-3.5 text-stone-400 cursor-pointer hover:text-memora-green" />
            </div>
            <p className="text-[11px] text-memora-text-muted italic mb-1.5">/ɪmˈpruːv/</p>
            <p className="text-[11px] text-stone-600 line-clamp-2 leading-tight mb-2">
              to make something better than before.
            </p>
            <Badge variant="amber" size="sm">
              Learning
            </Badge>
          </div>

          {/* Floating Card 5: achieve (Bottom Right) */}
          <div className="absolute -bottom-4 right-6 md:right-16 z-20 bg-white/95 backdrop-blur-md p-3.5 rounded-2xl border border-black/[0.08] shadow-card max-w-[165px] animate-float-slow">
            <div className="flex items-center justify-between gap-1 mb-1">
              <span className="font-bold text-sm text-memora-dark">achieve</span>
              <Volume2 className="w-3.5 h-3.5 text-stone-400 cursor-pointer hover:text-memora-green" />
            </div>
            <p className="text-[11px] text-memora-text-muted italic mb-1.5">/əˈtʃiːv/</p>
            <p className="text-[11px] text-stone-600 line-clamp-2 leading-tight mb-2">
              to successfully reach a desired goal.
            </p>
            <Badge variant="green" size="sm">
              <CheckCircle2 className="w-2.5 h-2.5" />
              Mastered
            </Badge>
          </div>
        </div>
      </div>
    </section>
  );
};

export default HeroSection;
