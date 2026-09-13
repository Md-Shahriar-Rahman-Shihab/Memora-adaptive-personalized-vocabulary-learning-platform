export interface DefinitionItem {
  definition: string;
  examples: string[];
}

export interface PartOfSpeechItem {
  partOfSpeech: string;
  definitions: DefinitionItem[];
}

export interface DictionaryResponse {
  word: string;
  headword?: string | null;
  pronunciation?: string | null;
  audioUrl?: string | null;
  partsOfSpeech: PartOfSpeechItem[];
  shortDefinitions: string[];
  etymology?: string | null;
  suggestions: string[];
}
