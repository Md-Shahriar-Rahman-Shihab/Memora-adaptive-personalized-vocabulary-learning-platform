import apiClient from './axios';
import { ApiResponse } from '../types/common';
import { DictionaryResponse } from '../types/dictionary';

export const dictionaryApi = {
  /**
   * Searches the backend dictionary service for word definitions, pronunciation, audio, and etymology.
   * Backed by Merriam-Webster Collegiate Dictionary on the backend.
   *
   * @param word Word to look up
   * @param signal Optional AbortSignal for in-flight request cancellation
   * @returns Standard ApiResponse enclosing the DictionaryResponse
   */
  lookupWord: async (
    word: string,
    signal?: AbortSignal
  ): Promise<ApiResponse<DictionaryResponse>> => {
    const trimmed = word.trim();
    const res = await apiClient.get<ApiResponse<DictionaryResponse>>(
      `/dictionary/${encodeURIComponent(trimmed)}`,
      { signal }
    );
    return res.data;
  },
};

export default dictionaryApi;
