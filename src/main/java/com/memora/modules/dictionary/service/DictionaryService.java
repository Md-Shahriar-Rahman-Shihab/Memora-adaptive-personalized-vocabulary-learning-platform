package com.memora.modules.dictionary.service;

import com.memora.modules.dictionary.dto.DictionaryResponse;

/**
 * Service interface for dictionary lookup operations.
 */
public interface DictionaryService {

    /**
     * Searches the dictionary for definitions, phonetics, audio, examples, and etymology.
     *
     * @param word Word to look up
     * @return Structured {@link DictionaryResponse}
     */
    DictionaryResponse lookupWord(String word);
}
