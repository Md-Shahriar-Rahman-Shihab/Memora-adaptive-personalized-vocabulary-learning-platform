import React, { useEffect, useState, useRef, useCallback } from 'react';
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
  Trophy,
  User,
  Volume2,
  AlertCircle,
  BookOpen,
  ChevronRight,
  RotateCcw,
} from 'lucide-react';
import { quizApi } from '../api/quizApi';
import { learningPathApi } from '../api/learningPathApi';
import { dictionaryApi } from '../api/dictionaryApi';
import {
  QuizResponse,
  QuestionResponse,
  AnswerResponse,
  QuizResultResponse,
} from '../types/quiz';
import { DictionaryResponse } from '../types/dictionary';
import { useToast } from '../context/ToastContext';
import { useAuth } from '../context/AuthContext';
import { AppShell } from '../components/layout/AppShell';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { ProgressBar } from '../components/ui/ProgressBar';
import { LoadingSpinner } from '../components/ui/LoadingSpinner';
import { WordStudyAiActions } from '../components/vocabulary/WordStudyAiActions';

interface CompletedQuestionRecord {
  question: QuestionResponse;
  userAnswer: string;
  isCorrect: boolean;
  feedback: AnswerResponse;
  latencyMs: number;
}

export const QuizPage: React.FC = () => {
  const { quizId } = useParams<{ quizId: string }>();
  const navigate = useNavigate();
  const { addToast } = useToast();
  const { user, refreshUser } = useAuth();

  // Quiz session state
  const [quiz, setQuiz] = useState<QuizResponse | null>(null);
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState<number>(0);
  const [completedRecords, setCompletedRecords] = useState<CompletedQuestionRecord[]>([]);

  // Active question input state
  const [selectedOption, setSelectedOption] = useState<string>('');
  const [textInput, setTextInput] = useState<string>('');
  const [answerFeedback, setAnswerFeedback] = useState<AnswerResponse | null>(null);
  const [showAiDrawer, setShowAiDrawer] = useState<boolean>(false);

  // Status flags
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [submissionError, setSubmissionError] = useState<string | null>(null);
  const [quizResult, setQuizResult] = useState<QuizResultResponse | null>(null);
  const [isCompleted, setIsCompleted] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  // Dictionary audio state
  const [dictionaryDetails, setDictionaryDetails] = useState<DictionaryResponse | null>(null);
  const [audioStatus, setAudioStatus] = useState<'idle' | 'playing' | 'error'>('idle');
  const audioRef = useRef<HTMLAudioElement | null>(null);
  const questionStartTimeRef = useRef<number>(Date.now());
  const inputRef = useRef<HTMLInputElement | null>(null);

  // Clean audio on unmount
  useEffect(() => {
    return () => {
      if (audioRef.current) {
        audioRef.current.pause();
        audioRef.current = null;
      }
      if ('speechSynthesis' in window) {
        window.speechSynthesis.cancel();
      }
    };
  }, []);

  // Load or start quiz
  const loadQuiz = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    setIsCompleted(false);
    setQuizResult(null);
    setCompletedRecords([]);

    try {
      let targetQuizId = Number(quizId);
      let quizData: QuizResponse | null = null;

      if (!targetQuizId || isNaN(targetQuizId)) {
        // Check if there is an active learning path with a pending quiz
        const pathRes = await learningPathApi.getTodayPath().catch(() => null);
        const pendingQuizItem = pathRes?.data?.items?.find(
          (it) => it.type === 'QUIZ' && it.status !== 'COMPLETED'
        );

        if (pendingQuizItem?.quizId) {
          targetQuizId = pendingQuizItem.quizId;
        } else {
          // Generate a fresh 5-question quiz for user's level
          const genRes = await quizApi.generateQuiz({
            targetLevel: user?.currentLevel ?? undefined,
            questionCount: 5,
          });
          if (genRes.success && genRes.data) {
            targetQuizId = genRes.data.id;
          }
        }
      }

      if (targetQuizId) {
        const startRes = await quizApi.startQuiz(targetQuizId);
        if (startRes.success && startRes.data) {
          quizData = startRes.data;
        }
      }

      if (!quizData || !quizData.questions || quizData.questions.length === 0) {
        setError('No quiz questions available for this session.');
        return;
      }

      setQuiz(quizData);

      // Handle resumption if some questions were already answered
      const answeredIds = quizData.answeredQuestionIds || [];
      if (answeredIds.length > 0) {
        if (answeredIds.length >= quizData.questions.length) {
          // All questions already answered: fetch result
          const resRes = await quizApi.getQuizResult(quizData.id).catch(() => null);
          if (resRes?.success && resRes.data) {
            setQuizResult(resRes.data);
            setIsCompleted(true);
            return;
          }
        } else {
          // Find first unanswered question
          const firstUnansweredIndex = quizData.questions.findIndex(
            (q) => !answeredIds.includes(q.id)
          );
          if (firstUnansweredIndex >= 0) {
            setCurrentQuestionIndex(firstUnansweredIndex);
          }
        }
      } else {
        setCurrentQuestionIndex(0);
      }
    } catch (err: unknown) {
      console.error('Error starting quiz:', err);
      const apiErr = err as { response?: { data?: { message?: string } } };
      setError(apiErr?.response?.data?.message || 'Failed to start quiz session.');
    } finally {
      setIsLoading(false);
    }
  }, [quizId, user?.currentLevel]);

  useEffect(() => {
    loadQuiz();
  }, [loadQuiz]);

  const currentQuestion: QuestionResponse | null =
    quiz && quiz.questions && quiz.questions.length > currentQuestionIndex
      ? quiz.questions[currentQuestionIndex]
      : null;

  // Reset per-question state when index changes
  useEffect(() => {
    questionStartTimeRef.current = Date.now();
    setSelectedOption('');
    setTextInput('');
    setAnswerFeedback(null);
    setSubmissionError(null);
    setShowAiDrawer(false);
    setDictionaryDetails(null);
    setAudioStatus('idle');

    if (audioRef.current) {
      audioRef.current.pause();
      audioRef.current = null;
    }
    if ('speechSynthesis' in window) {
      window.speechSynthesis.cancel();
    }

    // Auto-focus text input for translation or fill-in-the-blank
    if (currentQuestion && currentQuestion.questionType !== 'MULTIPLE_CHOICE') {
      setTimeout(() => {
        inputRef.current?.focus();
      }, 100);
    }

    // Lazy load dictionary entry for audio if target word is present
    if (currentQuestion?.word) {
      let isMounted = true;
      dictionaryApi
        .lookupWord(currentQuestion.word)
        .then((res) => {
          if (isMounted && res.success && res.data) {
            setDictionaryDetails(res.data);
          }
        })
        .catch(() => {
          if (isMounted) setDictionaryDetails(null);
        });

      return () => {
        isMounted = false;
      };
    }
  }, [currentQuestionIndex, currentQuestion?.word, currentQuestion?.questionType]);

  // Audio pronunciation player
  const handlePlayAudio = () => {
    if (!currentQuestion?.word) return;

    if (audioStatus === 'playing') {
      if (audioRef.current) {
        audioRef.current.pause();
        audioRef.current.currentTime = 0;
      }
      if ('speechSynthesis' in window) {
        window.speechSynthesis.cancel();
      }
      setAudioStatus('idle');
      return;
    }

    setAudioStatus('playing');

    const audioUrl = dictionaryDetails?.audioUrl;
    if (audioUrl) {
      if (audioRef.current) {
        audioRef.current.pause();
      }
      const audio = new Audio(audioUrl);
      audioRef.current = audio;
      audio.onended = () => setAudioStatus('idle');
      audio.onerror = () => {
        fallbackToSpeechSynthesis(currentQuestion.word!);
      };
      audio.play().catch(() => {
        fallbackToSpeechSynthesis(currentQuestion.word!);
      });
    } else {
      fallbackToSpeechSynthesis(currentQuestion.word);
    }
  };

  const fallbackToSpeechSynthesis = (wordToSpeak: string) => {
    if ('speechSynthesis' in window && wordToSpeak) {
      window.speechSynthesis.cancel();
      const utterance = new SpeechSynthesisUtterance(wordToSpeak);
      utterance.lang = 'en-US';
      utterance.rate = 0.9;
      utterance.onend = () => setAudioStatus('idle');
      utterance.onerror = () => setAudioStatus('idle');
      window.speechSynthesis.speak(utterance);
    } else {
      setAudioStatus('idle');
    }
  };

  // Submit and evaluate answer
  const handleSubmitAnswer = async () => {
    if (!quiz || !currentQuestion || isSubmitting || answerFeedback) return;

    const answer =
      currentQuestion.questionType === 'MULTIPLE_CHOICE' ? selectedOption : textInput.trim();

    if (!answer) return;

    const latencyMs = Math.max(100, Date.now() - questionStartTimeRef.current);
    setIsSubmitting(true);
    setSubmissionError(null);

    try {
      const res = await quizApi.submitAnswer(quiz.id, currentQuestion.id, {
        questionId: currentQuestion.id,
        answer,
        responseTimeMs: latencyMs,
      });

      if (res.success && res.data) {
        const feedback = res.data;
        setAnswerFeedback(feedback);

        // Record for mistake review / summary
        setCompletedRecords((prev) => [
          ...prev,
          {
            question: currentQuestion,
            userAnswer: answer,
            isCorrect: feedback.correct,
            feedback,
            latencyMs,
          },
        ]);

        if (feedback.correct) {
          addToast({
            type: 'xp',
            title: 'Correct! +10 XP',
            message: feedback.feedback || 'Answer confirmed and retention updated.',
            xpAmount: feedback.xpEarned ?? 10,
          });
        }
      } else {
        setSubmissionError(res.message || 'Failed to submit answer. Please try again.');
      }
    } catch (err: unknown) {
      console.error('Error submitting quiz answer:', err);
      const apiErr = err as { response?: { data?: { message?: string } } };
      setSubmissionError(
        apiErr?.response?.data?.message || 'Server error. Your answer was not recorded. Click Retry.'
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  // Advance to next question or complete quiz
  const handleNextOrFinish = async () => {
    if (!quiz) return;

    if (currentQuestionIndex + 1 < quiz.questions.length) {
      setCurrentQuestionIndex((prev) => prev + 1);
    } else {
      // Finalize quiz
      setIsSubmitting(true);
      try {
        const res = await quizApi.completeQuiz(quiz.id);
        if (res.success && res.data) {
          setQuizResult(res.data);
          setIsCompleted(true);
          await refreshUser().catch(() => null);

          const finalXp =
            res.data.xpEarned ??
            (res.data.correctAnswers * 10 +
              (res.data.correctAnswers === res.data.totalQuestions ? 20 : 0));

          addToast({
            type: 'xp',
            title: 'Quiz Complete! 🎉',
            message: `Scored ${res.data.correctAnswers}/${res.data.totalQuestions} (${Math.round(
              res.data.percentage
            )}%)`,
            xpAmount: finalXp,
          });
        } else {
          addToast({
            type: 'error',
            title: 'Error',
            message: res.message || 'Could not finalize quiz results.',
          });
        }
      } catch (err: unknown) {
        console.error('Error finalizing quiz:', err);
        const apiErr = err as { response?: { data?: { message?: string } } };
        addToast({
          type: 'error',
          title: 'Finalization Failed',
          message: apiErr?.response?.data?.message || 'Failed to finalize quiz. Please click again.',
        });
      } finally {
        setIsSubmitting(false);
      }
    }
  };

  // Keyboard navigation listener (1-4 for options, Enter for submit/advance)
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      // Don't intercept if user is typing in the text input for translation/blank
      if (
        document.activeElement?.tagName === 'INPUT' &&
        currentQuestion?.questionType !== 'MULTIPLE_CHOICE'
      ) {
        if (e.key === 'Enter' && !answerFeedback && !isSubmitting && textInput.trim()) {
          e.preventDefault();
          handleSubmitAnswer();
        }
        return;
      }

      if (!answerFeedback) {
        if (currentQuestion?.questionType === 'MULTIPLE_CHOICE' && currentQuestion.options) {
          if (['1', '2', '3', '4'].includes(e.key)) {
            const index = parseInt(e.key, 10) - 1;
            if (currentQuestion.options[index]) {
              setSelectedOption(currentQuestion.options[index]);
            }
          } else if (e.key === 'Enter' && selectedOption && !isSubmitting) {
            e.preventDefault();
            handleSubmitAnswer();
          }
        }
      } else {
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault();
          handleNextOrFinish();
        }
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [answerFeedback, currentQuestion, selectedOption, textInput, isSubmitting, currentQuestionIndex, quiz?.questions.length]);

  // Loading State
  if (isLoading) {
    return (
      <AppShell title="Vocabulary Quiz" subtitle="Preparing your questions...">
        <div className="py-24 flex flex-col items-center justify-center">
          <LoadingSpinner size="lg" label="Loading polymorphic quiz questions..." />
          <p className="text-xs text-stone-400 mt-4">
            Balancing multiple choice, translation, and fill-in-the-blank items...
          </p>
        </div>
      </AppShell>
    );
  }

  // Error State
  if (error || !quiz || (!currentQuestion && !isCompleted)) {
    return (
      <AppShell title="Vocabulary Quiz" subtitle="Session error">
        <div className="max-w-md mx-auto py-16 text-center">
          <Card className="p-8 space-y-4 border-red-200 bg-red-50/40">
            <AlertCircle className="w-12 h-12 text-red-600 mx-auto" />
            <h2 className="text-lg font-bold text-stone-900">Quiz Unavailable</h2>
            <p className="text-sm text-stone-600">{error || 'Could not load quiz questions.'}</p>
            <div className="pt-2 flex justify-center gap-3">
              <Button variant="primary" size="md" onClick={() => loadQuiz()}>
                Try Again
              </Button>
              <Button variant="outline" size="md" onClick={() => navigate('/learn-path')}>
                Back to Learning Path
              </Button>
            </div>
          </Card>
        </div>
      </AppShell>
    );
  }

  // Quiz Completion Screen
  if (isCompleted && quizResult) {
    const totalQ = quizResult.totalQuestions;
    const correctQ = quizResult.correctAnswers;
    const accuracy = Math.round(quizResult.percentage);
    const earnedXp =
      quizResult.xpEarned ??
      (correctQ * 10 + (correctQ === totalQ && totalQ > 0 ? 20 : 0));
    const streak = quizResult.currentStreak ?? user?.streak ?? 1;
    const missedItems = completedRecords.filter((r) => !r.isCorrect);

    return (
      <AppShell title="Quiz Complete" subtitle="Assessment & Retention Summary">
        <div className="max-w-2xl mx-auto py-6 space-y-6">
          {/* Header Card */}
          <Card className="p-8 text-center bg-gradient-to-b from-emerald-50/70 to-white border-emerald-200/80 shadow-sm space-y-4">
            <div className="w-16 h-16 rounded-full bg-emerald-100 flex items-center justify-center mx-auto text-emerald-700 shadow-inner">
              <Trophy className="w-8 h-8" />
            </div>

            <div>
              <span className="text-xs uppercase tracking-widest font-extrabold text-emerald-800">
                Quiz Evaluation
              </span>
              <h1 className="text-3xl font-extrabold text-stone-900 mt-1">
                {accuracy === 100
                  ? 'Perfect Score! 🌟'
                  : accuracy >= 70
                  ? 'Great Work! 🎉'
                  : 'Practice Complete! 📚'}
              </h1>
              <p className="text-sm text-stone-600 max-w-md mx-auto mt-2">
                Your answers have been evaluated and your vocabulary retention has been updated in
                the Memory Engine.
              </p>
            </div>

            {/* Metrics Grid */}
            <div className="grid grid-cols-3 gap-3 pt-4 border-t border-emerald-100 max-w-lg mx-auto">
              <div className="bg-white p-3.5 rounded-xl border border-stone-100 shadow-2xs">
                <span className="text-[11px] font-semibold text-stone-500 uppercase tracking-wider block">
                  Score
                </span>
                <span className="text-2xl font-black text-stone-900">
                  {correctQ} / {totalQ}
                </span>
                <span className="text-[10px] text-stone-400 block">{accuracy}% correct</span>
              </div>
              <div className="bg-white p-3.5 rounded-xl border border-stone-100 shadow-2xs">
                <span className="text-[11px] font-semibold text-stone-500 uppercase tracking-wider block">
                  XP Gained
                </span>
                <span className="text-2xl font-black text-amber-600">+{earnedXp}</span>
                <span className="text-[10px] text-stone-400 block">XP earned</span>
              </div>
              <div className="bg-white p-3.5 rounded-xl border border-stone-100 shadow-2xs">
                <span className="text-[11px] font-semibold text-stone-500 uppercase tracking-wider block">
                  Streak
                </span>
                <span className="text-2xl font-black text-emerald-700 flex items-center justify-center gap-1">
                  <Flame className="w-5 h-5 text-amber-500 fill-amber-500" />
                  {streak}
                </span>
                <span className="text-[10px] text-stone-400 block">days active</span>
              </div>
            </div>

            {/* Learning Path Sync Notice */}
            {quizResult.learningPathCompleted && (
              <div className="mt-4 p-3 bg-emerald-100/70 border border-emerald-200 rounded-xl text-xs font-semibold text-emerald-900 flex items-center justify-center gap-2">
                <Sparkles className="w-4 h-4 text-emerald-700 shrink-0" />
                <span>Today's Learning Path curriculum is now fully completed! 🎉</span>
              </div>
            )}
          </Card>

          {/* Review Mistakes Section */}
          {missedItems.length > 0 && (
            <Card className="p-6 border-amber-200/80 bg-amber-50/30 space-y-3">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2 text-amber-900 font-bold text-sm">
                  <RotateCcw className="w-4 h-4 text-amber-600" />
                  <span>Review Mistakes ({missedItems.length})</span>
                </div>
                <span className="text-[11px] text-stone-500">Scheduled for earlier spaced repetition</span>
              </div>

              <div className="divide-y divide-amber-200/60 rounded-xl bg-white border border-amber-100 overflow-hidden">
                {missedItems.map((item, idx) => (
                  <div key={idx} className="p-3.5 text-xs space-y-1.5">
                    <div className="flex items-center justify-between">
                      <span className="font-bold text-stone-900 text-sm">
                        {item.question.word || `Question #${idx + 1}`}
                      </span>
                      <Badge variant="neutral" size="sm">
                        {item.question.questionType.replace(/_/g, ' ')}
                      </Badge>
                    </div>

                    <p className="text-stone-600 italic">"{item.question.questionText}"</p>

                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 pt-1">
                      <div className="p-2 rounded bg-rose-50 border border-rose-100 text-rose-900">
                        <span className="text-[10px] font-bold uppercase block text-rose-500">
                          Your Answer
                        </span>
                        <span className="font-medium line-clamp-1">{item.userAnswer || 'No answer'}</span>
                      </div>
                      <div className="p-2 rounded bg-emerald-50 border border-emerald-100 text-emerald-900">
                        <span className="text-[10px] font-bold uppercase block text-emerald-600">
                          Expected Answer
                        </span>
                        <span className="font-medium line-clamp-1">
                          {item.feedback.correctAnswer || item.feedback.feedback.replace(/^Incorrect\. The (correct answer|expected translation|expected word) (is|was): /, '')}
                        </span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </Card>
          )}

          {/* Action Buttons */}
          <div className="flex flex-col sm:flex-row items-center justify-between gap-3 pt-2">
            <Button
              variant="outline"
              size="lg"
              onClick={() => navigate('/dashboard')}
              className="w-full sm:w-auto text-xs font-bold"
            >
              Back to Dashboard
            </Button>
            <div className="flex items-center gap-2 w-full sm:w-auto">
              <Button
                variant="secondary"
                size="lg"
                onClick={() => loadQuiz()}
                className="w-full sm:w-auto text-xs font-bold"
              >
                Retake / New Quiz
              </Button>
              <Button
                variant="primary"
                size="lg"
                onClick={() => navigate('/learn-path')}
                className="w-full sm:w-auto text-xs font-bold shadow-sm"
              >
                Continue Learning Path <ArrowRight className="w-4 h-4 ml-1.5" />
              </Button>
            </div>
          </div>
        </div>
      </AppShell>
    );
  }

  // Active Question Card State
  const progressPercent = Math.round(
    ((currentQuestionIndex + 1) / (quiz.questions.length || 1)) * 100
  );

  return (
    <AppShell
      title="Vocabulary Quiz"
      subtitle={`Question ${currentQuestionIndex + 1} of ${quiz.questions.length} • ${quiz.title}`}
    >
      <div className="max-w-2xl mx-auto py-4 space-y-5">
        {/* Top Progress Bar */}
        <div className="space-y-2">
          <div className="flex items-center justify-between text-xs">
            <span className="font-bold text-stone-800 flex items-center gap-1.5">
              <Sparkles className="w-3.5 h-3.5 text-emerald-600" />
              Quiz Progress
            </span>
            <span className="text-stone-500 font-medium">
              <strong className="text-stone-900">{currentQuestionIndex + 1}</strong> of{' '}
              <strong className="text-stone-900">{quiz.questions.length}</strong> questions
            </span>
          </div>
          <ProgressBar value={progressPercent} size="sm" barClassName="bg-emerald-600" />
        </div>

        {/* Main Question Card */}
        <Card className="p-6 sm:p-10 border border-stone-200/80 shadow-sm relative overflow-hidden bg-white space-y-6">
          {/* Header Badges */}
          <div className="flex items-center justify-between flex-wrap gap-2">
            <div className="flex items-center gap-2">
              <Badge
                variant={
                  currentQuestion?.questionType === 'MULTIPLE_CHOICE'
                    ? 'purple'
                    : currentQuestion?.questionType === 'TRANSLATION'
                    ? 'blue'
                    : 'amber'
                }
                size="sm"
              >
                {currentQuestion?.questionType?.replace(/_/g, ' ')}
              </Badge>
              {currentQuestion?.difficultyLevel && (
                <Badge variant="neutral" size="sm">
                  CEFR {currentQuestion.difficultyLevel}
                </Badge>
              )}
            </div>

            <div className="flex items-center gap-2">
              <span className="text-xs font-bold text-emerald-800 bg-emerald-50 border border-emerald-200/60 px-2.5 py-0.5 rounded-full">
                +{currentQuestion?.points || 10} Points
              </span>
            </div>
          </div>

          {/* Question Text Prompt */}
          <div className="space-y-3">
            <div className="flex items-start justify-between gap-3">
              <h2 className="text-xl sm:text-2xl font-bold text-stone-900 leading-snug font-serif">
                {currentQuestion?.questionText}
              </h2>

              {/* Audio button if target word is available */}
              {currentQuestion?.word && (
                <button
                  type="button"
                  onClick={handlePlayAudio}
                  className={`w-10 h-10 rounded-full flex items-center justify-center transition border shrink-0 ${
                    audioStatus === 'playing'
                      ? 'bg-emerald-100 text-emerald-700 border-emerald-300 scale-105 animate-pulse'
                      : 'bg-stone-50 hover:bg-emerald-50 text-stone-600 hover:text-emerald-700 border-stone-200 hover:border-emerald-200'
                  }`}
                  title="Listen to native pronunciation"
                  aria-label="Listen to pronunciation"
                >
                  <Volume2 className={`w-4 h-4 ${audioStatus === 'playing' ? 'animate-bounce' : ''}`} />
                </button>
              )}
            </div>

            {/* Sentence context for fill-in-the-blank */}
            {currentQuestion?.sentence && (
              <div className="p-4 bg-stone-50 rounded-xl border border-stone-200/70 text-sm text-stone-800 leading-relaxed font-sans">
                {currentQuestion.sentence.split('___').map((part, i, arr) => (
                  <React.Fragment key={i}>
                    <span>{part}</span>
                    {i < arr.length - 1 && (
                      <span className="inline-block px-3 py-0.5 mx-1 font-mono font-bold text-emerald-800 bg-emerald-100 border border-emerald-300 rounded">
                        {answerFeedback ? answerFeedback.correctAnswer || textInput || '___' : '___'}
                      </span>
                    )}
                  </React.Fragment>
                ))}
              </div>
            )}
          </div>

          {/* Submission Failure Alert */}
          {submissionError && (
            <div className="p-3.5 bg-red-50 border border-red-200 rounded-xl text-xs text-red-800 flex items-center justify-between gap-3 animate-fade-in">
              <div className="flex items-center gap-2">
                <AlertCircle className="w-4 h-4 shrink-0 text-red-600" />
                <span>{submissionError}</span>
              </div>
              <Button
                variant="outline"
                size="sm"
                onClick={handleSubmitAnswer}
                className="shrink-0 border-red-300 text-red-800 hover:bg-red-100 text-xs py-1"
              >
                Retry
              </Button>
            </div>
          )}

          {/* Question Interaction Area */}
          <div className="space-y-3">
            {currentQuestion?.questionType === 'MULTIPLE_CHOICE' && currentQuestion.options ? (
              <div className="grid grid-cols-1 gap-2.5">
                <div className="flex items-center justify-between text-xs text-stone-500 font-medium px-1">
                  <span>Select the correct option:</span>
                  <span className="hidden sm:inline text-[11px] text-stone-400">Keys 1-4 to select</span>
                </div>

                {currentQuestion.options.map((opt, idx) => {
                  const label = ['A', 'B', 'C', 'D'][idx];
                  const isSelected = selectedOption === opt;

                  let optionStyles = 'border-stone-200 hover:border-stone-300 bg-white text-stone-800';

                  if (answerFeedback) {
                    const isCorrectAnswer =
                      answerFeedback.correctAnswer?.trim().toLowerCase() === opt.trim().toLowerCase() ||
                      (answerFeedback.correct && isSelected);

                    if (isCorrectAnswer) {
                      optionStyles =
                        'border-emerald-400 bg-emerald-50 text-emerald-950 font-semibold ring-1 ring-emerald-400';
                    } else if (isSelected && !answerFeedback.correct) {
                      optionStyles = 'border-rose-400 bg-rose-50 text-rose-950 ring-1 ring-rose-400';
                    } else {
                      optionStyles = 'border-stone-100 bg-stone-50 text-stone-400 opacity-60';
                    }
                  } else if (isSelected) {
                    optionStyles =
                      'border-emerald-600 bg-emerald-50/50 text-stone-950 ring-1 ring-emerald-600 shadow-2xs';
                  }

                  return (
                    <button
                      key={idx}
                      type="button"
                      disabled={!!answerFeedback || isSubmitting}
                      onClick={() => setSelectedOption(opt)}
                      className={`w-full text-left p-3.5 sm:p-4 rounded-xl border text-sm transition flex items-start gap-3 select-none ${optionStyles}`}
                    >
                      <span
                        className={`w-6 h-6 rounded-md flex items-center justify-center text-xs font-bold shrink-0 mt-0.5 ${
                          answerFeedback &&
                          (answerFeedback.correctAnswer?.trim().toLowerCase() === opt.trim().toLowerCase() ||
                            (answerFeedback.correct && isSelected))
                            ? 'bg-emerald-600 text-white'
                            : answerFeedback && isSelected && !answerFeedback.correct
                            ? 'bg-rose-600 text-white'
                            : isSelected
                            ? 'bg-emerald-600 text-white'
                            : 'bg-stone-100 text-stone-600'
                        }`}
                      >
                        {answerFeedback &&
                        (answerFeedback.correctAnswer?.trim().toLowerCase() === opt.trim().toLowerCase() ||
                          (answerFeedback.correct && isSelected)) ? (
                          <CheckCircle2 className="w-4 h-4" />
                        ) : answerFeedback && isSelected && !answerFeedback.correct ? (
                          <XCircle className="w-4 h-4" />
                        ) : (
                          label
                        )}
                      </span>
                      <span className="flex-1 leading-snug">{opt}</span>
                    </button>
                  );
                })}
              </div>
            ) : (
              /* Translation or Fill in the blank Input */
              <div className="space-y-2">
                <label className="block text-xs font-bold text-stone-600 uppercase tracking-wider">
                  {currentQuestion?.questionType === 'TRANSLATION'
                    ? 'Your Translation / Meaning'
                    : 'Fill in the blank with the vocabulary word'}
                </label>
                <input
                  ref={inputRef}
                  type="text"
                  value={textInput}
                  disabled={!!answerFeedback || isSubmitting}
                  onChange={(e) => setTextInput(e.target.value)}
                  placeholder={
                    currentQuestion?.questionType === 'TRANSLATION'
                      ? 'Type the word meaning...'
                      : 'Type the missing word here...'
                  }
                  className={`w-full px-4 py-3 rounded-xl border text-sm font-semibold transition focus:outline-none ${
                    answerFeedback
                      ? answerFeedback.correct
                        ? 'border-emerald-400 bg-emerald-50 text-emerald-950 ring-1 ring-emerald-400'
                        : 'border-rose-400 bg-rose-50 text-rose-950 ring-1 ring-rose-400'
                      : 'border-stone-300 focus:border-emerald-600 focus:ring-1 focus:ring-emerald-600 bg-white text-stone-900'
                  }`}
                />
              </div>
            )}
          </div>

          {/* Immediate Feedback Panel */}
          {answerFeedback && (
            <div className="space-y-4 pt-4 border-t border-stone-200 animate-fade-in">
              <div
                className={`p-4 rounded-xl flex items-start justify-between text-sm ${
                  answerFeedback.correct
                    ? 'bg-emerald-50 text-emerald-950 border border-emerald-200'
                    : 'bg-amber-50 text-amber-950 border border-amber-200'
                }`}
              >
                <div className="flex items-start gap-2.5">
                  {answerFeedback.correct ? (
                    <CheckCircle2 className="w-5 h-5 text-emerald-600 shrink-0 mt-0.5" />
                  ) : (
                    <XCircle className="w-5 h-5 text-amber-600 shrink-0 mt-0.5" />
                  )}
                  <div className="space-y-1">
                    <span className="font-bold block text-sm">
                      {answerFeedback.correct ? 'Correct! Well done.' : 'Needs Practice'}
                    </span>
                    <p className="text-xs text-stone-700 leading-relaxed">
                      {answerFeedback.feedback}
                    </p>

                    {/* If user answered wrong, clearly show both answers */}
                    {!answerFeedback.correct && (
                      <div className="pt-1.5 flex flex-wrap gap-2 text-xs">
                        <span className="px-2 py-0.5 rounded bg-rose-100/80 text-rose-800 font-medium">
                          Your answer: {currentQuestion?.questionType === 'MULTIPLE_CHOICE' ? selectedOption : textInput || '(None)'}
                        </span>
                        {answerFeedback.correctAnswer && (
                          <span className="px-2 py-0.5 rounded bg-emerald-100 text-emerald-800 font-bold">
                            Correct: {answerFeedback.correctAnswer}
                          </span>
                        )}
                      </div>
                    )}
                  </div>
                </div>

                <div className="flex items-center gap-1.5 font-bold text-xs bg-white/80 px-2.5 py-1 rounded-full border border-black/[0.06] shadow-2xs shrink-0">
                  <Flame className="w-3.5 h-3.5 text-amber-500 fill-amber-500" />
                  <span>+{answerFeedback.score || (answerFeedback.correct ? 10 : 0)} Pts</span>
                </div>
              </div>

              {/* Memory Update Strip */}
              <div className="grid grid-cols-3 gap-2 text-center text-xs">
                <div className="p-2.5 bg-stone-50 rounded-lg border border-stone-200/60">
                  <span className="text-[10px] text-stone-400 block">Mastery Score</span>
                  <span className="font-bold text-stone-900">
                    {Math.round(answerFeedback.masteryScore)}%
                  </span>
                </div>
                <div className="p-2.5 bg-stone-50 rounded-lg border border-stone-200/60">
                  <span className="text-[10px] text-stone-400 block">Forgetting Risk</span>
                  <span className="font-bold text-stone-900 font-mono text-[11px]">
                    {answerFeedback.forgettingRisk}
                  </span>
                </div>
                <div className="p-2.5 bg-stone-50 rounded-lg border border-stone-200/60">
                  <span className="text-[10px] text-stone-400 block">Spaced Retention</span>
                  <span className="font-bold text-emerald-700">
                    {answerFeedback.nextReviewAt
                      ? new Date(answerFeedback.nextReviewAt).toLocaleDateString()
                      : 'Scheduled'}
                  </span>
                </div>
              </div>

              {/* Optional AI Assistant Accordion */}
              {currentQuestion?.word && (
                <div className="pt-1">
                  <button
                    type="button"
                    onClick={() => setShowAiDrawer((prev) => !prev)}
                    className="flex items-center justify-between w-full py-2 px-3 text-xs font-semibold text-emerald-800 bg-emerald-50/60 hover:bg-emerald-50 rounded-lg border border-emerald-200/60 transition"
                  >
                    <span className="flex items-center gap-1.5">
                      <Sparkles className="w-3.5 h-3.5 text-emerald-600" />
                      {showAiDrawer
                        ? 'Hide AI Learning Insights'
                        : `Explore "${currentQuestion.word}" with AI Learning Assistant`}
                    </span>
                    <span className="text-[11px] underline">
                      {showAiDrawer ? 'Collapse' : 'Show Tips & Examples'}
                    </span>
                  </button>

                  {showAiDrawer && (
                    <div className="mt-3 p-4 rounded-xl border border-stone-200 bg-white">
                      <WordStudyAiActions
                        word={currentQuestion.word}
                        cefrLevel={currentQuestion.difficultyLevel || quiz.difficultyLevel}
                      />
                    </div>
                  )}
                </div>
              )}
            </div>
          )}

          {/* Action Footer */}
          <div className="pt-2 flex items-center justify-between gap-3">
            {!answerFeedback ? (
              <div className="w-full flex items-center justify-between">
                <span className="text-xs text-stone-400 hidden sm:inline">
                  {currentQuestion?.questionType === 'MULTIPLE_CHOICE'
                    ? 'Press 1-4 to select, Enter ↵ to check'
                    : 'Press Enter ↵ to check answer'}
                </span>
                <Button
                  variant="primary"
                  size="md"
                  onClick={handleSubmitAnswer}
                  disabled={
                    currentQuestion?.questionType === 'MULTIPLE_CHOICE'
                      ? !selectedOption || isSubmitting
                      : !textInput.trim() || isSubmitting
                  }
                  isLoading={isSubmitting}
                  className="ml-auto text-xs font-bold px-7 shadow-sm"
                >
                  Check Answer <span className="hidden sm:inline ml-1 font-mono text-[10px] opacity-70">↵</span>
                </Button>
              </div>
            ) : (
              <div className="w-full flex items-center justify-between gap-3">
                <span className="text-xs text-stone-400 hidden sm:inline">
                  Press <kbd className="px-1.5 py-0.5 bg-stone-100 border border-stone-300 rounded text-[10px]">Enter ↵</kbd> or click next
                </span>
                <Button
                  variant="primary"
                  size="md"
                  onClick={handleNextOrFinish}
                  isLoading={isSubmitting}
                  className="w-full sm:w-auto ml-auto text-xs font-bold px-7 shadow-sm"
                >
                  {currentQuestionIndex + 1 < quiz.questions.length ? (
                    <>
                      Next Question <ArrowRight className="w-4 h-4 ml-1.5" />
                    </>
                  ) : (
                    <>
                      Complete Quiz <Award className="w-4 h-4 ml-1.5" />
                    </>
                  )}
                </Button>
              </div>
            )}
          </div>
        </Card>
      </div>
    </AppShell>
  );
};

export default QuizPage;
