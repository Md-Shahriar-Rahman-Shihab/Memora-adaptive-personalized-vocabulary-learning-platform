import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Compass, ArrowRight, Clock, CheckCircle2, AlertCircle } from 'lucide-react';
import { assessmentApi } from '../api/assessmentApi';
import { AssessmentQuestionResponse } from '../types/assessment';
import { AppShell } from '../components/layout/AppShell';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { ProgressBar } from '../components/ui/ProgressBar';
import { LoadingSpinner } from '../components/ui/LoadingSpinner';
import { ErrorState } from '../components/ui/ErrorState';
import { useOnboarding } from '../context/OnboardingContext';

type PageStatus = 'loading' | 'intro' | 'question' | 'error';

export const AssessmentPage: React.FC = () => {
  const navigate = useNavigate();
  const { assessmentId: routeAssessmentId } = useParams<{ assessmentId?: string }>();
  const { onboardingState, refreshOnboardingState } = useOnboarding();

  const hasActiveSession = Boolean(routeAssessmentId || (onboardingState?.state === 'ASSESSMENT_IN_PROGRESS' && onboardingState?.assessmentId));

  const [pageStatus, setPageStatus] = useState<PageStatus>(
    hasActiveSession ? 'loading' : 'intro'
  );
  const [assessmentId, setAssessmentId] = useState<number | null>(
    routeAssessmentId ? Number(routeAssessmentId) : (onboardingState?.state === 'ASSESSMENT_IN_PROGRESS' ? onboardingState?.assessmentId ?? null : null)
  );
  const [totalQuestions, setTotalQuestions] = useState<number>(10);
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState<number>(0);
  const [currentQuestion, setCurrentQuestion] = useState<AssessmentQuestionResponse | null>(null);
  const [questionsList, setQuestionsList] = useState<AssessmentQuestionResponse[]>([]);
  const [selectedOption, setSelectedOption] = useState<string>('');
  const [textInput, setTextInput] = useState<string>('');

  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [sessionError, setSessionError] = useState<string | null>(null);
  const [submitError, setSubmitError] = useState<string | null>(null);

  // Measure response latency
  const questionStartTimeRef = useRef<number>(Date.now());

  // Reset timer & selections on new question
  useEffect(() => {
    if (currentQuestion) {
      questionStartTimeRef.current = Date.now();
      setSelectedOption('');
      setTextInput('');
      setSubmitError(null);
    }
  }, [currentQuestion]);

  // Load existing session by route param
  const loadExistingSession = useCallback(async (id: number) => {
    setPageStatus('loading');
    setSessionError(null);
    try {
      const res = await assessmentApi.getAssessment(id);
      if (res.success && res.data) {
        const data = res.data;
        if (data.status === 'COMPLETED') {
          navigate(`/assessment/result?assessmentId=${data.assessmentId}`, { replace: true });
          return;
        }

        const qList = data.questions || [];
        setAssessmentId(data.assessmentId);
        setQuestionsList(qList);
        setTotalQuestions(data.totalQuestions || qList.length || 10);

        const nextIdx = data.answeredQuestions || 0;
        if (qList[nextIdx]) {
          setCurrentQuestion(qList[nextIdx]);
          setCurrentQuestionIndex(nextIdx + 1);
          setPageStatus('question');
        } else if (nextIdx >= qList.length && qList.length > 0) {
          // All questions already answered, complete session
          await assessmentApi.completeAssessment(data.assessmentId);
          await refreshOnboardingState();
          navigate(`/assessment/result?assessmentId=${data.assessmentId}`, { replace: true });
        } else {
          setSessionError('No questions found for this assessment session.');
          setPageStatus('error');
        }
      } else {
        setSessionError('Could not load existing assessment session.');
        setPageStatus('error');
      }
    } catch (err: any) {
      setSessionError(err?.response?.data?.message || 'Could not load existing assessment session.');
      setPageStatus('error');
    }
  }, [navigate, refreshOnboardingState]);

  useEffect(() => {
    if (routeAssessmentId) {
      loadExistingSession(Number(routeAssessmentId));
    } else if (onboardingState?.state === 'ASSESSMENT_IN_PROGRESS' && onboardingState?.assessmentId) {
      loadExistingSession(onboardingState.assessmentId);
    } else {
      setPageStatus('intro');
    }
  }, [routeAssessmentId, onboardingState?.state, onboardingState?.assessmentId, loadExistingSession]);

  // Start Assessment
  const handleStart = async () => {
    setPageStatus('loading');
    setSessionError(null);
    try {
      const res = await assessmentApi.startAssessment();
      if (res.success && res.data) {
        const data = res.data;
        const qList = data.questions || (data.firstQuestion ? [data.firstQuestion] : []);
        setQuestionsList(qList);
        setAssessmentId(data.assessmentId);
        setTotalQuestions(data.totalQuestions || qList.length || 10);

        const startIdx = data.answeredQuestions || 0;
        if (qList[startIdx]) {
          setCurrentQuestion(qList[startIdx]);
          setCurrentQuestionIndex(startIdx + 1);
          setPageStatus('question');
          // Refresh onboarding so state reflects ASSESSMENT_IN_PROGRESS
          refreshOnboardingState();
          // Synchronize URL with active assessment ID without reloading
          navigate(`/assessment/${data.assessmentId}`, { replace: true });
        } else {
          setSessionError('Assessment generated without questions. Please try again.');
          setPageStatus('error');
        }
      } else {
        setSessionError('Failed to start diagnostic assessment. Please try again.');
        setPageStatus('error');
      }
    } catch (err: any) {
      setSessionError(
        err?.response?.data?.message || 'Failed to start diagnostic assessment. Please try again.'
      );
      setPageStatus('error');
    }
  };

  // Submit Answer
  const handleSubmitAnswer = async () => {
    if (isSubmitting || !assessmentId || !currentQuestion) return;

    const answerValue =
      currentQuestion.questionType === 'MULTIPLE_CHOICE' ? selectedOption : textInput.trim();

    if (!answerValue) return;

    const latencyMs = Math.max(100, Date.now() - questionStartTimeRef.current);

    setIsSubmitting(true);
    setSubmitError(null);

    try {
      const res = await assessmentApi.submitAnswer(assessmentId, currentQuestion.questionId, {
        answer: answerValue,
        responseTimeMs: latencyMs,
      });

      if (res.success) {
        const nextIdx = currentQuestionIndex; // next 0-based question index
        if (nextIdx >= totalQuestions || (questionsList.length > 0 && nextIdx >= questionsList.length)) {
          // Finalize assessment when all questions are answered
          setPageStatus('loading');
          await assessmentApi.completeAssessment(assessmentId);
          await refreshOnboardingState();
          navigate(`/assessment/result?assessmentId=${assessmentId}`, { replace: true });
        } else if (questionsList[nextIdx]) {
          // Advance smoothly in-memory without extra round-trip
          setCurrentQuestion(questionsList[nextIdx]);
          setCurrentQuestionIndex(nextIdx + 1);
        } else {
          // Fallback if list was somehow incomplete
          setPageStatus('loading');
          await loadExistingSession(assessmentId);
        }
      } else {
        setSubmitError(res.message || 'Error submitting answer. Please try again.');
      }
    } catch (err: any) {
      setSubmitError(err?.response?.data?.message || 'Error submitting answer. Please try again.');
    } finally {
      setIsSubmitting(false);
    }
  };

  // Loading state
  if (pageStatus === 'loading') {
    return (
      <AppShell
        title="CEFR Diagnostic Placement"
        subtitle="Loading your assessment..."
      >
        <div className="max-w-2xl mx-auto py-20 flex flex-col items-center justify-center">
          <LoadingSpinner size="lg" label="Loading assessment session..." />
        </div>
      </AppShell>
    );
  }

  // Session error state — strictly renders error UI with retry, NEVER stale question cards
  if (pageStatus === 'error') {
    return (
      <AppShell
        title="Initial Diagnostic Calibration"
        subtitle="Session Error"
      >
        <div className="max-w-2xl mx-auto py-12">
          <ErrorState
            title="Session Error"
            message={sessionError || 'Could not load existing assessment session.'}
            onRetry={() => {
              if (routeAssessmentId) {
                loadExistingSession(Number(routeAssessmentId));
              } else {
                handleStart();
              }
            }}
          />
        </div>
      </AppShell>
    );
  }

  // Intro state
  if (pageStatus === 'intro') {
    return (
      <AppShell
        title="Initial Diagnostic Calibration"
        subtitle="Calibrate your starting level and estimate your learning frontier"
      >
        <div className="max-w-2xl mx-auto py-10">
          <Card variant="default" className="p-8 sm:p-12 text-center space-y-6">
            <div className="w-16 h-16 rounded-3xl bg-memora-green-light text-memora-green flex items-center justify-center mx-auto shadow-sm">
              <Compass className="w-8 h-8" />
            </div>

            <h2 className="text-3xl font-extrabold text-memora-dark tracking-tight">
              Calibrate Your Starting Level
            </h2>

            <p className="text-sm sm:text-base text-memora-text-muted leading-relaxed max-w-lg mx-auto">
              This compact 10-question placement assessment estimates your starting learning frontier
              across CEFR levels A1 through C1 so we can calibrate your memory engine and build your
              personalized learning path.
            </p>

            <div className="grid grid-cols-3 gap-4 py-4 max-w-md mx-auto text-center border-y border-black/[0.06]">
              <div>
                <span className="text-xl font-bold text-memora-dark block">10</span>
                <span className="text-xs text-memora-text-muted">Questions</span>
              </div>
              <div>
                <span className="text-xl font-bold text-memora-dark block">~2 min</span>
                <span className="text-xs text-memora-text-muted">Duration</span>
              </div>
              <div>
                <span className="text-xl font-bold text-memora-dark block">A1 - C1</span>
                <span className="text-xs text-memora-text-muted">Levels</span>
              </div>
            </div>

            <div className="pt-2">
              <Button
                variant="primary"
                size="lg"
                onClick={handleStart}
                rightIcon={<ArrowRight className="w-5 h-5" />}
                className="shadow-md"
              >
                Start Assessment
              </Button>
            </div>
          </Card>
        </div>
      </AppShell>
    );
  }

  // Active question state
  return (
    <AppShell
      title="Initial Diagnostic Calibration"
      subtitle={`Question ${currentQuestionIndex} of ${totalQuestions}`}
    >
      <div className="max-w-2xl mx-auto py-6 space-y-6">
        {/* Progress Tracker */}
        <div className="space-y-2">
          <div className="flex justify-between items-center text-xs font-bold text-memora-text-muted">
            <span>
              Question {currentQuestionIndex} of {totalQuestions}
            </span>
            <span className="text-memora-green">
              {Math.round(((currentQuestionIndex - 1) / totalQuestions) * 100)}% Completed
            </span>
          </div>
          <ProgressBar
            value={currentQuestionIndex - 1}
            max={totalQuestions}
            size="sm"
            barClassName="bg-memora-green"
          />
        </div>

        {/* Question Card */}
        {currentQuestion && (
          <Card variant="default" className="p-8 sm:p-10 space-y-6 shadow-card">
            <div className="flex items-center justify-between">
              <Badge variant="neutral" size="sm">
                {currentQuestion.questionType.replace('_', ' ')}
              </Badge>
              <span className="text-xs text-stone-400 font-semibold flex items-center gap-1">
                <Clock className="w-3.5 h-3.5" />
                Adaptive assessment
              </span>
            </div>

            {/* Question Text */}
            <div className="space-y-2">
              <h3 className="text-xl sm:text-2xl font-bold text-memora-dark leading-snug">
                {currentQuestion.questionText}
              </h3>
              {currentQuestion.sentence && (
                <p className="text-base text-stone-700 bg-stone-50 p-4 rounded-2xl border border-black/[0.04] italic">
                  "{currentQuestion.sentence}"
                </p>
              )}
            </div>

            {/* Answer Options / Inputs */}
            <div className="space-y-3 pt-2">
              {currentQuestion.questionType === 'MULTIPLE_CHOICE' && currentQuestion.options ? (
                currentQuestion.options.map((option, idx) => {
                  const isSelected = selectedOption === option;
                  return (
                    <button
                      key={idx}
                      type="button"
                      onClick={() => setSelectedOption(option)}
                      className={`w-full text-left p-4 rounded-2xl border text-sm font-semibold transition-all duration-150 flex items-center justify-between ${
                        isSelected
                          ? 'border-memora-green bg-memora-green-light text-memora-green-dark shadow-sm'
                          : 'border-black/[0.08] bg-[#FBFBF9] text-memora-dark hover:bg-stone-50'
                      }`}
                    >
                      <span>{option}</span>
                      {isSelected && (
                        <CheckCircle2 className="w-4 h-4 text-memora-green shrink-0" />
                      )}
                    </button>
                  );
                })
              ) : (
                <div>
                  <label className="block text-xs font-bold text-memora-dark uppercase tracking-wider mb-2">
                    Your answer
                  </label>
                  <input
                    type="text"
                    value={textInput}
                    onChange={(e) => setTextInput(e.target.value)}
                    onKeyDown={(e) => {
                      if (e.key === 'Enter') handleSubmitAnswer();
                    }}
                    placeholder="Type your answer here..."
                    className="w-full px-4 py-3 bg-stone-50 border border-black/[0.08] rounded-2xl text-sm font-semibold text-memora-dark placeholder:text-stone-400 focus:outline-none focus:ring-2 focus:ring-memora-green focus:bg-white"
                  />
                </div>
              )}
            </div>

            {/* Transient inline submit error */}
            {submitError && (
              <div className="flex items-center gap-2 p-3 bg-red-50 border border-red-200/60 rounded-xl text-red-700 text-xs font-medium">
                <AlertCircle className="w-4 h-4 shrink-0 text-red-500" />
                <span>{submitError}</span>
              </div>
            )}

            {/* Next / Submit Button */}
            <div className="pt-4 flex justify-end">
              <Button
                variant="primary"
                size="md"
                onClick={handleSubmitAnswer}
                isLoading={isSubmitting}
                disabled={
                  currentQuestion.questionType === 'MULTIPLE_CHOICE'
                    ? !selectedOption
                    : !textInput.trim()
                }
                rightIcon={<ArrowRight className="w-4 h-4" />}
                className="px-8"
              >
                {currentQuestionIndex === totalQuestions ? 'Finish Assessment' : 'Next Question'}
              </Button>
            </div>
          </Card>
        )}
      </div>
    </AppShell>
  );
};

export default AssessmentPage;
