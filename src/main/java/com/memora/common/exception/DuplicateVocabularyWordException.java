package com.memora.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when attempting to create a vocabulary word that already exists in the catalog.
 */
public class DuplicateVocabularyWordException extends ApiException {

    public DuplicateVocabularyWordException(String word) {
        super(String.format("Vocabulary word '%s' already exists in the catalog", word), HttpStatus.CONFLICT);
    }
}
