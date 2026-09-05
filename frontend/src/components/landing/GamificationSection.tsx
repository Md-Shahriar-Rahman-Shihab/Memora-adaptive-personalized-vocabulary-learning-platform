import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Flame, Award, ArrowRight } from 'lucide-react';
import { Card } from '../ui/Card';

export const GamificationSection: React.FC = () => {
  const navigate = useNavigate();

  return (
    <section id="features" className="py-20 bg-[#FBFBF9]">
      <div className="max-w-7xl mx-auto px-6">
        {/* Large Dark Card Shell */}
        <div className="bg-[#171F17] rounded-3xl p-8 sm:p-12 text-white shadow-2xl border border-white/[0.08] relative overflow-hidden">
          {/* Subtle background gradient glow */}
          <div className="absolute top-0 right-0 w-96 h-96 bg-emerald-900/30 rounded-full filter blur-3xl pointer-events-none" />

          <div className="grid grid-cols-1 lg:grid-cols-12 gap-10 items-center relative z-10">
            {/* Left Headline & Intro */}
            <div className="lg:col-span-5 space-y-5">
              <h2 className="text-3xl sm:text-4xl font-extrabold tracking-tight text-white leading-tight">
                Gamified. Engaging. Effective.
              </h2>
              <p className="text-stone-300 text-sm sm:text-base leading-relaxed">
                Stay motivated with streaks, achievements, leaderboards and challenges designed to
                reinforce everyday vocabulary habits.
              </p>

              <div className="pt-2">
                <button
                  onClick={() => navigate('/leaderboard')}
                  className="inline-flex items-center gap-2 text-sm font-bold text-white hover:text-memora-green-glow transition group"
                >
                  <span>Explore Features</span>
                  <div className="w-7 h-7 rounded-full bg-white/10 flex items-center justify-center group-hover:bg-memora-green transition">
                    <ArrowRight className="w-4 h-4" />
                  </div>
                </button>
              </div>
            </div>

            {/* Right: Gamification Showcase Widgets (Streak, Gem Badge, Leaderboard) */}
            <div className="lg:col-span-7 grid grid-cols-1 sm:grid-cols-3 gap-4">
              {/* Widget 1: Day Streak */}
              <div className="bg-[#212A21] border border-white/[0.08] rounded-2xl p-5 flex flex-col justify-between">
                <div>
                  <div className="flex items-center gap-1.5 text-orange-400 mb-2">
                    <Flame className="w-5 h-5 fill-current" />
                    <span className="text-2xl font-black text-white">12</span>
                  </div>
                  <span className="text-xs text-stone-300 font-medium block">Day Streak</span>
                </div>

                {/* Day of Week Dots */}
                <div className="flex items-center justify-between mt-6 pt-3 border-t border-white/[0.08]">
                  {['M', 'T', 'W', 'T', 'F', 'S', 'S'].map((day, idx) => (
                    <div
                      key={idx}
                      className={`w-5 h-5 rounded-full flex items-center justify-center text-[9px] font-bold ${
                        idx < 5
                          ? 'bg-memora-green text-white shadow-sm'
                          : 'bg-white/10 text-stone-400'
                      }`}
                    >
                      {day}
                    </div>
                  ))}
                </div>
              </div>

              {/* Widget 2: Vocabulary Master 3D Gem Badge */}
              <div className="bg-[#212A21] border border-white/[0.08] rounded-2xl p-5 flex flex-col items-center justify-center text-center">
                {/* 3D Purple Gem representation */}
                <div className="w-16 h-16 rounded-2xl bg-gradient-to-tr from-purple-700 via-indigo-600 to-purple-400 shadow-glow flex items-center justify-center mb-3 transform rotate-6 hover:rotate-0 transition duration-300">
                  <Award className="w-8 h-8 text-white" />
                </div>
                <h4 className="text-sm font-bold text-white mb-0.5">Vocabulary Master</h4>
                <p className="text-[11px] text-stone-400">Top 1% of learners</p>
              </div>

              {/* Widget 3: Weekly Leaderboard Preview */}
              <div className="bg-[#212A21] border border-white/[0.08] rounded-2xl p-4 flex flex-col justify-between">
                <div className="flex items-center justify-between mb-3">
                  <span className="text-xs font-bold text-white">Weekly Leaderboard</span>
                  <span
                    onClick={() => navigate('/leaderboard')}
                    className="text-[10px] text-stone-400 hover:text-white cursor-pointer"
                  >
                    View all
                  </span>
                </div>

                <div className="space-y-2">
                  {/* #1 Ariana */}
                  <div className="flex items-center justify-between text-xs py-1 px-2 rounded-xl bg-white/[0.04]">
                    <div className="flex items-center gap-2">
                      <span className="text-[10px] font-bold text-amber-400 w-3">1</span>
                      <div className="w-5 h-5 rounded-full bg-purple-500/30 text-purple-200 flex items-center justify-center text-[10px] font-bold">
                        A
                      </div>
                      <span className="text-white font-medium">Ariana</span>
                    </div>
                    <span className="text-stone-300 font-semibold text-[11px]">2,540 XP</span>
                  </div>

                  {/* #2 Shihab (Highlighted in green as in reference image) */}
                  <div className="flex items-center justify-between text-xs py-1.5 px-2 rounded-xl bg-memora-green text-white shadow-sm font-semibold">
                    <div className="flex items-center gap-2">
                      <span className="text-[10px] font-bold w-3">2</span>
                      <div className="w-5 h-5 rounded-full bg-white/30 text-white flex items-center justify-center text-[10px] font-bold">
                        S
                      </div>
                      <span>Shihab</span>
                    </div>
                    <span className="text-[11px]">2,150 XP</span>
                  </div>

                  {/* #3 Rafi */}
                  <div className="flex items-center justify-between text-xs py-1 px-2 rounded-xl bg-white/[0.04]">
                    <div className="flex items-center gap-2">
                      <span className="text-[10px] font-bold text-orange-400 w-3">3</span>
                      <div className="w-5 h-5 rounded-full bg-orange-500/30 text-orange-200 flex items-center justify-center text-[10px] font-bold">
                        R
                      </div>
                      <span className="text-white font-medium">Rafi</span>
                    </div>
                    <span className="text-stone-300 font-semibold text-[11px]">1,890 XP</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};

export default GamificationSection;
