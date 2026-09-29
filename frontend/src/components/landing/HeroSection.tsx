import React from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowRight, Play, Sparkles, Volume2, CheckCircle2, RotateCw, Zap, Brain } from 'lucide-react';
import { Button } from '../ui/Button';
import { Badge } from '../ui/Badge';

export const HeroSection: React.FC = () => {
  const navigate = useNavigate();

  const speakWord = (word: string) => {
    if ('speechSynthesis' in window) {
      window.speechSynthesis.cancel();
      const utterance = new SpeechSynthesisUtterance(word);
      utterance.lang = 'en-US';
      utterance.rate = 0.9;
      window.speechSynthesis.speak(utterance);
    }
  };

  return (
    <section id="home" className="relative pt-6 pb-20">
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
                  src="/images/avatars/avatar-1.jpg"
                  alt="Learner"
                />
                <img
                  className="inline-block h-10 w-10 rounded-full ring-2 ring-white object-cover shadow-sm"
                  src="/images/avatars/avatar-2.jpg"
                  alt="Learner"
                />
                <img
                  className="inline-block h-10 w-10 rounded-full ring-2 ring-white object-cover shadow-sm"
                  src="/images/avatars/avatar-3.jpg"
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
        <div className="lg:col-span-6 relative flex items-center justify-center min-h-[560px] md:min-h-[620px] select-none py-6">
          {/* Soft ambient background glow */}
          <div className="absolute w-96 h-96 rounded-full bg-emerald-200/40 filter blur-3xl -z-10 animate-pulse-glow" />

          {/* Central organic 3D brain island visual */}
          <div className="relative w-full max-w-[500px] sm:max-w-[540px] lg:max-w-[600px] flex items-center justify-center">
            <picture className="w-full flex items-center justify-center">
              <source srcSet="/hero-brain-ecosystem.webp" type="image/webp" />
              <img
                src="/hero-brain-ecosystem.png"
                alt="Organic AI memory island visualization"
                className="w-full h-auto object-contain drop-shadow-[0_20px_40px_rgba(0,0,0,0.12)] transition-transform duration-700 hover:scale-[1.02]"
                loading="eager"
              />
            </picture>
          </div>

          {/* Neural Node: Lightning / Fast Learning */}
          <div
            className="absolute top-8 left-[33%] md:left-[35%] z-20 w-8 h-8 rounded-full bg-[#EDE9FE] border border-[#DDD6FE] shadow-sm flex items-center justify-center text-[#7C3AED] animate-float-slow"
            title="Fast recall synapse"
          >
            <Zap className="w-4 h-4 fill-current" />
          </div>

          {/* Neural Node: Brain / Deep Memory */}
          <div
            className="absolute top-12 right-[14%] md:right-[16%] z-20 w-8 h-8 rounded-full bg-[#EAF2DE] border border-[#D5E6BE] shadow-sm flex items-center justify-center text-[#4D6D1A] animate-float-delayed"
            title="Memory core connection"
          >
            <Brain className="w-4 h-4" />
          </div>

          {/* Floating Card 1: curious (Top Center/Right) */}
          <div className="absolute top-1 right-12 sm:right-20 md:right-28 lg:right-28 z-20 bg-white/95 backdrop-blur-md p-3.5 rounded-2xl border border-black/[0.08] shadow-[0_8px_30px_rgb(0,0,0,0.08)] max-w-[170px] animate-float-slow hover:shadow-xl hover:-translate-y-0.5 transition-all duration-300">
            <div className="flex items-center justify-between gap-1 mb-1">
              <span className="font-bold text-sm text-memora-dark">curious</span>
              <button
                type="button"
                onClick={() => speakWord('curious')}
                className="text-stone-400 hover:text-memora-green transition-colors"
                title="Pronounce 'curious'"
              >
                <Volume2 className="w-3.5 h-3.5" />
              </button>
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
          <div className="absolute top-12 -left-2 sm:left-0 md:left-2 lg:-left-6 z-20 bg-white/95 backdrop-blur-md p-3.5 rounded-2xl border border-black/[0.08] shadow-[0_8px_30px_rgb(0,0,0,0.08)] max-w-[165px] animate-float-delayed hover:shadow-xl hover:-translate-y-0.5 transition-all duration-300">
            <div className="flex items-center justify-between gap-1 mb-1">
              <span className="font-bold text-sm text-memora-dark">explore</span>
              <button
                type="button"
                onClick={() => speakWord('explore')}
                className="text-stone-400 hover:text-memora-green transition-colors"
                title="Pronounce 'explore'"
              >
                <Volume2 className="w-3.5 h-3.5" />
              </button>
            </div>
            <p className="text-[11px] text-memora-text-muted italic mb-1.5">/ɪkˈsplɔːr/</p>
            <p className="text-[11px] text-stone-600 line-clamp-2 leading-tight mb-2">
              to travel in or through an area in order to learn about it.
            </p>
            <Badge variant="amber" size="sm">
              <RotateCw className="w-2.5 h-2.5" />
              Learning
            </Badge>
          </div>

          {/* Floating Card 3: focus (Middle Right) */}
          <div className="absolute top-28 -right-2 sm:right-0 md:right-2 lg:-right-6 z-20 bg-white/95 backdrop-blur-md p-3.5 rounded-2xl border border-black/[0.08] shadow-[0_8px_30px_rgb(0,0,0,0.08)] max-w-[165px] animate-float-slow hover:shadow-xl hover:-translate-y-0.5 transition-all duration-300">
            <div className="flex items-center justify-between gap-1 mb-1">
              <span className="font-bold text-sm text-memora-dark">focus</span>
              <button
                type="button"
                onClick={() => speakWord('focus')}
                className="text-stone-400 hover:text-memora-green transition-colors"
                title="Pronounce 'focus'"
              >
                <Volume2 className="w-3.5 h-3.5" />
              </button>
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
          <div className="absolute bottom-10 left-0 sm:left-2 md:left-4 lg:-left-2 z-20 bg-white/95 backdrop-blur-md p-3.5 rounded-2xl border border-black/[0.08] shadow-[0_8px_30px_rgb(0,0,0,0.08)] max-w-[165px] animate-float-delayed hover:shadow-xl hover:-translate-y-0.5 transition-all duration-300">
            <div className="flex items-center justify-between gap-1 mb-1">
              <span className="font-bold text-sm text-memora-dark">improve</span>
              <button
                type="button"
                onClick={() => speakWord('improve')}
                className="text-stone-400 hover:text-memora-green transition-colors"
                title="Pronounce 'improve'"
              >
                <Volume2 className="w-3.5 h-3.5" />
              </button>
            </div>
            <p className="text-[11px] text-memora-text-muted italic mb-1.5">/ɪmˈpruːv/</p>
            <p className="text-[11px] text-stone-600 line-clamp-2 leading-tight mb-2">
              to make something better than before.
            </p>
            <Badge variant="amber" size="sm">
              <RotateCw className="w-2.5 h-2.5" />
              Learning
            </Badge>
          </div>

          {/* Floating Card 5: achieve (Bottom Right) */}
          <div className="absolute bottom-8 right-2 sm:right-6 md:right-16 lg:right-6 z-20 bg-white/95 backdrop-blur-md p-3.5 rounded-2xl border border-black/[0.08] shadow-[0_8px_30px_rgb(0,0,0,0.08)] max-w-[165px] animate-float-slow hover:shadow-xl hover:-translate-y-0.5 transition-all duration-300">
            <div className="flex items-center justify-between gap-1 mb-1">
              <span className="font-bold text-sm text-memora-dark">achieve</span>
              <button
                type="button"
                onClick={() => speakWord('achieve')}
                className="text-stone-400 hover:text-memora-green transition-colors"
                title="Pronounce 'achieve'"
              >
                <Volume2 className="w-3.5 h-3.5" />
              </button>
            </div>
            <p className="text-[11px] text-memora-text-muted italic mb-1.5">/əˈtʃiːv/</p>
            <p className="text-[11px] text-stone-600 line-clamp-2 leading-tight mb-2">
              to successfully reach a goal.
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
