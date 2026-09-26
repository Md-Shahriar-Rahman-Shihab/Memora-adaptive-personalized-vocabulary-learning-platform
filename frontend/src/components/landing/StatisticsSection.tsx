import React, { useState, useEffect, useRef } from 'react';
import { Users, BookOpen, Heart, Target } from 'lucide-react';

interface CounterProps {
  target: number;
  suffix?: string;
  isFormattedWithCommas?: boolean;
  isVisible: boolean;
  finalDisplay: string;
}

const Counter: React.FC<CounterProps> = ({
  target,
  suffix = '',
  isFormattedWithCommas = false,
  isVisible,
  finalDisplay,
}) => {
  const [count, setCount] = useState(0);
  const [completed, setCompleted] = useState(false);

  useEffect(() => {
    if (!isVisible) return;

    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
      setCount(target);
      setCompleted(true);
      return;
    }

    let animationFrameId: number;
    const duration = 1800; // 1.8 seconds smooth count
    const startTime = performance.now();

    // Cubic ease-out: fast initial acceleration, smooth deceleration to final value
    const easeOutCubic = (x: number): number => 1 - Math.pow(1 - x, 3);

    const updateCounter = (currentTime: number) => {
      const elapsed = currentTime - startTime;
      const progress = Math.min(elapsed / duration, 1);
      const easedProgress = easeOutCubic(progress);
      const currentVal = Math.round(easedProgress * target);

      setCount(currentVal);

      if (progress < 1) {
        animationFrameId = requestAnimationFrame(updateCounter);
      } else {
        setCount(target);
        setCompleted(true);
      }
    };

    animationFrameId = requestAnimationFrame(updateCounter);

    return () => {
      if (animationFrameId) {
        cancelAnimationFrame(animationFrameId);
      }
    };
  }, [isVisible, target]);

  if (completed) {
    return <>{finalDisplay}</>;
  }

  const formattedNumber = isFormattedWithCommas
    ? count.toLocaleString('en-US')
    : count.toString();

  return (
    <>
      {formattedNumber}
      {suffix}
    </>
  );
};

export const StatisticsSection: React.FC = () => {
  const sectionRef = useRef<HTMLElement>(null);
  const [isVisible, setIsVisible] = useState(false);

  useEffect(() => {
    const el = sectionRef.current;
    if (!el) return;

    // Trigger counting once when 20% of section enters viewport
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          setIsVisible(true);
          observer.disconnect();
        }
      },
      {
        threshold: 0.2,
      }
    );

    observer.observe(el);

    return () => observer.disconnect();
  }, []);

  const stats = [
    {
      target: 12500,
      suffix: '+',
      isFormattedWithCommas: true,
      finalDisplay: '12,500+',
      label: 'Active Learners',
      icon: <Users className="w-6 h-6 text-stone-700" />,
    },
    {
      target: 420,
      suffix: 'K+',
      isFormattedWithCommas: false,
      finalDisplay: '420K+',
      label: 'Words Learned',
      icon: <BookOpen className="w-6 h-6 text-stone-700" />,
    },
    {
      target: 98,
      suffix: '%',
      isFormattedWithCommas: false,
      finalDisplay: '98%',
      label: 'Learner Satisfaction',
      icon: <Heart className="w-6 h-6 text-stone-700" />,
    },
    {
      target: 85,
      suffix: '%',
      isFormattedWithCommas: false,
      finalDisplay: '85%',
      label: 'Words Retention Rate',
      icon: <Target className="w-6 h-6 text-stone-700" />,
    },
  ];

  return (
    <section ref={sectionRef} className="py-12 bg-[#FBFBF9]">
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
                    <Counter
                      target={stat.target}
                      suffix={stat.suffix}
                      isFormattedWithCommas={stat.isFormattedWithCommas}
                      isVisible={isVisible}
                      finalDisplay={stat.finalDisplay}
                    />
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
