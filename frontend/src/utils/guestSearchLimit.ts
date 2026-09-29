/**
 * Utility managing guest dictionary search quota and browser storage.
 */

export const MEMORA_GUEST_DICTIONARY_SEARCH_LIMIT = 4;
export const GUEST_SEARCH_STORAGE_KEY = 'memora_guest_dictionary_search_count';

/**
 * Safely retrieves the current number of free searches performed by a guest visitor.
 * Defaults to 0 on missing, invalid, or blocked storage access.
 */
export const getGuestSearchCount = (): number => {
  try {
    if (typeof window === 'undefined' || !window.localStorage) {
      return 0;
    }
    const val = localStorage.getItem(GUEST_SEARCH_STORAGE_KEY);
    if (!val) return 0;
    const parsed = parseInt(val, 10);
    return Number.isFinite(parsed) && parsed >= 0 ? parsed : 0;
  } catch {
    return 0;
  }
};

/**
 * Safely increments the guest search counter in localStorage after a valid, successful search.
 * Returns the new count.
 */
export const incrementGuestSearchCount = (): number => {
  try {
    const current = getGuestSearchCount();
    const next = current + 1;
    if (typeof window !== 'undefined' && window.localStorage) {
      localStorage.setItem(GUEST_SEARCH_STORAGE_KEY, next.toString());
    }
    return next;
  } catch {
    return 0;
  }
};

/**
 * Checks whether a guest has reached or exceeded the free search quota.
 */
export const isGuestSearchLimitReached = (): boolean => {
  return getGuestSearchCount() >= MEMORA_GUEST_DICTIONARY_SEARCH_LIMIT;
};

/**
 * Safely resets guest search count (useful for testing or upon logout).
 */
export const resetGuestSearchCount = (): void => {
  try {
    if (typeof window !== 'undefined' && window.localStorage) {
      localStorage.removeItem(GUEST_SEARCH_STORAGE_KEY);
    }
  } catch {
    // Non-blocking
  }
};
