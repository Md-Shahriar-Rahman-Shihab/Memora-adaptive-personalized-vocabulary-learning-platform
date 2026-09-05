import React, { useEffect, useState, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  HelpCircle,
  CheckCircle2,
  XCircle,
  Sparkles,
  ArrowRight,
  Flame,
  Award,
  RefreshCw,
} from 'lucide-react';
import { quizApi } from '../api/quizApi';
import {
  QuizResponse,
  QuestionResponse,
  AnswerResponse,
  QuizResultResponse,
} from '../types/quiz';
import { useToast } from '../context/ToastContext';
import { useAuth } from '../context/AuthContext';
import { AppShell } from '../components/layout/AppShell';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { ProgressBar } from '../components/ui/ProgressBar';
import { Modal } from '../components/ui/Modal';
import { LoadingSpinner } from '../components/ui/LoadingSpinner';
import { ErrorState } from '../components/ui/ErrorState';

export const QuizPage: React.FC = () => {
  const { quizId } = useParams<{ quizId: string }>();
  const navigate = useNavigate();
  const { addToast } = useToast();
  const { refreshUser } = useAuth();

  const [quiz, setQuiz] = useState<QuizResponse | null>(null);
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState<number>(0);
  const [selectedOption, setSelectedOption] = useState<string>('');
  const [textInput, setTextInput] = useState<string>('');
  const [answerFeedback, setAnswerFeedback] = useState<AnswerResponse | null>(null);

  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [quizResult, setQuizResult] = useState<QuizResultResponse | null>(null);
  const [showCelebration, setShowCelebration] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const questionStartTimeRef = useRef<number>(Date.now());

  // Load or generate quiz
  const loadQuiz = async () => {
    setIsLoading(true);
    setError(null);
    try {
      let targetQuizId = Number(quizId);
      let quizData: QuizResponse | null = null;

      if (!targetQuizId || isNaN(targetQuizId)) {
        // Generate new quiz
        const genRes = await quizApi.generateQuiz({ questionCount: 5 });
        if (genRes.success && genRes.data) {
          quizData = genRes.data;
          targetQuizId = genRes.data.id;
        }
      } else {
        try {
          const res = await quizApi.startQuiz(targetQuizId);
          if (res.success && res.data) {
            quizData = res.data;
          }
        } catch {
          // If quiz doesn't exist, generate a new one
          const genRes = await quizApi.generateQuiz({ questionCount: 5 });
          if (genRes.success && genRes.data) {
            quizData = genRes.data;
          }
        }
      }

      setQuiz(quizData);
      setCurrentQuestionIndex(0);
      setAnswerFeedback(null);
      setQuizResult(null);
      setShowCelebration(false);
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to start quiz session.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadQuiz();
  }, [quizId]);

  const currentQuestion: QuestionResponse | null =
    quiz && quiz.questions && quiz.questions.length > currentQuestionIndex
      ? quiz.questions[currentQuestionIndex]
      : null;

  useEffect(() => {
    questionStartTimeRef.current = Date.now();
    setSelectedOption('');
    setTextInput('');
    setAnswerFeedback(null);
  }, [currentQuestionIndex]);

  const handleSubmitAnswer = async () => {
    if (!quiz || !currentQuestion || isSubmitting) return;

    const answer =
      currentQuestion.questionType === 'MULTIPLE_CHOICE' ? selectedOption : textInput.trim();

    if (!answer) return;

    const latencyMs = Math.max(100, Date.now() - questionStartTimeRef.current);
    setIsSubmitting(true);

    try {
      const res = await quizApi.submitAnswer(quiz.id, currentQuestion.id, {
        answer,
        responseTimeMs: latencyMs,
      });

      if (res.success && res.data) {
        setAnswerFeedback(res.data);
      }
    } catch (err: any) {
      addToast({
        type: 'error',
        title: 'Error',
        message: err?.response?.data?.message || 'Failed to submit answer.',
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleNextOrFinish = async () => {
    if (!quiz) return;

    if (currentQuestionIndex + 1 < quiz.questions.length) {
      setCurrentQuestionIndex((prev) => prev + 1);
    } else {
      // Complete quiz
      setIsSubmitting(true);
      try {
        const res = await quizApi.completeQuiz(quiz.id);
        if (res.success && res.data) {
          setQuizResult(res.data);
          setShowCelebration(true);
          await refreshUser();
          addToast({
            type: 'xp',
            title: 'Quiz Complete! 🎉',
            message: `Scored ${res.data.correctAnswers}/${res.data.totalQuestions} (${Math.round(res.data.percentage)}%)`,
            xpAmount: 40,
          });
        }
      } catch (err: any) {
        addToast({
          type: 'error',
          title: 'Error',
          message: err?.response?.data?.message || 'Failed to finalize quiz.',
        });
      } finally {
        setIsSubmitting(false);
      }
    }
  };

  if (isLoading) {
    return (
      <AppShell title="Interactive Quiz" subtitle="Preparing your vocabulary questions...">
        <div className="py-20 flex justify-center">
          <LoadingSpinner size="lg" label="Generating polymorphic quiz questions..." />
        </div>
      </AppShell>
    );
  }

  if (error || !quiz || !currentQuestion) {
    return (
      <AppShell title="Interactive Quiz" subtitle="Quiz session">
        <ErrorState
          message={error || 'Could not find or generate quiz.'}
          onRetry={loadQuiz}
        />
      </AppShell>
    );
  }

  return (
    <AppShell
      title="Vocabulary Quiz"
      subtitle={`Question ${currentQuestionIndex + 1} of ${quiz.questions.length} • ${quiz.title}`}
    >
      <div className="max-w-2xl mx-auto py-6 space-y-6">
        {/* Progress Bar */}
        <div className="space-y-2">
          <div className="flex justify-between items-center text-xs font-bold text-memora-text-muted">
            <span>
              Question {currentQuestionIndex + 1} of {quiz.questions.length}
            </span>
            <span className="text-memora-green font-bold">
              {Math.round((currentQuestionIndex / quiz.questions.length) * 100)}% Complete
            </span>
          </div>
          <ProgressBar
            value={currentQuestionIndex}
            max={quiz.questions.length}
            size="sm"
            barClassName="bg-memora-green"
          />
        </div>

        {/* Question Card */}
        <Card variant="default" className="p-8 sm:p-10 space-y-6 shadow-card">
          <div className="flex items-center justify-between">
            <Badge variant="neutral" size="sm">
              {currentQuestion.questionType.replace(/_/g, ' ')}
            </Badge>
            <span className="text-xs font-bold text-memora-green bg-memora-green-light px-2.5 py-1 rounded-full">
              +{currentQuestion.points} Points
            </span>
          </div>

          {/* Question Prompt */}
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

          {/* Options / Input Form */}
          {!answerFeedback ? (
            <div className="space-y-3 pt-2">
              {currentQuestion.questionType === 'MULTIPLE_CHOICE' && currentQuestion.options ? (
                currentQuestion.options.map((opt, idx) => {
                  const isSelected = selectedOption === opt;
                  return (
                    <button
                      key={idx}
                      type="button"
                      onClick={() => setSelectedOption(opt)}
                      className={`w-full text-left p-4 rounded-2xl border text-sm font-semibold transition-all duration-150 flex items-center justify-between ${
                        isSelected
                          ? 'border-memora-green bg-memora-green-light text-memora-green-dark shadow-sm animate-select-pulse'
                          : 'border-black/[0.08] bg-[#FBFBF9] text-memora-dark hover:bg-stone-50'
                      }`}
                    >
                      <span>{opt}</span>
                      {isSelected && (
                        <CheckCircle2 className="w-4 h-4 text-memora-green shrink-0" />
                      )}
                    </button>
                  );
                })
              ) : (
                <div>
                  <label className="block text-xs font-bold text-memora-dark uppercase tracking-wider mb-2">
                    Enter your answer
                  </label>
                  <input
                    type="text"
                    value={textInput}
                    onChange={(e) => setTextInput(e.target.value)}
                    onKeyDown={(e) => {
                      if (e.key === 'Enter') handleSubmitAnswer();
                    }}
                    placeholder="Type answer here..."
                    className="w-full px-4 py-3 bg-stone-50 border border-black/[0.08] rounded-2xl text-sm font-semibold text-memora-dark placeholder:text-stone-400 focus:outline-none focus:ring-2 focus:ring-memora-green focus:bg-white"
                  />
                </div>
              )}

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
                  className="px-8 shadow-sm"
                >
                  Submit Answer
                </Button>
              </div>
            </div>
          ) : (
            /* Immediate Feedback Panel */
            <div className="space-y-5 pt-2 animate-fade-in">
              <div
                className={`p-4 rounded-2xl border flex items-start gap-3 ${
                  answerFeedback.correct
                    ? 'bg-emerald-50 border-emerald-200 text-emerald-900'
                    : 'bg-red-50 border-red-200 text-red-900'
                }`}
              >
                {answerFeedback.correct ? (
                  <CheckCircle2 className="w-5 h-5 text-emerald-600 shrink-0 mt-0.5" />
                ) : (
                  <XCircle className="w-5 h-5 text-red-600 shrink-0 mt-0.5" />
                )}
                <div>
                  <h4 className="text-sm font-bold">
                    {answerFeedback.correct ? 'Correct! Well done.' : 'Incorrect answer.'}
                  </h4>
                  <p className="text-xs mt-1 leading-relaxed">{answerFeedback.feedback}</p>
                </div>
              </div>

              {/* Memory impact badge */}
              <div className="flex items-center justify-between text-xs text-memora-text-muted bg-[#F8F8F5] p-3 rounded-xl border border-black/[0.04]">
                <span>
                  Mastery: {Math.round(answerFeedback.masteryScore)}% • Risk:{' '}
                  {answerFeedback.forgettingRisk}
                </span>
                <span className="font-bold text-memora-green">
                  +{answerFeedback.score} Points Awarded
                </span>
              </div>

              <div className="flex justify-end pt-2">
                <Button
                  variant="primary"
                  size="md"
                  onClick={handleNextOrFinish}
                  isLoading={isSubmitting}
                  rightIcon={<ArrowRight className="w-4 h-4" />}
                  className="px-8 shadow-sm"
                >
                  {currentQuestionIndex + 1 === quiz.questions.length
                    ? 'Complete Quiz'
                    : 'Next Question'}
                </Button>
              </div>
            </div>
          )}
        </Card>

        {/* Celebration Completion Modal */}
        <Modal
          isOpen={showCelebration}
          onClose={() => {
            setShowCelebration(false);
            navigate('/dashboard');
          }}
          maxWidth="md"
        >
          {quizResult && (
            <div className="text-center space-y-6 py-2">
              <div className="w-20 h-20 rounded-full bg-gradient-to-tr from-memora-green to-[#A2D94B] text-white flex items-center justify-center mx-auto shadow-glow">
                <Sparkles className="w-10 h-10" />
              </div>

              <div>
                <span className="text-xs font-bold uppercase tracking-wider text-memora-green block mb-1">
                  Quiz Completed 🎉
                </span>
                <h3 className="text-3xl font-extrabold text-memora-dark tracking-tight">
                  {quizResult.percentage === 100 ? 'Perfect Score!' : 'Great Job!'}
                </h3>
                <p className="text-sm text-memora-text-muted mt-1.5">
                  You scored {quizResult.correctAnswers} out of {quizResult.totalQuestions} questions
                  correctly ({Math.round(quizResult.percentage)}%).
                </p>
              </div>

              <div className="grid grid-cols-3 gap-3 p-4 rounded-2xl bg-[#F8F8F5] border border-black/[0.04] text-center">
                <div>
                  <span className="text-xl font-bold text-memora-dark block">
                    {quizResult.totalScore}
                  </span>
                  <span className="text-xs text-memora-text-muted">Score</span>
                </div>
                <div>
                  <span className="text-xl font-bold text-memora-green block">+40 XP</span>
                  <span className="text-xs text-memora-text-muted">Earned</span>
                </div>
                <div>
                  <span className="text-xl font-bold text-memora-dark block">
                    {Math.round(quizResult.percentage)}%
                  </span>
                  <span className="text-xs text-memora-text-muted">Accuracy</span>
                </div>
              </div>

              <div className="pt-2 flex flex-col gap-3">
                <Button
                  variant="primary"
                  size="lg"
                  onClick={() => navigate('/learn-path')}
                  className="w-full justify-center shadow-md"
                >
                  Continue Learning Path
                </Button>
                <Button
                  variant="outline"
                  size="md"
                  onClick={() => navigate('/dashboard')}
                  className="w-full justify-center"
                >
                  Back to Dashboard
                </Button>
              </div>
            </div>
          )}
        </Modal>
      </div>
    </AppShell>
  );
};

export default QuizPage;
