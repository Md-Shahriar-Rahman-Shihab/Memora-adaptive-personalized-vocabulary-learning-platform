import React, { useState, useEffect, useRef } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Compass, ArrowRight, Clock, CheckCircle2 } from 'lucide-react';
import { assessmentApi } from '../api/assessmentApi';
import { AssessmentQuestionResponse, AssessmentDetailResponse } from '../types/assessment';
import { AppShell } from '../components/layout/AppShell';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { ProgressBar } from '../components/ui/ProgressBar';
import { LoadingSpinner } from '../components/ui/LoadingSpinner';
import { ErrorState } from '../components/ui/ErrorState';

export const AssessmentPage: React.FC = () => {
  const navigate = useNavigate();
  const { assessmentId: routeAssessmentId } = useParams<{ assessmentId?: string }>();

  const [assessmentId, setAssessmentId] = useState<number | null>(null);
  const [totalQuestions, setTotalQuestions] = useState<number>(20);
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState<number>(0);
  const [currentQuestion, setCurrentQuestion] = useState<AssessmentQuestionResponse | null>(null);
  const [selectedOption, setSelectedOption] = useState<string>('');
  const [textInput, setTextInput] = useState<string>('');

  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [started, setStarted] = useState<boolean>(false);

  useEffect(() => {
    if (routeAssessmentId) {
      const loadExisting = async () => {
        setIsLoading(true);
        setError(null);
        try {
          const res = await assessmentApi.getAssessment(Number(routeAssessmentId));
          if (res.success && res.data) {
            const data = res.data;
            if (data.status === 'COMPLETED') {
              navigate(`/assessment/result?assessmentId=${data.assessmentId}`);
              return;
            }
            setAssessmentId(data.assessmentId);
            setTotalQuestions(data.totalQuestions || 20);
            const nextIdx = data.answeredQuestions;
            if (data.questions && data.questions[nextIdx]) {
              setCurrentQuestion(data.questions[nextIdx]);
              setCurrentQuestionIndex(nextIdx + 1);
              setStarted(true);
            }
          }
        } catch (err: any) {
          setError(err?.response?.data?.message || 'Could not load existing assessment session.');
        } finally {
          setIsLoading(false);
        }
      };
      loadExisting();
    }
  }, [routeAssessmentId]);

  // Measure response time in ms
  const questionStartTimeRef = useRef<number>(Date.now());

  // Reset timer on new question
  useEffect(() => {
    if (currentQuestion) {
      questionStartTimeRef.current = Date.now();
      setSelectedOption('');
      setTextInput('');
    }
  }, [currentQuestion]);

  // Start Assessment
  const handleStart = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const res = await assessmentApi.startAssessment();
      if (res.success && res.data) {
        setAssessmentId(res.data.assessmentId);
        setTotalQuestions(res.data.totalQuestions || 20);
        setCurrentQuestion(res.data.firstQuestion);
        setCurrentQuestionIndex(1);
        setStarted(true);
      }
    } catch (err: any) {
      setError(
        err?.response?.data?.message || 'Failed to start diagnostic assessment. Please try again.'
      );
    } finally {
      setIsLoading(false);
    }
  };

  // Submit Answer
  const handleSubmitAnswer = async () => {
    if (!assessmentId || !currentQuestion) return;

    const answerValue =
      currentQuestion.questionType === 'MULTIPLE_CHOICE' ? selectedOption : textInput.trim();

    if (!answerValue) return;

    const latencyMs = Math.max(100, Date.now() - questionStartTimeRef.current);

    setIsSubmitting(true);
    try {
      const res = await assessmentApi.submitAnswer(assessmentId, currentQuestion.questionId, {
        answer: answerValue,
        responseTimeMs: latencyMs,
      });

      if (res.success && res.data) {
        if (res.data.isComplete || !res.data.nextQuestion) {
          // Finalize assessment
          await assessmentApi.completeAssessment(assessmentId);
          navigate(`/assessment/result?assessmentId=${assessmentId}`);
        } else {
          // Next question
          setCurrentQuestion(res.data.nextQuestion);
          setCurrentQuestionIndex(res.data.answeredQuestions + 1);
        }
      }
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Error submitting answer. Please try again.');
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!started) {
    return (
      <AppShell
        title="CEFR Diagnostic Placement"
        subtitle="Establish your initial baseline proficiency"
      >
        <div className="max-w-2xl mx-auto py-10">
          <Card variant="default" className="p-8 sm:p-12 text-center space-y-6">
            <div className="w-16 h-16 rounded-3xl bg-memora-green-light text-memora-green flex items-center justify-center mx-auto shadow-sm">
              <Compass className="w-8 h-8" />
            </div>

            <h2 className="text-3xl font-extrabold text-memora-dark tracking-tight">
              Test Your Real Vocabulary Level
            </h2>

            <p className="text-sm sm:text-base text-memora-text-muted leading-relaxed max-w-lg mx-auto">
              Memora's placement engine presents 20 questions spanning CEFR levels A1 through C1.
              Our algorithm evaluates your accuracy, response latency, and difficulty consistency
              to generate your personalized learning path.
            </p>

            <div className="grid grid-cols-3 gap-4 py-4 max-w-md mx-auto text-center border-y border-black/[0.06]">
              <div>
                <span className="text-xl font-bold text-memora-dark block">20</span>
                <span className="text-xs text-memora-text-muted">Questions</span>
              </div>
              <div>
                <span className="text-xl font-bold text-memora-dark block">~5 min</span>
                <span className="text-xs text-memora-text-muted">Duration</span>
              </div>
              <div>
                <span className="text-xl font-bold text-memora-dark block">A1 - C1</span>
                <span className="text-xs text-memora-text-muted">Levels</span>
              </div>
            </div>

            {error && <ErrorState message={error} />}

            <div className="pt-2">
              <Button
                variant="primary"
                size="lg"
                onClick={handleStart}
                isLoading={isLoading}
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

  return (
    <AppShell
      title="Diagnostic Placement Test"
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

        {error && <ErrorState message={error} />}

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
