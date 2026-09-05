import React from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowRight, Sparkles } from 'lucide-react';
import { Button } from '../ui/Button';

export const FinalCtaSection: React.FC = () => {
  const navigate = useNavigate();

  return (
    <section className="py-20 bg-gradient-to-b from-[#FBFBF9] to-[#F3F7ED]">
      <div className="max-w-4xl mx-auto px-6 text-center space-y-6">
        <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-[#EAF2DE] border border-[#D5E6BE] text-[#4D6D1A] text-xs font-bold">
          <Sparkles className="w-3.5 h-3.5 fill-current" />
          <span>Personalized Adaptive Learning</span>
        </div>

        <h2 className="text-4xl sm:text-5xl font-extrabold text-memora-dark tracking-tight leading-tight">
          Build a vocabulary that stays with you.
        </h2>

        <p className="text-base sm:text-lg text-memora-text-muted max-w-xl mx-auto font-normal leading-relaxed">
          Join thousands of learners mastering words that matter through memory-based spaced repetition
          and adaptive daily paths.
        </p>

        <div className="pt-4 flex items-center justify-center gap-4 flex-wrap">
          <Button
            variant="primary"
            size="lg"
            onClick={() => navigate('/register')}
            rightIcon={<ArrowRight className="w-5 h-5" />}
            className="shadow-lg"
          >
            Start Learning
          </Button>
          <Button
            variant="outline"
            size="lg"
            onClick={() => {
              const el = document.getElementById('features');
              if (el) el.scrollIntoView({ behavior: 'smooth' });
              else navigate('/leaderboard');
            }}
            className="bg-white"
          >
            Explore Features
          </Button>
        </div>
      </div>
    </section>
  );
};

export default FinalCtaSection;
