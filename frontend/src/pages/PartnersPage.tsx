import React, { useEffect, useState, useCallback, useRef } from 'react';
import {
  Users,
  UserPlus,
  ShieldCheck,
  Sparkles,
  Flame,
  BookOpen,
  CheckCircle2,
  Target,
  Star,
  Award,
  Search,
  Check,
  X,
  Clock,
  AlertCircle,
  RefreshCw,
  Swords,
  Trophy,
  Play,
  ArrowRight,
  HelpCircle,
  CheckCircle,
  XCircle,
  ChevronRight,
  RotateCcw,
  Mail,
} from 'lucide-react';
import { partnerApi } from '../api/partnerApi';
import {
  PartnerUserSummary,
  PartnerProgress,
  PartnerRequestsSummary,
  ChallengeSummary,
  ChallengeQuestion,
  ChallengeResult,
  PartnerLeaderboardEntry,
  PartnerActivity,
} from '../types/partner';
import { useAuth } from '../context/AuthContext';
import { AppShell } from '../components/layout/AppShell';
import { Card } from '../components/ui/Card';
import { Badge } from '../components/ui/Badge';
import { Button } from '../components/ui/Button';
import { Modal } from '../components/ui/Modal';
import { ProgressBar } from '../components/ui/ProgressBar';
import { LoadingSpinner } from '../components/ui/LoadingSpinner';
import { useToast } from '../context/ToastContext';


export const PartnersPage: React.FC = () => {
  const { user: currentUser } = useAuth();
  const { addToast } = useToast();

  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [partners, setPartners] = useState<PartnerUserSummary[]>([]);
  const [selectedPartnerId, setSelectedPartnerId] = useState<number | null>(null);
  const [partnerProgress, setPartnerProgress] = useState<PartnerProgress | null>(null);
  const [isProgressLoading, setIsProgressLoading] = useState<boolean>(false);
  const [progressError, setProgressError] = useState<string | null>(null);

  const [requests, setRequests] = useState<PartnerRequestsSummary>({ incoming: [], outgoing: [] });
  const [isSearchModalOpen, setIsSearchModalOpen] = useState<boolean>(false);
  const [modalTab, setModalTab] = useState<'search' | 'incoming' | 'outgoing'>('search');

  // Search state
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [searchResults, setSearchResults] = useState<PartnerUserSummary[]>([]);
  const [isSearching, setIsSearching] = useState<boolean>(false);
  const [hasSearched, setHasSearched] = useState<boolean>(false);
  const [actionInProgressId, setActionInProgressId] = useState<number | null>(null);

  // Email search state (Phase E)
  const [searchEmail, setSearchEmail] = useState<string>('');
  const [emailSearchResult, setEmailSearchResult] = useState<PartnerUserSummary | null>(null);
  const [isSearchingEmail, setIsSearchingEmail] = useState<boolean>(false);
  const [emailSearchError, setEmailSearchError] = useState<string | null>(null);
  const [hasSearchedEmail, setHasSearchedEmail] = useState<boolean>(false);

  // Pair-Only Leaderboard state (Phase E)
  const [leaderboard, setLeaderboard] = useState<PartnerLeaderboardEntry[]>([]);
  const [isLeaderboardLoading, setIsLeaderboardLoading] = useState<boolean>(false);

  // Partner Activity Feed state (Phase E)
  const [activities, setActivities] = useState<PartnerActivity[]>([]);
  const [isActivitiesLoading, setIsActivitiesLoading] = useState<boolean>(false);

  // Challenges state
  const [challenges, setChallenges] = useState<ChallengeSummary[]>([]);
  const [isChallengesLoading, setIsChallengesLoading] = useState<boolean>(false);
  const [isCreateChallengeModalOpen, setIsCreateChallengeModalOpen] = useState<boolean>(false);
  const [selectedCefrLevel, setSelectedCefrLevel] = useState<string>('B1');
  const [selectedQuestionCount, setSelectedQuestionCount] = useState<number>(5);
  const [isCreatingChallenge, setIsCreatingChallenge] = useState<boolean>(false);

  // Challenge Runner Modal State
  const [isQuizModalOpen, setIsQuizModalOpen] = useState<boolean>(false);
  const [quizChallenge, setQuizChallenge] = useState<ChallengeSummary | null>(null);
  const [quizQuestions, setQuizQuestions] = useState<ChallengeQuestion[]>([]);
  const [isQuizLoading, setIsQuizLoading] = useState<boolean>(false);
  const [currentQuestionIdx, setCurrentQuestionIdx] = useState<number>(0);
  const [answersMap, setAnswersMap] = useState<Record<number, string>>({});
  const [latenciesMap, setLatenciesMap] = useState<Record<number, number>>({});
  const [questionStartTime, setQuestionStartTime] = useState<number>(Date.now());
  const [isSubmittingQuiz, setIsSubmittingQuiz] = useState<boolean>(false);

  // Challenge Results Modal State
  const [isResultModalOpen, setIsResultModalOpen] = useState<boolean>(false);
  const [resultData, setResultData] = useState<ChallengeResult | null>(null);
  const [isLoadingResult, setIsLoadingResult] = useState<boolean>(false);

  const questionStartTimeRef = useRef<number>(Date.now());

  const fetchChallenges = useCallback(async () => {
    setIsChallengesLoading(true);
    try {
      const res = await partnerApi.getMyChallenges();
      if (res.success) {
        setChallenges(res.data || []);
      }
    } catch {
      // quiet fail for background challenges load
    } finally {
      setIsChallengesLoading(false);
    }
  }, []);

  const fetchLeaderboard = useCallback(async () => {
    setIsLeaderboardLoading(true);
    try {
      const res = await partnerApi.getPartnerLeaderboard();
      if (res.success) {
        setLeaderboard(res.data || []);
      }
    } catch {
      // quiet fail for background leaderboard load
    } finally {
      setIsLeaderboardLoading(false);
    }
  }, []);

  const fetchActivities = useCallback(async () => {
    setIsActivitiesLoading(true);
    try {
      const res = await partnerApi.getPartnerActivities(20);
      if (res.success) {
        setActivities(res.data || []);
      }
    } catch {
      // quiet fail for background activities load
    } finally {
      setIsActivitiesLoading(false);
    }
  }, []);

  const fetchPartnersAndRequests = useCallback(async () => {
    setIsLoading(true);
    try {
      const [partnersRes, requestsRes] = await Promise.allSettled([
        partnerApi.getActivePartners(),
        partnerApi.getPartnerRequests(),
      ]);

      if (partnersRes.status === 'fulfilled' && partnersRes.value.success) {
        const fetchedPartners = partnersRes.value.data || [];
        setPartners(fetchedPartners);
        if (fetchedPartners.length > 0 && !selectedPartnerId) {
          setSelectedPartnerId(fetchedPartners[0].id);
        }
      }

      if (requestsRes.status === 'fulfilled' && requestsRes.value.success) {
        setRequests(requestsRes.value.data || { incoming: [], outgoing: [] });
      }

      fetchChallenges();
      fetchLeaderboard();
      fetchActivities();
    } catch {
      addToast({
        type: 'error',
        title: 'Error Loading Partners',
        message: 'Could not fetch your learning partners. Please check your connection.',
      });
    } finally {
      setIsLoading(false);
    }
  }, [addToast, selectedPartnerId, fetchChallenges, fetchLeaderboard, fetchActivities]);


  useEffect(() => {
    fetchPartnersAndRequests();
  }, [fetchPartnersAndRequests]);

  // Load progress when selected partner changes
  useEffect(() => {
    if (!selectedPartnerId) {
      setPartnerProgress(null);
      return;
    }

    const fetchProgress = async () => {
      setIsProgressLoading(true);
      setProgressError(null);
      try {
        const res = await partnerApi.getPartnerProgress(selectedPartnerId);
        if (res.success) {
          setPartnerProgress(res.data);
        } else {
          setProgressError(res.message || 'Could not load partner progress.');
        }
      } catch (err: unknown) {
        const error = err as { response?: { status?: number; data?: { message?: string } } };
        if (error.response?.status === 403) {
          setProgressError('This progress is only available to your learning partner.');
        } else {
          setProgressError('Unable to load progress at this time. Please try again.');
        }
      } finally {
        setIsProgressLoading(false);
      }
    };

    fetchProgress();
  }, [selectedPartnerId]);

  // Handle Email Search (Phase E)
  const handleEmailSearch = async (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    const email = searchEmail.trim();
    if (!email) return;

    setIsSearchingEmail(true);
    setHasSearchedEmail(true);
    setEmailSearchError(null);
    setEmailSearchResult(null);

    try {
      const res = await partnerApi.searchByEmail(email);
      if (res.success && res.data) {
        setEmailSearchResult(res.data);
      } else {
        setEmailSearchError(res.message || 'No Memora account found with this email.');
      }
    } catch (err: unknown) {
      const error = err as { response?: { status?: number; data?: { message?: string } } };
      if (error.response?.status === 404) {
        setEmailSearchError('No Memora account found with this email.');
      } else if (error.response?.status === 400) {
        setEmailSearchError(error.response?.data?.message || 'You cannot add yourself as a learning partner.');
      } else {
        setEmailSearchError(error.response?.data?.message || 'Could not find a user with this email.');
      }
    } finally {
      setIsSearchingEmail(false);
    }
  };

  const formatActivityTime = (dateStr: string) => {
    try {
      const d = new Date(dateStr);
      const now = new Date();
      const diffMs = now.getTime() - d.getTime();
      const diffSec = Math.floor(diffMs / 1000);
      const diffMin = Math.floor(diffSec / 60);
      const diffHour = Math.floor(diffMin / 60);
      const diffDay = Math.floor(diffHour / 24);

      if (diffMin < 1) return 'Just now';
      if (diffMin < 60) return `${diffMin}m ago`;
      if (diffHour < 24) return `${diffHour}h ago`;
      if (diffDay < 7) return `${diffDay}d ago`;
      return d.toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
    } catch {
      return 'Recent';
    }
  };

  const getActivityIcon = (type: string) => {
    switch (type) {
      case 'PARTNER_CONNECTED':
        return <Users className="w-4 h-4 text-emerald-600" />;
      case 'CHALLENGE_CREATED':
        return <Swords className="w-4 h-4 text-indigo-600" />;
      case 'CHALLENGE_ACCEPTED':
        return <CheckCircle className="w-4 h-4 text-blue-600" />;
      case 'CHALLENGE_COMPLETED':
        return <Target className="w-4 h-4 text-teal-600" />;
      case 'CHALLENGE_WON':
        return <Trophy className="w-4 h-4 text-amber-500" />;
      case 'CHALLENGE_DRAW':
        return <RotateCcw className="w-4 h-4 text-orange-500" />;
      case 'XP_MILESTONE':
      case 'STREAK_MILESTONE':
      case 'WORDS_MILESTONE':
        return <Award className="w-4 h-4 text-purple-600" />;
      default:
        return <Sparkles className="w-4 h-4 text-emerald-600" />;
    }
  };

  // Handle Search
  const handleSearch = async (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    if (!searchQuery.trim()) return;

    setIsSearching(true);
    setHasSearched(true);
    try {
      const res = await partnerApi.searchUsers(searchQuery.trim());
      if (res.success) {
        setSearchResults(res.data || []);
      }
    } catch {
      addToast({
        type: 'error',
        title: 'Search Failed',
        message: 'Could not complete user search. Please try again.',
      });
    } finally {
      setIsSearching(false);
    }
  };

  // Send request
  const handleSendRequest = async (targetUserId: number) => {
    setActionInProgressId(targetUserId);
    try {
      const res = await partnerApi.sendPartnerRequest(targetUserId);
      if (res.success) {
        addToast({
          type: 'success',
          title: 'Request Sent',
          message: 'Learning partner connection request sent successfully!',
        });
        setSearchResults((prev) => prev.filter((u) => u.id !== targetUserId));
        if (emailSearchResult && emailSearchResult.id === targetUserId) {
          setEmailSearchResult((prev) => (prev ? { ...prev, relationshipStatus: 'PENDING' } : null));
        }
        fetchPartnersAndRequests();
      }
    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } };
      addToast({
        type: 'error',
        title: 'Could Not Send Request',
        message: error.response?.data?.message || 'Failed to send partner request.',
      });
    } finally {
      setActionInProgressId(null);
    }
  };

  // Accept request
  const handleAcceptRequest = async (requestId: number) => {
    setActionInProgressId(requestId);
    try {
      const res = await partnerApi.acceptPartnerRequest(requestId);
      if (res.success) {
        addToast({
          type: 'success',
          title: 'Partner Connected!',
          message: `You and ${res.data.name} are now learning partners!`,
        });
        await fetchPartnersAndRequests();
        fetchLeaderboard();
        fetchActivities();
        if (res.data.id) {
          setSelectedPartnerId(res.data.id);
        }
      }
    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } };
      addToast({
        type: 'error',
        title: 'Failed to Accept',
        message: error.response?.data?.message || 'Could not accept request.',
      });
    } finally {
      setActionInProgressId(null);
    }
  };


  // Reject request
  const handleRejectRequest = async (requestId: number) => {
    setActionInProgressId(requestId);
    try {
      const res = await partnerApi.rejectPartnerRequest(requestId);
      if (res.success) {
        addToast({
          type: 'info',
          title: 'Request Declined',
          message: 'Partner request declined.',
        });
        fetchPartnersAndRequests();
      }
    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } };
      addToast({
        type: 'error',
        title: 'Action Failed',
        message: error.response?.data?.message || 'Could not decline request.',
      });
    } finally {
      setActionInProgressId(null);
    }
  };

  // Cancel request
  const handleCancelRequest = async (requestId: number) => {
    setActionInProgressId(requestId);
    try {
      const res = await partnerApi.cancelPartnerRequest(requestId);
      if (res.success) {
        addToast({
          type: 'info',
          title: 'Request Cancelled',
          message: 'Your partner request has been cancelled.',
        });
        fetchPartnersAndRequests();
      }
    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } };
      addToast({
        type: 'error',
        title: 'Action Failed',
        message: error.response?.data?.message || 'Could not cancel request.',
      });
    } finally {
      setActionInProgressId(null);
    }
  };

  // --- Challenge Actions ---

  const handleCreateChallenge = async () => {
    if (!selectedPartnerId) return;
    setIsCreatingChallenge(true);
    try {
      const res = await partnerApi.createChallenge(
        selectedPartnerId,
        selectedCefrLevel,
        selectedQuestionCount
      );
      if (res.success) {
        addToast({
          type: 'success',
          title: 'Challenge Dispatched!',
          message: `Challenge sent to ${res.data.challengedUserName}! Waiting for them to accept.`,
        });
        setIsCreateChallengeModalOpen(false);
        fetchChallenges();
        fetchActivities();
      }
    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } };
      addToast({
        type: 'error',
        title: 'Challenge Failed',
        message: error.response?.data?.message || 'Could not create challenge. Please try again.',
      });
    } finally {
      setIsCreatingChallenge(false);
    }
  };

  const handleAcceptChallenge = async (challengeId: number) => {
    setActionInProgressId(challengeId);
    try {
      const res = await partnerApi.acceptChallenge(challengeId);
      if (res.success) {
        addToast({
          type: 'success',
          title: 'Challenge Accepted!',
          message: 'The match is ready! You can now start the quiz.',
        });
        fetchChallenges();
        fetchActivities();
      }

    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } };
      addToast({
        type: 'error',
        title: 'Failed to Accept Challenge',
        message: error.response?.data?.message || 'Could not accept challenge.',
      });
    } finally {
      setActionInProgressId(null);
    }
  };

  const handleDeclineChallenge = async (challengeId: number) => {
    setActionInProgressId(challengeId);
    try {
      const res = await partnerApi.declineChallenge(challengeId);
      if (res.success) {
        addToast({
          type: 'info',
          title: 'Challenge Declined',
          message: 'The challenge has been declined.',
        });
        fetchChallenges();
      }
    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } };
      addToast({
        type: 'error',
        title: 'Action Failed',
        message: error.response?.data?.message || 'Could not decline challenge.',
      });
    } finally {
      setActionInProgressId(null);
    }
  };

  const handleCancelChallenge = async (challengeId: number) => {
    setActionInProgressId(challengeId);
    try {
      const res = await partnerApi.cancelChallenge(challengeId);
      if (res.success) {
        addToast({
          type: 'info',
          title: 'Challenge Cancelled',
          message: 'Your challenge invitation has been cancelled.',
        });
        fetchChallenges();
      }
    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } };
      addToast({
        type: 'error',
        title: 'Action Failed',
        message: error.response?.data?.message || 'Could not cancel challenge.',
      });
    } finally {
      setActionInProgressId(null);
    }
  };

  // Launch Challenge Quiz Runner
  const handleStartQuiz = async (challenge: ChallengeSummary) => {
    setQuizChallenge(challenge);
    setIsQuizLoading(true);
    setIsQuizModalOpen(true);
    setCurrentQuestionIdx(0);
    setAnswersMap({});
    setLatenciesMap({});
    questionStartTimeRef.current = Date.now();
    setQuestionStartTime(Date.now());

    try {
      const res = await partnerApi.getChallengeQuestions(challenge.id);
      if (res.success && res.data.length > 0) {
        setQuizQuestions(res.data);
      } else {
        addToast({
          type: 'error',
          title: 'No Questions Found',
          message: 'Could not load questions for this challenge.',
        });
        setIsQuizModalOpen(false);
      }
    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } };
      addToast({
        type: 'error',
        title: 'Failed to Load Quiz',
        message: error.response?.data?.message || 'Could not start challenge quiz.',
      });
      setIsQuizModalOpen(false);
    } finally {
      setIsQuizLoading(false);
    }
  };

  const handleSelectAnswer = (questionId: number, answerValue: string) => {
    const elapsed = Date.now() - questionStartTime;
    setAnswersMap((prev) => ({ ...prev, [questionId]: answerValue }));
    setLatenciesMap((prev) => ({ ...prev, [questionId]: elapsed }));
  };

  const handleNextQuestion = () => {
    if (currentQuestionIdx < quizQuestions.length - 1) {
      setCurrentQuestionIdx((prev) => prev + 1);
      setQuestionStartTime(Date.now());
    }
  };

  const handlePrevQuestion = () => {
    if (currentQuestionIdx > 0) {
      setCurrentQuestionIdx((prev) => prev - 1);
      setQuestionStartTime(Date.now());
    }
  };

  const handleSubmitQuiz = async () => {
    if (!quizChallenge) return;

    // Check unanswered
    const unansweredCount = quizQuestions.filter((q) => !answersMap[q.id]?.trim()).length;
    if (unansweredCount > 0) {
      const confirmSubmit = window.confirm(
        `You have ${unansweredCount} unanswered questions. Submit anyway?`
      );
      if (!confirmSubmit) return;
    }

    setIsSubmittingQuiz(true);
    try {
      const formattedAnswers = quizQuestions.map((q) => ({
        questionId: q.id,
        answer: answersMap[q.id]?.trim() || '',
        responseTimeMs: latenciesMap[q.id] || 1500,
      }));

      const res = await partnerApi.submitChallenge(quizChallenge.id, formattedAnswers);
      if (res.success) {
        addToast({
          type: 'success',
          title: 'Challenge Submitted!',
          message: `Submission complete! You earned +${res.data.xpEarned} XP.`,
        });
        setIsQuizModalOpen(false);
        setResultData(res.data);
        setIsResultModalOpen(true);
        fetchChallenges();
        fetchLeaderboard();
        fetchActivities();
        if (selectedPartnerId) {
          partnerApi.getPartnerProgress(selectedPartnerId).then((pRes) => {
            if (pRes.success) setPartnerProgress(pRes.data);
          }).catch(() => {});
        }
      }
    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } };
      addToast({
        type: 'error',
        title: 'Submission Failed',
        message: error.response?.data?.message || 'Could not submit your challenge answers.',
      });
    } finally {
      setIsSubmittingQuiz(false);
    }
  };

  const handleOpenResults = async (challengeId: number) => {
    setIsLoadingResult(true);
    setIsResultModalOpen(true);
    try {
      const res = await partnerApi.getChallengeResult(challengeId);
      if (res.success) {
        setResultData(res.data);
      }
    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } };
      addToast({
        type: 'error',
        title: 'Error Loading Results',
        message: error.response?.data?.message || 'Could not retrieve challenge results.',
      });
      setIsResultModalOpen(false);
    } finally {
      setIsLoadingResult(false);
    }
  };

  const selectedPartner = partners.find((p) => p.id === selectedPartnerId) || partners[0] || null;

  // Filter challenges involving the selected partner
  const partnerChallenges = selectedPartner
    ? challenges.filter(
        (c) => c.challengerId === selectedPartner.id || c.challengedUserId === selectedPartner.id
      )
    : [];

  const activePartnerChallenge = partnerChallenges.find(
    (c) => c.status === 'PENDING' || c.status === 'ACCEPTED' || c.status === 'IN_PROGRESS'
  );

  const pastPartnerChallenges = partnerChallenges.filter(
    (c) => c.status === 'COMPLETED' || c.status === 'DECLINED' || c.status === 'CANCELLED'
  );

  return (
    <AppShell
      title="Learning Partners"
      subtitle="Study together, compete in vocabulary duels, and share privacy-safe milestones."
    >
      <div className="space-y-6">
        {/* Top Header Controls */}
        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 bg-white rounded-3xl p-5 md:p-6 border border-black/[0.06] shadow-xs">
          <div className="flex items-center gap-3.5">
            <div className="w-12 h-12 rounded-2xl bg-memora-green-light flex items-center justify-center text-memora-green shadow-xs">
              <Users className="w-6 h-6" />
            </div>
            <div>
              <h2 className="text-lg md:text-xl font-extrabold text-memora-dark tracking-tight">
                Partner Hub
              </h2>
              <p className="text-xs md:text-sm text-memora-text-muted">
                {partners.length > 0
                  ? `Active Partnership (${partners.length})`
                  : 'Connect with a study partner'}
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2.5 w-full sm:w-auto">
            {selectedPartner && (
              <Button
                onClick={() => setIsCreateChallengeModalOpen(true)}
                variant="outline"
                className="flex items-center gap-2 border-stone-200 text-stone-700 hover:border-memora-green hover:text-memora-green"
              >
                <Swords className="w-4 h-4 text-emerald-600" />
                <span>Challenge Partner</span>
              </Button>
            )}
            <Button
              onClick={() => {
                setModalTab('search');
                setIsSearchModalOpen(true);
              }}
              variant="primary"
              className="flex-1 sm:flex-initial flex items-center justify-center gap-2"
            >
              <UserPlus className="w-4 h-4" />
              <span>Find Partner</span>
              {requests.incoming.length > 0 && (
                <span className="w-5 h-5 rounded-full bg-white text-memora-green font-bold text-[10px] flex items-center justify-center ml-1">
                  {requests.incoming.length}
                </span>
              )}
            </Button>
          </div>
        </div>

        {/* Loading State */}
        {isLoading ? (
          <Card className="py-20 flex flex-col items-center justify-center text-center">
            <LoadingSpinner size="lg" label="Loading learning partner data..." />
          </Card>
        ) : partners.length === 0 ? (
          /* Empty State: No Partner Yet */
          <div className="space-y-6">
            <Card className="py-16 px-6 text-center max-w-2xl mx-auto flex flex-col items-center">
              <div className="w-20 h-20 rounded-3xl bg-memora-green-light flex items-center justify-center text-memora-green mb-5 shadow-sm">
                <Users className="w-10 h-10" />
              </div>
              <h3 className="text-2xl font-extrabold text-memora-dark tracking-tight mb-2">
                You don't have a learning partner yet.
              </h3>
              <p className="text-sm md:text-base text-memora-text-muted max-w-md mx-auto mb-8 leading-relaxed">
                Connect with a fellow Memora learner to challenge each other in vocabulary duels, share progress milestones, and stay motivated.
              </p>
              <div className="flex flex-col sm:flex-row items-center gap-3">
                <Button
                  onClick={() => {
                    setModalTab('search');
                    setIsSearchModalOpen(true);
                  }}
                  variant="primary"
                  className="flex items-center gap-2 px-6"
                >
                  <Search className="w-4 h-4" />
                  <span>Find a Learning Partner</span>
                </Button>
                {requests.incoming.length > 0 && (
                  <Button
                    onClick={() => {
                      setModalTab('incoming');
                      setIsSearchModalOpen(true);
                    }}
                    variant="outline"
                    className="flex items-center gap-2"
                  >
                    <Clock className="w-4 h-4 text-amber-600" />
                    <span>View Pending Requests ({requests.incoming.length})</span>
                  </Button>
                )}
              </div>
            </Card>

            {/* Pending Incoming Requests Preview */}
            {requests.incoming.length > 0 && (
              <Card className="p-6">
                <div className="flex items-center justify-between mb-4">
                  <h4 className="text-base font-bold text-memora-dark flex items-center gap-2">
                    <Clock className="w-4 h-4 text-amber-600" />
                    <span>Incoming Partner Requests</span>
                  </h4>
                  <Badge variant="amber">{requests.incoming.length} Pending</Badge>
                </div>
                <div className="space-y-3">
                  {requests.incoming.map((req) => (
                    <div
                      key={req.id}
                      className="flex flex-col sm:flex-row sm:items-center justify-between p-4 rounded-2xl bg-stone-50 border border-black/[0.04] gap-3"
                    >
                      <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-xl bg-white border border-black/[0.06] flex items-center justify-center font-bold text-memora-dark">
                          {req.sender.name.charAt(0).toUpperCase()}
                        </div>
                        <div>
                          <p className="font-bold text-sm text-memora-dark">{req.sender.name}</p>
                          <div className="flex items-center gap-2 text-xs text-memora-text-muted mt-0.5">
                            <Badge variant="blue" size="sm">Level {req.sender.currentLevel}</Badge>
                            <span>•</span>
                            <span>{req.sender.xp.toLocaleString()} XP</span>
                          </div>
                        </div>
                      </div>
                      <div className="flex items-center gap-2">
                        <Button
                          size="sm"
                          variant="primary"
                          onClick={() => handleAcceptRequest(req.id)}
                          disabled={actionInProgressId === req.id}
                          className="flex items-center gap-1.5"
                        >
                          <Check className="w-3.5 h-3.5" />
                          <span>Accept</span>
                        </Button>
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => handleRejectRequest(req.id)}
                          disabled={actionInProgressId === req.id}
                          className="text-stone-600 hover:text-red-600 hover:border-red-200"
                        >
                          <X className="w-3.5 h-3.5" />
                          <span>Decline</span>
                        </Button>
                      </div>
                    </div>
                  ))}
                </div>
              </Card>
            )}

            {/* Empty Leaderboard State (Section 10) */}
            <Card className="p-8 text-center max-w-2xl mx-auto border border-black/[0.06] bg-white">
              <div className="w-12 h-12 rounded-2xl bg-amber-50 text-amber-600 flex items-center justify-center mx-auto mb-3 shadow-xs">
                <Trophy className="w-6 h-6" />
              </div>
              <h4 className="text-lg font-bold text-memora-dark mb-1">
                Your Partner Leaderboard
              </h4>
              <p className="text-sm text-memora-text-muted max-w-md mx-auto mb-5">
                Connect with a learning partner to start competing together.
              </p>
              <Button
                size="sm"
                variant="primary"
                onClick={() => {
                  setModalTab('search');
                  setIsSearchModalOpen(true);
                }}
                className="inline-flex items-center gap-2"
              >
                <UserPlus className="w-4 h-4" />
                <span>Find a Partner</span>
              </Button>
            </Card>
          </div>
        ) : (
          /* Partner Active View */
          <div className="space-y-6">
            {/* Multiple Partners Selector (if user has > 1 partner) */}
            {partners.length > 1 && (
              <div className="flex items-center gap-2 overflow-x-auto pb-1">
                <span className="text-xs font-bold uppercase text-memora-text-muted tracking-wider mr-1">
                  Select Partner:
                </span>
                {partners.map((partner) => (
                  <button
                    key={partner.id}
                    onClick={() => setSelectedPartnerId(partner.id)}
                    className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold transition-all ${
                      selectedPartnerId === partner.id
                        ? 'bg-memora-dark text-white shadow-xs'
                        : 'bg-white text-stone-600 border border-black/[0.06] hover:bg-stone-50'
                    }`}
                  >
                    <span>{partner.name}</span>
                    <Badge
                      variant={selectedPartnerId === partner.id ? 'green' : 'neutral'}
                      size="sm"
                    >
                      {partner.currentLevel}
                    </Badge>
                  </button>
                ))}
              </div>
            )}

            {/* Partner Overview Card */}
            {selectedPartner && (
              <Card className="p-6 md:p-8 bg-gradient-to-br from-white to-stone-50/60 border border-black/[0.06] relative overflow-hidden">
                <div className="flex flex-col md:flex-row md:items-center justify-between gap-6 relative z-10">
                  <div className="flex items-center gap-4">
                    <div className="w-16 h-16 rounded-2xl bg-emerald-100 text-memora-green flex items-center justify-center text-2xl font-black shadow-inner">
                      {selectedPartner.name.charAt(0).toUpperCase()}
                    </div>
                    <div>
                      <div className="flex items-center gap-2.5 flex-wrap">
                        <h3 className="text-xl md:text-2xl font-black text-memora-dark tracking-tight">
                          {selectedPartner.name}
                        </h3>
                        <Badge variant="green" size="md">
                          Learning Partner
                        </Badge>
                        <Badge variant="blue" size="md">
                          Level {selectedPartner.currentLevel}
                        </Badge>
                      </div>
                      <p className="text-xs md:text-sm text-memora-text-muted mt-1">
                        Partnered learner on Memora • Real-time progress & challenge sync
                      </p>
                    </div>
                  </div>

                  {/* Summary Metric Pills */}
                  <div className="grid grid-cols-2 sm:grid-cols-3 gap-3 w-full md:w-auto">
                    <div className="bg-white p-3 rounded-2xl border border-black/[0.05] text-center shadow-xs">
                      <div className="flex items-center justify-center gap-1 text-amber-500 mb-0.5">
                        <Star className="w-3.5 h-3.5 fill-current" />
                        <span className="text-[11px] font-bold uppercase tracking-wider text-memora-text-muted">XP</span>
                      </div>
                      <p className="font-extrabold text-base text-memora-dark">
                        {selectedPartner.xp.toLocaleString()}
                      </p>
                    </div>

                    <div className="bg-white p-3 rounded-2xl border border-black/[0.05] text-center shadow-xs">
                      <div className="flex items-center justify-center gap-1 text-orange-500 mb-0.5">
                        <Flame className="w-3.5 h-3.5 fill-current" />
                        <span className="text-[11px] font-bold uppercase tracking-wider text-memora-text-muted">Streak</span>
                      </div>
                      <p className="font-extrabold text-base text-memora-dark">
                        {selectedPartner.streak} {selectedPartner.streak === 1 ? 'day' : 'days'}
                      </p>
                    </div>

                    <div className="bg-white p-3 rounded-2xl border border-black/[0.05] text-center shadow-xs col-span-2 sm:col-span-1">
                      <div className="flex items-center justify-center gap-1 text-emerald-600 mb-0.5">
                        <BookOpen className="w-3.5 h-3.5" />
                        <span className="text-[11px] font-bold uppercase tracking-wider text-memora-text-muted">Learned</span>
                      </div>
                      <p className="font-extrabold text-base text-memora-dark">
                        {partnerProgress ? partnerProgress.wordsLearned : '—'}
                      </p>
                    </div>
                  </div>
                </div>
              </Card>
            )}

            {/* --- VOCABULARY CHALLENGE SECTION --- */}
            {selectedPartner && (
              <Card className="p-6 md:p-8 border border-emerald-950/10 shadow-xs relative overflow-hidden bg-gradient-to-r from-emerald-900 via-stone-900 to-emerald-950 text-white">
                <div className="relative z-10">
                  <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 rounded-xl bg-emerald-500/20 text-emerald-400 flex items-center justify-center border border-emerald-500/30">
                        <Swords className="w-5 h-5" />
                      </div>
                      <div>
                        <h3 className="text-lg md:text-xl font-black text-white tracking-tight flex items-center gap-2">
                          <span>Vocabulary Duel</span>
                          <span className="text-xs px-2 py-0.5 rounded-full bg-emerald-500/20 text-emerald-300 border border-emerald-500/30 font-bold">
                            Phase D Live
                          </span>
                        </h3>
                        <p className="text-xs text-stone-300">
                          Compete head-to-head on the same deterministic question set!
                        </p>
                      </div>
                    </div>

                    {!activePartnerChallenge && (
                      <Button
                        onClick={() => setIsCreateChallengeModalOpen(true)}
                        variant="primary"
                        className="bg-emerald-500 hover:bg-emerald-400 text-stone-950 font-extrabold flex items-center gap-2 self-start sm:self-auto"
                      >
                        <Swords className="w-4 h-4" />
                        <span>New Challenge</span>
                      </Button>
                    )}
                  </div>

                  {/* Active Challenge Card */}
                  {activePartnerChallenge ? (
                    <div className="bg-white/10 backdrop-blur-md rounded-2xl p-5 border border-white/15">
                      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                        <div className="space-y-1.5">
                          <div className="flex items-center gap-2 flex-wrap">
                            <span className="font-extrabold text-base text-white">
                              Challenge #{activePartnerChallenge.id}
                            </span>
                            <Badge
                              variant={
                                activePartnerChallenge.status === 'PENDING'
                                  ? 'amber'
                                  : activePartnerChallenge.status === 'ACCEPTED' ||
                                    activePartnerChallenge.status === 'IN_PROGRESS'
                                  ? 'blue'
                                  : 'green'
                              }
                            >
                              {activePartnerChallenge.status}
                            </Badge>
                            <span className="text-xs text-stone-300">
                              Level {activePartnerChallenge.cefrLevel} • {activePartnerChallenge.questionCount} Questions
                            </span>
                          </div>

                          {/* Dynamic State Descriptions */}
                          {activePartnerChallenge.status === 'PENDING' && (
                            <p className="text-xs text-stone-300">
                              {activePartnerChallenge.challengerId === currentUser?.id
                                ? `Waiting for ${selectedPartner.name} to accept your challenge invitation.`
                                : `${selectedPartner.name} invited you to a vocabulary challenge!`}
                            </p>
                          )}

                          {(activePartnerChallenge.status === 'ACCEPTED' ||
                            activePartnerChallenge.status === 'IN_PROGRESS') && (
                            <p className="text-xs text-stone-300">
                              {activePartnerChallenge.currentUserCompleted
                                ? `You scored ${activePartnerChallenge.myScore} pts. Waiting for ${selectedPartner.name} to submit their answers.`
                                : `Challenge active! Both players have access to the same question set.`}
                            </p>
                          )}
                        </div>

                        {/* Actions depending on challenge state and role */}
                        <div className="flex items-center gap-2.5 flex-wrap">
                          {/* Case 1: Pending & User is Challenged -> Accept / Decline */}
                          {activePartnerChallenge.status === 'PENDING' &&
                            activePartnerChallenge.challengedUserId === currentUser?.id && (
                              <>
                                <Button
                                  size="sm"
                                  variant="primary"
                                  onClick={() => handleAcceptChallenge(activePartnerChallenge.id)}
                                  disabled={actionInProgressId === activePartnerChallenge.id}
                                  className="bg-emerald-500 hover:bg-emerald-400 text-stone-950 font-bold flex items-center gap-1.5"
                                >
                                  <Check className="w-3.5 h-3.5" />
                                  <span>Accept Challenge</span>
                                </Button>
                                <Button
                                  size="sm"
                                  variant="outline"
                                  onClick={() => handleDeclineChallenge(activePartnerChallenge.id)}
                                  disabled={actionInProgressId === activePartnerChallenge.id}
                                  className="border-white/20 text-white hover:bg-white/10"
                                >
                                  <X className="w-3.5 h-3.5" />
                                  <span>Decline</span>
                                </Button>
                              </>
                            )}

                          {/* Case 2: Pending & User is Challenger -> Cancel */}
                          {activePartnerChallenge.status === 'PENDING' &&
                            activePartnerChallenge.challengerId === currentUser?.id && (
                              <Button
                                size="sm"
                                variant="outline"
                                onClick={() => handleCancelChallenge(activePartnerChallenge.id)}
                                disabled={actionInProgressId === activePartnerChallenge.id}
                                className="border-white/20 text-stone-300 hover:text-white hover:bg-white/10"
                              >
                                <X className="w-3.5 h-3.5" />
                                <span>Cancel Challenge</span>
                              </Button>
                            )}

                          {/* Case 3: Accepted or In Progress & User hasn't finished -> Play Quiz */}
                          {(activePartnerChallenge.status === 'ACCEPTED' ||
                            activePartnerChallenge.status === 'IN_PROGRESS') &&
                            !activePartnerChallenge.currentUserCompleted && (
                              <Button
                                size="sm"
                                variant="primary"
                                onClick={() => handleStartQuiz(activePartnerChallenge)}
                                className="bg-emerald-400 hover:bg-emerald-300 text-stone-950 font-black px-4 flex items-center gap-2 shadow-lg"
                              >
                                <Play className="w-4 h-4 fill-current" />
                                <span>Start Quiz Now</span>
                              </Button>
                            )}

                          {/* Case 4: User completed & waiting for partner */}
                          {activePartnerChallenge.currentUserCompleted && (
                            <Button
                              size="sm"
                              variant="outline"
                              onClick={() => handleOpenResults(activePartnerChallenge.id)}
                              className="border-white/30 text-white hover:bg-white/15 flex items-center gap-1.5"
                            >
                              <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400" />
                              <span>View My Submission</span>
                            </Button>
                          )}
                        </div>
                      </div>
                    </div>
                  ) : (
                    /* No Active Challenge: Invitation prompt */
                    <div className="flex flex-col sm:flex-row sm:items-center justify-between p-4 rounded-2xl bg-white/5 border border-white/10 gap-3">
                      <div className="flex items-center gap-3">
                        <Trophy className="w-8 h-8 text-amber-400 shrink-0" />
                        <div>
                          <p className="text-sm font-bold text-white">No active challenge right now</p>
                          <p className="text-xs text-stone-300">
                            Challenge {selectedPartner.name} to a 5 or 10-question retention duel!
                          </p>
                        </div>
                      </div>
                      <Button
                        size="sm"
                        onClick={() => setIsCreateChallengeModalOpen(true)}
                        className="bg-emerald-500 hover:bg-emerald-400 text-stone-950 font-bold self-start sm:self-auto shrink-0"
                      >
                        Send Challenge
                      </Button>
                    </div>
                  )}

                  {/* Past Match History for this partner (if any) */}
                  {pastPartnerChallenges.length > 0 && (
                    <div className="mt-5 pt-4 border-t border-white/10">
                      <p className="text-xs font-bold text-stone-300 uppercase tracking-wider mb-2">
                        Recent Match Results
                      </p>
                      <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
                        {pastPartnerChallenges.slice(0, 2).map((past) => (
                          <div
                            key={past.id}
                            className="bg-white/5 border border-white/10 rounded-xl p-3 flex items-center justify-between text-xs"
                          >
                            <div className="flex items-center gap-2">
                              {past.isDraw ? (
                                <Badge variant="amber" size="sm">Draw</Badge>
                              ) : past.winnerId === currentUser?.id ? (
                                <Badge variant="green" size="sm">Won 🏆</Badge>
                              ) : (
                                <Badge variant="neutral" size="sm">Lost</Badge>
                              )}
                              <span className="font-semibold text-stone-200">
                                {past.myScore} pts vs {past.partnerScore ?? 0} pts
                              </span>
                            </div>
                            <Button
                              size="sm"
                              variant="ghost"
                              onClick={() => handleOpenResults(past.id)}
                              className="text-stone-300 hover:text-white p-1 h-auto"
                            >
                              <span>Review</span>
                              <ChevronRight className="w-3.5 h-3.5 ml-1" />
                            </Button>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}
                </div>
              </Card>
            )}

            {/* Privacy UX Notice Banner */}
            <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between p-4 rounded-2xl bg-stone-50 border border-black/[0.05] gap-3 text-xs">
              <div className="flex items-center gap-2.5">
                <div className="w-7 h-7 rounded-lg bg-emerald-100 text-memora-green flex items-center justify-center shrink-0">
                  <ShieldCheck className="w-4 h-4" />
                </div>
                <div>
                  <p className="font-bold text-stone-800">Shared with your learning partner</p>
                  <p className="text-[11px] text-stone-500">
                    XP • Level • Streak • Words Learned • Mastered Words • Accuracy
                  </p>
                </div>
              </div>
              <span className="text-[11px] text-stone-400 font-medium">
                🔒 Private notes, passwords & test answers are strictly private
              </span>
            </div>

            {/* Progress View Cards */}
            {isProgressLoading ? (
              <Card className="py-16 text-center">
                <LoadingSpinner size="md" label="Retrieving latest learning progress..." />
              </Card>
            ) : progressError ? (
              <Card className="py-12 px-6 text-center">
                <AlertCircle className="w-10 h-10 text-amber-500 mx-auto mb-3" />
                <h4 className="text-base font-bold text-memora-dark mb-1">Notice</h4>
                <p className="text-sm text-memora-text-muted mb-4">{progressError}</p>
                <Button
                  size="sm"
                  variant="outline"
                  onClick={() => selectedPartnerId && setSelectedPartnerId(selectedPartnerId)}
                  className="flex items-center gap-2 mx-auto"
                >
                  <RefreshCw className="w-3.5 h-3.5" />
                  <span>Retry</span>
                </Button>
              </Card>
            ) : partnerProgress ? (
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
                {/* 1. Current CEFR Level */}
                <Card className="p-5 flex flex-col justify-between">
                  <div>
                    <div className="flex items-center justify-between mb-3">
                      <span className="text-xs font-bold uppercase tracking-wider text-memora-text-muted">
                        Current Level
                      </span>
                      <div className="w-8 h-8 rounded-xl bg-indigo-50 text-indigo-600 flex items-center justify-center">
                        <Award className="w-4 h-4" />
                      </div>
                    </div>
                    <p className="text-2xl font-black text-memora-dark mb-1">
                      {partnerProgress.currentLevel}
                    </p>
                    <p className="text-xs text-memora-text-muted">
                      CEFR standard proficiency benchmark
                    </p>
                  </div>
                  <div className="mt-4 pt-3 border-t border-black/[0.04]">
                    <Badge variant="blue" size="sm">Active Vocabulary Track</Badge>
                  </div>
                </Card>

                {/* 2. Total XP */}
                <Card className="p-5 flex flex-col justify-between">
                  <div>
                    <div className="flex items-center justify-between mb-3">
                      <span className="text-xs font-bold uppercase tracking-wider text-memora-text-muted">
                        Total Experience
                      </span>
                      <div className="w-8 h-8 rounded-xl bg-amber-50 text-amber-600 flex items-center justify-center">
                        <Star className="w-4 h-4 fill-current" />
                      </div>
                    </div>
                    <p className="text-2xl font-black text-memora-dark mb-1">
                      {partnerProgress.xp.toLocaleString()} XP
                    </p>
                    <p className="text-xs text-memora-text-muted">
                      Accumulated from lessons, reviews & quizzes
                    </p>
                  </div>
                  <div className="mt-4 pt-3 border-t border-black/[0.04]">
                    <Badge variant="amber" size="sm">Mastery Points</Badge>
                  </div>
                </Card>

                {/* 3. Current Streak */}
                <Card className="p-5 flex flex-col justify-between">
                  <div>
                    <div className="flex items-center justify-between mb-3">
                      <span className="text-xs font-bold uppercase tracking-wider text-memora-text-muted">
                        Current Streak
                      </span>
                      <div className="w-8 h-8 rounded-xl bg-orange-50 text-orange-600 flex items-center justify-center">
                        <Flame className="w-4 h-4 fill-current" />
                      </div>
                    </div>
                    <p className="text-2xl font-black text-memora-dark mb-1">
                      {partnerProgress.streak} {partnerProgress.streak === 1 ? 'Day' : 'Days'}
                    </p>
                    <p className="text-xs text-memora-text-muted">
                      Consecutive days of vocabulary practice
                    </p>
                  </div>
                  <div className="mt-4 pt-3 border-t border-black/[0.04]">
                    <Badge variant="amber" size="sm">
                      {partnerProgress.streak > 0 ? 'Streak Active 🔥' : 'Pending Review'}
                    </Badge>
                  </div>
                </Card>

                {/* 4. Words Learned */}
                <Card className="p-5 flex flex-col justify-between">
                  <div>
                    <div className="flex items-center justify-between mb-3">
                      <span className="text-xs font-bold uppercase tracking-wider text-memora-text-muted">
                        Words Learned
                      </span>
                      <div className="w-8 h-8 rounded-xl bg-emerald-50 text-memora-green flex items-center justify-center">
                        <BookOpen className="w-4 h-4" />
                      </div>
                    </div>
                    <p className="text-2xl font-black text-memora-dark mb-1">
                      {partnerProgress.wordsLearned}
                    </p>
                    <p className="text-xs text-memora-text-muted">
                      Total unique catalog words practiced
                    </p>
                  </div>
                  <div className="mt-4 pt-3 border-t border-black/[0.04]">
                    <Badge variant="green" size="sm">Catalog Progress</Badge>
                  </div>
                </Card>

                {/* 5. Mastered Words */}
                <Card className="p-5 flex flex-col justify-between">
                  <div>
                    <div className="flex items-center justify-between mb-3">
                      <span className="text-xs font-bold uppercase tracking-wider text-memora-text-muted">
                        Mastered Words
                      </span>
                      <div className="w-8 h-8 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center">
                        <CheckCircle2 className="w-4 h-4" />
                      </div>
                    </div>
                    <p className="text-2xl font-black text-memora-dark mb-1">
                      {partnerProgress.masteredWords}
                    </p>
                    <p className="text-xs text-memora-text-muted">
                      Passed Leitner Box 4 or 80%+ mastery threshold
                    </p>
                  </div>
                  <div className="mt-4 pt-3 border-t border-black/[0.04]">
                    <Badge variant="blue" size="sm">Long-term Memory</Badge>
                  </div>
                </Card>

                {/* 6. Learning Accuracy */}
                <Card className="p-5 flex flex-col justify-between">
                  <div>
                    <div className="flex items-center justify-between mb-3">
                      <span className="text-xs font-bold uppercase tracking-wider text-memora-text-muted">
                        Quiz Accuracy
                      </span>
                      <div className="w-8 h-8 rounded-xl bg-teal-50 text-teal-600 flex items-center justify-center">
                        <Target className="w-4 h-4" />
                      </div>
                    </div>
                    <p className="text-2xl font-black text-memora-dark mb-2">
                      {partnerProgress.accuracy.toFixed(1)}%
                    </p>
                    <ProgressBar
                      value={partnerProgress.accuracy}
                      max={100}
                      barClassName={partnerProgress.accuracy >= 75 ? 'bg-memora-green' : 'bg-amber-500'}
                    />
                  </div>
                  <div className="mt-4 pt-3 border-t border-black/[0.04]">
                    <span className="text-[11px] text-memora-text-muted font-medium">
                      Evaluated across quiz answers & memory recalls
                    </span>
                  </div>
                </Card>
              </div>
            ) : null}

            {/* --- SECTION: PAIR-ONLY LEADERBOARD & PARTNER ACTIVITY FEED --- */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 pt-2">
              {/* 1. Pair-Only Leaderboard Card */}
              <Card className="p-6 border border-black/[0.06] bg-white flex flex-col justify-between shadow-xs">
                <div>
                  <div className="flex items-center justify-between mb-4 pb-3 border-b border-black/[0.05]">
                    <div className="flex items-center gap-2.5">
                      <div className="w-9 h-9 rounded-xl bg-amber-50 text-amber-600 flex items-center justify-center shadow-xs">
                        <Trophy className="w-5 h-5" />
                      </div>
                      <div>
                        <h4 className="text-base font-bold text-memora-dark">Partner Leaderboard</h4>
                        <p className="text-xs text-memora-text-muted">Ranked by Total XP among your accepted partners</p>
                      </div>
                    </div>
                    <Badge variant="amber" size="sm">{leaderboard.length} Ranked</Badge>
                  </div>

                  {isLeaderboardLoading ? (
                    <div className="py-12 text-center">
                      <LoadingSpinner size="sm" label="Updating partner standings..." />
                    </div>
                  ) : leaderboard.length === 0 ? (
                    <div className="py-10 text-center text-sm text-memora-text-muted">
                      No partner rankings available yet.
                    </div>
                  ) : (
                    <div className="space-y-2.5">
                      {leaderboard.map((entry) => {
                        const isMe = entry.userId === currentUser?.id;
                        return (
                          <div
                            key={entry.userId}
                            className={`flex items-center justify-between p-3.5 rounded-2xl border transition-all ${
                              isMe
                                ? 'bg-emerald-50/70 border-memora-green/30 text-emerald-950 shadow-xs'
                                : 'bg-stone-50 border-black/[0.04] text-stone-800'
                            }`}
                          >
                            <div className="flex items-center gap-3">
                              <div
                                className={`w-7 h-7 rounded-xl flex items-center justify-center text-xs font-black ${
                                  entry.rank === 1
                                    ? 'bg-amber-400 text-amber-950 shadow-xs'
                                    : entry.rank === 2
                                    ? 'bg-stone-300 text-stone-800'
                                    : entry.rank === 3
                                    ? 'bg-amber-700/20 text-amber-900'
                                    : 'bg-stone-200 text-stone-600'
                                }`}
                              >
                                {entry.rank}
                              </div>
                              <div>
                                <div className="flex items-center gap-2">
                                  <span className="font-bold text-sm text-memora-dark">
                                    {entry.name}
                                  </span>
                                  {isMe && (
                                    <Badge variant="green" size="sm">You</Badge>
                                  )}
                                  <Badge variant="blue" size="sm">Level {entry.currentLevel}</Badge>
                                </div>
                                <div className="flex items-center gap-2.5 text-xs text-memora-text-muted mt-0.5">
                                  <span className="flex items-center gap-1">
                                    <Flame className="w-3.5 h-3.5 text-orange-500 fill-current" />
                                    <span>{entry.streak}d streak</span>
                                  </span>
                                  <span>•</span>
                                  <span className="flex items-center gap-1">
                                    <BookOpen className="w-3.5 h-3.5 text-stone-400" />
                                    <span>{entry.wordsLearned} words</span>
                                  </span>
                                </div>
                              </div>
                            </div>

                            <div className="text-right">
                              <span className="font-black text-sm text-memora-dark">
                                {entry.xp.toLocaleString()}
                              </span>
                              <span className="text-[11px] text-memora-text-muted block font-medium">XP</span>
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  )}
                </div>
              </Card>

              {/* 2. Recent Partner Activity Feed Card */}
              <Card className="p-6 border border-black/[0.06] bg-white flex flex-col justify-between shadow-xs">
                <div>
                  <div className="flex items-center justify-between mb-4 pb-3 border-b border-black/[0.05]">
                    <div className="flex items-center gap-2.5">
                      <div className="w-9 h-9 rounded-xl bg-purple-50 text-purple-600 flex items-center justify-center shadow-xs">
                        <Sparkles className="w-5 h-5" />
                      </div>
                      <div>
                        <h4 className="text-base font-bold text-memora-dark">Recent Activity</h4>
                        <p className="text-xs text-memora-text-muted">Live milestones and duel updates</p>
                      </div>
                    </div>
                    <Badge variant="neutral" size="sm">Live Feed</Badge>
                  </div>

                  {isActivitiesLoading ? (
                    <div className="py-12 text-center">
                      <LoadingSpinner size="sm" label="Loading activity feed..." />
                    </div>
                  ) : activities.length === 0 ? (
                    <div className="py-10 text-center text-sm text-memora-text-muted">
                      No recent partner activity yet. Complete challenges or achieve milestones to see updates here!
                    </div>
                  ) : (
                    <div className="space-y-3 max-h-96 overflow-y-auto pr-1">
                      {activities.map((act) => (
                        <div
                          key={act.id}
                          className="flex items-start gap-3 p-3 rounded-2xl bg-stone-50 border border-black/[0.04]"
                        >
                          <div className="w-8 h-8 rounded-xl bg-white border border-black/[0.06] flex items-center justify-center shrink-0 mt-0.5 shadow-2xs">
                            {getActivityIcon(act.activityType)}
                          </div>
                          <div className="flex-1 min-w-0">
                            <div className="flex items-center justify-between gap-2">
                              <p className="font-bold text-xs text-memora-dark truncate">
                                {act.title}
                              </p>
                              <span className="text-[10px] text-memora-text-muted shrink-0 whitespace-nowrap">
                                {formatActivityTime(act.createdAt)}
                              </span>
                            </div>
                            {act.details && (
                              <p className="text-[11px] text-memora-text-muted mt-0.5 line-clamp-1">
                                {act.details}
                              </p>
                            )}
                          </div>
                          {act.xpEarned && act.xpEarned > 0 ? (
                            <Badge variant="amber" size="sm" className="shrink-0 self-center">
                              +{act.xpEarned} XP
                            </Badge>
                          ) : null}
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              </Card>
            </div>
          </div>
        )}
      </div>

      {/* --- MODAL 1: Create Challenge Modal (Viewport safe with pinned sticky footer) --- */}
      <Modal
        isOpen={isCreateChallengeModalOpen}
        onClose={() => setIsCreateChallengeModalOpen(false)}
        title={selectedPartner ? `Challenge ${selectedPartner.name}` : 'New Vocabulary Duel'}
        maxWidth="md"
        footer={
          <>
            <Button
              variant="outline"
              onClick={() => setIsCreateChallengeModalOpen(false)}
            >
              Cancel
            </Button>
            <Button
              variant="primary"
              onClick={handleCreateChallenge}
              disabled={isCreatingChallenge}
              className="flex items-center gap-2"
            >
              {isCreatingChallenge ? (
                <LoadingSpinner size="sm" />
              ) : (
                <>
                  <Swords className="w-4 h-4" />
                  <span>Send Challenge Invitation</span>
                </>
              )}
            </Button>
          </>
        }
      >
        <div className="space-y-5">
          <p className="text-sm text-memora-text-muted">
            Send a competitive vocabulary duel to your partner. Both learners will receive the exact same question set to solve!
          </p>

          <div className="space-y-4">
            <div>
              <label className="block text-xs font-bold uppercase tracking-wider text-memora-dark mb-2">
                CEFR Difficulty Level
              </label>
              <div className="grid grid-cols-5 gap-2">
                {['A1', 'A2', 'B1', 'B2', 'C1'].map((lvl) => (
                  <button
                    key={lvl}
                    type="button"
                    onClick={() => setSelectedCefrLevel(lvl)}
                    className={`py-2 rounded-xl text-xs font-bold border transition-all ${
                      selectedCefrLevel === lvl
                        ? 'bg-memora-green text-white border-memora-green shadow-xs'
                        : 'bg-stone-50 border-black/[0.08] text-stone-700 hover:bg-stone-100'
                    }`}
                  >
                    {lvl}
                  </button>
                ))}
              </div>
            </div>

            <div>
              <label className="block text-xs font-bold uppercase tracking-wider text-memora-dark mb-2">
                Questions Count
              </label>
              <div className="grid grid-cols-2 gap-3">
                {[5, 10].map((count) => (
                  <button
                    key={count}
                    type="button"
                    onClick={() => setSelectedQuestionCount(count)}
                    className={`py-3 rounded-2xl text-xs font-bold border transition-all flex items-center justify-center gap-2 ${
                      selectedQuestionCount === count
                        ? 'bg-memora-dark text-white border-memora-dark shadow-xs'
                        : 'bg-stone-50 border-black/[0.08] text-stone-700 hover:bg-stone-100'
                    }`}
                  >
                    <span>{count} Questions</span>
                    <span className="text-[10px] opacity-75">
                      {count === 5 ? '(Fast Duel)' : '(Full Match)'}
                    </span>
                  </button>
                ))}
              </div>
            </div>
          </div>
        </div>
      </Modal>


      {/* --- MODAL 2: Interactive Challenge Quiz Runner --- */}
      <Modal
        isOpen={isQuizModalOpen}
        onClose={() => {
          if (window.confirm('Are you sure you want to exit? Your answers will not be saved until submitted.')) {
            setIsQuizModalOpen(false);
          }
        }}
        title={quizChallenge ? `Vocabulary Challenge: ${quizChallenge.cefrLevel} Level` : 'Challenge Quiz'}
        maxWidth="lg"
      >
        {isQuizLoading ? (
          <div className="py-20 text-center">
            <LoadingSpinner size="lg" label="Generating deterministic duel questions..." />
          </div>
        ) : quizQuestions.length > 0 ? (
          <div className="space-y-6">
            {/* Progress Header */}
            <div>
              <div className="flex items-center justify-between text-xs font-bold text-memora-text-muted mb-2">
                <span>Question {currentQuestionIdx + 1} of {quizQuestions.length}</span>
                <span className="text-memora-green font-bold">10 Points</span>
              </div>
              <ProgressBar
                value={currentQuestionIdx + 1}
                max={quizQuestions.length}
                barClassName="bg-memora-green"
              />
            </div>

            {/* Current Question Body */}
            {(() => {
              const currentQ = quizQuestions[currentQuestionIdx];
              if (!currentQ) return null;

              return (
                <div className="space-y-5">
                  <div className="p-5 rounded-2xl bg-stone-50 border border-black/[0.06]">
                    <div className="flex items-center gap-2 mb-2">
                      <Badge variant="blue" size="sm">
                        {currentQ.questionType.replace(/_/g, ' ')}
                      </Badge>
                      <Badge variant="neutral" size="sm">
                        Target Word: <span className="font-bold ml-1">{currentQ.word}</span>
                      </Badge>
                    </div>
                    <p className="text-base font-bold text-memora-dark">
                      {currentQ.questionText}
                    </p>
                    {currentQ.sentence && (
                      <p className="mt-3 text-sm p-3 bg-white rounded-xl border border-black/[0.06] text-stone-700 italic">
                        "{currentQ.sentence}"
                      </p>
                    )}
                  </div>

                  {/* Options or Answer Input */}
                  {currentQ.questionType === 'MULTIPLE_CHOICE' && currentQ.options ? (
                    <div className="grid grid-cols-1 gap-2.5">
                      {currentQ.options.map((option, idx) => {
                        const isSelected = answersMap[currentQ.id] === option;
                        return (
                          <button
                            key={idx}
                            type="button"
                            onClick={() => handleSelectAnswer(currentQ.id, option)}
                            className={`p-4 rounded-2xl text-left text-sm font-semibold transition-all border flex items-center justify-between ${
                              isSelected
                                ? 'bg-emerald-50 border-memora-green text-emerald-950 ring-2 ring-memora-green/20 shadow-xs'
                                : 'bg-white border-black/[0.08] text-stone-800 hover:bg-stone-50'
                            }`}
                          >
                            <span>{option}</span>
                            <div
                              className={`w-5 h-5 rounded-full border flex items-center justify-center ${
                                isSelected
                                  ? 'bg-memora-green border-memora-green text-white'
                                  : 'border-stone-300'
                              }`}
                            >
                              {isSelected && <Check className="w-3 h-3 stroke-[3]" />}
                            </div>
                          </button>
                        );
                      })}
                    </div>
                  ) : (
                    /* Free Text Input for Fill in the Blank / Translation */
                    <div>
                      <label className="block text-xs font-bold text-memora-dark mb-1.5 uppercase tracking-wider">
                        Your Answer
                      </label>
                      <input
                        type="text"
                        value={answersMap[currentQ.id] || ''}
                        onChange={(e) => handleSelectAnswer(currentQ.id, e.target.value)}
                        placeholder="Type your answer here..."
                        className="w-full px-4 py-3 rounded-2xl border border-black/[0.1] bg-white text-sm font-medium focus:outline-hidden focus:ring-2 focus:ring-memora-green/30 focus:border-memora-green transition-all"
                      />
                    </div>
                  )}

                  {/* Navigation & Submit Controls */}
                  <div className="flex items-center justify-between pt-4 border-t border-black/[0.06]">
                    <Button
                      variant="outline"
                      onClick={handlePrevQuestion}
                      disabled={currentQuestionIdx === 0}
                      size="sm"
                    >
                      Previous
                    </Button>

                    <div className="flex items-center gap-2">
                      {currentQuestionIdx < quizQuestions.length - 1 ? (
                        <Button
                          variant="primary"
                          onClick={handleNextQuestion}
                          size="sm"
                          className="flex items-center gap-1.5"
                        >
                          <span>Next</span>
                          <ArrowRight className="w-3.5 h-3.5" />
                        </Button>
                      ) : (
                        <Button
                          variant="primary"
                          onClick={handleSubmitQuiz}
                          disabled={isSubmittingQuiz}
                          size="sm"
                          className="bg-emerald-600 hover:bg-emerald-500 font-extrabold flex items-center gap-1.5 px-5 shadow-sm"
                        >
                          {isSubmittingQuiz ? (
                            <LoadingSpinner size="sm" />
                          ) : (
                            <>
                              <CheckCircle className="w-4 h-4" />
                              <span>Submit Challenge</span>
                            </>
                          )}
                        </Button>
                      )}
                    </div>
                  </div>
                </div>
              );
            })()}
          </div>
        ) : null}
      </Modal>

      {/* --- MODAL 3: Challenge Results / Review Modal --- */}
      <Modal
        isOpen={isResultModalOpen}
        onClose={() => setIsResultModalOpen(false)}
        title="Challenge Results & Answers"
        maxWidth="lg"
      >
        {isLoadingResult ? (
          <div className="py-16 text-center">
            <LoadingSpinner size="md" label="Loading match results..." />
          </div>
        ) : resultData ? (
          <div className="space-y-6">
            {/* Outcome Banner */}
            <div
              className={`p-6 rounded-2xl text-center border relative overflow-hidden ${
                resultData.completed
                  ? resultData.isDraw
                    ? 'bg-amber-50 border-amber-200 text-amber-950'
                    : resultData.winnerId === currentUser?.id
                    ? 'bg-emerald-50 border-emerald-200 text-emerald-950'
                    : 'bg-stone-100 border-stone-200 text-stone-900'
                  : 'bg-blue-50 border-blue-200 text-blue-950'
              }`}
            >
              <div className="relative z-10 space-y-1">
                <div className="inline-flex p-3 rounded-2xl bg-white shadow-xs mb-1">
                  {resultData.completed ? (
                    resultData.isDraw ? (
                      <RotateCcw className="w-8 h-8 text-amber-600" />
                    ) : resultData.winnerId === currentUser?.id ? (
                      <Trophy className="w-8 h-8 text-emerald-600" />
                    ) : (
                      <Award className="w-8 h-8 text-stone-600" />
                    )
                  ) : (
                    <Clock className="w-8 h-8 text-blue-600" />
                  )}
                </div>

                <h3 className="text-xl font-black tracking-tight">
                  {resultData.completed
                    ? resultData.isDraw
                      ? "It's a Draw!"
                      : resultData.winnerId === currentUser?.id
                      ? 'Victory! You Won the Challenge!'
                      : `${resultData.winnerName || 'Partner'} Won This Duel`
                    : 'Submitted! Waiting for Partner'}
                </h3>

                <p className="text-xs opacity-80 max-w-sm mx-auto">
                  {resultData.completed
                    ? 'Both players have submitted their attempts. Final scores are verified!'
                    : 'Your answers are locked. Partner results will be revealed as soon as they complete.'}
                </p>

                {resultData.xpEarned > 0 && (
                  <div className="pt-2">
                    <span className="inline-flex items-center gap-1 px-3 py-1 rounded-full bg-amber-100 text-amber-900 text-xs font-extrabold border border-amber-300 shadow-xs">
                      <Star className="w-3.5 h-3.5 fill-current text-amber-600" />
                      <span>+{resultData.xpEarned} XP Earned</span>
                    </span>
                  </div>
                )}
              </div>
            </div>

            {/* Side-by-Side Scores */}
            <div className="grid grid-cols-2 gap-3.5">
              <div className="p-4 rounded-2xl bg-stone-50 border border-black/[0.06] text-center">
                <span className="text-[11px] font-bold text-memora-text-muted uppercase tracking-wider block mb-1">
                  Your Score
                </span>
                <p className="text-2xl font-black text-memora-dark">
                  {resultData.myScore ?? 0} <span className="text-xs font-normal text-stone-500">pts</span>
                </p>
                <span className="text-xs text-stone-500">
                  {resultData.myCorrectCount ?? 0} / {resultData.totalQuestions} correct
                </span>
              </div>

              <div className="p-4 rounded-2xl bg-stone-50 border border-black/[0.06] text-center">
                <span className="text-[11px] font-bold text-memora-text-muted uppercase tracking-wider block mb-1">
                  Partner Score
                </span>
                <p className="text-2xl font-black text-memora-dark">
                  {resultData.partnerScore !== null ? (
                    <>
                      {resultData.partnerScore} <span className="text-xs font-normal text-stone-500">pts</span>
                    </>
                  ) : (
                    <span className="text-sm font-semibold text-stone-400 italic">In progress...</span>
                  )}
                </p>
                <span className="text-xs text-stone-500">
                  {resultData.partnerCorrectCount !== null
                    ? `${resultData.partnerCorrectCount} / ${resultData.totalQuestions} correct`
                    : 'Hidden until finished'}
                </span>
              </div>
            </div>

            {/* Question Breakdown List */}
            {resultData.myQuestionBreakdown && resultData.myQuestionBreakdown.length > 0 && (
              <div className="space-y-3">
                <h4 className="text-xs font-bold uppercase tracking-wider text-memora-dark">
                  Your Question Breakdown
                </h4>
                <div className="space-y-2.5 max-h-72 overflow-y-auto pr-1">
                  {resultData.myQuestionBreakdown.map((item, idx) => (
                    <div
                      key={idx}
                      className={`p-3.5 rounded-xl border text-xs ${
                        item.isCorrect
                          ? 'bg-emerald-50/70 border-emerald-200/80 text-emerald-950'
                          : 'bg-red-50/70 border-red-200/80 text-red-950'
                      }`}
                    >
                      <div className="flex items-center justify-between mb-1.5">
                        <span className="font-bold text-[11px] opacity-75">
                          Question {idx + 1}
                        </span>
                        <Badge variant={item.isCorrect ? 'green' : 'red'} size="sm">
                          {item.isCorrect ? '+10 pts' : '0 pts'}
                        </Badge>
                      </div>
                      <p className="font-semibold text-stone-800 mb-2">{item.questionText}</p>
                      <div className="flex items-center gap-4 flex-wrap text-[11px]">
                        <div>
                          <span className="text-stone-500 mr-1">Your answer:</span>
                          <span className={`font-bold ${item.isCorrect ? 'text-emerald-700' : 'text-red-700'}`}>
                            {item.userAnswer || '(blank)'}
                          </span>
                        </div>
                        {!item.isCorrect && item.correctAnswer && (
                          <div>
                            <span className="text-stone-500 mr-1">Correct answer:</span>
                            <span className="font-bold text-stone-800">{item.correctAnswer}</span>
                          </div>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            <div className="pt-3 border-t border-black/[0.06] flex items-center justify-end">
              <Button
                variant="primary"
                onClick={() => setIsResultModalOpen(false)}
                className="px-6"
              >
                Close
              </Button>
            </div>
          </div>
        ) : null}
      </Modal>

      {/* --- MODAL 4: Find, Connect & Manage Partner Requests --- */}
      <Modal
        isOpen={isSearchModalOpen}
        onClose={() => {
          setIsSearchModalOpen(false);
          setSearchEmail('');
          setEmailSearchResult(null);
          setEmailSearchError(null);
          setHasSearchedEmail(false);
          setSearchQuery('');
          setSearchResults([]);
          setHasSearched(false);
        }}
        title="Find & Connect with Learning Partners"
        maxWidth="lg"
      >
        <div className="space-y-5">
          {/* Modal Tabs */}
          <div className="flex items-center gap-1.5 p-1 bg-stone-100 rounded-2xl border border-black/[0.04]">
            <button
              onClick={() => setModalTab('search')}
              className={`flex-1 py-2 rounded-xl text-xs font-bold transition-all ${
                modalTab === 'search'
                  ? 'bg-white text-memora-dark shadow-xs'
                  : 'text-stone-600 hover:text-memora-dark'
              }`}
            >
              Discover Learners
            </button>
            <button
              onClick={() => setModalTab('incoming')}
              className={`flex-1 py-2 rounded-xl text-xs font-bold transition-all relative ${
                modalTab === 'incoming'
                  ? 'bg-white text-memora-dark shadow-xs'
                  : 'text-stone-600 hover:text-memora-dark'
              }`}
            >
              <span>Incoming Requests</span>
              {requests.incoming.length > 0 && (
                <span className="ml-1.5 px-1.5 py-0.2 rounded-full bg-amber-500 text-white text-[10px]">
                  {requests.incoming.length}
                </span>
              )}
            </button>
            <button
              onClick={() => setModalTab('outgoing')}
              className={`flex-1 py-2 rounded-xl text-xs font-bold transition-all ${
                modalTab === 'outgoing'
                  ? 'bg-white text-memora-dark shadow-xs'
                  : 'text-stone-600 hover:text-memora-dark'
              }`}
            >
              Sent Requests ({requests.outgoing.length})
            </button>
          </div>

          {/* TAB 1: Search by Email */}
          {modalTab === 'search' && (
            <div className="space-y-5">
              <div className="bg-stone-50/70 p-4 rounded-2xl border border-black/[0.05]">
                <h4 className="text-sm font-bold text-memora-dark mb-1">
                  Find a learning partner
                </h4>
                <p className="text-xs text-memora-text-muted">
                  Enter their Memora email address to find them and send a partnership invitation.
                </p>

                <form onSubmit={handleEmailSearch} className="mt-3 flex gap-2">
                  <div className="relative flex-1">
                    <Mail className="w-4 h-4 text-stone-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
                    <input
                      type="email"
                      value={searchEmail}
                      onChange={(e) => {
                        setSearchEmail(e.target.value);
                        if (emailSearchError) setEmailSearchError(null);
                      }}
                      placeholder="example@email.com"
                      className="w-full pl-9 pr-4 py-2.5 rounded-xl border border-black/[0.1] bg-white text-sm focus:outline-hidden focus:ring-2 focus:ring-memora-green/20 focus:border-memora-green transition-all"
                    />
                  </div>
                  <Button
                    type="submit"
                    variant="primary"
                    disabled={isSearchingEmail || !searchEmail.trim()}
                    className="flex items-center gap-1.5 px-4 shrink-0"
                  >
                    {isSearchingEmail ? <LoadingSpinner size="sm" /> : <span>Search Partner</span>}
                  </Button>
                </form>
              </div>

              {/* Email Search Results / Feedback */}
              {isSearchingEmail ? (
                <div className="py-10 text-center">
                  <LoadingSpinner size="md" label="Searching for partner by email..." />
                </div>
              ) : emailSearchError ? (
                <div className="p-4 rounded-2xl bg-amber-50 border border-amber-200/80 text-amber-950 text-xs flex items-center gap-2.5">
                  <AlertCircle className="w-4 h-4 text-amber-600 shrink-0" />
                  <span>{emailSearchError}</span>
                </div>
              ) : emailSearchResult ? (
                <div className="p-4 rounded-2xl bg-stone-50 border border-black/[0.06] space-y-4">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <div className="w-12 h-12 rounded-2xl bg-emerald-100 text-memora-green flex items-center justify-center font-black text-lg shadow-xs">
                        {emailSearchResult.name.charAt(0).toUpperCase()}
                      </div>
                      <div>
                        <h5 className="font-extrabold text-sm text-memora-dark">
                          {emailSearchResult.name}
                        </h5>
                        <div className="flex items-center gap-2 text-xs text-memora-text-muted mt-0.5">
                          <Badge variant="blue" size="sm">Level {emailSearchResult.currentLevel}</Badge>
                          <span>•</span>
                          <span>{emailSearchResult.xp.toLocaleString()} XP</span>
                          <span>•</span>
                          <span>{emailSearchResult.streak} day streak</span>
                        </div>
                      </div>
                    </div>
                  </div>

                  <div className="pt-3 border-t border-black/[0.06] flex items-center justify-end">
                    {emailSearchResult.relationshipStatus === 'ACCEPTED' ? (
                      <Badge variant="green" size="md">
                        <Check className="w-3.5 h-3.5 mr-1" />
                        <span>Already connected</span>
                      </Badge>
                    ) : emailSearchResult.relationshipStatus === 'PENDING' ? (
                      <Badge variant="amber" size="md">
                        <Clock className="w-3.5 h-3.5 mr-1" />
                        <span>Request pending</span>
                      </Badge>
                    ) : (
                      <Button
                        size="sm"
                        variant="primary"
                        onClick={async () => {
                          await handleSendRequest(emailSearchResult.id);
                          setEmailSearchResult((prev) =>
                            prev ? { ...prev, relationshipStatus: 'PENDING' } : null
                          );
                        }}
                        disabled={actionInProgressId === emailSearchResult.id}
                        className="flex items-center gap-1.5"
                      >
                        {actionInProgressId === emailSearchResult.id ? (
                          <LoadingSpinner size="sm" />
                        ) : (
                          <>
                            <UserPlus className="w-3.5 h-3.5" />
                            <span>Send Partner Request</span>
                          </>
                        )}
                      </Button>
                    )}
                  </div>
                </div>
              ) : hasSearchedEmail ? (
                <div className="py-8 text-center text-xs text-memora-text-muted">
                  No Memora account found with this email.
                </div>
              ) : null}
            </div>
          )}


          {/* TAB 2: Incoming Requests */}
          {modalTab === 'incoming' && (
            <div className="space-y-3 max-h-80 overflow-y-auto pr-1">
              {requests.incoming.length === 0 ? (
                <div className="py-12 text-center text-sm text-memora-text-muted">
                  No incoming partner requests at the moment.
                </div>
              ) : (
                requests.incoming.map((req) => (
                  <div
                    key={req.id}
                    className="flex flex-col sm:flex-row sm:items-center justify-between p-4 rounded-2xl bg-stone-50 border border-black/[0.04] gap-3"
                  >
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 rounded-xl bg-white border border-black/[0.06] flex items-center justify-center font-bold text-memora-dark">
                        {req.sender.name.charAt(0).toUpperCase()}
                      </div>
                      <div>
                        <p className="font-bold text-sm text-memora-dark">{req.sender.name}</p>
                        <div className="flex items-center gap-2 text-xs text-memora-text-muted">
                          <Badge variant="blue" size="sm">Level {req.sender.currentLevel}</Badge>
                          <span>•</span>
                          <span>{req.sender.xp.toLocaleString()} XP</span>
                        </div>
                      </div>
                    </div>
                    <div className="flex items-center gap-2">
                      <Button
                        size="sm"
                        variant="primary"
                        onClick={() => handleAcceptRequest(req.id)}
                        disabled={actionInProgressId === req.id}
                        className="flex items-center gap-1.5"
                      >
                        <Check className="w-3.5 h-3.5" />
                        <span>Accept</span>
                      </Button>
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => handleRejectRequest(req.id)}
                        disabled={actionInProgressId === req.id}
                        className="text-stone-600 hover:text-red-600"
                      >
                        <X className="w-3.5 h-3.5" />
                        <span>Decline</span>
                      </Button>
                    </div>
                  </div>
                ))
              )}
            </div>
          )}

          {/* TAB 3: Outgoing Requests */}
          {modalTab === 'outgoing' && (
            <div className="space-y-3 max-h-80 overflow-y-auto pr-1">
              {requests.outgoing.length === 0 ? (
                <div className="py-12 text-center text-sm text-memora-text-muted">
                  You haven't sent any pending partner requests.
                </div>
              ) : (
                requests.outgoing.map((req) => (
                  <div
                    key={req.id}
                    className="flex items-center justify-between p-4 rounded-2xl bg-stone-50 border border-black/[0.04]"
                  >
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 rounded-xl bg-white border border-black/[0.06] flex items-center justify-center font-bold text-memora-dark">
                        {req.receiver.name.charAt(0).toUpperCase()}
                      </div>
                      <div>
                        <p className="font-bold text-sm text-memora-dark">{req.receiver.name}</p>
                        <span className="text-xs text-amber-600 flex items-center gap-1 font-medium">
                          <Clock className="w-3 h-3" />
                          <span>Pending Acceptance</span>
                        </span>
                      </div>
                    </div>
                    <Button
                      size="sm"
                      variant="outline"
                      onClick={() => handleCancelRequest(req.id)}
                      disabled={actionInProgressId === req.id}
                      className="text-xs text-stone-600 hover:text-red-600"
                    >
                      <span>Cancel</span>
                    </Button>
                  </div>
                ))
              )}
            </div>
          )}
        </div>
      </Modal>
    </AppShell>
  );
};

export default PartnersPage;
