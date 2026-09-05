import React, { useEffect, useState } from 'react';
import {
  TrendingUp,
  Award,
  Flame,
  Sparkles,
  BookOpen,
  RotateCw,
  CheckCircle2,
  AlertTriangle,
} from 'lucide-react';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
} from 'recharts';
import { profileApi } from '../api/profileApi';
import { vocabularyApi } from '../api/vocabularyApi';
import { LearnerStatsResponse } from '../types/gamification';
import { UserWordProgressResponse } from '../types/vocabulary';
import { AppShell } from '../components/layout/AppShell';
import { Card } from '../components/ui/Card';
import { Badge } from '../components/ui/Badge';
import { LoadingSpinner } from '../components/ui/LoadingSpinner';

export const ProgressPage: React.FC = () => {
  const [stats, setStats] = useState<LearnerStatsResponse | null>(null);
  const [progressList, setProgressList] = useState<UserWordProgressResponse[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  useEffect(() => {
    const fetchData = async () => {
      setIsLoading(true);
      try {
        const [statsRes, progRes] = await Promise.allSettled([
          profileApi.getStats(),
          vocabularyApi.getUserProgress(),
        ]);

        if (statsRes.status === 'fulfilled' && statsRes.value.success) {
          setStats(statsRes.value.data);
        }
        if (progRes.status === 'fulfilled' && progRes.value.success) {
          setProgressList(progRes.value.data);
        }
      } finally {
        setIsLoading(false);
      }
    };

    fetchData();
  }, []);

  if (isLoading) {
    return (
      <AppShell title="Learning Analytics" subtitle="Loading your memory data...">
        <div className="py-20 flex justify-center">
          <LoadingSpinner size="lg" label="Computing retention and mastery analytics..." />
        </div>
      </AppShell>
    );
  }

  // Group mastery distribution into 5 buckets
  const masteryBuckets = [
    { range: '0-20%', count: 0 },
    { range: '21-40%', count: 0 },
    { range: '41-60%', count: 0 },
    { range: '61-80%', count: 0 },
    { range: '81-100%', count: 0 },
  ];

  progressList.forEach((w) => {
    const score = w.masteryScore;
    if (score <= 20) masteryBuckets[0].count++;
    else if (score <= 40) masteryBuckets[1].count++;
    else if (score <= 60) masteryBuckets[2].count++;
    else if (score <= 80) masteryBuckets[3].count++;
    else masteryBuckets[4].count++;
  });

  const pieData = [
    { name: 'Mastered', value: stats?.wordsMastered ?? 0, color: '#6A8D2F' },
    {
      name: 'Learning',
      value: Math.max(0, (stats?.wordsLearned ?? 0) - (stats?.wordsMastered ?? 0)),
      color: '#F59E0B',
    },
    { name: 'Review Due', value: stats?.wordsReviewDue ?? 0, color: '#EF4444' },
  ];

  return (
    <AppShell
      title="Learning Analytics & Mastery"
      subtitle="Data-driven insights into your retention rate and memory distribution"
    >
      <div className="max-w-5xl mx-auto space-y-8">
        {/* Top 4 Stat Cards */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
          <Card variant="default" className="p-5 hover-lift">
            <span className="text-xs font-bold text-memora-text-muted uppercase tracking-wider block mb-1">
              Total XP Earned
            </span>
            <span className="text-2xl font-black text-memora-dark">
              {(stats?.totalXp ?? 0).toLocaleString()}
            </span>
          </Card>

          <Card variant="default" className="p-5">
            <span className="text-xs font-bold text-memora-text-muted uppercase tracking-wider block mb-1">
              Active Streak
            </span>
            <span className="text-2xl font-black text-orange-600">
              {stats?.currentStreak ?? 0} days
            </span>
          </Card>

          <Card variant="default" className="p-5">
            <span className="text-xs font-bold text-memora-text-muted uppercase tracking-wider block mb-1">
              Words Mastered
            </span>
            <span className="text-2xl font-black text-memora-green">
              {stats?.wordsMastered ?? 0}
            </span>
          </Card>

          <Card variant="default" className="p-5">
            <span className="text-xs font-bold text-memora-text-muted uppercase tracking-wider block mb-1">
              Overall Accuracy
            </span>
            <span className="text-2xl font-black text-memora-dark">
              {Math.round(stats?.overallAccuracy ?? 0)}%
            </span>
          </Card>
        </div>

        {/* Charts Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
          {/* Mastery Score Distribution Chart (7 cols) */}
          <Card variant="default" className="lg:col-span-7 p-6 md:p-8 space-y-4 shadow-card">
            <div>
              <h3 className="text-lg font-bold text-memora-dark">Mastery Score Distribution</h3>
              <p className="text-xs text-memora-text-muted">
                Number of vocabulary words categorized by current retention mastery
              </p>
            </div>

            <div className="h-64 w-full pt-4">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={masteryBuckets} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                  <XAxis dataKey="range" tick={{ fontSize: 11, fill: '#737A71' }} />
                  <YAxis tick={{ fontSize: 11, fill: '#737A71' }} allowDecimals={false} />
                  <Tooltip
                    contentStyle={{
                      backgroundColor: '#FFFFFF',
                      borderRadius: '16px',
                      border: '1px solid rgba(0,0,0,0.08)',
                      boxShadow: '0 4px 20px rgba(0,0,0,0.06)',
                      fontSize: '12px',
                    }}
                  />
                  <Bar dataKey="count" fill="#6A8D2F" radius={[8, 8, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </Card>

          {/* Retention Breakdown Pie (5 cols) */}
          <Card variant="default" className="lg:col-span-5 p-6 md:p-8 space-y-4 shadow-card flex flex-col justify-between">
            <div>
              <h3 className="text-lg font-bold text-memora-dark">Retention Status</h3>
              <p className="text-xs text-memora-text-muted">
                Proportion of words mastered, currently learning, and due for review
              </p>
            </div>

            <div className="h-56 w-full flex items-center justify-center">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={pieData}
                    cx="50%"
                    cy="50%"
                    innerRadius={55}
                    outerRadius={75}
                    paddingAngle={5}
                    dataKey="value"
                  >
                    {pieData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Pie>
                  <Tooltip
                    contentStyle={{
                      backgroundColor: '#FFFFFF',
                      borderRadius: '16px',
                      border: '1px solid rgba(0,0,0,0.08)',
                      fontSize: '12px',
                    }}
                  />
                </PieChart>
              </ResponsiveContainer>
            </div>

            <div className="flex items-center justify-around text-xs font-semibold pt-2 border-t border-black/[0.06]">
              {pieData.map((item) => (
                <div key={item.name} className="flex items-center gap-1.5">
                  <div className="w-3 h-3 rounded-full" style={{ backgroundColor: item.color }} />
                  <span>
                    {item.name}: {item.value}
                  </span>
                </div>
              ))}
            </div>
          </Card>
        </div>

        {/* Vocabulary Progress Table */}
        <Card variant="default" className="p-6 md:p-8 shadow-card space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="text-lg font-bold text-memora-dark">Tracked Vocabulary Words</h3>
              <p className="text-xs text-memora-text-muted">
                Detailed retention metrics recorded by the Memory Engine
              </p>
            </div>
            <span className="text-xs font-bold text-memora-green bg-memora-green-light px-3 py-1 rounded-full">
              {progressList.length} Tracked Words
            </span>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="border-b border-black/[0.08] text-memora-text-muted uppercase font-bold text-[10px]">
                <tr>
                  <th className="py-3 px-3">Word</th>
                  <th className="py-3 px-3">Mastery</th>
                  <th className="py-3 px-3">Forgetting Risk</th>
                  <th className="py-3 px-3">Attempts</th>
                  <th className="py-3 px-3">Accuracy</th>
                  <th className="py-3 px-3">Next Review</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-black/[0.04]">
                {progressList.slice(0, 10).map((w) => (
                  <tr key={w.vocabularyWordId} className="hover:bg-stone-50/70 transition-colors">
                    <td className="py-3 px-3 font-bold text-memora-dark">{w.word}</td>
                    <td className="py-3 px-3 font-semibold text-stone-700">
                      {Math.round(w.masteryScore)}%
                    </td>
                    <td className="py-3 px-3">
                      <Badge
                        variant={
                          w.forgettingRisk === 'HIGH' || w.forgettingRisk === 'CRITICAL'
                            ? 'red'
                            : w.forgettingRisk === 'MEDIUM'
                            ? 'amber'
                            : 'green'
                        }
                        size="sm"
                      >
                        {w.forgettingRisk}
                      </Badge>
                    </td>
                    <td className="py-3 px-3 text-memora-text-muted">
                      {w.correctAttempts}/{w.totalAttempts}
                    </td>
                    <td className="py-3 px-3 font-semibold text-stone-700">
                      {Math.round(w.accuracy)}%
                    </td>
                    <td className="py-3 px-3 text-memora-text-muted">
                      {w.nextReviewAt ? new Date(w.nextReviewAt).toLocaleDateString() : 'Immediate'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Card>
      </div>
    </AppShell>
  );
};

export default ProgressPage;
