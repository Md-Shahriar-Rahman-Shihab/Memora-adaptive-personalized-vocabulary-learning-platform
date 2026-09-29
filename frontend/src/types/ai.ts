import { DifficultyLevel } from './vocabulary';

export interface AiBaseRequest {
  wordId?: number;
  word: string;
  cefrLevel?: DifficultyLevel | string;
}

export interface AiExplanationRequest extends AiBaseRequest {}

export interface AiExplanationResponse {
  word: string;
  cefrLevel?: string;
  explanation: string;
  breakdown?: string;
  provider: string;
  isFallback: boolean;
  cached: boolean;
  model?: string | null;
}

export interface AiExampleRequest extends AiBaseRequest {}

export interface AiExampleResponse {
  word: string;
  cefrLevel?: string;
  exampleSentence: string;
  context?: string;
  sentence?: string;
  provider: string;
  isFallback: boolean;
  cached: boolean;
  model?: string | null;
}

export interface AiMemoryTipRequest extends AiBaseRequest {}

export interface AiMemoryTipResponse {
  word: string;
  cefrLevel?: string;
  memoryTip: string;
  association?: string;
  tip?: string;
  technique?: string;
  provider: string;
  isFallback: boolean;
  cached: boolean;
  model?: string | null;
}

export interface AiUsageRequest extends AiBaseRequest {}

export interface AiUsageResponse {
  word: string;
  cefrLevel?: string;
  usageNotes: string;
  collocations: string[] | string;
  register?: string;
  context?: string;
  nuances?: string;
  provider: string;
  isFallback: boolean;
  cached: boolean;
  model?: string | null;
}

export interface AiWordRelationsRequest extends AiBaseRequest {
  partOfSpeech?: string;
  definition?: string;
}

export interface AiWordRelationsResponse {
  word: string;
  synonyms: string[];
  antonyms: string[];
  wordFamily: Record<string, string>;
  provider: string;
  isFallback: boolean;
  cached: boolean;
  model?: string | null;
}
