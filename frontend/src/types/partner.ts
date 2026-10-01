export interface PartnerUserSummary {
  id: number;
  name: string;
  currentLevel: string;
  xp: number;
  streak: number;
  relationshipStatus?: string;
}

export interface PartnerRequest {
  id: number;
  sender: PartnerUserSummary;
  receiver: PartnerUserSummary;
  status: 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'CANCELLED';
  createdAt: string;
  incoming: boolean;
}

export interface PartnerRequestsSummary {
  incoming: PartnerRequest[];
  outgoing: PartnerRequest[];
}

export interface PartnerProgress {
  id: number;
  name: string;
  currentLevel: string;
  xp: number;
  streak: number;
  wordsLearned: number;
  masteredWords: number;
  accuracy: number;
}

export interface ChallengeSummary {
  id: number;
  relationshipId: number;
  challengerId: number;
  challengerName: string;
  challengedUserId: number;
  challengedUserName: string;
  status: 'PENDING' | 'ACCEPTED' | 'IN_PROGRESS' | 'COMPLETED' | 'DECLINED' | 'CANCELLED';
  cefrLevel: string;
  questionCount: number;
  currentUserCompleted: boolean;
  partnerCompleted: boolean;
  myScore: number | null;
  partnerScore: number | null;
  myCompletedAt: string | null;
  partnerCompletedAt: string | null;
  winnerId: number | null;
  winnerName: string | null;
  isDraw: boolean;
  createdAt: string;
}

export interface ChallengeQuestion {
  id: number;
  wordId: number;
  word: string;
  questionType: string;
  points: number;
  questionText: string;
  options?: string[];
  sentence?: string;
}

export interface ChallengeQuestionResult {
  questionId: number;
  questionText: string;
  userAnswer: string;
  correctAnswer: string;
  isCorrect: boolean;
  pointsEarned: number;
}

export interface ChallengeResult {
  challengeId: number;
  status: string;
  completed: boolean;
  waitingForPartner: boolean;
  myScore: number | null;
  myCorrectCount: number | null;
  totalQuestions: number;
  partnerScore: number | null;
  partnerCorrectCount: number | null;
  winnerId: number | null;
  winnerName: string | null;
  isDraw: boolean;
  xpEarned: number;
  myQuestionBreakdown: ChallengeQuestionResult[];
}

export interface PartnerLeaderboardEntry {
  rank: number;
  userId: number;
  name: string;
  currentLevel: string;
  xp: number;
  streak: number;
  wordsLearned: number;
}

export interface PartnerActivityActor {
  id: number;
  name: string;
}

export interface PartnerActivity {
  id: number;
  actor: PartnerActivityActor;
  activityType: string;
  title: string;
  details: string | null;
  xpEarned: number | null;
  createdAt: string;
}

