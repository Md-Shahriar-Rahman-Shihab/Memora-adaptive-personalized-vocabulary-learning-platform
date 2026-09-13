package com.memora.modules.dictionary.client;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Common abstraction for dictionary providers fetching raw dictionary definitions and suggestions.
 */
public interface DictionaryProvider {

    /**
     * Queries the underlying dictionary provider for word entries or suggestions.
     *
     * @param word Normalized, sanitized word
     * @return JsonNode representing entries array or suggestions array
     */
    JsonNode fetchWordEntries(String word);
}
