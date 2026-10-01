import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import ProtectedRoute from './ProtectedRoute';

// Pages
import LandingPage from '../pages/LandingPage';
import LoginPage from '../pages/LoginPage';
import RegisterPage from '../pages/RegisterPage';
import DashboardPage from '../pages/DashboardPage';
import AssessmentPage from '../pages/AssessmentPage';
import AssessmentResultPage from '../pages/AssessmentResultPage';
import LearningPathPage from '../pages/LearningPathPage';
import ReviewPage from '../pages/ReviewPage';
import QuizPage from '../pages/QuizPage';
import ProgressPage from '../pages/ProgressPage';
import AchievementsPage from '../pages/AchievementsPage';
import LeaderboardPage from '../pages/LeaderboardPage';
import ProfilePage from '../pages/ProfilePage';
import DictionaryPage from '../pages/DictionaryPage';
import OnboardingPage from '../pages/OnboardingPage';
import WordDetailPage from '../pages/WordDetailPage';
import PartnersPage from '../pages/PartnersPage';

import { Outlet } from 'react-router-dom';
import { useOnboarding } from '../context/OnboardingContext';
import { LoadingSpinner } from '../components/ui/LoadingSpinner';

// Component redirecting authenticated users away from /login and /register
const PublicOnlyRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated, isLoading } = useAuth();
  if (isLoading) return null;
  return isAuthenticated ? <Navigate to="/dashboard" replace /> : <>{children}</>;
};

// Route guard preventing access to core learning features before assessment and learning path are active
const LearningRouteGuard: React.FC = () => {
  const { onboardingState, isLoading, isLearningUnlocked } = useOnboarding();

  if (isLoading) {
    return (
      <div className="min-h-screen bg-[#FBFBF9] flex items-center justify-center">
        <LoadingSpinner size="lg" label="Checking learning authorization..." />
      </div>
    );
  }

  if (!isLearningUnlocked) {
    if (onboardingState?.state === 'ASSESSMENT_IN_PROGRESS') {
      const dest = onboardingState.assessmentId ? `/assessment/${onboardingState.assessmentId}` : '/assessment';
      return <Navigate to={dest} replace />;
    }
    if (onboardingState?.state === 'LEARNING_PATH_REQUIRED') {
      const dest = onboardingState.assessmentId ? `/assessment/result?assessmentId=${onboardingState.assessmentId}` : '/assessment/result';
      return <Navigate to={dest} replace />;
    }
    return <Navigate to="/dashboard" replace />;
  }

  return <Outlet />;
};

export const AppRoutes: React.FC = () => {
  return (
    <BrowserRouter>
      <Routes>
        {/* Public Routes */}
        <Route path="/" element={<LandingPage />} />
        <Route
          path="/login"
          element={
            <PublicOnlyRoute>
              <LoginPage />
            </PublicOnlyRoute>
          }
        />
        <Route
          path="/register"
          element={
            <PublicOnlyRoute>
              <RegisterPage />
            </PublicOnlyRoute>
          }
        />
        {/* Public Routes with fallback */}
        <Route path="/leaderboard" element={<LeaderboardPage />} />
        <Route path="/word/:word" element={<WordDetailPage />} />
        <Route path="/words/:word" element={<WordDetailPage />} />

        {/* Protected Routes */}
        <Route element={<ProtectedRoute />}>
          <Route path="/onboarding" element={<OnboardingPage />} />
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/assessment" element={<AssessmentPage />} />
          <Route path="/assessment/:assessmentId" element={<AssessmentPage />} />
          <Route path="/assessment/result" element={<AssessmentResultPage />} />
          <Route path="/dictionary" element={<DictionaryPage />} />
          <Route path="/profile" element={<ProfilePage />} />
          <Route path="/partners" element={<PartnersPage />} />

          {/* Locked Core Learning Routes Guard */}
          <Route element={<LearningRouteGuard />}>
            <Route path="/learn-path" element={<LearningPathPage />} />
            <Route path="/learn" element={<LearningPathPage />} />
            <Route path="/learning" element={<LearningPathPage />} />
            <Route path="/review" element={<ReviewPage />} />
            <Route path="/quiz" element={<QuizPage />} />
            <Route path="/quiz/:quizId" element={<QuizPage />} />
            <Route path="/progress" element={<ProgressPage />} />
            <Route path="/achievements" element={<AchievementsPage />} />
          </Route>
        </Route>

        {/* Fallback */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
};

export default AppRoutes;
