import React from 'react';
import { Users, BookOpen, Heart, Target } from 'lucide-react';

export const StatisticsSection: React.FC = () => {
  const stats = [
    {
      value: '12,500+',
      label: 'Active Learners',
      icon: <Users className="w-6 h-6 text-stone-700" />,
    },
    {
      value: '420K+',
      label: 'Words Learned',
      icon: <BookOpen className="w-6 h-6 text-stone-700" />,
    },
    {
      value: '98%',
      label: 'Learner Satisfaction',
      icon: <Heart className="w-6 h-6 text-stone-700" />,
    },
    {
      value: '85%',
      label: 'Words Retention Rate',
      icon: <Target className="w-6 h-6 text-stone-700" />,
    },
  ];

  return (
    <section className="py-12 bg-[#FBFBF9]">
      <div className="max-w-7xl mx-auto px-6">
        <div className="bg-white rounded-3xl border border-black/[0.06] shadow-sm p-6 sm:p-8">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-6 md:gap-8 divide-y md:divide-y-0 md:divide-x divide-black/[0.06]">
            {stats.map((stat, idx) => (
              <div
                key={stat.label}
                className={`flex items-center gap-4 ${idx > 0 ? 'pt-4 md:pt-0 md:pl-6 lg:pl-8' : ''}`}
              >
                <div className="w-12 h-12 rounded-2xl bg-[#F8F8F5] border border-black/[0.04] flex items-center justify-center shrink-0">
                  {stat.icon}
                </div>
                <div>
                  <span className="text-2xl sm:text-3xl font-extrabold text-memora-dark tracking-tight block">
                    {stat.value}
                  </span>
                  <span className="text-xs sm:text-sm font-medium text-memora-text-muted">
                    {stat.label}
                  </span>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
};

export default StatisticsSection;
